package com.example.videoproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

public class PlaylistVideoAdapter extends BaseAdapter {

    private Context context;
    private List<VideoItem> videos;
    private List<Long> mappingIds;
    private PlaylistActionCallback callback;
    private LruCache<String, Bitmap> thumbnailCache;

    public interface PlaylistActionCallback {
        void onMoveUp(int position);
        void onMoveDown(int position);
    }

    public PlaylistVideoAdapter(Context context, List<VideoItem> videos, 
                                List<Long> mappingIds, PlaylistActionCallback callback) {
        this.context = context;
        this.videos = videos;
        this.mappingIds = mappingIds;
        this.callback = callback;

        // Initialize thumbnail cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8; // Use 1/8th of available memory
        thumbnailCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void updateList(List<VideoItem> newVideos, List<Long> newMappingIds) {
        this.videos = newVideos;
        this.mappingIds = newMappingIds;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return videos.size();
    }

    @Override
    public Object getItem(int position) {
        return videos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mappingIds.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_playlist_video, parent, false);
            holder = new ViewHolder();
            holder.imgThumbnail = convertView.findViewById(R.id.imgThumbnail);
            holder.tvTitle = convertView.findViewById(R.id.tvVideoName);
            holder.tvDuration = convertView.findViewById(R.id.tvDuration);
            holder.btnUp = convertView.findViewById(R.id.btnMoveUp);
            holder.btnDown = convertView.findViewById(R.id.btnMoveDown);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        VideoItem video = videos.get(position);

        holder.tvTitle.setText(video.getTitle());
        holder.tvDuration.setText(video.getFormattedDuration());

        // Manage button visibility
        holder.btnUp.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.btnDown.setVisibility(position == videos.size() - 1 ? View.INVISIBLE : View.VISIBLE);

        // Setup click listeners
        holder.btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) callback.onMoveUp(position);
            }
        });

        holder.btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) callback.onMoveDown(position);
            }
        });

        // Load thumbnail
        loadThumbnail(video.getId(), holder.imgThumbnail);

        return convertView;
    }

    private void loadThumbnail(long videoId, ImageView imageView) {
        final String imageKey = String.valueOf(videoId);
        final Bitmap bitmap = thumbnailCache.get(imageKey);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            // Set placeholder and clear any pending tasks for this image view
            imageView.setImageResource(R.drawable.ic_video_placeholder);
            
            // Check if there's an existing task for this ImageView
            ThumbnailTask existingTask = (ThumbnailTask) imageView.getTag();
            if (existingTask != null) {
                existingTask.cancel(true);
            }

            // Create and execute new task
            ThumbnailTask task = new ThumbnailTask(imageView, context, thumbnailCache);
            imageView.setTag(task);
            task.execute(videoId);
        }
    }

    static class ViewHolder {
        ImageView imgThumbnail;
        TextView tvTitle;
        TextView tvDuration;
        ImageButton btnUp;
        ImageButton btnDown;
    }

    private static class ThumbnailTask extends AsyncTask<Long, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final Context context;
        private final LruCache<String, Bitmap> cache;
        private long videoId;

        public ThumbnailTask(ImageView imageView, Context context, LruCache<String, Bitmap> cache) {
            imageViewReference = new WeakReference<>(imageView);
            this.context = context;
            this.cache = cache;
        }

        @Override
        protected Bitmap doInBackground(Long... params) {
            videoId = params[0];
            return VideoUtils.getVideoThumbnail(context, videoId);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (bitmap != null) {
                // Add to cache
                cache.put(String.valueOf(videoId), bitmap);

                // Update UI if imageView is still available and the task matches
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    ThumbnailTask task = (ThumbnailTask) imageView.getTag();
                    if (this == task) {
                        imageView.setImageBitmap(bitmap);
                        imageView.setTag(null);
                    }
                }
            }
        }
    }
}