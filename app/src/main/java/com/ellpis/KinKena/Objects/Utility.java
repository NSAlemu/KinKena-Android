package com.ellpis.KinKena.Objects;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.ellpis.KinKena.Adapters.PlaylistTabAdapter;
import com.ellpis.KinKena.MainActivity;
import com.ellpis.KinKena.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class Utility {

    public static int manipulateColor(int color, float whiteAmount) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * 0.6f);
        int g = Math.round(Color.green(color) * 0.6f);
        int b = Math.round(Color.blue(color) * 0.6f);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }

    public static void createPlaylist(Context context, Song song) {

        final EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.leftMargin = 50;
        lp.rightMargin = 50;
        lp.topMargin = 50;
        input.setLayoutParams(lp);
        input.setHint("Playlist Name");
        input.setHintTextColor(context.getResources().getColor(R.color.White));
        input.setBackgroundColor(context.getResources().getColor(R.color.OffWhite));
        input.setTextColor(context.getResources().getColor(R.color.White));
        ColorStateList colorStateList = ColorStateList.valueOf(context.getResources().getColor(R.color.selectedItem));
        ViewCompat.setBackgroundTintList(input, colorStateList);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(ll);
        linearLayout.addView(input);
        linearLayout.setBackgroundColor(context.getResources().getColor(R.color.Transparent));
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle("Create Playlist")
                .setView(linearLayout)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String currentUserID = FirebaseAuth.getInstance().getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Playlist playlist = new Playlist();
                        List<Song> songs = new ArrayList<>();
                        if (song != null) {
                            songs.add(song);
                        }
                        playlist.setSongs(songs);
                        playlist.setOwnerID(currentUserID);
                        playlist.setTitle(input.getText().toString().trim());
                        playlist.setOwnerUsername(MainActivity.username);
                        db.collection("Users").document(currentUserID).collection("Playlists").add(playlist);
                    }
                });
        builder.setBackground(context.getDrawable(R.drawable.dialog_backgound));
        builder.show();
    }

    public static void createPlaylist(Context context, PlaylistTabAdapter adapter) {

        final EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.leftMargin = 50;
        lp.rightMargin = 50;
        lp.topMargin = 50;
        input.setLayoutParams(lp);
        input.setHint("Playlist Name");
        input.setBackgroundColor(context.getResources().getColor(R.color.selectedItem));
        input.setTextColor(context.getResources().getColor(R.color.OffWhite));
        ColorStateList colorStateList = ColorStateList.valueOf(context.getResources().getColor(R.color.selectedItem));
        ViewCompat.setBackgroundTintList(input, colorStateList);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(ll);
        linearLayout.addView(input);
        linearLayout.setBackgroundColor(context.getResources().getColor(R.color.Transparent));
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle("Create Playlist")
                .setView(linearLayout)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String currentUserID = FirebaseAuth.getInstance().getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Playlist playlist = new Playlist();
                        List<Song> songs = new ArrayList<>();
                        playlist.setSongs(songs);
                        playlist.setTitle(input.getText().toString().trim());
                        playlist.setOwnerID(currentUserID);
                        playlist.setOwnerUsername(MainActivity.username);
                        db.collection("Users").document(currentUserID).collection("Playlists")
                                .add(playlist).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                db.collection("Users").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        playlist.setSubtitle(task.getResult().get("username").toString());
                                        adapter.add(playlist);
                                    }
                                });

                            }
                        });


                    }
                });
        builder.setBackground(context.getDrawable(R.drawable.dialog_backgound));
        builder.show();
    }



    public static void renamePlaylist(Fragment fragment, Playlist playlist) {

        final EditText input = new EditText(fragment.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.leftMargin = 50;
        lp.rightMargin = 50;
        lp.topMargin = 50;
        input.setLayoutParams(lp);
        input.setHint("Playlist Name");
        input.setText(playlist.getTitle());
        input.setBackgroundColor(fragment.getContext().getResources().getColor(R.color.selectedItem));
        input.setTextColor(fragment.getContext().getResources().getColor(R.color.OffWhite));
        ColorStateList colorStateList = ColorStateList.valueOf(fragment.getContext().getResources().getColor(R.color.selectedItem));
        ViewCompat.setBackgroundTintList(input, colorStateList);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout linearLayout = new LinearLayout(fragment.getContext());
        linearLayout.setLayoutParams(ll);
        linearLayout.addView(input);
        linearLayout.setBackgroundColor(fragment.getContext().getResources().getColor(R.color.Transparent));
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(fragment.getContext())
                .setTitle("Rename Playlist")
                .setView(linearLayout)
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String currentUserID = FirebaseAuth.getInstance().getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Users").document(currentUserID).collection("Playlists")
                                .document(playlist.getId()).update("title",input.getText().toString().trim() );
                        ((TextView) fragment.getView().findViewById(R.id.playlist_title)).setText(input.getText().toString());
                    }
                });
        builder.setBackground(fragment.getContext().getDrawable(R.drawable.dialog_backgound));
        builder.show();
    }
    public static void deletePlaylist(Fragment fragment, Playlist playlist) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(fragment.getContext())
                .setTitle("Are you sure you want to delete your playlist \""+playlist.getTitle()+"\"")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String currentUserID = FirebaseAuth.getInstance().getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Users").document(currentUserID).collection("Playlists")
                                .document(playlist.getId()).delete();
                        fragment.getFragmentManager().popBackStackImmediate();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.setBackground(fragment.getContext().getDrawable(R.drawable.dialog_backgound));
        builder.show();
    }
}
