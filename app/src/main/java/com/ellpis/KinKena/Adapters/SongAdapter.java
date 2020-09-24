package com.ellpis.KinKena.Adapters;

import android.content.Context;
import android.net.Uri;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.ArtistItemFragment;
import com.ellpis.KinKena.MainActivity;
import com.ellpis.KinKena.MusicPlayerSheet;
import com.ellpis.KinKena.Objects.AddToPlaylistBottomSheet;
import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Objects.Utility;
import com.ellpis.KinKena.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {


    private Fragment callerFragment;
    private String playlistId;
    private String ownerId;
    List<Song> songList;

    private SongAdapter.ItemClickListener mClickListener;
    private SongAdapter.ItemLongClickListener mLongClickListener;
    private SongAdapter.ItemDragListener mDragListener;
    private SongAdapter.ItemLTouchListener mLTouchListener;
    String currentUserID = FirebaseAuth.getInstance().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();


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
                mClickListener.onSongItemClick(v, getAdapterPosition());
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

    public SongAdapter(List<Song> bookCoverList, Fragment callerFragment, String ownerId, String playlistId) {
        this.ownerId = ownerId;
        songList = bookCoverList;
        this.playlistId = playlistId;

        this.callerFragment = callerFragment;
    }


    @NonNull
    @Override
    public SongAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.card_song_item, viewGroup, false);

        // Return a new holder instance
        SongAdapter.ViewHolder viewHolder = new SongAdapter.ViewHolder(contactView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder viewHolder, int i) {
        Uri songUri = Uri.parse("http://www.arifzefen.com/json/playSong.php?id=" + songList.get(i).getSongId());
        Utility.getImageLinkMini( songList.get(i).getThumbnail(), link -> {
            Picasso.get().load(link)
                    .into(viewHolder.songCoverImage);
        });


        viewHolder.songCoverImage.setAdjustViewBounds(true);
        viewHolder.title.setText(songList.get(i).getSongName());
        if (MusicPlayerSheet.getCurrentSongID() != null && MusicPlayerSheet.getCurrentSongID().equals(songList.get(i).getSongId() + "")) {
            viewHolder.title.setTextColor(viewHolder.title.getContext().getResources().getColor(R.color.green));
        }else {
            viewHolder.title.setTextColor(viewHolder.title.getContext().getResources().getColor(R.color.White));
        }
        viewHolder.artist.setText(songList.get(i).getArtistName());
        if (isSongDownloaded(songUri)) {
            viewHolder.downloadIcon.setImageDrawable(viewHolder.downloadIcon.getContext().getDrawable(R.drawable.ic_download_completed));
            viewHolder.downloadIcon.setVisibility(View.VISIBLE);
        }

        if (isSongQueueForDownload(songUri)) {
            viewHolder.downloadIcon.setImageDrawable(viewHolder.downloadIcon.getContext().getDrawable(R.drawable.ic_download_queued));
            viewHolder.downloadIcon.setVisibility(View.VISIBLE);
        }
        if (isSongDownloading(songUri)) {
            viewHolder.downloadIcon.setImageDrawable(viewHolder.downloadIcon.getContext().getDrawable(R.drawable.ic_loading));
            viewHolder.downloadIcon.setVisibility(View.VISIBLE);
        }
        viewHolder.overflowMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet(viewHolder, i);
                if (callerFragment.getView().findViewById(R.id.search_searchview) != null) {
                    callerFragment.getView().findViewById(R.id.search_searchview).clearFocus();
                }
            }
        });


    }

    private boolean isSongDownloaded(Uri songUri) {
        return MainActivity.songDownloadApplication.getDownloadTracker().isDownloaded(songUri);
    }

    private boolean isSongQueueForDownload(Uri songUri) {
        return MainActivity.songDownloadApplication.getDownloadTracker().isQueuedForDownloaded(songUri);
    }
    private boolean isSongDownloading(Uri songUri) {
        return MainActivity.songDownloadApplication.getDownloadTracker().isDownloading(songUri);
    }

    void showBottomSheet(ViewHolder viewHolder, int i) {
        View view = ((FragmentActivity) viewHolder.overflowMenu.getContext()).getLayoutInflater().inflate(R.layout.card_song_overflow_menu_bottomsheet, null);

        ((TextView) view.findViewById(R.id.card_song_menu_title)).setText(songList.get(i).getSongName());
        ((TextView) view.findViewById(R.id.card_song_menu_artist)).setText(songList.get(i).getArtistName());
        Utility.getImageLinkMini( songList.get(i).getThumbnail(), link -> {
            Picasso.get().load(link )
                    .into(((ImageView) view.findViewById(R.id.card_song_menu_cover)));
        });


        ((LinearLayout) view.findViewById(R.id.card_song_menu_like)).setOnClickListener((View.OnClickListener) v -> Toast.makeText(v.getContext(), songList.get(i).getSongName() + " liked", Toast.LENGTH_LONG).show());
//        ((TextView) );
//        ((TextView)view.findViewById(R.id.card_song_menu_add_playlist));
        ((TextView) view.findViewById(R.id.card_song_menu_add_queue)).setOnClickListener(v -> {
            MainActivity.addToQueue(songList.get(i));
        });
        BottomSheetDialog menuDialog = new BottomSheetDialog(viewHolder.artist.getContext(), R.style.SheetDialog);
        menuDialog.setContentView(view);
        menuDialog.show();
        view.findViewById(R.id.card_song_menu_add_playlist).setOnClickListener(v -> {
            menuDialog.dismiss();
            View newview = ((FragmentActivity) viewHolder.overflowMenu.getContext()).getLayoutInflater().inflate(R.layout.bottomsheet_add_playlist, null);
            AddToPlaylistBottomSheet dialog = new AddToPlaylistBottomSheet(viewHolder.artist.getContext(), R.style.SheetDialog, callerFragment.getActivity(), songList.get(i));
            dialog.setContentView(newview);
            dialog.show();
        });
        view.findViewById(R.id.card_song_menu_view_artist).setOnClickListener(v -> {
            menuDialog.dismiss();
            callerFragment.getFragmentManager().beginTransaction()
                    .replace(callerFragment.getId(), ArtistItemFragment.newInstance(songList.get(i).getArtistId() + ""))
                    .addToBackStack(null)
                    .commit();
        });
        if (ownerId == null || !ownerId.equals(currentUserID)) {
            ((LinearLayout) view.findViewById(R.id.card_song_item_delete_playlist).getParent()).setVisibility(View.GONE);
        }
        view.findViewById(R.id.card_song_item_delete_playlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Song Removed", Toast.LENGTH_SHORT).show();
                //docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                deleteSong(playlistId, i);
                SongAdapter.this.notifyItemRemoved(i);
                menuDialog.dismiss();
            }
        });
        view.findViewById(R.id.card_song_menu_add_queue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                MainActivity.musicPlayer.player
            }
        });
        view.findViewById(R.id.card_song_menu_add_queue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.addToQueue(songList.get(i));
                menuDialog.dismiss();
            }
        });
    }

    public void deleteSong(String playlistID, int i) {
        Map<String, Object> updates = new HashMap<>();
        songList.remove(i);
        updates.put("songs", songList);
        db.collection("Users").document(currentUserID).collection("Playlists").document(playlistID).update(updates);
    }

    public void add(Song item) {
        int size = songList.size();
        songList.add(item);

    }

    public void setSongList(List<Song> bookList) {
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

    public void setClickListener(SongAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setLongClickListener(SongAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    public void setDragListener(SongAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setTouchListener(SongAdapter.ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onSongItemClick(View view, int position);
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
