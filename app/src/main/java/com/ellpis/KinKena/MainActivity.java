package com.ellpis.KinKena;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
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

import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Objects.Utils;
import com.ellpis.KinKena.Repository.UserRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

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
//        Log.e("TAG", "demoApplication: "+demoApplication.getDownloadManager().addDownload(););
        bottomNavigationView = findViewById(R.id.main_bottom_nav);
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
    }

    void setMiniPlayerImageTransiion(float slideOffset) {
        findViewById(R.id.mini_player).setAlpha(1 - slideOffset);
        findViewById(R.id.queue_container).setAlpha(slideOffset);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayheight = displayMetrics.heightPixels;

        int minWidth = findViewById(R.id.mini_player).getHeight();
        int maxWidth = 0;
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
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            CharSequence name2 = SongDownloadApplication.DOWNLOAD_NOTIFICATION_CHANNEL_ID;
            String description2 = SongDownloadApplication.DOWNLOAD_NOTIFICATION_CHANNEL_ID;
            int importance2 = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel2 = new NotificationChannel(SongDownloadApplication.DOWNLOAD_NOTIFICATION_CHANNEL_ID, name2, importance2);
            channel.setDescription(description2);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager2 = getSystemService(NotificationManager.class);
            notificationManager2.createNotificationChannel(channel2);
        }
    }

    public static void playSong(int playbackPosition, ArrayList<Song> playlist, boolean shuffled) {
        if (playlist == null || playbackPosition >= playlist.size() || playlist.size() == 0) {
            return;
        }
        context.findViewById(R.id.main_player_sheet_container).setVisibility(View.VISIBLE);
        sheetBehavior = BottomSheetBehavior.from(context.findViewById(R.id.constraintLayout));

        sheetBehavior.addBottomSheetCallback(bottomSheetCallback);
        musicPlayer = MusicPlayerSheet.newInstance(playbackPosition, playlist, shuffled);
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
            username = task.get("username").toString();

        });
    }

    private void prepareCookie() {
        FirebaseFirestore.getInstance().collection("Keys").document("arifzefenKeys").get().addOnSuccessListener(task -> {
            arifzefenCookie = task.getString("cookies");
            Log.e("TAG", "prepareCookie: " + arifzefenCookie);
        });
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
            new MaterialAlertDialogBuilder(context)
                    .setTitle("You are about to leave the App")
                    .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            System.exit(0);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setBackground(getResources().getDrawable(R.drawable.dialog_backgound))
                    .show();
        } else {
            super.onBackPressed();
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
                                findViewById(R.id.main_notification_bar).setVisibility(View.GONE);
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

}


