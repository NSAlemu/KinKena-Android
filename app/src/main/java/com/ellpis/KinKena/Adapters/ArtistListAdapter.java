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

import com.ellpis.KinKena.Objects.SearchArtist;
import com.ellpis.KinKena.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ArtistListAdapter extends RecyclerView.Adapter<ArtistListAdapter.ViewHolder> {


private Fragment callerFragment;
private String playlistId;
private String ownerId;
        List<SearchArtist> searchArtistList;

private ArtistListAdapter.ItemClickListener mClickListener;
private ArtistListAdapter.ItemLongClickListener mLongClickListener;
private ArtistListAdapter.ItemDragListener mDragListener;
private ArtistListAdapter.ItemLTouchListener mLTouchListener;
        String currentUserID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


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
            mClickListener.onArtistItemClick(v, getAdapterPosition());
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

    public ArtistListAdapter(List<SearchArtist> bookCoverList, Fragment callerFragment, String ownerId, String playlistId) {
        this.ownerId = ownerId;
        searchArtistList = bookCoverList;
        this.playlistId = playlistId;

        this.callerFragment = callerFragment;
    }


    @NonNull
    @Override
    public ArtistListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.card_profile_item, viewGroup, false);

        // Return a new holder instance
        ArtistListAdapter.ViewHolder viewHolder = new ArtistListAdapter.ViewHolder(contactView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistListAdapter.ViewHolder viewHolder, int i) {

        Picasso.get().load("http://www.arifzefen.com" + searchArtistList.get(i).getThumbnail())
                .into(viewHolder.songCoverImage);

        viewHolder.songCoverImage.setAdjustViewBounds(true);
        viewHolder.title.setText(searchArtistList.get(i).getName());

    }




    public void add(SearchArtist item) {
        int size = searchArtistList.size();
        searchArtistList.add(item);

    }

    public void setSearchArtistList(List<SearchArtist> bookList) {
        this.searchArtistList = bookList;
    }

    public void remove(int i) {
        searchArtistList.remove(i);
    }

    @Override
    public int getItemCount() {

        return searchArtistList.size();
    }
    // allows clicks events to be caught

    public void setClickListener(ArtistListAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setLongClickListener(ArtistListAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    public void setDragListener(ArtistListAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setTouchListener(ArtistListAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

// parent activity will implement this method to respond to click events
public interface ItemClickListener {
    void onArtistItemClick(View view, int position);
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
