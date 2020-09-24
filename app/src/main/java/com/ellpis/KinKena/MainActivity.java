package com.ellpis.KinKena;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.FragmentManager;

import com.ellpis.KinKena.Objects.BrowseItems;
import com.ellpis.KinKena.Objects.MiniPlaylist;
import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Objects.Utils;
import com.ellpis.KinKena.Repository.BrowseRepository;
import com.ellpis.KinKena.Repository.UserRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.exoplayer2.Player.REPEAT_MODE_OFF;

public class MainActivity extends AppCompatActivity {
    static int currentActive;
    static FragmentManager manager;
    public static MainActivity context;
    static int repeatMode = REPEAT_MODE_OFF;
    BottomNavigationView bottomNavigationView;
    static BottomSheetBehavior sheetBehavior;
    int guideLineMaxheight = 0;
    float temp = 0f;
    public static MusicPlayerSheet musicPlayer;
    public static String username;
    String currentUserID = FirebaseAuth.getInstance().getUid();
    static String arifzefenCookie = "";
    static BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;
    private Guideline guideLine;
    public static SongDownloadApplication songDownloadApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        setupBottomNavigation();
        createNotificationChannel();
        songDownloadApplication = new SongDownloadApplication(getApplicationContext());
        songDownloadApplication.getDownloadManager().resumeDownloads();
//        Log.e("TAG", "demoApplication: "+demoApplication.getDownloadManager().addDownload(););
        bottomNavigationView = findViewById(R.id.main_bottom_nav);
        bottomNavigationView.setItemIconTintList(null);
        guideLine = findViewById(R.id.main_bottom_nav_guideline);
        ConstraintLayout.LayoutParams guideLineParams = (ConstraintLayout.LayoutParams) guideLine.getLayoutParams();
        guideLineMaxheight = guideLineParams.guideEnd;
        bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) {
                    findViewById(R.id.queue_container).setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (findViewById(R.id.mini_player) != null) {

                    setMiniPlayerImageTransiion(slideOffset);
                    ConstraintLayout.LayoutParams guideLineParams = (ConstraintLayout.LayoutParams) guideLine.getLayoutParams();

                    guideLineParams.guideEnd = (int) Math.floor(guideLineMaxheight * (1 - slideOffset)); // 45% // range: 0 <-> 1
                    guideLine.setLayoutParams(guideLineParams);
                }
            }
        };

        manager = getSupportFragmentManager();
        registerNetworkCallbackV23();
        findViewById(R.id.main_fragment_search_container).setVisibility(View.GONE);
        prepareUsername();
        prepareCookie();
        playSong(0,new ArrayList<>(),false);
    }

    void setMiniPlayerImageTransiion(float slideOffset) {
        findViewById(R.id.mini_player).setAlpha(1 - slideOffset);
        findViewById(R.id.queue_container).setAlpha(slideOffset);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayheight = displayMetrics.heightPixels;

        int minWidth = findViewById(R.id.mini_player).getHeight();
        int maxWidth;
        if (findViewById(R.id.miniplsyer_image_end_params).getWidth() > findViewById(R.id.miniplsyer_image_end_params).getHeight())
            maxWidth = findViewById(R.id.miniplsyer_image_end_params).getHeight();
        else
            maxWidth = findViewById(R.id.miniplsyer_image_end_params).getWidth();

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) findViewById(R.id.miniplayer_image_card).getLayoutParams();
        params.horizontalBias = slideOffset / 2;
        params.verticalBias = slideOffset / 2;

        params.width = (int) Math.floor((maxWidth * (0.9 * slideOffset)) + minWidth);
        params.height = params.width;
        params.topMargin = (int) ((displayheight / 15) * slideOffset);
        findViewById(R.id.miniplayer_image_card).setLayoutParams(params);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.player_channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(getString(R.string.player_channel_name), name, importance);
            channel.setDescription(description);
            channel.setShowBadge(false);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            CharSequence name2 = SongDownloadApplication.DOWNLOAD_NOTIFICATION_CHANNEL_ID;
            String description2 = SongDownloadApplication.DOWNLOAD_NOTIFICATION_CHANNEL_ID;
            int importance2 = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel2 = new NotificationChannel(SongDownloadApplication.DOWNLOAD_NOTIFICATION_CHANNEL_ID, name2, importance2);
            channel2.setDescription(description2);
            channel2.setShowBadge(false);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager2 = getSystemService(NotificationManager.class);
            notificationManager2.createNotificationChannel(channel2);
        }
    }

    public static void playSong(int playbackPosition, ArrayList<Song> playlist, Boolean shuffled) {
        if (playlist == null || playbackPosition >= playlist.size() || playlist.size() == 0) {
            return;
        }
        context.findViewById(R.id.main_player_sheet_container).setVisibility(View.VISIBLE);
        sheetBehavior = BottomSheetBehavior.from(context.findViewById(R.id.constraintLayout));

        sheetBehavior.addBottomSheetCallback(bottomSheetCallback);
        musicPlayer = shuffled == null ?  MusicPlayerSheet.newInstance(playbackPosition, playlist) :
                MusicPlayerSheet.newInstance(playbackPosition, playlist, shuffled);
        manager.beginTransaction().replace(R.id.main_fragment_music_player_container, musicPlayer).commit();
    }

    public static void addToQueue(Song song) {
        if(musicPlayer==null){
            ArrayList<Song> playlist = new ArrayList<>();
            playlist.add(song);
            playSong(0, playlist, false);
        }
       musicPlayer.addSongToQueue(song);
    }

    public void toggleBottomSheet(View view) {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.main_bottom_nav);
        currentActive = R.id.action_menu_home;
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            setVisibility();
            if (currentActive == menuItem.getItemId()) {
                getSupportFragmentManager().popBackStackImmediate();
            }
            currentActive = menuItem.getItemId();
            switch (menuItem.getItemId()) {
                case R.id.action_menu_home:
                    findViewById(R.id.main_fragment_browse_container).setVisibility(View.VISIBLE);
                    break;
                case R.id.action_menu_search:
                    findViewById(R.id.main_fragment_search_container).setVisibility(View.VISIBLE);

                    break;
                case R.id.action_menu_playlist:
                    findViewById(R.id.main_fragment_playlist_container).setVisibility(View.VISIBLE);

                    break;
            }
            return true;
        });
    }

    private void prepareUsername() {
        currentUserID = FirebaseAuth.getInstance().getUid();
        UserRepository.getUser(currentUserID, task -> {
            if(task.get("username")==null){
                username = "test";
            }
            else
            username = task.get("username").toString();

        });
    }

    private void prepareCookie() {
            String ga = "_ga=GA1.2.";
            ga+= (int)(Math.random()*Math.pow(10,10)) ;
            ga+= "." + System.currentTimeMillis()/1000;
            ga+= "; _gid=GA1.2.";
            ga+= (int)(Math.random()*Math.pow(10,10)) ;
            ga+= "." + System.currentTimeMillis()/1000;

            arifzefenCookie = ga;
    }

    public void setVisibility() {
        findViewById(R.id.main_fragment_browse_container).setVisibility(View.GONE);
        findViewById(R.id.main_fragment_search_container).setVisibility(View.GONE);
        findViewById(R.id.main_fragment_playlist_container).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {

        if (sheetBehavior != null && sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        } else {
            getSupportFragmentManager().popBackStack();
        }


    }

    private void registerNetworkCallbackV23() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        if (!Utils.isNetworkConnected(this)) {
            ((TextView) findViewById(R.id.main_notification_bar)).setText("No Internet Connection");
            findViewById(R.id.main_notification_bar).setVisibility(View.VISIBLE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            MainActivity.this.runOnUiThread(() -> {
                                if(MusicPlayerSheet.queue!=null && MusicPlayerSheet.queue.size()>0){
//                                    ForegroundService.player.retry();
                                }
                                findViewById(R.id.main_notification_bar).setVisibility(View.GONE);
                                songDownloadApplication.getDownloadManager().resumeDownloads();
                            });

                        }

                        @Override
                        public void onLost(Network network) {
                            MainActivity.this.runOnUiThread(() -> {
                                ((TextView) findViewById(R.id.main_notification_bar)).setText("No Internet Connection");
                                findViewById(R.id.main_notification_bar).setVisibility(View.VISIBLE);
                            });

                        }
                    }

            );
        }
    }


    private void runUpdate(){
        new Thread(new Runnable() {
            public void run() {
                try {
                    int i=0;
                    List<BrowseItems> browseItemsList = new ArrayList<>();
                    for (Map.Entry<String, List<String>> entry : list().entrySet()){
                        List<MiniPlaylist> mps = new ArrayList<>();
                        System.out.println("---------------------------  "+i+"  ------------------------------");
                        for(String str:entry.getValue()){
                            Connection.Response res = Jsoup.connect(str)
                                    .referrer("http://www.arifzefen.com/")
                                    .ignoreContentType(true)
                                    .execute();
                            Gson gson = new Gson();
                            Type typeOfT = new TypeToken<MiniPlaylist>() {
                            }.getType();
                            MiniPlaylist mp;
                            try {
                                mp = gson.fromJson(res.body(), typeOfT);
                            }catch(JsonSyntaxException e){
                                continue;
                            }
                            mp.setLink(str);
                            mps.add(mp);

                        }
                        BrowseItems browseItems = new BrowseItems();
                        browseItems.setTitle(entry.getKey());
                        browseItems.setPlaylists(mps);
                        if(entry.getKey().equals("Top Songs")){
                            browseItems.setType(1);
                        }else{
                            browseItems.setType(0);
                        }
                        if(browseItems.getTitle().equals("Featured Playlists")) {
                            browseItems.setPosition(0);
                        }else if(browseItems.getTitle().equals("Top Songs")) {
                            browseItems.setPosition(1);
                        }
                        else if(browseItems.getTitle().equals("Genre")) {
                            browseItems.setPosition(2);
                        }
                        else if(browseItems.getTitle().equals("More Playlists")) {
                            browseItems.setPosition(3);
                        }
                        browseItemsList.add(browseItems);
                        Log.e("browse item",browseItems.getPosition() +"  "+ browseItems.getType()+"  "+browseItems.getTitle());
                    }
                    BrowseRepository.createBrowseLinks(browseItemsList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    private static  Map<String,List<String>> list() {
        Map<String,List<String>> map = new HashMap<>();
        List<String> featured = new ArrayList<>();
        featured.add("http://www.arifzefen.com/json/curated/dancenchifera.json");
        featured.add("http://www.arifzefen.com/json/curated/ethiohip-hop.json");
        featured.add("http://www.arifzefen.com/json/curated/ethiojazz.json");
        featured.add("http://www.arifzefen.com/json/curated/oldies.json");
        featured.add("http://www.arifzefen.com/json/curated/reggaefusion.json");
        featured.add("http://www.arifzefen.com/json/curated/slowjamz.json");
        featured.add("http://www.arifzefen.com/json/curated/tizita.json");
        featured.add("http://www.arifzefen.com/json/curated/traditional.json");
        featured.add("http://www.arifzefen.com/json/curated/weddingsongs.json");
        featured.add("http://www.arifzefen.com/json/curated/workout.json");
        map.put("Featured Playlists", featured);
        List<String> genre = new ArrayList<>();
        genre.add("http://www.arifzefen.com/json/list/orthodoxmezmur.json");
        genre.add("http://www.arifzefen.com/json/list/protestantmezmur.json");
        genre.add("http://www.arifzefen.com/json/list/menzuma.json");
        genre.add("http://www.arifzefen.com/json/list/drama.json");
        genre.add("http://www.arifzefen.com/json/list/audiobooks.json");
        genre.add("http://www.arifzefen.com/json/list/instrumentals.json");
        genre.add("http://www.arifzefen.com/json/list/other.json");
        genre.add("http://www.arifzefen.com/json/list/azmarisounds.json");
        genre.add("http://www.arifzefen.com/json/list/kids.json");
        genre.add("http://www.arifzefen.com/json/list/amahric.json");
        genre.add("http://www.arifzefen.com/json/list/english.json");
        genre.add("http://www.arifzefen.com/json/list/guragegna.json");
        genre.add("http://www.arifzefen.com/json/list/guragigna.json");
        genre.add("http://www.arifzefen.com/json/list/haderegna.json");
        genre.add("http://www.arifzefen.com/json/list/harari.json");
        genre.add("http://www.arifzefen.com/json/list/instrumental.json");
        genre.add("http://www.arifzefen.com/json/list/oromiffa.json");
        genre.add("http://www.arifzefen.com/json/list/oromigna.json");
        genre.add("http://www.arifzefen.com/json/list/sudanese.json");
        genre.add("http://www.arifzefen.com/json/list/wolita.json");
        map.put("Genre", genre);
        List<String> all = new ArrayList<>();
        all.add("http://www.arifzefen.com/json/featured/735950.json");
        all.add("http://www.arifzefen.com/json/featured/735949.json");
        all.add("http://www.arifzefen.com/json/featured/737233.json");
        all.add("http://www.arifzefen.com/json/featured/736129.json");
        all.add("http://www.arifzefen.com/json/featured/714254.json");
        all.add("http://www.arifzefen.com/json/featured/736247.json");
        all.add("http://www.arifzefen.com/json/featured/733398.json");
        all.add("http://www.arifzefen.com/json/featured/732416.json");
        all.add("http://www.arifzefen.com/json/featured/729641.json");
        all.add("http://www.arifzefen.com/json/featured/729639.json");
        all.add("http://www.arifzefen.com/json/featured/729816.json");
        all.add("http://www.arifzefen.com/json/featured/728561.json");
        all.add("http://www.arifzefen.com/json/featured/735254.json");
        all.add("http://www.arifzefen.com/json/featured/725360.json");
        all.add("http://www.arifzefen.com/json/featured/725038.json");
        all.add("http://www.arifzefen.com/json/featured/724855.json");
        all.add("http://www.arifzefen.com/json/featured/723076.json");
        all.add("http://www.arifzefen.com/json/featured/717787.json");
        all.add("http://www.arifzefen.com/json/featured/717961.json");
        all.add("http://www.arifzefen.com/json/featured/717336.json");
        all.add("http://www.arifzefen.com/json/featured/717777.json");
        all.add("http://www.arifzefen.com/json/featured/721811.json");
        all.add("http://www.arifzefen.com/json/featured/730236.json");
        all.add("http://www.arifzefen.com/json/featured/721626.json");
        all.add("http://www.arifzefen.com/json/featured/718031.json");
        all.add("http://www.arifzefen.com/json/featured/717253.json");
        all.add("http://www.arifzefen.com/json/featured/716889.json");
        all.add("http://www.arifzefen.com/json/featured/715913.json");
        all.add("http://www.arifzefen.com/json/featured/715577.json");
        all.add("http://www.arifzefen.com/json/featured/709094.json");
        all.add("http://www.arifzefen.com/json/featured/709095.json");
        all.add("http://www.arifzefen.com/json/featured/708750.json");
        all.add("http://www.arifzefen.com/json/featured/702086.json");
        all.add("http://www.arifzefen.com/json/featured/706177.json");
        all.add("http://www.arifzefen.com/json/featured/701954.json");
        all.add("http://www.arifzefen.com/json/featured/702093.json");
        all.add("http://www.arifzefen.com/json/featured/702090.json");
        all.add("http://www.arifzefen.com/json/featured/699681.json");
        all.add("http://www.arifzefen.com/json/featured/694319.json");
        all.add("http://www.arifzefen.com/json/featured/710405.json");
        all.add("http://www.arifzefen.com/json/featured/695210.json");
        all.add("http://www.arifzefen.com/json/featured/689762.json");
        all.add("http://www.arifzefen.com/json/featured/689169.json");
        all.add("http://www.arifzefen.com/json/featured/687675.json");
        all.add("http://www.arifzefen.com/json/featured/699779.json");
        all.add("http://www.arifzefen.com/json/featured/679096.json");
        all.add("http://www.arifzefen.com/json/featured/692262.json");
        all.add("http://www.arifzefen.com/json/featured/683663.json");
        all.add("http://www.arifzefen.com/json/featured/683669.json");
        all.add("http://www.arifzefen.com/json/featured/683662.json");
        all.add("http://www.arifzefen.com/json/featured/682208.json");
        all.add("http://www.arifzefen.com/json/featured/677651.json");
        all.add("http://www.arifzefen.com/json/featured/677652.json");
        all.add("http://www.arifzefen.com/json/featured/679273.json");
        all.add("http://www.arifzefen.com/json/featured/679274.json");
        all.add("http://www.arifzefen.com/json/featured/678123.json");
        all.add("http://www.arifzefen.com/json/featured/671552.json");
        all.add("http://www.arifzefen.com/json/featured/610494.json");
        map.put("More Playlists", all);
        List<String> popular = new ArrayList<>();
        popular.add("http://www.arifzefen.com/json/list/mostrecent.json");
        popular.add("http://www.arifzefen.com/json/list/mostliked.json");
        popular.add("http://www.arifzefen.com/json/list/mostplayed.json");
        map.put("Top Songs", popular);
        return map;
    }

    @Override
    protected void onDestroy() {
        songDownloadApplication.downloadCache.release();
        super.onDestroy();
    }
}


