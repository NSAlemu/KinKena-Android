package com.example.ethiopianmusicapp;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.example.ethiopianmusicapp.Objects.Song;
import com.example.ethiopianmusicapp.Objects.Utility;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.android.exoplayer2.Player.REPEAT_MODE_ALL;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_OFF;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_ONE;
import static com.google.android.exoplayer2.Player.STATE_READY;


public class MusicPlayerSheet extends Fragment implements TimeBar.OnScrubListener {

    private static String RADIO_DATASET_CHANGED = "RADIO_DATASET_CHANGED";

    private String audioURL = "";
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    Handler mHandler;
    Runnable runnable;
    Song song;

    @BindView(R.id.miniplayer_title)
    TextView miniTitle;
    @BindView(R.id.miniplayer_artist)
    TextView miniArtist;
    @BindView(R.id.miniplayer_image)
    ImageView cover;
    @BindView(R.id.mini_player_play_pause)
    ImageButton miniPlaypause;
    @BindView(R.id.exoplayer_next)
    ImageButton nextControl;
    @BindView(R.id.exoplayer_playpause)
    ImageButton playpauseControl;
    @BindView(R.id.exoplayer_previous)
    ImageButton previousControl;
    @BindView(R.id.exoplayer_repeat)
    ImageButton repeatControl;
    @BindView(R.id.exoplayer_shuffle)
    ImageButton shuffleControl;
    @BindView(R.id.exoplayer_title)
    TextView titleControl;
    @BindView(R.id.exoplayer_artist)
    TextView artistControl;
    @BindView(R.id.exoplayer_progress)
    DefaultTimeBar exoSeekBar;
    @BindView(R.id.exoplayer_cur_time)
    Chronometer curTimeChronometer;
    @BindView(R.id.exoplayer_duration)
    Chronometer durationChronometer;
    @BindView(R.id.music_player)
    ConstraintLayout playerBackground;
    @BindView(R.id.mini_player)
    LinearLayout miniPlayerBackground;
    private SimpleExoPlayer player;

    public static MusicPlayerSheet newInstance(Song song) {
        MusicPlayerSheet myFragment = new MusicPlayerSheet();
        RADIO_DATASET_CHANGED = RADIO_DATASET_CHANGED + song;
        Bundle args = new Bundle();
        args.putSerializable("song", song);

        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        song = (Song) getArguments().getSerializable("song");
        audioURL = "http://www.arifzefen.com/json/playSong.php?id=" + song.getSongId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music_player_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        miniTitle.setText(song.getSongName());
        miniArtist.setText(song.getArtistName());
        titleControl.setText(song.getSongName());
        artistControl.setText(song.getArtistName());
        Picasso.get().load("http://www.arifzefen.com" + song.getThumbnail()).into(cover);
        exoSeekBar.addListener(this);


    }

    private MediaSource buildMediaSource(Uri uri) {


        return new ProgressiveMediaSource.Factory(
                () -> {
                    HttpDataSource dataSource =
                            new DefaultHttpDataSource("exoplayer-codelab");
                    // Set a custom authentication request header.
                    dataSource.setRequestProperty("Cookies", "_ga=GA1.2.1315506873.1587686720; _gid=GA1.2.1614071904.1587686720");
                    dataSource.setRequestProperty("Referer", "http://www.arifzefen.com/");

                    return dataSource;
                })
                .createMediaSource(uri);
    }

    private void initializePlayer() {
        player = new SimpleExoPlayer.Builder(getContext()).build();
        Uri uri = Uri.parse(audioURL);
        MediaSource mediaSource = buildMediaSource(uri);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare(mediaSource, false, false);
        setupSeek();
        playpauseControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
        miniPlaypause.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
        player.getCurrentPosition();
        setupControls();
        Picasso.get().load("http://www.arifzefen.com" + song.getThumbnail()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    public void onGenerated(Palette p) {
                        playerBackground.setBackgroundColor(Utility.manipulateColor(p.getDominantColor(getResources().getColor(R.color.on_top_background)),0.6f));
                        miniPlayerBackground.setBackgroundColor(Utility.manipulateColor(p.getDominantColor(getResources().getColor(R.color.on_top_background)),0.6f));
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

        setRepeatMode(MainActivity.repeatMode);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //enterImmersiveMode();
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

    @Override
    public void onScrubStart(TimeBar timeBar, long position) {
        curTimeChronometer.setBase(SystemClock.elapsedRealtime() - position);
        player.setPlayWhenReady(false);
    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {
        curTimeChronometer.setBase(SystemClock.elapsedRealtime() - position);
        player.setPlayWhenReady(false);
    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        if (!canceled) {
            player.seekTo(position);
        }
        player.setPlayWhenReady(true);

    }

    private void setupSeek() {
        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == STATE_READY) {
                    exoSeekBar.setDuration(player.getDuration());
                    durationChronometer.setBase(SystemClock.elapsedRealtime() - player.getDuration());
                    exoSeekBar.setPosition(player.getCurrentPosition());
                    if (runnable == null) {
                        mHandler = new Handler();
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (player != null) {
                                    curTimeChronometer.setBase(SystemClock.elapsedRealtime() - player.getCurrentPosition());
                                    exoSeekBar.setPosition(player.getCurrentPosition());
                                }
                                mHandler.postDelayed(this, 1000);
                            }
                        };

                        getActivity().runOnUiThread(runnable);
                    }
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
            }

        });
    }

    private void setupControls() {
        playpauseControl.setOnClickListener(v -> {
            if (player.isPlaying()) {
                playpauseControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_play));
                miniPlaypause.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_play));
                player.setPlayWhenReady(false);
            } else {
                playpauseControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
                miniPlaypause.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
                player.setPlayWhenReady(true);
            }
        });
        miniPlaypause.setOnClickListener(v -> {
            if (player.isPlaying()) {
                playpauseControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_play));
                miniPlaypause.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_play));
                player.setPlayWhenReady(false);
            } else {
                playpauseControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
                miniPlaypause.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
                player.setPlayWhenReady(true);
            }
        });
        nextControl.setOnClickListener(v -> {
            player.next();
        });
        previousControl.setOnClickListener(v -> {
            player.previous();
        });
        repeatControl.setOnClickListener(v -> {
            switch (player.getRepeatMode()) {
                case REPEAT_MODE_OFF:
                    setRepeatMode(REPEAT_MODE_ALL);
                case REPEAT_MODE_ALL:
                    setRepeatMode(REPEAT_MODE_ONE);
                case REPEAT_MODE_ONE:
                    setRepeatMode(REPEAT_MODE_OFF);
            }
        });
        shuffleControl.setOnClickListener(v -> {
            player.next();
        });
    }

    private void setRepeatMode(int repeatMode) {
        switch (repeatMode) {
            case REPEAT_MODE_OFF:
                repeatControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_repeat_off));
            case REPEAT_MODE_ALL:
                repeatControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_icon_repeat_all));
            case REPEAT_MODE_ONE:
                repeatControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_repeat_one));
        }
        player.setRepeatMode(repeatMode);
        MainActivity.repeatMode = repeatMode;
    }

}
