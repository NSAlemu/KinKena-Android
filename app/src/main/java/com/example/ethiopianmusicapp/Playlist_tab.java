package com.example.ethiopianmusicapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ethiopianmusicapp.Adapters.PlaylistTabAdapter;
import com.example.ethiopianmusicapp.Objects.Playlist;
import com.example.ethiopianmusicapp.Repository.PlaylistRepository;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class Playlist_tab extends Fragment implements PlaylistTabAdapter.ItemClickListener{
    @BindView(R.id.playlist_tab_rv)
    RecyclerView recyclerView;
    PlaylistTabAdapter adapter;
    List<Playlist> playlist;
    PlaylistRepository playlistRepository;
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
        ButterKnife.bind(this,view);
        playlistRepository= new PlaylistRepository(getActivity());
        playlist = playlistRepository.getList();
        adapter = new PlaylistTabAdapter(playlist);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onItemClick(View view, int position) {
        getFragmentManager().beginTransaction().replace(getId(), PlaylistItemFragment.newInstance(playlist.get(position).getTitle()))
                .addToBackStack(null)
                .commit();
    }
}
