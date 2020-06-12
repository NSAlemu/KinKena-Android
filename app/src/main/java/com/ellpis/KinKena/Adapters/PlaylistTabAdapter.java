package com.ellpis.KinKena.Adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.MainActivity;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.R;
import com.ellpis.KinKena.Repository.DownloadsRepository;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PlaylistTabAdapter extends RecyclerView.Adapter<PlaylistTabAdapter.ViewHolder> {

    List<Playlist> playlists;

    private PlaylistTabAdapter.ItemClickListener mClickListener;
    private PlaylistTabAdapter.ItemLongClickListener mLongClickListener;
    private PlaylistTabAdapter.ItemDragListener mDragListener;
    private PlaylistTabAdapter.ItemLTouchListener mLTouchListener;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnDragListener, View.OnTouchListener {

        ImageView songCoverImage, overflowMenu, downloadIcon;
        TextView title, artist;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songCoverImage = itemView.findViewById(R.id.search_song_cover);
            title = itemView.findViewById(R.id.search_song_name);
            artist = itemView.findViewById(R.id.search_song_artist);
            overflowMenu = itemView.findViewById(R.id.search_song_overflow_menu);
            downloadIcon = itemView.findViewById(R.id.card_song_download_icon);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnDragListener(this);
            itemView.setOnTouchListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null)
                mClickListener.onPlaylistItemClick(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if (mLongClickListener != null)
                mLongClickListener.onLongClick(v, getAdapterPosition());
            return false;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (mDragListener != null)
                mDragListener.onItemDrag(v, getAdapterPosition(), event);
            return false;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mLTouchListener != null) {
                mLTouchListener.onItemTouch(v, getAdapterPosition(), event);
            }

            return false;
        }
    }

    public PlaylistTabAdapter(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public PlaylistTabAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.card_song_item, viewGroup, false);

        // Return a new holder instance
        PlaylistTabAdapter.ViewHolder viewHolder = new PlaylistTabAdapter.ViewHolder(contactView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistTabAdapter.ViewHolder viewHolder, int i) {

        if(playlists.get(i).isFromFirebase()){
            Picasso.get().load("https://firebasestorage.googleapis.com"  + playlists.get(i).getThumbnail())
                    .placeholder(R.drawable.ic_library_music_black_24dp)
                    .into(viewHolder.songCoverImage);
        }else{
            Picasso.get().load("http://www.arifzefen.com" + playlists.get(i).getThumbnail())
                    .placeholder(R.drawable.ic_library_music_black_24dp)
                    .into(viewHolder.songCoverImage);
        }


        viewHolder.songCoverImage.setAdjustViewBounds(true);
        viewHolder.title.setText(playlists.get(i).getTitle());
        if(playlists.get(i).getSubtitle()==null){
            viewHolder.artist.setText(playlists.get(i).getOwnerUsername());
        }else{
            viewHolder.artist.setText(playlists.get(i).getSubtitle());
        }
        if(DownloadsRepository.isDownloaded(playlists.get(i))){
            viewHolder.downloadIcon.setVisibility(View.VISIBLE);
        }else{
            viewHolder.downloadIcon.setVisibility(View.GONE);
        }


        viewHolder.overflowMenu.setVisibility(View.GONE);

    }




    public void add(Playlist item) {
        playlists.add(0, item);
        this.notifyItemInserted(0);
    }

    public void setPlaylists(List<Playlist> bookList) {
        this.playlists = bookList;
    }

    public void remove(int i) {
        playlists.remove(i);
    }

    @Override
    public int getItemCount() {

        return playlists.size();
    }
    // allows clicks events to be caught

    public void setClickListener(PlaylistTabAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setLongClickListener(PlaylistTabAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    public void setDragListener(PlaylistTabAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setTouchListener(PlaylistTabAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onPlaylistItemClick(View view, int position);
    }

    public interface ItemLongClickListener {
        void onLongClick(View view, int position);
    }

    public interface ItemDragListener {
        void onItemDrag(View view, int position, DragEvent event);
    }

    public interface ItemLTouchListener {
        void onItemTouch(View view, int position, MotionEvent event);
    }


}
