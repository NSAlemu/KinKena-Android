package com.ellpis.KinKena.Adapters;

import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.MainActivity;
import com.ellpis.KinKena.Objects.Album;
import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Objects.Utility;
import com.ellpis.KinKena.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AlbumListAdapter  extends RecyclerView.Adapter<AlbumListAdapter.ViewHolder> {

    List<Album> albumList;

    private AlbumListAdapter.ItemClickListener mClickListener;
    private AlbumListAdapter.ItemLongClickListener mLongClickListener;
    private AlbumListAdapter.ItemDragListener mDragListener;
    private AlbumListAdapter.ItemLTouchListener mLTouchListener;
    private Fragment callerFragment;



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnDragListener, View.OnTouchListener {

        TextView title;
        ImageView albumCoverImage;
        RecyclerView recyclerView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumCoverImage = itemView.findViewById(R.id.card_artists_album_image);
            title = itemView.findViewById(R.id.card_artists_album_title);
            recyclerView = itemView.findViewById(R.id.card_artists_album_rv);


            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnDragListener(this);
            itemView.setOnTouchListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null)
                mClickListener.onItemClick(v, getAdapterPosition());
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
            if (mLTouchListener != null)
                mLTouchListener.onItemTouch(v, getAdapterPosition(), event);
            return false;
        }
    }

    public AlbumListAdapter(List<Album> bookCoverList, Fragment callerFragment) {
        albumList = bookCoverList;
        this.callerFragment = callerFragment;
    }

    @NonNull
    @Override
    public AlbumListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.card_artists_album, viewGroup, false);

        // Return a new holder instance
        AlbumListAdapter.ViewHolder viewHolder = new AlbumListAdapter.ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumListAdapter.ViewHolder viewHolder, int i) {
        try {
            Utility.getImageLinkMini(albumList.get(i).getAlbumPurl(), link -> {
                Picasso.get().load(link )
                        .into(viewHolder.albumCoverImage);

            });
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_library_music_black_24dp).into(viewHolder.albumCoverImage);
        }
        try {
            viewHolder.title.setText(albumList.get(i).getAlbumName());
        }catch (Exception e){
            viewHolder.title.setText("");
        }
        List<Song> newSongList = new ArrayList<>();
        for(Song song: albumList.get(i).getSongs()){

            song.setThumbnail(albumList.get(i).getAlbumPurl());
            newSongList.add(song);
        }
        SongAdapter songAdapter = new SongAdapter(newSongList,callerFragment,null,null);
        songAdapter.setClickListener(new SongAdapter.ItemClickListener() {
            @Override
            public void onSongItemClick(View view, int position) {
                ArrayList<Song> newSongList = new ArrayList<>();
                newSongList.addAll(albumList.get(i).getSongs());
                MainActivity.playSong(position, newSongList, null);
            }
        });
//        viewHolder.recyclerView.setNestedScrollingEnabled(false);
        viewHolder.recyclerView.setHasFixedSize(true);
        viewHolder.recyclerView.setAdapter(songAdapter);
        viewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(viewHolder.recyclerView.getContext()));
    }

    public void add(Album item) {
        int size = albumList.size();
        albumList.add(item);

    }

    public void setAlbumList(List<Album> bookList) {
        this.albumList = bookList;
    }

    public void remove(int i) {
        albumList.remove(i);
    }

    @Override
    public int getItemCount() {

        return albumList.size();
    }
    // allows clicks events to be caught

    public void setClickListener(AlbumListAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public void setLongClickListener(AlbumListAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }
    public void setDragListener(AlbumListAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public void setTouchListener(AlbumListAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    public interface ItemLongClickListener {
        void onLongClick(View view, int position);
    }
    public interface ItemDragListener {
        void onItemDrag(View view, int position, DragEvent event);
    }
    public interface ItemLTouchListener {
        void onItemTouch(View view, int position,  MotionEvent event);
    }


}
