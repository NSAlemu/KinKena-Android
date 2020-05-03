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
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Objects.Profile;
import com.ellpis.KinKena.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {


    private Fragment callerFragment;
    private String playlistId;
    private String ownerId;
    List<Profile> profileList;

    private ProfileAdapter.ItemClickListener mClickListener;
    private ProfileAdapter.ItemLongClickListener mLongClickListener;
    private ProfileAdapter.ItemDragListener mDragListener;
    private ProfileAdapter.ItemLTouchListener mLTouchListener;



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnDragListener, View.OnTouchListener {

        ImageView songCoverImage;
        TextView title;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songCoverImage = itemView.findViewById(R.id.card_profile_item_cover);
            title = itemView.findViewById(R.id.card_profile_item_name);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnDragListener(this);
            itemView.setOnTouchListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null)
                mClickListener.onProfileItemClick(v, getAdapterPosition());
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

    public ProfileAdapter(List<Profile> bookCoverList, Fragment callerFragment, String ownerId, String playlistId) {
        this.ownerId = ownerId;
        profileList = bookCoverList;
        this.playlistId = playlistId;

        this.callerFragment = callerFragment;
    }


    @NonNull
    @Override
    public ProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.card_profile_item, viewGroup, false);

        // Return a new holder instance
        ProfileAdapter.ViewHolder viewHolder = new ProfileAdapter.ViewHolder(contactView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ViewHolder viewHolder, int i) {

        Picasso.get().load("https://firebasestorage.googleapis.com"+profileList.get(i).getProfileImage())
                .placeholder(R.drawable.ic_profile)
                .into(viewHolder.songCoverImage);

        viewHolder.songCoverImage.setAdjustViewBounds(true);
        viewHolder.title.setText(profileList.get(i).getUsername());

    }




    public void add(Profile item) {
        int size = profileList.size();
        profileList.add(item);

    }

    public void setProfileList(List<Profile> bookList) {
        this.profileList = bookList;
    }

    public void remove(int i) {
        profileList.remove(i);
    }

    @Override
    public int getItemCount() {

        return profileList.size();
    }
    // allows clicks events to be caught

    public void setClickListener(ProfileAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setLongClickListener(ProfileAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    public void setDragListener(ProfileAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setTouchListener(ProfileAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onProfileItemClick(View view, int position);
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
