package com.example.musicproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying video items in a ListView.
 * Handles thumbnail loading asynchronously with caching.
 */
public class VideoListAdapter extends BaseAdapter {

    private final Context context;
    private List<VideoItem> videoList;
    private final LayoutInflater inflater;
    // Simple in-memory thumbnail cache
    private final Map<Long, Bitmap> thumbnailCache = new HashMap<>();

    public VideoListAdapter(Context context, List<VideoItem> videoList) {
        this.context = context;
        this.videoList = videoList;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Update the video list and refresh the view.
     */
    public void updateList(List<VideoItem> newList) {
        this.videoList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return videoList != null ? videoList.size() : 0;
    }

    @Override
    public VideoItem getItem(int position) {
        return videoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return videoList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_video, parent, false);
            holder = new ViewHolder();
            holder.imgThumbnail = convertView.findViewById(R.id.imgThumbnail);
            holder.tvVideoName = convertView.findViewById(R.id.tvVideoName);
            holder.tvDuration = convertView.findViewById(R.id.tvDuration);
            holder.tvFileSize = convertView.findViewById(R.id.tvFileSize);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        VideoItem video = getItem(position);

        // Set video info
        holder.tvVideoName.setText(video.getTitle());
        holder.tvDuration.setText(video.getFormattedDuration());
        holder.tvFileSize.setText(video.getFormattedSize());

        // Load thumbnail
        long videoId = video.getId();
        holder.imgThumbnail.setTag(videoId); // Track which video this ImageView is for

        if (thumbnailCache.containsKey(videoId)) {
            Bitmap cached = thumbnailCache.get(videoId);
            if (cached != null) {
                holder.imgThumbnail.setImageBitmap(cached);
            } else {
                holder.imgThumbnail.setImageResource(R.drawable.ic_video_placeholder);
            }
        } else {
            // Set placeholder while loading
            holder.imgThumbnail.setImageResource(R.drawable.ic_video_placeholder);
            // Load thumbnail asynchronously
            new ThumbnailTask(context, holder.imgThumbnail, videoId, thumbnailCache).execute();
        }

        return convertView;
    }

    /**
     * ViewHolder pattern for efficient list recycling.
     */
    static class ViewHolder {
        ImageView imgThumbnail;
        TextView tvVideoName;
        TextView tvDuration;
        TextView tvFileSize;
    }

    /**
     * AsyncTask to load video thumbnails in background.
     */
    private static class ThumbnailTask extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<Context> contextRef;
        private final WeakReference<ImageView> imageViewRef;
        private final long videoId;
        private final Map<Long, Bitmap> cache;

        ThumbnailTask(Context context, ImageView imageView, long videoId,
                      Map<Long, Bitmap> cache) {
            this.contextRef = new WeakReference<>(context);
            this.imageViewRef = new WeakReference<>(imageView);
            this.videoId = videoId;
            this.cache = cache;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Context ctx = contextRef.get();
            if (ctx == null) return null;
            return VideoUtils.loadThumbnail(ctx, videoId);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Cache the result (even if null)
            cache.put(videoId, bitmap);

            ImageView imageView = imageViewRef.get();
            if (imageView != null) {
                // Check if this ImageView is still supposed to show this video
                Object tag = imageView.getTag();
                if (tag instanceof Long && (Long) tag == videoId) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageResource(R.drawable.ic_video_placeholder);
                    }
                }
            }
        }
    }

    /**
     * Clear the thumbnail cache.
     */
    public void clearCache() {
        thumbnailCache.clear();
    }
}
