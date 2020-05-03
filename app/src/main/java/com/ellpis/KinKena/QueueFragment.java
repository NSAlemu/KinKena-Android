package com.ellpis.KinKena;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Adapters.SongAdapter;
import com.ellpis.KinKena.Objects.Song;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class QueueFragment extends Fragment implements SongAdapter.ItemClickListener {

    @BindView(R.id.card_queue_rv)
    RecyclerView recyclerView;
    SongAdapter adapter;
    private ArrayList<Song> songList;

    public static QueueFragment newInstance(ArrayList<Song> songList) {
        QueueFragment fragment = new QueueFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("Songlist", songList);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            songList = getArguments().getParcelableArrayList("Songlist");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_queue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        adapter = new SongAdapter(songList,this,null, null);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onSongItemClick(View view, int position) {
        //TODO: NOT YET IMPLEMENTED
        //MainActivity.playSongInQueue(position);
    }
}
