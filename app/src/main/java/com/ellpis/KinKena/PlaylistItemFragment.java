package com.ellpis.KinKena;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Adapters.SongAdapter;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.Objects.PlaylistBottomSheet;
import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Retrofit.MusicRetrofit;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
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
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Retrofit retrofit;
    private Playlist playlist;

    public static PlaylistItemFragment newInstance(String playlistId, boolean fromFirebase) {
        PlaylistItemFragment myFragment = new PlaylistItemFragment();
        Bundle args = new Bundle();
        args.putString("playlist", playlistId);
        args.putBoolean("fromFirebase", fromFirebase);
        myFragment.setArguments(args);

        return myFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getArguments().getString("playlist") != null) {
            playlistID = getArguments().getString("playlist");
            isFromFirebase = getArguments().getBoolean("fromFirebase");
        }
        return inflater.inflate(R.layout.fragment_playlist_item, container, false);

    }


    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (playlistID != null) {
            getPlayList();
        }
    }


    private void getIsFollowing() {
        if (playlist.getOwnerID().equals(currentUserID)) {
            followBtn.setVisibility(View.GONE);
            return;
        }
        db.collection("Users").document(currentUserID).collection("Playlists")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (playlistID.equals(document.get("ownerID").toString())) {
                                    isFollowing = true;
                                }
                            }
                            if (isFollowing) {
                                followBtn.setBackgroundColor(getContext().getResources().getColor(R.color.selectedItem));
                            } else {
                                followBtn.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));
                            }
                        } else {

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Could not set Following. Please try agiain later.", Toast.LENGTH_LONG).show();
            }
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
            newPlaylist.setTitle(playlist.getTitle());
            newPlaylist.setOwnerID(playlistID);
            newPlaylist.setFromFirebase(isFromFirebase);
            newPlaylist.setSongs(new ArrayList<>());
            newPlaylist.setThumbnail(playlist.getThumbnail());
            String firebaseID = playlistID.substring(playlistID.indexOf('/') + 1);
            db.collection("Users").document(currentUserID).collection("Playlists").document(firebaseID).set(newPlaylist);
            followBtn.setBackgroundColor(getContext().getResources().getColor(R.color.selectedItem));
//            getPlayList(String PlaylistID);

        } else {
            String firebaseID = playlistID.substring(playlistID.indexOf('/') + 1);
            db.collection("Users").document(currentUserID).collection("Playlists").document(firebaseID).delete();
            followBtn.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));
//            deletePlayList(String PlaylistID);
        }
        isFollowing = !isFollowing;
    }

    private void getPlayList() {
        if (isFromFirebase) {
            db.collection("Users").document(currentUserID).collection("Playlists").document(playlistID)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        playlist = task.getResult().toObject(Playlist.class);
                        setupRecyclerview();
                        if (playlist != null) {
                            for (Song song : playlist.getSongs()) {
                                songs.add(song);
                                adapter.notifyItemInserted(songs.size() - 1);
                            }
                        }

                    } else {

                    }
                }
            });
        } else {
            int index = playlistID.indexOf('/');
            Log.e("retrofit res", playlistID.substring(0, index) + playlistID.substring(index + 1));
            getData(playlistID.substring(0, index), playlistID.substring(index + 1));
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
                                playlist = resBookSearch.body();
                                playlist.setOwnerID(firstPath + "/" + secondPath);
                                playlist.setFromFirebase(false);
                                setupRecyclerview();
                                for (Song song : playlist.getSongs()) {
                                    songs.add(song);
                                    adapter.notifyItemInserted(songs.size() - 1);
                                }

                            }
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


    void setupRecyclerview() {

        adapter = new SongAdapter(songs, this, playlist.getOwnerID(), playlist.getId());
        adapter.setClickListener(PlaylistItemFragment.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        title.setText(playlist.getTitle());
        subtitle.setText(playlist.getSubtitle());
        if (playlist.getThumbnail() != null ){
            if(playlist.isFromFirebase()){
                Picasso.get().load("https://firebasestorage.googleapis.com" + playlist.getThumbnail()).into(cover);
            }else{
                Picasso.get().load("http://www.arifzefen.com" + playlist.getThumbnail()).into(cover);
            }
        }

        setupSubtitle();
        getIsFollowing();
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsFollowing();
            }
        });
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

    }

    private View.OnClickListener shuffleOnClickListener() {
        return v -> {
            MainActivity.playSong((new Random()).nextInt(playlist.getSongs().size() - 2)+1, (ArrayList<Song>) playlist.getSongs(), true);
        };
    }

    private View.OnClickListener overflowMenuOnClickListener() {
        return v -> {
            View view = getLayoutInflater().inflate(R.layout.bottomsheet_playlist_menu, null);
            PlaylistBottomSheet playlistBottomSheet = new PlaylistBottomSheet(getContext(), R.style.SheetDialog, this, playlist);
            playlistBottomSheet.setContentView(view);
            playlistBottomSheet.show();
        };
    }

    @Override
    public void onItemClick(View view, int position) {
        MainActivity.playSong(position, songs, shuffled);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageBitmap=Bitmap.createBitmap(imageBitmap, 0,0,imageBitmap.getWidth(), imageBitmap.getWidth());

                    cover.setImageBitmap(imageBitmap);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    final StorageReference firebaseImageFolder = FirebaseStorage.getInstance()
                            .getReference()
                            .child(currentUserID + "/" + playlist.getId() + "/playlistCover.jpg");

                    UploadTask uploadTask  = firebaseImageFolder.putBytes(byteArray);

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return firebaseImageFolder.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                Uri downloadUri = task.getResult();
                                try {
                                    db.collection("Users").document(currentUserID)
                                            .collection("Playlists").document(playlistID)
                                            .update("thumbnail", (new URL(downloadUri.toString())).getPath()+"?"+downloadUri.getQuery());
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    });
                }
                break;

        }
    }


}

