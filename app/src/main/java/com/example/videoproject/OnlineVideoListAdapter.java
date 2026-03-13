package com.example.videoproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class OnlineVideoListAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<OnlineVideoItem> items;

    public OnlineVideoListAdapter(Context context, List<OnlineVideoItem> items) {
        this.inflater = LayoutInflater.from(context);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_online_video, parent, false);
            holder = new ViewHolder();
            holder.thumbnail = convertView.findViewById(R.id.imgOnlineThumbnail);
            holder.sourceLabel = convertView.findViewById(R.id.tvOnlineSourceLabel);
            holder.title = convertView.findViewById(R.id.tvOnlineTitle);
            holder.description = convertView.findViewById(R.id.tvOnlineDescription);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        OnlineVideoItem item = items.get(position);
        holder.thumbnail.setImageResource(R.drawable.ic_video_placeholder);
        holder.sourceLabel.setText(item.isUserAdded()
                ? R.string.custom_url_label
                : R.string.sample_label);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDescription());

        return convertView;
    }

    private static class ViewHolder {
        ImageView thumbnail;
        TextView sourceLabel;
        TextView title;
        TextView description;
    }
}