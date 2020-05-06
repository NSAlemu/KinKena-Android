package com.ellpis.KinKena.Objects;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ellpis.KinKena.R;
import com.ellpis.KinKena.Repository.PlaylistRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class PlaylistBottomSheet extends BottomSheetDialog{
    ImageView cover;
    TextView title;
    TextView createdBy;
    TextView renamePlaylist;
    TextView deletePlaylist;
    TextView changeCover;
    TextView privacy;

    Fragment parentFragment;
    Playlist playlist;
    String currentUserID = FirebaseAuth.getInstance().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PlaylistBottomSheet(@NonNull Context context, int theme, Fragment parentFragment, Playlist playlist) {
        super(context, theme);
        this.parentFragment = parentFragment;
        this.playlist = playlist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        cover =view.findViewById(R.id.card_playlist_menu_cover);
        title =view.findViewById(R.id.card_playlist_menu_title);
        createdBy =view.findViewById(R.id.card_playlist_menu_creator);
        renamePlaylist =view.findViewById(R.id.card_playlist_menu_rename_playlist);
        deletePlaylist =view.findViewById(R.id.card_playlist_menu_delete_playlist);
        changeCover =view.findViewById(R.id.card_playlist_menu_change_cover);
        privacy =view.findViewById(R.id.card_playlist_menu_private_playlist);

        if(playlist.isFromFirebase()){
            Picasso.get().load("https://firebasestorage.googleapis.com"  +playlist.getThumbnail())
                    .placeholder(R.drawable.ic_library_music_black_24dp)
                    .into(cover);
        }else{
            Picasso.get().load("http://www.arifzefen.com" + playlist.getThumbnail())
                    .placeholder(R.drawable.ic_library_music_black_24dp)
                    .into(cover);
        }
        if(playlist.isPrivacy()){
            privacy.setText("Make Playlist Public");
        }else{
            privacy.setText("Make Playlist Private");
        }
        title.setText(playlist.getTitle());
        createdBy.setText("By "+playlist.getOwnerUsername());
        renamePlaylist.setOnClickListener(renamePlaylistOnclick());
        deletePlaylist.setOnClickListener(deletePlaylistOnclick());
        changeCover.setOnClickListener(changeCoverOnclick());
        privacy.setOnClickListener(privacyOnclick());
    }


    private View.OnClickListener renamePlaylistOnclick(){
        return v -> {
            Utility.renamePlaylist(parentFragment, playlist);
            this.dismiss();
        };
    }
    private View.OnClickListener deletePlaylistOnclick(){
        return v -> {
            Utility.deletePlaylist(parentFragment, playlist);
            this.dismiss();
        };
    }
    private View.OnClickListener changeCoverOnclick(){
        return v -> {
            Utility.getPickImageIntent(parentFragment);
            this.dismiss();
        };
    }
    private View.OnClickListener privacyOnclick(){
        return v -> {
            PlaylistRepository.setPrivacyPlaylist(playlist.getId(), !playlist.isPrivacy(),parentFragment);
            playlist.setPrivacy(!playlist.isPrivacy());
            this.dismiss();
        };
    }

}