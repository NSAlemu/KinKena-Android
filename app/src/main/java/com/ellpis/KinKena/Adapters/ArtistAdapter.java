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
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.Objects.Utility;
import com.ellpis.KinKena.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ArtistAdapter  extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    List<Playlist> songList;

    private ArtistAdapter.ItemClickListener mClickListener;
    private ArtistAdapter.ItemLongClickListener mLongClickListener;
    private ArtistAdapter.ItemDragListener mDragListener;
    private ArtistAdapter.ItemLTouchListener mLTouchListener;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnDragListener, View.OnTouchListener {

        ImageView songCoverImage;
        TextView title;
        TextView subtitle;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songCoverImage = itemView.findViewById(R.id.card_album_art_horiz_image);
            title = itemView.findViewById(R.id.card_album_art_horiz_title);
            subtitle = itemView.findViewById(R.id.card_album_art_horiz_subtitle);


            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnDragListener(this);
            itemView.setOnTouchListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null)
                mClickListener.onItemClick(v, getAdapterPosition(), songList.get(getAdapterPosition()));
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

    public ArtistAdapter(List<Playlist> bookCoverList) {
        songList = bookCoverList;
    }

    @NonNull
    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.card_album_art_horiz, viewGroup, false);

        // Return a new holder instance
        ArtistAdapter.ViewHolder viewHolder = new ArtistAdapter.ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistAdapter.ViewHolder viewHolder, int i) {
        try {
            Utility.getImageLinkMini(songList.get(i).getThumbnail(), link -> {
                Picasso.get().load(link).into(viewHolder.songCoverImage);

            });
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_library_music_black_24dp).into(viewHolder.songCoverImage);
        }
        try {
            viewHolder.title.setText(songList.get(i).getTitle());
        }catch (Exception e){
            viewHolder.title.setText("");
        }
        try {
            viewHolder.subtitle.setText(songList.get(i).getSubtitle());
        }catch (Exception e){
            viewHolder.subtitle.setText("");
        }
    }

    public void add(Playlist item) {
        int size = songList.size();
        songList.add(item);

    }

    public void setSongList(List<Playlist> bookList) {
        this.songList = bookList;
    }

    public void remove(int i) {
        songList.remove(i);
    }

    @Override
    public int getItemCount() {

        return songList.size();
    }
    // allows clicks events to be caught

    public void setClickListener(ArtistAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public void setLongClickListener(ArtistAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }
    public void setDragListener(ArtistAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public void setTouchListener(ArtistAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, Playlist result);
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
