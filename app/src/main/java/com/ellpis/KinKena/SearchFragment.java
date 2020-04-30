package com.ellpis.KinKena;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Adapters.SongAdapter;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Repository.SearchRepository;
import com.ellpis.KinKena.Retrofit.MusicRetrofit;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SearchFragment extends Fragment implements SongAdapter.ItemClickListener {
    private final String BASE_URL = "http://www.arifzefen.com/";
    SearchRepository searchRepository;
    @BindView(R.id.search_searchview)
    SearchView searchview;
    private Retrofit retrofit;
    ArrayList<Song> songList = new ArrayList<>();
    SongAdapter adapter;
    @BindView(R.id.search_rv)
    RecyclerView rv;
    @BindView(R.id.search_recent)
    TextView recentSearchTV;
    @BindView(R.id.search_clear)
    TextView clearSearchTV;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        // save the task list to preference
        searchRepository = new SearchRepository(getActivity());
        songList.addAll(searchRepository.getList());
        adapter = new SongAdapter(songList,this,null,null);
        adapter.setClickListener(this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        setupSearch();
        setUpSearchClear();
    }

    public void clearSearchPref() {
        songList.clear();
        adapter.notifyDataSetChanged();
        searchRepository.clearSearchPref();
    }


    void setupSearch() {
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!query.isEmpty()) {
                    if (retrofit == null) {
                        retrofit = new retrofit2.Retrofit.Builder()
                                .baseUrl(BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                    }
                    MusicRetrofit service = retrofit.create(MusicRetrofit.class);
                    Call<Playlist> call = service.getSearch(query);
                    Log.e("retrofit", call.toString());
                    call.enqueue(new retrofit2.Callback<Playlist>() {

                        @Override
                        public void onResponse(Call<Playlist> call, Response<Playlist> resBookSearch) {
                            Log.e("retrofit", resBookSearch.code() + "\n" + resBookSearch.raw() + resBookSearch);

                            songList.clear();
                            songList.addAll(resBookSearch.body().getSongs());
                            Log.e("retrofit", songList.size() + "");
                            adapter.notifyDataSetChanged();
                            if (adapter.getItemCount() > 0) {
                                recentSearchTV.setVisibility(View.GONE);
                                clearSearchTV.setVisibility(View.GONE);
                            } else {
                                recentSearchTV.setVisibility(View.VISIBLE);
                                clearSearchTV.setVisibility(View.VISIBLE);
                                songList.addAll(searchRepository.getList());
                            }
                            Log.e("retrofit", adapter.getItemCount() + ": in adapter");
                        }

                        @Override
                        public void onFailure(Call<Playlist> call, Throwable t) {
                            Log.e("retrofit", t.getMessage() + "\n" + t.toString());
                        }
                    });

                } else {

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //    adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    void setUpSearchClear() {
        clearSearchTV.setOnClickListener(v -> {
            clearSearchPref();
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        searchRepository.addToList(songList.get(position));
        MainActivity.playSong(position,songList, false);
    }
}
