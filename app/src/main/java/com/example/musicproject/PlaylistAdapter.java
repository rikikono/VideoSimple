package com.example.musicproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class PlaylistAdapter extends BaseAdapter {

    private Context context;
    private List<VideoDBHelper.PlaylistItem> playlists;
    private VideoDBHelper dbHelper;

    public PlaylistAdapter(Context context, List<VideoDBHelper.PlaylistItem> playlists) {
        this.context = context;
        this.playlists = playlists;
        this.dbHelper = VideoDBHelper.getInstance(context);
    }

    public void updateList(List<VideoDBHelper.PlaylistItem> newList) {
        this.playlists = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public Object getItem(int position) {
        return playlists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return playlists.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tvPlaylistName);
            holder.tvCount = convertView.findViewById(R.id.tvPlaylistInfo);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        VideoDBHelper.PlaylistItem playlist = playlists.get(position);
        holder.tvName.setText(playlist.name);
        
        // Get video count
        int count = dbHelper.getVideosInPlaylist(playlist.id).size();
        holder.tvCount.setText(count + " videos");

        return convertView;
    }

    static class ViewHolder {
        TextView tvName;
        TextView tvCount;
    }
}