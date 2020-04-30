package com.ellpis.KinKena;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Adapters.AlbumListAdapter;
import com.ellpis.KinKena.Adapters.SimpleCircleCoverAdapter;
import com.ellpis.KinKena.Adapters.SongAdapter;
import com.ellpis.KinKena.Objects.Album;
import com.ellpis.KinKena.Objects.Artist;
import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Retrofit.MusicRetrofit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ArtistItemFragment extends Fragment {
    private final String BASE_URL = "http://www.arifzefen.com";
    @BindView(R.id.artist_title)
    TextView title;
    @BindView(R.id.artist_subtitle)
    TextView subtitle;
    @BindView(R.id.artist_cover)
    ImageView cover;

    @BindView(R.id.artist_top_tracks_rv)
    RecyclerView topTrackRv;
    @BindView(R.id.artist_album_rv)
    RecyclerView albumCoverRv;
    @BindView(R.id.artist_album_list_rv)
    RecyclerView albumListRv;
    @BindView(R.id.artist_item_shuffle)
    Button shuffleBtn;

    SongAdapter topTrackAdapter;
    SimpleCircleCoverAdapter albumCoverAdapter;
    AlbumListAdapter albumListAdapter;

    List<Song> topTrackList = new ArrayList<>();
    List<Album> albumList = new ArrayList<>();
    private String artistID;
    private Retrofit retrofit;

    public static ArtistItemFragment newInstance(String artistID) {
        ArtistItemFragment myFragment = new ArtistItemFragment();
        Bundle args = new Bundle();
        args.putString("artistID", artistID);
        myFragment.setArguments(args);

        return myFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getArguments().getString("artistID") != null) {
            artistID = getArguments().getString("artistID");
        }
        return inflater.inflate(R.layout.fragment_artist_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        getData();
    }

    void getData() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Log.e("retro", artistID + ".json");
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        MusicRetrofit service = retrofit.create(MusicRetrofit.class);
        Call<Artist> call = service.getArtist(artistID + ".json");
        call.enqueue(new retrofit2.Callback<Artist>() {

            @Override
            public void onResponse(Call<Artist> call, Response<Artist> resBookSearch) {
                if (resBookSearch.errorBody() == null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Artist artist = resBookSearch.body();
                            artist.setSong_sArtistData();
                            initalizeViews(artist);

                        }
                    });

                } else {
                    Log.e("retrofit res", resBookSearch.errorBody().toString());
                }

            }

            @Override
            public void onFailure(Call<Artist> call, Throwable t) {
                Log.e("retrofit", t.getMessage() + "\n" + t.toString());
            }
        });

    }

    private void initalizeViews(Artist artist) {
        topTrackList = getTopTracks(artist.getAlbums());
        title.setText(artist.getArtistName());
        subtitle.setText(artist.getArtistBio());
        albumList.addAll(artist.getAlbums());
        Picasso.get().load(BASE_URL + artist.getThumbnail()).into(cover);
        topTrackAdapter = new SongAdapter(topTrackList, this, null, null);
        albumCoverAdapter = new SimpleCircleCoverAdapter(albumList);
        albumListAdapter = new AlbumListAdapter(albumList, this);

        topTrackRv.setAdapter(topTrackAdapter);
        topTrackRv.setNestedScrollingEnabled(false);
        albumCoverRv.setAdapter(albumCoverAdapter);
        albumCoverRv.setNestedScrollingEnabled(false);
        albumListRv.setAdapter(albumListAdapter);
        albumListRv.setNestedScrollingEnabled(false);

        topTrackRv.setLayoutManager(new LinearLayoutManager(getContext()));
        albumCoverRv.setLayoutManager(new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.HORIZONTAL,
                false));
        albumListRv.setLayoutManager(new LinearLayoutManager(getContext()));
        shuffleBtn.setOnClickListener(shuffleOnClickListener());
        getView().findViewById(R.id.artist_item_progressbar).setVisibility(View.GONE);
        getView().findViewById(R.id.artist_item_nestedscrollview).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.browse_collapser).setVisibility(View.VISIBLE);
    }

    private List<Song> getTopTracks(List<Album> albums) {
        ArrayList<Song> sortSongs = new ArrayList<>();
        for (Album album : albums) {
            for (Song song : album.getSongs()) {
                int insertpos = 0;
                for (int i = 0; i < sortSongs.size(); i++) {
                    if (sortSongs.get(i).getSongLikes() > song.getSongLikes()) {
                        break;
                    }
                    insertpos++;
                }
                if (insertpos == sortSongs.size()) {
                    sortSongs.add(song);
                }
                sortSongs.add(insertpos, song);
            }
        }
        ArrayList<Song> topSongList = new ArrayList<>();
        for (int i = 0; i < 5 && i < sortSongs.size(); i++) {
            topSongList.add(sortSongs.get(i));
        }
        return topSongList;
    }

    private View.OnClickListener shuffleOnClickListener() {
        return v -> {

                ArrayList<Song> tempSongs = new ArrayList<>();
                for (Album album : albumList) {
                    tempSongs.addAll(album.getSongs());
                }
            if(tempSongs.size()>0){
                MainActivity.playSong((new Random()).nextInt(tempSongs.size() - 1), tempSongs, true);
            }

        };
    }
}
