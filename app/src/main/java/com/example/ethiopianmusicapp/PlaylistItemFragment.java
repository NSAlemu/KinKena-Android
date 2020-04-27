package com.example.ethiopianmusicapp;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ethiopianmusicapp.Adapters.SearchAdapter;
import com.example.ethiopianmusicapp.Objects.Playlist;
import com.example.ethiopianmusicapp.Objects.Song;
import com.example.ethiopianmusicapp.Objects.Utility;
import com.example.ethiopianmusicapp.Repository.PlaylistRepository;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PlaylistItemFragment extends Fragment implements SearchAdapter.ItemClickListener {

    @BindView(R.id.playlist_cover)
    ImageView cover;

    @BindView(R.id.playlist_gradient_background)
    ImageView playlist_gradient_background;
    @BindView(R.id.playlist_title)
    TextView title;
    @BindView(R.id.playlist_subtitle)
    TextView subtitle;
    @BindView(R.id.playlist_rv)
    RecyclerView recyclerView;
    private SearchAdapter adapter;
    Playlist playlist;
    List<Song> songs = new ArrayList<>();
    PlaylistRepository playlistRepository;
    public static PlaylistItemFragment newInstance(String playlistName) {
        PlaylistItemFragment myFragment = new PlaylistItemFragment();
        Bundle args = new Bundle();
        args.putString("playlist", playlistName);

        myFragment.setArguments(args);

        return myFragment;
    }


    public static PlaylistItemFragment newInstance(Playlist playlist) {
        PlaylistItemFragment myFragment = new PlaylistItemFragment();
        Bundle args = new Bundle();
        args.putSerializable("songs", playlist);

        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(getArguments().getString("playlist")!=null){
            playlistRepository = new PlaylistRepository(getActivity());
            playlist = playlistRepository.getPlaylist(getArguments().getString("playlist"));
            songs = playlist.getSongs();
        }
        if(getArguments().getSerializable("songs")!=null){
            playlist = (Playlist) getArguments().getSerializable("songs");
            songs = playlist.getSongs();
        }

        return inflater.inflate(R.layout.fragment_playlist_item, container, false);

    }

    @Override
    public void onResume() {
        super.onResume();
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        adapter = new SearchAdapter(songs,getActivity());
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Picasso.get().load("http://www.arifzefen.com" + playlist.getThumbnail()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                cover.setImageBitmap(bitmap);

                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    public void onGenerated(Palette p) {
                        int backgroundColor = Utility.manipulateColor(p.getDominantColor(getResources().getColor(R.color.on_top_background)), 1.5f);
                        int backgroundColorMiddle = ColorUtils.setAlphaComponent(Utility
                                .manipulateColor(p.getDominantColor(getResources().getColor(R.color.on_top_background)),0.5f), 127);
                        GradientDrawable gd = new GradientDrawable(
                                GradientDrawable.Orientation.TOP_BOTTOM,
                                new int[]{backgroundColor,backgroundColorMiddle, 0 });
                        gd.setCornerRadius(0f);
                        playlist_gradient_background.setImageDrawable(gd);
                    }
                });

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
        title.setText(playlist.getTitle());
        subtitle.setText(playlist.getSubtitle());


    }

    @Override
    public void onItemClick(View view, int position) {
        MainActivity.playSong(songs.get(position));
    }
}
