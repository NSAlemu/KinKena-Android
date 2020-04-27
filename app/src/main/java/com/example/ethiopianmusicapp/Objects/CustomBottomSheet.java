package com.example.ethiopianmusicapp.Objects;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ethiopianmusicapp.Adapters.PlaylistTabAdapter;
import com.example.ethiopianmusicapp.R;
import com.example.ethiopianmusicapp.Repository.PlaylistRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class CustomBottomSheet extends BottomSheetDialog implements PlaylistTabAdapter.ItemClickListener {
    Button newPlaylist;
    RecyclerView recyclerView;
    PlaylistTabAdapter adapter;
    List<Playlist> playlist;
    Activity parentActivity;
    Song song;
    PlaylistRepository playlistRepository;

    public CustomBottomSheet(@NonNull Context context, int theme, Activity parentActivity, Song song) {
        super(context, theme);
        this.parentActivity = parentActivity;
        this.song = song;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistRepository = new PlaylistRepository(parentActivity);
        playlist = playlistRepository.getList();
        recyclerView = findViewById(R.id.bottomsheet_add_to_playlist);
        adapter = new PlaylistTabAdapter(playlist);
        adapter.setClickListener(this);
        newPlaylist = findViewById(R.id.bottomsheet_add_new_playlist);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newPlaylist = findViewById(R.id.bottomsheet_add_new_playlist);
        newPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final EditText input = new EditText(getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                lp.leftMargin = 50;
                lp.rightMargin = 50;
                lp.topMargin = 50;
                input.setLayoutParams(lp);
                input.setBackgroundColor(getContext().getResources().getColor(R.color.selectedItem));
                input.setTextColor(getContext().getResources().getColor(R.color.OffWhite));
                ColorStateList colorStateList = ColorStateList.valueOf(getContext().getResources().getColor(R.color.selectedItem));
                ViewCompat.setBackgroundTintList(input, colorStateList);
                LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout linearLayout = new LinearLayout(getContext());
                linearLayout.setLayoutParams(ll);
                linearLayout.addView(input);
                linearLayout.setBackgroundColor(getContext().getResources().getColor(R.color.Transparent));
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Create Playlist")
                        .setView(linearLayout)
                        .setPositiveButton("Create", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                playlistRepository.createPlaylist(input.getText().toString().trim());
                            }
                        });
                builder.setBackground(getContext().getDrawable(R.drawable.dialog_backgound));
                builder.show();
            }
        });
    }


    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getContext(),"Added to Playlist",Toast.LENGTH_LONG).show();
        playlistRepository.addToPlaylist(playlist.get(position).getTitle(), song);
        this.dismiss();
    }
}