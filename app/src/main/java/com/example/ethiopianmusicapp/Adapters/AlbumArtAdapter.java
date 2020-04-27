package com.example.ethiopianmusicapp.Adapters;

import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ethiopianmusicapp.Objects.Playlist;
import com.example.ethiopianmusicapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AlbumArtAdapter extends RecyclerView.Adapter<AlbumArtAdapter.ViewHolder> {

        List<Playlist> songList;

private AlbumArtAdapter.ItemClickListener mClickListener;
private AlbumArtAdapter.ItemLongClickListener mLongClickListener;
private AlbumArtAdapter.ItemDragListener mDragListener;
private AlbumArtAdapter.ItemLTouchListener mLTouchListener;


public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnDragListener, View.OnTouchListener {

    ImageView songCoverImage;
    TextView title;
LinearLayout labelBackground;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        songCoverImage = itemView.findViewById(R.id.card_abum_art_image);
        title = itemView.findViewById(R.id.card_abum_art_text);
        labelBackground = itemView.findViewById(R.id.card_album_label_background);


        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
        itemView.setOnDragListener(this);
        itemView.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null)
            mClickListener.onItemClick(v, getAdapterPosition(), songList.get( getAdapterPosition()));
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

    public AlbumArtAdapter(List<Playlist> bookCoverList) {
        songList = bookCoverList;
    }

    @NonNull
    @Override
    public AlbumArtAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.card_album_art, viewGroup, false);

        // Return a new holder instance
        AlbumArtAdapter.ViewHolder viewHolder = new AlbumArtAdapter.ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumArtAdapter.ViewHolder viewHolder, int i) {
    try {
        Picasso.get().load("http://www.arifzefen.com"+songList.get(i).getThumbnail()).into(viewHolder.songCoverImage);

    }catch (Exception e){
        Picasso.get().load(R.drawable.ic_library_music_black_24dp).into(viewHolder.songCoverImage);
    }
        try {
            viewHolder.title.setText(songList.get(i).getTitle());
        }catch (Exception e){
            try {
                viewHolder.title.setText(songList.get(i).getSubtitle().substring(0,10));
            }catch (Exception ex){
                viewHolder.title.setText("");
            }
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

    public void setClickListener(AlbumArtAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public void setLongClickListener(AlbumArtAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }
    public void setDragListener(AlbumArtAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public void setTouchListener(AlbumArtAdapter.ItemLongClickListener itemLongClickListener) {
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
