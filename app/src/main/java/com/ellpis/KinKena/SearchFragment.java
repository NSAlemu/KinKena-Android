package com.ellpis.KinKena;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.ellpis.KinKena.Adapters.ArtistListAdapter;
import com.ellpis.KinKena.Adapters.PlaylistTabAdapter;
import com.ellpis.KinKena.Adapters.ProfileAdapter;
import com.ellpis.KinKena.Adapters.SongAdapter;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.Objects.Profile;
import com.ellpis.KinKena.Objects.SearchArtist;
import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Repository.SearchRepository;
import com.ellpis.KinKena.Retrofit.MusicRetrofit;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SearchFragment extends Fragment implements SongAdapter.ItemClickListener, ArtistListAdapter.ItemClickListener, PlaylistTabAdapter.ItemClickListener, ProfileAdapter.ItemClickListener {
    private final String BASE_URL = "http://www.arifzefen.com/";
    SearchRepository searchRepository;
    Client client;

    private Retrofit retrofit;
    ArrayList<Song> songList = new ArrayList<>();
    ArrayList<SearchArtist> artistList = new ArrayList<>();
    ArrayList<Playlist> playlists = new ArrayList<>();
    ArrayList<Profile> profilesList = new ArrayList<>();
    SongAdapter songAdapter;
    ArtistListAdapter artistListAdapter;
    PlaylistTabAdapter playlistAdapter;
    ProfileAdapter profileAdapter;
    @BindView(R.id.search_songs_rv)
    RecyclerView songsrv;
    @BindView(R.id.search_artists_rv)
    RecyclerView artistsrv;
    @BindView(R.id.search_playlist_rv)
    RecyclerView playlistsrv;
    @BindView(R.id.search_profiles_rv)
    RecyclerView profilesrv;
    @BindView(R.id.search_recent)
    TextView recentSearchTV;
    @BindView(R.id.search_clear)
    TextView clearSearchTV;
    @BindView(R.id.search_radioGroup)
    RadioGroup searchRadioGroup;
    @BindView(R.id.search_searchview)
    SearchView searchview;
    int visibleRVId=R.id.search_songs_rv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this
        client = new Client(getString(R.string.algolia_application_id), getString(R.string.algolia_api_key));
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        // save the task list to preference
        searchRepository = new SearchRepository(getActivity());
        songList.addAll(searchRepository.getList());
        songAdapter = new SongAdapter(songList, this, null, null);
        songAdapter.setClickListener(this);
        songsrv.setAdapter(songAdapter);
        songsrv.setLayoutManager(new LinearLayoutManager(getContext()));

        artistListAdapter = new ArtistListAdapter(artistList, this, null, null);
        artistListAdapter.setClickListener(this);
        artistsrv.setAdapter(artistListAdapter);
        artistsrv.setLayoutManager(new LinearLayoutManager(getContext()));

        playlistAdapter = new PlaylistTabAdapter(playlists);
        playlistAdapter.setClickListener(this);
        playlistsrv.setAdapter(playlistAdapter);
        playlistsrv.setLayoutManager(new LinearLayoutManager(getContext()));

        profileAdapter = new ProfileAdapter(profilesList, this, null, null);
        profileAdapter.setClickListener(this);
        profilesrv.setAdapter(profileAdapter);
        profilesrv.setLayoutManager(new LinearLayoutManager(getContext()));

        setupSearch();
        setUpSearchClear();
        searchRadioGroup.setOnCheckedChangeListener(onRadioButtonClicked());
    }

    public void clearSearchPref() {
        songList.clear();
        songAdapter.notifyDataSetChanged();
        searchRepository.clearSearchPref();
    }


    void setupSearch() {
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchview, R.drawable.cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
        }
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!query.isEmpty()) {
                    searchSongs(query);
                    searchArtists(query);
                    searchPlaylists(query);
                    searchProfiles(query);
                } else {

                }
                searchview.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //    adapter.getFilter().filter(newText);
                return false;
            }
        });
    }


    private void searchSongs(String query){
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        MusicRetrofit service = retrofit.create(MusicRetrofit.class);
        Call<Playlist> call = service.getSearch(query);
        call.enqueue(new retrofit2.Callback<Playlist>() {

            @Override
            public void onResponse(Call<Playlist> call, Response<Playlist> resBookSearch) {
                songList.clear();
                songList.addAll(resBookSearch.body().getSongs());
                songAdapter.notifyDataSetChanged();
                    recentSearchTV.setVisibility(View.GONE);
                    clearSearchTV.setVisibility(View.GONE);
                    searchRadioGroup.setVisibility(View.VISIBLE);
                    songList.addAll(searchRepository.getList());

            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                Log.e("retrofit", t.getMessage() + "\n" + t.toString());
            }
        });

    }

    private void searchArtists(String query){
        Index index = client.getIndex("Artists");
        Query algoliaQuery = new Query(query)
                .setHitsPerPage(20);
        index.searchAsync(algoliaQuery, new CompletionHandler() {
            @Override
            public void requestCompleted(JSONObject content, AlgoliaException error) {
                try {
                    artistList.clear();
                    for(int i=0; i<content.getJSONArray("hits").length();i++){
                        SearchArtist artist = new Gson().fromJson(content.getJSONArray("hits").get(i).toString(), SearchArtist.class);
                        artistList.add(artist);
                    }
                    artistListAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void searchPlaylists(String query){
        Index index = client.getIndex("Playlists");
        Query algoliaQuery = new Query(query)
                .setHitsPerPage(20);
        index.searchAsync(algoliaQuery, (content, error) -> {
            try {
                playlists.clear();
                for(int i=0; i<content.getJSONArray("hits").length();i++){
                    Playlist playlist = new Gson().fromJson(content.getJSONArray("hits").get(i).toString(), Playlist.class);
                    playlist.setId(content.getJSONArray("hits").getJSONObject(i).getString("objectID"));
                    if(!playlist.isPrivacy()){
                        playlists.add(playlist);
                    }

                }
                playlistAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
    private void searchProfiles(String query){
        Index index = client.getIndex("Users");
        Query algoliaQuery = new Query(query)
                .setHitsPerPage(50);
        index.searchAsync(algoliaQuery, (content, error) -> {
            try {
                profilesList.clear();
                for(int i=0; i<content.getJSONArray("hits").length();i++){
                    Profile profile = new Gson().fromJson(content.getJSONArray("hits").get(i).toString(), Profile.class);
                    profile.setId(content.getJSONArray("hits").getJSONObject(i).getString("objectID"));
                    profilesList.add(profile);
                }
                profileAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    void setUpSearchClear() {
        clearSearchTV.setOnClickListener(v -> {
            clearSearchPref();
        });
    }

    @Override
    public void onSongItemClick(View view, int position) {
        this.onPause();
        searchview.clearFocus();
        searchRepository.addToList(songList.get(position));
        MainActivity.playSong(position, songList, null);
    }


    public RadioGroup.OnCheckedChangeListener onRadioButtonClicked() {
        // Is the button now checked?
        return (group, checkedId) -> {

            setSelectedRadioColor(checkedId);
            if(recentSearchTV.getVisibility()==View.VISIBLE){
                return;
            }
            hideRecyclerviews();
            // Check which radio button was clicked
            switch (checkedId) {
                case R.id.search_toggleButton_Songs:
                        // Pirates are the best
                    songsrv.setVisibility(View.VISIBLE);
                    visibleRVId= R.id.search_songs_rv;
                        break;
                case R.id.search_toggleButton_Artists:
                        // Ninjas rule
                    artistsrv.setVisibility(View.VISIBLE);
                    visibleRVId= R.id.search_artists_rv;
                        break;
                case R.id.search_toggleButton_playlists:
                        // Pirates are the best
                    playlistsrv.setVisibility(View.VISIBLE);
                    visibleRVId= R.id.search_playlist_rv;
                        break;
                case R.id.search_toggleButton_profiles:
                        // Ninjas rule
                    profilesrv.setVisibility(View.VISIBLE);
                    visibleRVId = R.id.search_profiles_rv;
                        break;
            }

        };
    }

    private void setSelectedRadioColor(int id) {
        for (int i = 0; i < searchRadioGroup.getChildCount(); i++) {
            searchRadioGroup.getChildAt(i).setBackground(getContext().getResources().getDrawable(R.drawable.button_unselected));
        }
        getView().findViewById(id).setBackground(getContext().getResources().getDrawable(R.drawable.button_selected));
    }
    private void hideRecyclerviews(){
         songsrv.setVisibility(View.GONE);
         artistsrv.setVisibility(View.GONE);
         playlistsrv.setVisibility(View.GONE);
         profilesrv.setVisibility(View.GONE);
    }

    @Override
    public void onArtistItemClick(View view, int position) {
        searchview.clearFocus();
        getFragmentManager().beginTransaction()
                .replace(getId(), ArtistItemFragment.newInstance(artistList.get(position).getId()+""))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPlaylistItemClick(View view, int position) {
        searchview.clearFocus();
        getFragmentManager().beginTransaction()
                .replace(getId(), PlaylistItemFragment.newInstance(playlists.get(position).getOwnerID(),playlists.get(position).getId(), true))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        this.onResume();
    }

    @Override
    public void onProfileItemClick(View view, int position) {
        searchview.clearFocus();
        getFragmentManager().beginTransaction().replace(getId(), ProfileFragment.newInstance(profilesList.get(position).getId()))
                .addToBackStack(null)
                .commit();
    }
}
