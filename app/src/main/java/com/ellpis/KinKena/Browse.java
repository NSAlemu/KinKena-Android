package com.ellpis.KinKena;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Adapters.AlbumArtAdapter;
import com.ellpis.KinKena.Adapters.AlbumArtHorizAdapter;
import com.ellpis.KinKena.Objects.Featured;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.Objects.Utils;
import com.ellpis.KinKena.Repository.BrowseRepository;
import com.ellpis.KinKena.Retrofit.MusicRetrofit;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Browse extends Fragment implements AlbumArtAdapter.ItemClickListener, AlbumArtHorizAdapter.ItemClickListener {
    private final String BASE_URL = "http://www.arifzefen.com";
    List<Featured> allPlaylists = new ArrayList<>();

    private Retrofit retrofit;
    List<Playlist> featuredPlaylists = new ArrayList<>();
    List<Playlist> popularPlaylists = new ArrayList<>();
    List<Playlist> GenrePlaylists = new ArrayList<>();
    List<Playlist> topPicksPlaylists = new ArrayList<>();
    List<Playlist> morePlaylists = new ArrayList<>();
    AlbumArtAdapter featuredAdapter;
    AlbumArtHorizAdapter popularAdapter;
    AlbumArtAdapter GenreAdapter;
    AlbumArtAdapter topPicksAdapter;
    AlbumArtAdapter morePlaylistsAdapter;
    @BindView(R.id.browse_featured_rv)
    RecyclerView featuredRv;
    @BindView(R.id.browse_popular_rv)
    RecyclerView popularRv;
    @BindView(R.id.browse_genre_rv)
    RecyclerView genreRv;
    @BindView(R.id.browse_top_pic_rv)
    RecyclerView topPicksRv;
    @BindView(R.id.browse_more_rv)
    RecyclerView morePlaylistsRv;
    @BindView(R.id.browse_load_more_btn)
    TextView morePlaylistsBtn;
    Map<String, String[]> linkMap = new HashMap<>();
    Map<String, List<Playlist>> playlistMap = new HashMap<>();
    Map<String, RecyclerView.Adapter> adapterMap = new HashMap<>();
    Map<String, RecyclerView> recyclerViewMap = new HashMap<>();
    private int loadedPlaylists;
    private OkHttpClient client;
    boolean initializing = true;

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
        File httpCacheDirectory = new File(getContext().getCacheDir(), "responses");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        client = new OkHttpClient.Builder()
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .cache(cache)
                .build();
        getAllData();
        morePlaylistsBtn.setOnClickListener(loadMorePlaylist());
        BrowseRepository.getBrowseLinks(task -> {
            setupViews(task);
        });
        registerNetworkCallbackV23();
    }

    void setupViews(Task<QuerySnapshot> task) {
        if (getView() == null) return;
        for (DocumentSnapshot document : task.getResult().getDocuments()) {
            List<String> links = ((List<String>) document.get("links"));
            Collections.shuffle(links);
            linkMap.put(document.getString("title"), links.toArray(new String[0]));
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
                loadData(entry.getKey(), 0);
            } else {
                setupRecyclerview(LinearLayoutManager.HORIZONTAL, entry.getValue(), entry.getKey());
                loadData(entry.getKey(), 0);
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
                    .client(client)
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
                            Playlist playlist = resBookSearch.body();
                            playlist.setOwnerID(firstPath + "/" + secondPath);
                            playlist.setFromFirebase(false);
                            list.add(playlist);

                            adapter.notifyItemInserted(list.size() - 1);
                        }
                    });

                } else {
                    Log.e("retrofit res", resBookSearch.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                Log.e("retrofit", t.getMessage() + "\n" + t.toString());
            }
        });

    }

    void getAllData() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        MusicRetrofit service = retrofit.create(MusicRetrofit.class);
        Call<List<Featured>> call = service.getFeatured();
        call.enqueue(new retrofit2.Callback<List<Featured>>() {

            @Override
            public void onResponse(Call<List<Featured>> call, Response<List<Featured>> resBookSearch) {
                if (resBookSearch.errorBody() == null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            allPlaylists.addAll(resBookSearch.body());
                            morePlaylistsAdapter = new AlbumArtAdapter(morePlaylists);
                            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
                            morePlaylistsAdapter.setClickListener(Browse.this);
                            morePlaylistsBtn.performClick();

                            morePlaylistsRv.setAdapter(morePlaylistsAdapter);
                            morePlaylistsRv.setLayoutManager(gridLayoutManager);
                        }
                    });

                } else {
                    Log.e("retrofit res", resBookSearch.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<List<Featured>> call, Throwable t) {
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

    private void loadData(String key, int totalItemCount) {
        for (int i = totalItemCount; i < linkMap.get(key).length; i++) {
            String url = linkMap.get(key)[i];
            int s = url.indexOf('/');
            try {
                getData(url.substring(0, s), url.substring(s + 1), playlistMap.get(key), adapterMap.get(key));
            } catch (Exception e) {
            }
        }
    }

    private View.OnClickListener loadMorePlaylist() {
        return v -> {
            int i = loadedPlaylists;
            for (; i < loadedPlaylists + 12 && i < allPlaylists.size(); i++) {
                Featured featured = allPlaylists.get(i);
                getData("featured", featured.getId() + "", morePlaylists, morePlaylistsAdapter);
            }
            loadedPlaylists = i;
            if (loadedPlaylists >= allPlaylists.size() - 1) {
                morePlaylistsBtn.setVisibility(View.GONE);
            }
        };
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
                Log.e("TAG", e.toString() + "\n" + key);
            }
        }
    }

    @Override
    public void onItemClick(View view, int position, Playlist result) {
        Log.e("TAG", "onItemClick: " + result.toString());
        getFragmentManager().beginTransaction().replace(getId(), PlaylistItemFragment.newInstance(result.getOwnerID(), result.getId(), result.isFromFirebase()))
                .addToBackStack(null)
                .commit();
    }

    private final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = chain -> {
        okhttp3.Response originalResponse = chain.proceed(chain.request());
        if (Utils.isNetworkConnected(getContext())) {
            int maxAge = 60; // read from cache for 1 minute
            return originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .build();
        } else {
            int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
            return originalResponse.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }
    };

    private void registerNetworkCallbackV23() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            Browse.this.getActivity().runOnUiThread(() -> {
                                if (!initializing) {
                                    if (featuredPlaylists.isEmpty()) {
                                        getAllData();
                                        morePlaylistsBtn.setOnClickListener(loadMorePlaylist());
                                        BrowseRepository.getBrowseLinks(task -> {
                                            setupViews(task);
                                        });
                                    }
                                }
                                initializing = false;
                            });

                        }

                        @Override
                        public void onLost(Network network) {
                            Browse.this.getActivity().runOnUiThread(() -> {

                            });

                        }

                    }

            );

        }

    }
}
