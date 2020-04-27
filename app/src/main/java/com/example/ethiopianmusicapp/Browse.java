package com.example.ethiopianmusicapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ethiopianmusicapp.Adapters.AlbumArtAdapter;
import com.example.ethiopianmusicapp.Adapters.AlbumArtHorizAdapter;
import com.example.ethiopianmusicapp.Objects.Playlist;
import com.example.ethiopianmusicapp.Retrofit.MusicRetrofit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Browse extends Fragment implements AlbumArtAdapter.ItemClickListener, AlbumArtHorizAdapter.ItemClickListener {
    private final String BASE_URL = "http://www.arifzefen.com";
    Random rand = new Random();


    String[] featuredLinks = {"curated/dancenchifera",
            "curated/ethiohip-hop",
            "curated/ethiojazz",
            "curated/oldies",
            "curated/reggaefusion",
            "curated/slowjamz",
            "curated/tizita",
            "curated/traditional",
            "curated/weddingsongs",
            "curated/workout"};
    String[] popularLinks = {"list/mostrecent",
            "list/mostliked",
            "list/mostplayed"};
    String[] genreLinks = {"list/orthodoxmezmur",
            "list/protestantmezmur",
            "list/menzuma",
            "list/drama",
            "list/audiobooks",
            "list/instrumentals",
            "list/other",
            "list/azmarisounds",
            "list/kids",
            "list/amahric",
            "list/english",
            "list/guragegna",
            "list/guragigna",
            "list/haderegna",
            "list/harari",
            "list/instrumental",
            "list/oromiffa",
            "list/oromigna",
            "list/sudanese",
            "list/wolita"};
    String[] topPicksLinks = {
            "curated/dancenchifera",
            "curated/ethiohip-hop",
            "curated/ethiojazz",
            "curated/oldies",
            "curated/reggaefusion",
            "curated/slowjamz",
            "curated/tizita",
            "curated/traditional",
            "curated/weddingsongs",
            "#curated/workout"};


    private Retrofit retrofit;
    List<Playlist> featuredPlaylists = new ArrayList<>();
    List<Playlist> popularPlaylists = new ArrayList<>();
    List<Playlist> GenrePlaylists = new ArrayList<>();
    List<Playlist> topPicksPlaylists = new ArrayList<>();
    AlbumArtAdapter featuredAdapter;
    AlbumArtHorizAdapter popularAdapter;
    AlbumArtAdapter GenreAdapter;
    AlbumArtAdapter topPicksAdapter;
    @BindView(R.id.browse_featured_rv)
    RecyclerView featuredRv;
    @BindView(R.id.browse_popular_rv)
    RecyclerView popularRv;
    @BindView(R.id.browse_genre_rv)
    RecyclerView genreRv;
    @BindView(R.id.browse_top_pic_rv)
    RecyclerView topPicksRv;
    Map<String, String[]> linkMap = new HashMap<>();
    Map<String, List<Playlist>> playlistMap = new HashMap<>();
    Map<String, RecyclerView.Adapter> adapterMap = new HashMap<>();
    Map<String, RecyclerView> recyclerViewMap = new HashMap<>();
    private boolean loading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        linkMap.put("Featured", featuredLinks);
        linkMap.put("popular", popularLinks);
        linkMap.put("Genre", genreLinks);
        linkMap.put("TopPicks", topPicksLinks);

        for (Map.Entry<String, String[]> entry : linkMap.entrySet()) {
            List<String> ArrayList= Arrays.asList(entry.getValue());
            Collections.shuffle(ArrayList);
            linkMap.put(entry.getKey(), (String[])ArrayList.toArray());
        }

        featuredAdapter = new AlbumArtAdapter(featuredPlaylists);
        featuredAdapter.setClickListener(this);
        popularAdapter = new AlbumArtHorizAdapter(popularPlaylists);
        popularAdapter.setClickListener(this);
        GenreAdapter = new AlbumArtAdapter(GenrePlaylists);
        GenreAdapter.setClickListener(this);
        topPicksAdapter = new AlbumArtAdapter(topPicksPlaylists);
        topPicksAdapter.setClickListener(this);

        playlistMap.put("Featured", featuredPlaylists);
        playlistMap.put("popular", popularPlaylists);
        playlistMap.put("Genre", GenrePlaylists);
        playlistMap.put("TopPicks", topPicksPlaylists);

        adapterMap.put("Featured", featuredAdapter);
        adapterMap.put("popular", popularAdapter);
        adapterMap.put("Genre", GenreAdapter);
        adapterMap.put("TopPicks", topPicksAdapter);

        recyclerViewMap.put("Featured", featuredRv);
        recyclerViewMap.put("popular", popularRv);
        recyclerViewMap.put("Genre", genreRv);
        recyclerViewMap.put("TopPicks", topPicksRv);
        featuredAdapter = new AlbumArtAdapter(featuredPlaylists);
        featuredAdapter.setClickListener(this);


        for (Map.Entry<String, RecyclerView> entry : recyclerViewMap.entrySet()) {
            if (entry.getKey().equals("popular")) {
                setupRecyclerview(LinearLayoutManager.VERTICAL, entry.getValue(), entry.getKey());
                loadMore( entry.getKey(), 0);
            } else {
                setupRecyclerview(LinearLayoutManager.HORIZONTAL, entry.getValue(), entry.getKey());
                loadMore( entry.getKey(), 0);
            }

        }


    }

    void getData(String firstPath, String secondPath, List<Playlist> list, RecyclerView.Adapter adapter) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        MusicRetrofit service = retrofit.create(MusicRetrofit.class);
        Call<Playlist> call = service.getCategory(firstPath, secondPath + ".json");
        call.enqueue(new retrofit2.Callback<Playlist>() {

            @Override
            public void onResponse(Call<Playlist> call, Response<Playlist> resBookSearch) {
                if (resBookSearch.errorBody() == null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            list.add(resBookSearch.body());
                            adapter.notifyItemInserted(list.size() - 1);
                        }
                    });

                } else {
                    Log.e("retrofit res", resBookSearch.errorBody().toString());
                }
                loading = false;
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                Log.e("retrofit", t.getMessage() + "\n" + t.toString());
            }
        });

    }

    private void setupRecyclerview(int orientation, RecyclerView recyclerView, String key) {
        WrapContentLinearLayoutManager layoutManager = new WrapContentLinearLayoutManager(
                getContext(),
                orientation,
                false,
                key);

        recyclerView.setAdapter(adapterMap.get(key));
        recyclerView.setLayoutManager(layoutManager);

    }

    private void loadMore(String key, int totalItemCount) {
        for (int i = totalItemCount; i < totalItemCount + 5 && i < linkMap.get(key).length; i++) {
            String url = linkMap.get(key)[i];
            int s = url.indexOf('/');
            try {
                loading = true;
                getData(url.substring(0, s), url.substring(s + 1), playlistMap.get(key), adapterMap.get(key));
            } catch (Exception e) {
            }
        }
    }
 class WrapContentLinearLayoutManager extends LinearLayoutManager {

    String key;

    public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout, String key) {
        super(context, orientation, reverseLayout);
        this.key = key;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.e("TAG", e.toString()+"\n"+key);
        }
    }
}
    @Override
    public void onItemClick(View view, int position, Playlist result) {
        getFragmentManager().beginTransaction().replace(getId(), PlaylistItemFragment.newInstance(result))
                .addToBackStack(null)
                .commit();
    }
}
