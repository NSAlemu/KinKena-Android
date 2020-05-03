package com.ellpis.KinKena;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.FragmentManager;

import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Repository.UserRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.google.android.exoplayer2.Player.REPEAT_MODE_OFF;

public class MainActivity extends AppCompatActivity {
    static int currentActive;
    static FragmentManager manager;
    static MainActivity context;
    static int repeatMode = REPEAT_MODE_OFF;
    BottomNavigationView bottomNavigationView;
    static BottomSheetBehavior sheetBehavior;
    int bottomNavigationViewMaxheight = 0;
    float temp = 0f;
    public static MusicPlayerSheet musicPlayer;
    public static String username;
    String currentUserID = FirebaseAuth.getInstance().getUid();
    static String arifzefenCookie="";
    static BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        setupBottomNavigation();
        createNotificationChannel();
        bottomNavigationView = findViewById(R.id.main_bottom_nav);
        bottomNavigationViewMaxheight = bottomNavigationView.getHeight();
        bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (findViewById(R.id.mini_player) != null) {
                    findViewById(R.id.mini_player).setAlpha(1 - slideOffset);

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int width = displayMetrics.widthPixels;

                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) findViewById(R.id.miniplayer_image_card).getLayoutParams();
                    params.horizontalBias = slideOffset / 2;
                    params.verticalBias = slideOffset / 3;
                    int minWidth = findViewById(R.id.mini_player).getHeight();
                    params.width = (int) Math.floor((width * (0.5 * slideOffset)) + minWidth);
                    findViewById(R.id.miniplayer_image_card).setLayoutParams(params);
                    ((CardView) findViewById(R.id.miniplayer_image_card)).setRadius(20 * slideOffset);


                    Guideline guideLine = findViewById(R.id.main_bottom_nav_guideline);
                    ConstraintLayout.LayoutParams guideLineParams = (ConstraintLayout.LayoutParams) guideLine.getLayoutParams();
                    DecimalFormat df = new DecimalFormat("#.##");
                    if (temp != (0.14 * (slideOffset)) && slideOffset >= 0) {
                        temp = 0.14f * (slideOffset);
                        guideLineParams.guidePercent = 0.93f + (temp / 2); // 45% // range: 0 <-> 1
                    }
                    guideLine.setLayoutParams(guideLineParams);
                }
            }
        };

        manager = getSupportFragmentManager();
        findViewById(R.id.main_fragment_search_container).setVisibility(View.GONE);
        prepareUsername();
        prepareCookie();
    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_name), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
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

    /**
     * Not yet Implemented
     *
     * @param song
     */
    public static void addToQueue(Song song) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not Yet implemented
     *
     * @param position
     */
    public static void playSongInQueue(int position) {
        throw new UnsupportedOperationException();
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
            username = task.getResult().get("username").toString();

        });
    }
    private void prepareCookie() {
        FirebaseFirestore.getInstance().collection("Keys").document("arifzefenKeys").get().addOnCompleteListener(task->{
            Log.e("TAG", "prepareCookie: "+task.getResult().getString("cookies") );
            arifzefenCookie = task.getResult().getString("cookies");
            Log.e("TAG", "prepareCookie: "+arifzefenCookie );
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
}
