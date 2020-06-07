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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.ForegroundService;
import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.R;
import com.ellpis.KinKena.helper.ItemTouchHelperAdapter;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    List<Song> songList;

    private QueueItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;
    private ItemDragListener mDragListener;
    private ItemLTouchListener mLTouchListener;

    @Override
    public void onItemDismiss(int position) {
        songList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(songList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnDragListener, View.OnTouchListener {

        ImageView songCoverImage, overflowMenu;
        TextView title, artist;
        ConstraintLayout  isPlayingIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songCoverImage = itemView.findViewById(R.id.queue_item_cover);
            title = itemView.findViewById(R.id.queue_item_name);
            artist = itemView.findViewById(R.id.queue_item_artist);
            overflowMenu = itemView.findViewById(R.id.queue_item_drag);
            isPlayingIcon = itemView.findViewById(R.id.queue_item_isplaying);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnDragListener(this);
            itemView.setOnTouchListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null)
                mClickListener.onQueueItemClick(v, getAdapterPosition());
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

    public QueueAdapter(List<Song> songList) {
        this.songList = songList;
    }

    @NonNull
    @Override
    public QueueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.card_queue_item, viewGroup, false);

        // Return a new holder instance
        QueueAdapter.ViewHolder viewHolder = new QueueAdapter.ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull QueueAdapter.ViewHolder viewHolder, int i) {
        Picasso.get().load("http://www.arifzefen.com" + songList.get(i).getThumbnail())
                .into(viewHolder.songCoverImage);

        viewHolder.songCoverImage.setAdjustViewBounds(true);
        viewHolder.title.setText(songList.get(i).getSongName());
        viewHolder.artist.setText(songList.get(i).getArtistName());
        viewHolder.isPlayingIcon.setVisibility(ForegroundService.player.getCurrentWindowIndex()==i? View.VISIBLE:View.GONE);
    }

    public void add(Song item) {
        int size = songList.size();
        songList.add(item);
    }

    public void remove(int i) {
        songList.remove(i);
    }

    @Override
    public int getItemCount() {

        return songList.size();
    }
    // allows clicks events to be caught

    public void setClickListener(QueueItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    public void setDragListener(ItemDragListener itemClickListener) {
        this.mDragListener = itemClickListener;
    }

    public void setTouchListener(ItemLTouchListener itemTouchListener) {
        this.mLTouchListener = itemTouchListener;
    }

    // parent activity will implement this method to respond to click events
    public interface QueueItemClickListener {
        void onQueueItemClick(View view, int position);
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
