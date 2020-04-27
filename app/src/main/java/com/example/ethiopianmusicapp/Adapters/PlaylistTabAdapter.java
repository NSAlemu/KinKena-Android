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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ethiopianmusicapp.Objects.Playlist;
import com.example.ethiopianmusicapp.Objects.Song;
import com.example.ethiopianmusicapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PlaylistTabAdapter extends RecyclerView.Adapter<PlaylistTabAdapter.ViewHolder> {

        List<Playlist> playlists;

private PlaylistTabAdapter.ItemClickListener mClickListener;
private PlaylistTabAdapter.ItemLongClickListener mLongClickListener;
private PlaylistTabAdapter.ItemDragListener mDragListener;
private PlaylistTabAdapter.ItemLTouchListener mLTouchListener;


public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnDragListener, View.OnTouchListener {

    ImageView songCoverImage, overflowMenu;
    TextView title, artist;


    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        songCoverImage = itemView.findViewById(R.id.search_song_cover);
        title = itemView.findViewById(R.id.search_song_name);
        artist = itemView.findViewById(R.id.search_song_artist);
        overflowMenu = itemView.findViewById(R.id.search_song_overflow_menu);
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
        if (mLTouchListener != null){
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
        View contactView = inflater.inflate(R.layout.card_search_result, viewGroup, false);

        // Return a new holder instance
        PlaylistTabAdapter.ViewHolder viewHolder = new PlaylistTabAdapter.ViewHolder(contactView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistTabAdapter.ViewHolder viewHolder, int i) {

        Picasso.get().load("http://www.arifzefen.com"+ playlists.get(i).getThumbnail())
                .into(viewHolder.songCoverImage);

        viewHolder.songCoverImage.setAdjustViewBounds(true);
        viewHolder.title.setText(playlists.get(i).getTitle());
        viewHolder.artist.setText(playlists.get(i).getSubtitle());


    }

    void showBottomSheet(SearchAdapter.ViewHolder viewHolder, Song song){
        View view = ((FragmentActivity)viewHolder.overflowMenu.getContext()).getLayoutInflater().inflate(R.layout.card_song_overflow_menu_bottomsheet, null);

        ((TextView)view.findViewById(R.id.card_song_menu_artist)).setText(song.getSongName());
        ((TextView) view.findViewById(R.id.card_song_menu_title)).setText(song.getArtistName());
        Picasso.get().load("http://www.arifzefen.com"+song.getThumbnail())
                .into(((ImageView) view.findViewById(R.id.card_song_menu_cover)));

        ((LinearLayout) view.findViewById(R.id.card_song_menu_like)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), song.getSongName() +" liked",Toast.LENGTH_LONG).show();
            }
        });
//        ((TextView)  view.findViewById(R.id.card_song_menu_remove_from_playlist));
//        ((TextView)view.findViewById(R.id.card_song_menu_add_playlist));
//        ((TextView)view.findViewById(R.id.card_song_menu_add_queue));
//        ((TextView) view.findViewById(R.id.card_song_menu_view_artist));;




        BottomSheetDialog dialog = new BottomSheetDialog(viewHolder.artist.getContext(),R.style.SheetDialog);
        dialog.setContentView(view);
        dialog.show();
    }

    public void add(Playlist item) {
        int size = playlists.size();
        playlists.add(item);

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
