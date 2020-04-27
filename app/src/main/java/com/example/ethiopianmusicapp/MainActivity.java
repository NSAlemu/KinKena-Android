package com.example.ethiopianmusicapp;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.ethiopianmusicapp.Objects.Song;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.DecimalFormat;

import static com.google.android.exoplayer2.Player.REPEAT_MODE_OFF;

public class MainActivity extends AppCompatActivity {

    static Fragment currentActive;
    static FragmentManager manager;
    static MainActivity context;
    static int repeatMode = REPEAT_MODE_OFF;
    BottomNavigationView bottomNavigationView;
    static BottomSheetBehavior sheetBehavior;
    int bottomNavigationViewMaxheight=0;
    float temp=0f;
     static BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        setupBottomNavigation();

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

                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) findViewById(R.id.miniplayer_image).getLayoutParams();
                    params.horizontalBias = slideOffset / 2;
                    params.verticalBias = slideOffset / 3;
                    int minWidth = findViewById(R.id.mini_player).getHeight();
                    params.width = (int) Math.floor((width * (0.5 * slideOffset)) + minWidth);
                    findViewById(R.id.miniplayer_image).setLayoutParams(params);

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
    }

    static void playSong(Song song) {
        context.findViewById(R.id.main_player_sheet_container).setVisibility(View.VISIBLE);
        sheetBehavior = BottomSheetBehavior.from(context.findViewById(R.id.constraintLayout));

        sheetBehavior.addBottomSheetCallback(bottomSheetCallback);

        manager.beginTransaction().replace(R.id.main_fragment_profile_container, MusicPlayerSheet.newInstance(song)).commit();
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

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            setVisibility();
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

    public void setVisibility() {
        findViewById(R.id.main_fragment_browse_container).setVisibility(View.GONE);
        findViewById(R.id.main_fragment_search_container).setVisibility(View.GONE);
        findViewById(R.id.main_fragment_playlist_container).setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {

        if (sheetBehavior!=null && sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();

        }

    }
}
