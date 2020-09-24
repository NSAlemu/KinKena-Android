package com.ellpis.KinKena.Adapters;

import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Objects.Album;
import com.ellpis.KinKena.Objects.Utility;
import com.ellpis.KinKena.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SimpleCircleCoverAdapter  extends RecyclerView.Adapter<SimpleCircleCoverAdapter.ViewHolder> {

    List<Album> albumList;

    private SimpleCircleCoverAdapter.ItemClickListener mClickListener;
    private SimpleCircleCoverAdapter.ItemLongClickListener mLongClickListener;
    private SimpleCircleCoverAdapter.ItemDragListener mDragListener;
    private SimpleCircleCoverAdapter.ItemLTouchListener mLTouchListener;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnDragListener, View.OnTouchListener {

        ImageView coverImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.card_simple_circle_cover_image);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnDragListener(this);
            itemView.setOnTouchListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null)
                mClickListener.onSimpleCoverItemClick(v, getAdapterPosition());
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

    public SimpleCircleCoverAdapter(List<Album> albumList) {
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public SimpleCircleCoverAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.card_simple_circle_cover, viewGroup, false);

        // Return a new holder instance
        SimpleCircleCoverAdapter.ViewHolder viewHolder = new SimpleCircleCoverAdapter.ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleCircleCoverAdapter.ViewHolder viewHolder, int i) {
        try {
            Utility.getImageLinkMini( albumList.get(i).getAlbumPurl(), link -> {
                Picasso.get().load(link).into(viewHolder.coverImage);
            });
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_library_music_black_24dp).into(viewHolder.coverImage);
        }

    }

    public void add(Album item) {
        int size = albumList.size();
        albumList.add(item);

    }

    public void setAlbumList(List<Album> albumList) {
        this.albumList = albumList;
    }

    public void remove(int i) {
        albumList.remove(i);
    }

    @Override
    public int getItemCount() {

        return albumList.size();
    }
    // allows clicks events to be caught

    public void setClickListener(SimpleCircleCoverAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public void setLongClickListener(SimpleCircleCoverAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }
    public void setDragListener(SimpleCircleCoverAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    public void setTouchListener(SimpleCircleCoverAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onSimpleCoverItemClick(View view, int position);
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
