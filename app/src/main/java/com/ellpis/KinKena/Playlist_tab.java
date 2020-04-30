package com.ellpis.KinKena;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ellpis.KinKena.Adapters.PlaylistTabAdapter;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.Objects.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class Playlist_tab extends Fragment implements PlaylistTabAdapter.ItemClickListener {
    @BindView(R.id.playlist_tab_rv)
    RecyclerView recyclerView;
    @BindView(R.id.playlist_tab_create_playlist)
    Button createPlaylist;
    @BindView(R.id.playlist_tab_SwipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    PlaylistTabAdapter adapter;
    List<Playlist> playlist = new ArrayList<>();
    String currentUserID = FirebaseAuth.getInstance().getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist_tab, container, false);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        adapter = new PlaylistTabAdapter(playlist);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getPlaylist();
        createPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.createPlaylist(getContext(), adapter);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                playlist.clear();
                getPlaylist();
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
                                playlist.add(document.toObject(Playlist.class));
                                Log.e(("fromfirebase"), document.toObject(Playlist.class).isFromFirebase() + "");
                                final int pos = playlist.size() - 1;
                                playlist.get(pos).setId(document.getId());

                                if (!playlist.get(pos).isFromFirebase()) {
                                    playlist.get(pos).setSubtitle("Arifzfen");
                                }
                                adapter.notifyItemInserted(pos);
                            }

                            swipeRefreshLayout.setRefreshing(false);
                        } else {

                        }
                    }
                });
    }

    @Override
    public void onItemClick(View view, int position) {
        String id;
        if(playlist.get(position).isFromFirebase()){
            id = playlist.get(position).getId();
        }else{
            id = playlist.get(position).getOwnerID();
        }
        getFragmentManager().beginTransaction().replace(getId(), PlaylistItemFragment.newInstance(id, playlist.get(position).isFromFirebase()))
                .addToBackStack(null)
                .commit();
    }


}
