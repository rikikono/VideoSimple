package com.example.videoproject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;

public class ImageViewerActivity extends Activity {

    public static final String EXTRA_IMAGE_URI = "image_uri";

    private static final int MAX_IMAGE_DIMENSION = 2048;

    private ImageView imageViewer;
    private ImageButton btnBackImageViewer;

    private final Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleDetector;

    private float scaleFactor = 1.0f;
    private float lastTouchX;
    private float lastTouchY;
    private boolean isDragging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageViewer = findViewById(R.id.imageViewer);
        btnBackImageViewer = findViewById(R.id.btnBackImageViewer);

        btnBackImageViewer.setOnClickListener(v -> finish());

        scaleDetector = new ScaleGestureDetector(this, new ScaleListener());

        String imageUri = getIntent().getStringExtra(EXTRA_IMAGE_URI);
        if (imageUri == null || imageUri.trim().isEmpty()) {
            Toast.makeText(this, R.string.error_loading_image, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadImage(Uri.parse(imageUri));
    }

    private void loadImage(Uri imageUri) {
        new AsyncTask<Uri, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Uri... uris) {
                return decodeSampledBitmap(uris[0], MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (isFinishing()) {
                    return;
                }

                if (bitmap != null) {
                    imageViewer.setScaleType(ImageView.ScaleType.MATRIX);
                    imageViewer.setImageBitmap(bitmap);
                    imageViewer.setImageMatrix(matrix);
                    setupTouchHandling();
                } else {
                    Toast.makeText(ImageViewerActivity.this,
                            R.string.error_loading_image, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }.execute(imageUri);
    }

    private Bitmap decodeSampledBitmap(Uri uri, int reqWidth, int reqHeight) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;

        InputStream boundsStream = null;
        try {
            boundsStream = getContentResolver().openInputStream(uri);
            if (boundsStream == null) {
                return null;
            }
            BitmapFactory.decodeStream(boundsStream, null, bounds);
        } catch (Exception ignored) {
            return null;
        } finally {
            closeQuietly(boundsStream);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds, reqWidth, reqHeight);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        InputStream decodeStream = null;
        try {
            decodeStream = getContentResolver().openInputStream(uri);
            if (decodeStream == null) {
                return null;
            }
            return BitmapFactory.decodeStream(decodeStream, null, options);
        } catch (Exception ignored) {
            return null;
        } finally {
            closeQuietly(decodeStream);
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return Math.max(1, inSampleSize);
    }

    private void closeQuietly(InputStream stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (Exception ignored) {
        }
    }

    private void setupTouchHandling() {
        imageViewer.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    isDragging = true;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!scaleDetector.isInProgress() && isDragging && event.getPointerCount() == 1) {
                        float dx = event.getX() - lastTouchX;
                        float dy = event.getY() - lastTouchY;
                        matrix.postTranslate(dx, dy);
                        imageViewer.setImageMatrix(matrix);
                        lastTouchX = event.getX();
                        lastTouchY = event.getY();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isDragging = false;
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    isDragging = false;
                    break;
            }

            return true;
        });
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float detectorScale = detector.getScaleFactor();
            float nextScale = scaleFactor * detectorScale;

            if (nextScale < 1.0f) {
                detectorScale = 1.0f / scaleFactor;
                scaleFactor = 1.0f;
            } else if (nextScale > 5.0f) {
                detectorScale = 5.0f / scaleFactor;
                scaleFactor = 5.0f;
            } else {
                scaleFactor = nextScale;
            }

            matrix.postScale(
                    detectorScale,
                    detectorScale,
                    detector.getFocusX(),
                    detector.getFocusY()
            );
            imageViewer.setImageMatrix(matrix);
            return true;
        }
    }
}