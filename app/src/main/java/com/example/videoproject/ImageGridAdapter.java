package com.example.videoproject;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

public class ImageGridAdapter extends BaseAdapter {
    private static final int THUMBNAIL_SIZE_PX = 240;

    private final Context context;
    private final List<ImageItem> imageList;
    private final LayoutInflater inflater;
    private final LruCache<String, Bitmap> thumbnailCache;

    public ImageGridAdapter(Context context, List<ImageItem> imageList) {
        this.context = context.getApplicationContext();
        this.imageList = imageList;
        this.inflater = LayoutInflater.from(context);

        int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024L / 16L);
        thumbnailCache = new LruCache<String, Bitmap>(Math.max(1024, cacheSize)) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return Math.max(1, value.getByteCount() / 1024);
            }
        };
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return imageList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_image, parent, false);
            holder = new ViewHolder();
            holder.imageThumb = convertView.findViewById(R.id.imageThumb);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageItem item = imageList.get(position);
        String imageUri = item.getContentUri();
        holder.imageThumb.setTag(imageUri);

        Bitmap cachedBitmap = thumbnailCache.get(imageUri);
        if (cachedBitmap != null) {
            holder.imageThumb.setImageBitmap(cachedBitmap);
        } else {
            holder.imageThumb.setImageResource(R.drawable.ic_video_placeholder);
            new ThumbnailTask(holder.imageThumb, context, thumbnailCache)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imageUri);
        }

        return convertView;
    }

    private static Bitmap decodeSampledBitmap(ContentResolver resolver, Uri uri,
                                              int reqWidth, int reqHeight) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        InputStream boundsStream = null;
        try {
            boundsStream = resolver.openInputStream(uri);
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
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        InputStream decodeStream = null;
        try {
            decodeStream = resolver.openInputStream(uri);
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

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
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

    private static void closeQuietly(InputStream stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (Exception ignored) {
        }
    }

    private static class ViewHolder {
        ImageView imageThumb;
    }

    private static class ThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewRef;
        private final Context context;
        private final LruCache<String, Bitmap> cache;
        private String imageUri;

        ThumbnailTask(ImageView imageView, Context context, LruCache<String, Bitmap> cache) {
            this.imageViewRef = new WeakReference<>(imageView);
            this.context = context;
            this.cache = cache;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            imageUri = params[0];
            Bitmap bitmap = cache.get(imageUri);
            if (bitmap != null) {
                return bitmap;
            }
            return decodeSampledBitmap(
                    context.getContentResolver(),
                    Uri.parse(imageUri),
                    THUMBNAIL_SIZE_PX,
                    THUMBNAIL_SIZE_PX
            );
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }

            cache.put(imageUri, bitmap);

            ImageView imageView = imageViewRef.get();
            if (imageView != null && imageUri.equals(imageView.getTag())) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}