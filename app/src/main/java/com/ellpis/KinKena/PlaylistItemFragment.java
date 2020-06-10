package com.ellpis.KinKena;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
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

import com.ellpis.KinKena.Adapters.SongAdapter;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.Objects.PlaylistBottomSheet;
import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Objects.Utility;
import com.ellpis.KinKena.Repository.PlaylistRepository;
import com.ellpis.KinKena.Repository.StorageRepository;
import com.ellpis.KinKena.Retrofit.MusicRetrofit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;


public class PlaylistItemFragment extends Fragment implements SongAdapter.ItemClickListener {

    @BindView(R.id.playlist_cover)
    ImageView cover;
    @BindView(R.id.playlist_item_overflow_menu)
    ImageView overflowMenu;
    @BindView(R.id.playlist_item_download_icon)
    ImageView downloadIcon;
    @BindView(R.id.playlist_item_edit_playlist)
    Button editPlaylist;
    @BindView(R.id.playlist_title)
    TextView title;
    @BindView(R.id.playlist_subtitle)
    TextView subtitle;
    @BindView(R.id.playlist_rv)
    RecyclerView recyclerView;
    @BindView(R.id.playlist_item_follow)
    Button followBtn;
    @BindView(R.id.playlist_item_shuffle)
    Button shuffleBtn;
    private final String BASE_URL = "http://www.arifzefen.com";
    boolean isFollowing = false;
    boolean isFromFirebase = false;
    private SongAdapter adapter;
    private boolean shuffled = false;
    //    Playlist playlist;
    String playlistID;
    ArrayList<Song> songs = new ArrayList<>();
    String currentUserID = FirebaseAuth.getInstance().getUid();
    String ownerID;
    private Retrofit retrofit;
    private Playlist playlist;
    private ListenerRegistration registration;
    private boolean completedSetUp = false;
    private boolean initializingNetworkListener = true;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    public static PlaylistItemFragment newInstance(String ownerID, String playlistId, boolean fromFirebase) {
        PlaylistItemFragment myFragment = new PlaylistItemFragment();
        Bundle args = new Bundle();
        args.putString("playlist", playlistId);
        args.putString("ownerID", ownerID);
        args.putBoolean("fromFirebase", fromFirebase);
        myFragment.setArguments(args);

        return myFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ownerID = getArguments().getString("ownerID");
        playlistID = getArguments().getString("playlist");
        isFromFirebase = getArguments().getBoolean("fromFirebase");
        return inflater.inflate(R.layout.fragment_playlist_item, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (ownerID != null) {
            getPlayList();
            registerNetworkCallbackV23();
            MusicPlayerSheet.addPlaylistObserver(this);
            SongDownloadService.addPlaylistObserver(this);
        }
    }

    private void getPlayList() {
        if (isFromFirebase) {
            if (ownerID.equals(FirebaseAuth.getInstance().getUid())) {
                final DocumentReference docRef = FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(FirebaseAuth.getInstance().getUid())
                        .collection("Playlists")
                        .document(playlistID);
                registration = docRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
                    loadPlaylist(queryDocumentSnapshots);
                });
            } else {
                PlaylistRepository.getPlaylist(ownerID, playlistID, returnedData -> {
                    loadPlaylist(returnedData);
                });
            }
        } else {
            if (getView() != null) {
                int index = ownerID.indexOf('/');
                getData(ownerID.substring(0, index), ownerID.substring(index + 1));
            }

        }
    }

    private void loadPlaylist(DocumentSnapshot returnedData) {
        if (getView() != null) {
            playlist = returnedData.toObject(Playlist.class);
            songs.clear();
            setupViews();
            if (playlist != null) {
                for (Song song : playlist.getSongs()) {
                    songs.add(song);
                    adapter.notifyItemInserted(songs.size() - 1);
                }
            }
        }
    }

    void setupViews() {

        adapter = new SongAdapter(songs, this, ownerID, playlistID);
        adapter.setClickListener(PlaylistItemFragment.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        title.setText(playlist.getTitle());
        subtitle.setText(playlist.getSubtitle());
        if (playlist.getThumbnail() != null) {
            if (playlist.isFromFirebase()) {
                Picasso.get().load("https://firebasestorage.googleapis.com" + playlist.getThumbnail()).into(cover);
            } else {
                Picasso.get().load("http://www.arifzefen.com" + playlist.getThumbnail()).into(cover);
            }
        }

        setupSubtitle();
        getIsFollowing();
        followBtn.setOnClickListener(v -> setIsFollowing());
        getView().findViewById(R.id.browse_progressBar).setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.browse_collapser).setVisibility(View.VISIBLE);
        shuffleBtn.setOnClickListener(shuffleOnClickListener());
        if (currentUserID.equals(playlist.getOwnerID())) {
            overflowMenu.setOnClickListener(overflowMenuOnClickListener());
            editPlaylist.setOnClickListener(overflowMenuOnClickListener());
        } else {
            overflowMenu.setVisibility(View.GONE);
            editPlaylist.setVisibility(View.GONE);
        }
        completedSetUp = true;
    }


    private void getIsFollowing() {
        if (playlist.getOwnerID().equals(currentUserID)) {
            followBtn.setVisibility(View.GONE);
            return;
        }
        PlaylistRepository.getAllPlaylists(currentUserID, task -> {
            for (QueryDocumentSnapshot document : task) {
                if (isFromFirebase) {
                    if (playlist.getId().equals(document.getId())) {
                        isFollowing = true;
                    }
                } else {
                    if (playlist.getOwnerID().equals(document.getString("ownerID"))) {
                        isFollowing = true;
                    }
                }

            }
            if (getView() == null) return;
            setIsFollowingDesign();

        });
    }

    public void setupSubtitle() {
        if (isFromFirebase) {

            String username = playlist.getOwnerUsername();
            String trackLength = playlist.getSongs().size() + "";
            String playlistLength = playlist.getPlaylistLength();
            subtitle.setText("By " + username + " · " + trackLength + " songs · " + playlistLength);

        } else {
            subtitle.setText("By Arifzefen · " + playlist.getSongs().size() + " songs · " + playlist.getPlaylistLength());
        }

    }

    private void setIsFollowing() {
        if (!isFollowing) {
            Playlist newPlaylist = new Playlist();
            newPlaylist.setId(playlist.getId());
            newPlaylist.setTitle(playlist.getTitle());
            newPlaylist.setOwnerID(ownerID);
            newPlaylist.setOwnerUsername(playlist.getOwnerUsername());
            newPlaylist.setFromFirebase(isFromFirebase);
            newPlaylist.setSongs(new ArrayList<>());
            newPlaylist.setThumbnail(playlist.getThumbnail());
            PlaylistRepository.followPlaylist(ownerID, newPlaylist);
        } else {
            PlaylistRepository.unFollowPlaylist(ownerID, playlist);
        }
        isFollowing = !isFollowing;
        setIsFollowingDesign();
    }

    void setIsFollowingDesign() {
        if (isFollowing) {
            followBtn.setBackground(getContext().getResources().getDrawable(R.drawable.button_unselected));
            followBtn.setText("Following");
        } else {
            followBtn.setBackground(getContext().getResources().getDrawable(R.drawable.button_selected));
            followBtn.setText("Follow");
        }
    }

    void getData(String firstPath, String secondPath) {
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
                            if (resBookSearch.body() != null) {
                                if (getView() == null) {
                                    return;
                                }
                                playlist = resBookSearch.body();
                                playlist.setOwnerID(firstPath + "/" + secondPath);
                                playlist.setFromFirebase(false);
                                setupViews();
                                for (Song song : playlist.getSongs()) {
                                    songs.add(song);
                                    adapter.notifyItemInserted(songs.size() - 1);
                                }

                            }
                        }
                    });

                } else {
                }
            }

            @Override
            public void onFailure(Call<Playlist> call, Throwable t) {
                Log.e("retrofit", t.getMessage() + "\n" + t.toString());
            }
        });

    }


    private View.OnClickListener shuffleOnClickListener() {
        return v -> {
            if (playlist != null && playlist.getSongs().size() > 0)
                MainActivity.playSong((new Random()).nextInt(playlist.getSongs().size()), (ArrayList<Song>) playlist.getSongs(), true);
        };
    }
//    private View.OnClickListener downloadOnClickListener() {
//
//    }
    private View.OnClickListener overflowMenuOnClickListener() {
        return v -> {
            View view = getLayoutInflater().inflate(R.layout.bottomsheet_playlist_menu, null);
            PlaylistBottomSheet playlistBottomSheet = new PlaylistBottomSheet(getContext(), R.style.SheetDialog, this, playlist);
            playlistBottomSheet.setContentView(view);
            playlistBottomSheet.show();
        };
    }

    @Override
    public void onSongItemClick(View view, int position) {
        MainActivity.playSong(position, songs, shuffled);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap == null) {
                        Picasso.get().load(imageReturnedIntent.getData()).into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                cover.setImageBitmap(bitmap);
                                StorageRepository.savePlaylistImageToFirebase(bitmap, playlistID, task -> {

                                    Uri downloadUri = task;
                                    try {
                                        FirebaseFirestore.getInstance().collection("Users").document(currentUserID)
                                                .collection("Playlists").document(playlistID)
                                                .update("thumbnail", (new URL(downloadUri.toString())).getPath() + "?" + downloadUri.getQuery());
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
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
                        return;
                    }
                    cover.setImageBitmap(imageBitmap);
                    StorageRepository.savePlaylistImageToFirebase(imageBitmap, playlistID, task -> {

                        Uri downloadUri = task;
                        try {
                            FirebaseFirestore.getInstance().collection("Users").document(currentUserID)
                                    .collection("Playlists").document(playlistID)
                                    .update("thumbnail", (new URL(downloadUri.toString())).getPath() + "?" + downloadUri.getQuery());
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    });

                }
                break;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (registration != null) {
            registration.remove();
        }
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    private void registerNetworkCallbackV23() {
        connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                PlaylistItemFragment.this.getActivity().runOnUiThread(() -> {
                    if (getView() != null && !initializingNetworkListener && !completedSetUp) {
                        if (ownerID != null) {
                            getPlayList();
                        }
                    }
                });

            }

            @Override
            public void onLost(Network network) {
                PlaylistItemFragment.this.getActivity().runOnUiThread(() -> {

                });

            }

        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);

        }
        initializingNetworkListener = false;
    }

    public void songChanged(){
        adapter.notifyDataSetChanged();
    }

}

