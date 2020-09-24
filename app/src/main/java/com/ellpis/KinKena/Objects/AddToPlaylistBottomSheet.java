package com.ellpis.KinKena.Objects;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Adapters.PlaylistTabAdapter;
import com.ellpis.KinKena.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddToPlaylistBottomSheet extends BottomSheetDialog implements PlaylistTabAdapter.ItemClickListener {
    Button newPlaylist;
    RecyclerView recyclerView;
    PlaylistTabAdapter adapter;
    Activity parentActivity;
    Song song;
    List<Playlist> playlist = new ArrayList<>();
    String currentUserID = FirebaseAuth.getInstance().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AddToPlaylistBottomSheet(@NonNull Context context, int theme, Activity parentActivity, Song song) {
        super(context, theme);
        this.parentActivity = parentActivity;
        this.song = song;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPlaylist();
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
                Dialogs.createPlaylist(getContext(), song);
                AddToPlaylistBottomSheet.this.dismiss();
            }
        });
    }

    private void getPlaylist() {

        db.collection("Users").document(currentUserID).collection("Playlists")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Playlist newPlaylist = document.toObject(Playlist.class);
                                if (newPlaylist.getOwnerID().equals(FirebaseAuth.getInstance().getUid())) {
                                    playlist.add(newPlaylist);
                                }

                                adapter.notifyItemInserted(playlist.size() - 1);
                            }
                        } else {

                        }
                    }
                });
    }


    @Override
    public void onPlaylistItemClick(View view, int position) {
        Toast.makeText(getContext(), "Added to Playlist", Toast.LENGTH_LONG).show();
        Map<String, Object> updates = new HashMap<>();
        List<Song> songList = new ArrayList<>();
        if (playlist.get(position).getSongs() != null) {
            songList = playlist.get(position).getSongs();
        }
        songList.add(song);
        updates.put("songs", songList);
        db.collection("Users").document(currentUserID).collection("Playlists").document(playlist.get(position).getId()).update(updates);
        this.dismiss();
    }
}