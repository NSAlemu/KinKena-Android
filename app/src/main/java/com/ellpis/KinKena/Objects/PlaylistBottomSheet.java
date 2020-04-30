package com.ellpis.KinKena.Objects;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ellpis.KinKena.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PlaylistBottomSheet extends BottomSheetDialog{
    ImageView cover;
    TextView title;
    TextView createdBy;
    TextView renamePlaylist;
    TextView deletePlaylist;
    TextView changeCover;

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

        Picasso.get().load(playlist.getThumbnail()).into(cover);
        title.setText(playlist.getTitle());
        createdBy.setText("By "+playlist.getOwnerUsername());
        renamePlaylist.setOnClickListener(renamePlaylistOnclick());
        deletePlaylist.setOnClickListener(deletePlaylistOnclick());
        changeCover.setOnClickListener(changeCoverOnclick());
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
            getPickImageIntent();
            this.dismiss();
        };
    }
    public  Intent getPickImageIntent() {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
//        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)));
        intentList = addIntentsToList(parentFragment.getContext(), intentList, pickIntent);
        intentList = addIntentsToList(parentFragment.getContext(), intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                   "choose a Playlist cover Image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }
        parentFragment.startActivityForResult(chooserIntent, 0);
        return chooserIntent;
    }

    private  List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }
}