package com.example.videoproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ImageGalleryActivity extends Activity {

    private static final int IMAGE_PERMISSION_REQUEST_CODE = 201;

    private GridView gridImages;
    private ProgressBar progressGallery;
    private TextView tvEmptyGallery;
    private ImageButton btnBackGallery;

    private final List<ImageItem> imageList = new ArrayList<>();
    private ImageGridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        initViews();
        setupListeners();

        if (hasImagePermission()) {
            loadImages();
        } else {
            showPermissionState();
            requestImagePermission();
        }
    }

    private void initViews() {
        gridImages = findViewById(R.id.gridImages);
        progressGallery = findViewById(R.id.progressGallery);
        tvEmptyGallery = findViewById(R.id.tvEmptyGallery);
        btnBackGallery = findViewById(R.id.btnBackGallery);

        adapter = new ImageGridAdapter(this, imageList);
        gridImages.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBackGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        gridImages.setOnItemClickListener((parent, view, position, id) -> {
            ImageItem item = imageList.get(position);
            android.content.Intent intent =
                    new android.content.Intent(ImageGalleryActivity.this, ImageViewerActivity.class);
            intent.putExtra(ImageViewerActivity.EXTRA_IMAGE_URI, item.getContentUri());
            startActivity(intent);
        });
    }

    private boolean hasImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestImagePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            loadImages();
            return;
        }

        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_title)
                    .setMessage(R.string.permission_images_message)
                    .setPositiveButton(R.string.grant, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{permission}, IMAGE_PERMISSION_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showPermissionState();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            requestPermissions(new String[]{permission}, IMAGE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == IMAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImages();
            } else {
                showPermissionState();
            }
        }
    }

    private void loadImages() {
        progressGallery.setVisibility(View.VISIBLE);
        gridImages.setVisibility(View.GONE);
        tvEmptyGallery.setVisibility(View.GONE);

        new AsyncTask<Void, Void, List<ImageItem>>() {
            @Override
            protected List<ImageItem> doInBackground(Void... voids) {
                return getAllImages();
            }

            @Override
            protected void onPostExecute(List<ImageItem> result) {
                progressGallery.setVisibility(View.GONE);
                imageList.clear();
                imageList.addAll(result);
                adapter.notifyDataSetChanged();

                if (imageList.isEmpty()) {
                    showEmptyState();
                } else {
                    gridImages.setVisibility(View.VISIBLE);
                    tvEmptyGallery.setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    private List<ImageItem> getAllImages() {
        List<ImageItem> result = new ArrayList<>();
        String[] projection = {
                MediaStore.Images.Media._ID
        };

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
            );

            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idCol);
                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                    );
                    result.add(new ImageItem(id, contentUri.toString()));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    private void showEmptyState() {
        gridImages.setVisibility(View.GONE);
        progressGallery.setVisibility(View.GONE);
        tvEmptyGallery.setText(R.string.no_images_found);
        tvEmptyGallery.setVisibility(View.VISIBLE);
    }

    private void showPermissionState() {
        gridImages.setVisibility(View.GONE);
        progressGallery.setVisibility(View.GONE);
        tvEmptyGallery.setText(R.string.gallery_permission_required_state);
        tvEmptyGallery.setVisibility(View.VISIBLE);
    }
}