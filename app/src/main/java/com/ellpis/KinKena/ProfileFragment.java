package com.ellpis.KinKena;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Adapters.PlaylistTabAdapter;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.Repository.PlaylistRepository;
import com.ellpis.KinKena.Repository.UserRepository;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ProfileFragment extends Fragment implements PlaylistTabAdapter.ItemClickListener {
    String profileID = "";
    @BindView(R.id.profile_cover)
    ImageView cover;
    @BindView(R.id.profile_playlists_rv)
    RecyclerView recyclerView;
    @BindView(R.id.profile_title)
    TextView title;
    @BindView(R.id.profile_cover_container)
    CardView coverContainer;
    @BindView(R.id.profile_playlists_empty_notifier)
    TextView emptyNotifier;
    PlaylistTabAdapter playlistTabAdapter;
    List<Playlist> playlists = new ArrayList<>();

    public static ProfileFragment newInstance(String profileID) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("profileID", profileID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            profileID = getArguments().getString("profileID");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        getProfile();
        playlistTabAdapter = new PlaylistTabAdapter(playlists);
        playlistTabAdapter.setClickListener(this);
        recyclerView.setAdapter(playlistTabAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void getProfile() {
        UserRepository.getUser(profileID, task -> {
            if(getView()!=null){
                Picasso.get().load("https://firebasestorage.googleapis.com" + task.getResult().getString("profileImage"))
                        .placeholder(R.drawable.ic_profile)
                        .into(cover, new Callback() {
                            @Override
                            public void onSuccess() {
                                coverContainer.setRadius(cover.getWidth() / 2f);
                            }

                            @Override
                            public void onError(Exception e) {
                                coverContainer.setRadius(cover.getWidth() / 2f);
                            }
                        });
                title.setText(task.getResult().getString("username"));
            }

        });

        PlaylistRepository.getAllPlaylists(profileID, task -> {
            if(getView()!=null) {
                playlists.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Playlist playlist = document.toObject(Playlist.class);
//                    playlist.setPrivacy(document.getBoolean("privacy"));
                    Log.e("TAG", "getProfile: " + playlist.toString());
                    if (playlist.getOwnerID().equals(profileID) && !playlist.isPrivacy()) {
                        playlists.add(playlist);
                    }
                }
                Log.e("TAG", "getProfile: size = " + playlists.size());
                playlistTabAdapter.notifyDataSetChanged();
                setEmptyNotifier();
            }
        });
    }

    private void setEmptyNotifier() {
        if (playlists.size() == 0) {
            emptyNotifier.setVisibility(View.VISIBLE);
        } else {
            emptyNotifier.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlaylistItemClick(View view, int position) {
        getFragmentManager().beginTransaction().replace(getId(), PlaylistItemFragment.newInstance(playlists.get(position).getOwnerID(), playlists.get(position).getId(), playlists.get(position).isFromFirebase()))
                .addToBackStack(null)
                .commit();
    }
}
