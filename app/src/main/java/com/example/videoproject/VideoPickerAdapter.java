package com.example.videoproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class VideoPickerAdapter extends BaseAdapter {

    private Context context;
    private List<VideoItem> videos;
    private List<Long> existingVideoIds;
    private List<VideoItem> selectedVideos;
    private LruCache<String, Bitmap> thumbnailCache;

    public VideoPickerAdapter(Context context, List<VideoItem> videos) {
        this.context = context;
        this.videos = videos;
        this.existingVideoIds = new ArrayList<>();
        this.selectedVideos = new ArrayList<>();

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        thumbnailCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void setExistingVideoIds(List<Long> ids) {
        this.existingVideoIds = ids;
        notifyDataSetChanged();
    }

    public List<VideoItem> getSelectedVideos() {
        return selectedVideos;
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
        return videos.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_video_picker, parent, false);
            holder = new ViewHolder();
            holder.imgThumbnail = convertView.findViewById(R.id.imgThumbnail);
            holder.tvTitle = convertView.findViewById(R.id.tvVideoName);
            holder.tvDuration = convertView.findViewById(R.id.tvDuration);
            holder.checkbox = convertView.findViewById(R.id.checkBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final VideoItem video = videos.get(position);

        holder.tvTitle.setText(video.getTitle());
        holder.tvDuration.setText(video.getFormattedDuration());

        // Check if video is already in playlist
        boolean isExisting = existingVideoIds.contains(video.getId());
        
        // Remove listener temporarily to avoid trigger during view recycling
        holder.checkbox.setOnCheckedChangeListener(null);
        
        if (isExisting) {
            holder.checkbox.setChecked(true);
            holder.checkbox.setEnabled(false);
            holder.tvTitle.setAlpha(0.5f);
        } else {
            holder.checkbox.setChecked(selectedVideos.contains(video));
            holder.checkbox.setEnabled(true);
            holder.tvTitle.setAlpha(1.0f);
            
            holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (!selectedVideos.contains(video)) {
                            selectedVideos.add(video);
                        }
                    } else {
                        selectedVideos.remove(video);
                    }
                }
            });
        }

        // Entire row click toggles checkbox if it's not disabled
        if (!isExisting) {
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = v.findViewById(R.id.checkBox);
                    cb.setChecked(!cb.isChecked());
                }
            });
        } else {
            convertView.setOnClickListener(null);
        }

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
            imageView.setImageResource(R.drawable.ic_video_placeholder);
            
            ThumbnailTask existingTask = (ThumbnailTask) imageView.getTag();
            if (existingTask != null) {
                existingTask.cancel(true);
            }

            ThumbnailTask task = new ThumbnailTask(imageView, context, thumbnailCache);
            imageView.setTag(task);
            task.execute(videoId);
        }
    }

    static class ViewHolder {
        ImageView imgThumbnail;
        TextView tvTitle;
        TextView tvDuration;
        CheckBox checkbox;
    }

    // Identical async task to the others, but kept isolated for simplicity
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
                cache.put(String.valueOf(videoId), bitmap);
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