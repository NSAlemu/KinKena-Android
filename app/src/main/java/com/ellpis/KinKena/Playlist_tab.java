package com.ellpis.KinKena;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Adapters.PlaylistTabAdapter;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.Objects.Utility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class Playlist_tab extends Fragment implements PlaylistTabAdapter.ItemClickListener {
    @BindView(R.id.playlist_tab_rv)
    RecyclerView recyclerView;
    @BindView(R.id.playlist_tab_create_playlist)
    Button createPlaylist;
    @BindView(R.id.playlist_tab_setting)
    ImageButton settingsBtn;
    @BindView(R.id.playlist_tab_empty_notifier)
    TextView emptyNotifier;
    PlaylistTabAdapter adapter;
    List<Playlist> playlist = new ArrayList<>();
    private ListenerRegistration registration;


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
        createPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.createPlaylist(getContext(), adapter);
            }
        });
        settingsBtn.setOnClickListener(settingsOnClick());

        final CollectionReference docRef = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Playlists");

        registration = docRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            getPlaylist(queryDocumentSnapshots);
        });
    }

    private void getPlaylist(QuerySnapshot queryDocumentSnapshots) {
        playlist.clear();
        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
            playlist.add(document.toObject(Playlist.class));
            final int pos = playlist.size() - 1;
            playlist.get(pos).setId(document.getId());

            if (!playlist.get(pos).isFromFirebase()) {
                playlist.get(pos).setSubtitle("Arifzfen");
            }

        }
        adapter.notifyDataSetChanged();
        setEmptyNotifier();
    }

    private void setEmptyNotifier() {
        if(playlist.size()==0){
            emptyNotifier.setVisibility(View.VISIBLE);
        }
        else{
            emptyNotifier.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener settingsOnClick(){
        return v->{
            getFragmentManager().beginTransaction().replace(getId(), new AccountSettings())
                    .addToBackStack(null)
                    .commit();
        };
    }

    @Override
    public void onPlaylistItemClick(View view, int position) {
        Log.e(TAG, "onPlaylistItemClick: "+playlist.get(position).toString() );
        getFragmentManager().beginTransaction().replace(getId(), PlaylistItemFragment.newInstance(playlist.get(position).getOwnerID(),playlist.get(position).getId(), playlist.get(position).isFromFirebase()))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registration.remove();
    }
}
