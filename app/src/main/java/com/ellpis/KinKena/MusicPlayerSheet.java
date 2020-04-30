package com.ellpis.KinKena;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Objects.Utility;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.android.exoplayer2.Player.REPEAT_MODE_ALL;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_OFF;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_ONE;
import static com.google.android.exoplayer2.Player.STATE_READY;


public class MusicPlayerSheet extends Fragment implements TimeBar.OnScrubListener,
        Player.EventListener{

    private String audioURL = "";
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    Handler mHandler;
    Runnable runnable;


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

    private static ConcatenatingMediaSource concatenatedSource;
    private boolean shuffled;
    static ArrayList<Song> queue;
    static Bitmap bitmap;
    static Notification notification;
    static MediaSessionCompat.Callback mMediaSessionCallback;
    String ArifzefenSongPath = "http://www.arifzefen.com/json/playSong.php?id=";
    DefaultTrackSelector trackSelector;
    //    private MediaSessionCompat mMediaSession;
    PlaybackStateCompat.Builder mStateBuilder;
    static MediaSessionConnector mediaSessionConnector;
    float volume;


    public static MusicPlayerSheet newInstance(int currentWindow, ArrayList<Song> playlist, boolean shuffled) {
        MusicPlayerSheet myFragment = new MusicPlayerSheet();
        Bundle args = new Bundle();
        args.putParcelableArrayList("playlist", playlist);
        args.putBoolean("shuffled", shuffled);
        args.putInt("currentWindow", currentWindow);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = getArguments().getParcelableArrayList("playlist");
        shuffled = getArguments().getBoolean("shuffled");
        currentWindow = getArguments().getInt("currentWindow");
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

    }

    void setViews(Song song) {
        miniTitle.setText(song.getSongName());
        miniArtist.setText(song.getArtistName());
        titleControl.setText(song.getSongName());
        artistControl.setText(song.getArtistName());
        Picasso.get().load("http://www.arifzefen.com" + song.getThumbnail()).into(cover);
        Picasso.get().load("http://www.arifzefen.com" + queue.get(ForegroundService.player.getCurrentWindowIndex()).getThumbnail()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    public void onGenerated(Palette p) {
                        playerBackground.setBackgroundColor(Utility.manipulateColor(p.getDominantColor(getResources().getColor(R.color.on_top_background)), 0.6f));
                        miniPlayerBackground.setBackgroundColor(Utility.manipulateColor(p.getDominantColor(getResources().getColor(R.color.on_top_background)), 0.6f));

                    }
                });

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                setViews(queue.get(ForegroundService.player.getCurrentWindowIndex()));
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });


        exoSeekBar.addListener(this);
    }


    private ConcatenatingMediaSource setPlaylist() {
        if (shuffled) {
            Collections.shuffle(queue);
        }
        concatenatedSource = new ConcatenatingMediaSource();
        for (int i = 0; i < queue.size(); i++) {
            concatenatedSource.addMediaSource(buildMediaSource(Uri.parse(ArifzefenSongPath + queue.get(i).getSongId()), i));
        }
        return concatenatedSource;
    }

    private MediaSource buildMediaSource(Uri uri, int playbackPosition) {

        return new ProgressiveMediaSource.Factory(
                () -> {
                    HttpDataSource dataSource =
                            new DefaultHttpDataSource("exoplayer-codelab");
                    // Set a custom authentication request header.
                    dataSource.setRequestProperty("Cookies", "_ga=GA1.2.436202454.1587347535; _gid=GA1.2.921082897.1587347535; _ga=GA1.2.1355987553.1587348657; _gid=GA1.2.1039899393.1587847357; _gat=1");
                    dataSource.setRequestProperty("Referer", "http://www.arifzefen.com/");
                    return dataSource;
                })
                .setTag(playbackPosition)
                .createMediaSource(uri);
    }

    private void initializePlayer() {
        trackSelector = new DefaultTrackSelector(getContext());
        ForegroundService.player = new SimpleExoPlayer.Builder(getContext())
                .setTrackSelector(trackSelector)
                .build();
        ConcatenatingMediaSource concatenatingMediaSource = setPlaylist();
        ForegroundService.player.setPlayWhenReady(playWhenReady);
        ForegroundService.player.seekTo(currentWindow, playbackPosition);
        ForegroundService.player.prepare(concatenatingMediaSource, false, false);

        ForegroundService.player.setWakeMode(C.WAKE_MODE_NETWORK);

        setupSeek();
        playpauseControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
        miniPlaypause.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
        ForegroundService.player.addListener(this);
        setupControls();
        mMediaSessionCallback = setupMediaSessionCallback();
        setViews(queue.get(ForegroundService.player.getCurrentWindowIndex()));
        sendMediaStyleNotification();
        setRepeatMode();
        setShuffleMode();

    }
    MediaSessionCompat.Callback setupMediaSessionCallback(){
        return new MediaSessionCompat.Callback() {
            @Override
            public void onPause() {
                playpauseControl.performClick();
                super.onPause();
            }

            @Override
            public void onSkipToNext() {
                nextControl.performClick();
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                previousControl.performClick();
                super.onSkipToPrevious();
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
        getContext().stopService(serviceIntent);
        releasePlayer();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24 && ForegroundService.player == null) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //enterImmersiveMode();
        if ((Util.SDK_INT < 24 || ForegroundService.player == null)) {
//            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
//            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (ForegroundService.player != null) {
            playWhenReady = ForegroundService.player.getPlayWhenReady();
            playbackPosition = ForegroundService.player.getCurrentPosition();
            currentWindow = ForegroundService.player.getCurrentWindowIndex();
            ForegroundService.player.release();
            ForegroundService.player = null;
        }
    }

    private void sendMediaStyleNotification() {
        Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(getContext(), serviceIntent);
        } else {
            getContext().startService(serviceIntent);
        }
    }



    /**
     * Not yet Implemented
     *
     * @param position
     */
    public void playSongInQueue(int position) {
        //TODO: CREATE ABILITY PLAY FROM QUEUE
        throw new UnsupportedOperationException();
    }

    /**
     * Not yet Implemented
     *
     * @param song
     */
    public void addSongToQueue(Song song) {
        //TODO: CREATE ABILITY TO ADD TO QUEUE
        throw new UnsupportedOperationException();
    }

    private void setupSeek() {
        ForegroundService.player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == STATE_READY) {
                    exoSeekBar.setDuration(ForegroundService.player.getDuration());
                    durationChronometer.setBase(SystemClock.elapsedRealtime() - ForegroundService.player.getDuration());
                    exoSeekBar.setPosition(ForegroundService.player.getCurrentPosition());
                    if (runnable == null) {
                        mHandler = new Handler();
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (ForegroundService.player != null) {
                                    curTimeChronometer.setBase(SystemClock.elapsedRealtime() - ForegroundService.player.getCurrentPosition());
                                    exoSeekBar.setPosition(ForegroundService.player.getCurrentPosition());
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
            if (ForegroundService.player.isPlaying()) {
                setPlayPause(true);
            } else {
                setPlayPause(false);
            }
        });
        miniPlaypause.setOnClickListener(v -> {
            if (ForegroundService.player.isPlaying()) {
                playpauseControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_play));
                miniPlaypause.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_play));
                ForegroundService.player.setPlayWhenReady(false);
            } else {
                playpauseControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
                miniPlaypause.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
                ForegroundService.player.setPlayWhenReady(true);
            }
        });
        nextControl.setOnClickListener(v -> {
            ForegroundService.player.next();
        });
        previousControl.setOnClickListener(v -> {
            ForegroundService.player.previous();
        });
        repeatControl.setOnClickListener(v -> {
            int repeatMode = 1;
            switch (ForegroundService.player.getRepeatMode()) {
                case REPEAT_MODE_OFF:
                    ForegroundService.player.setRepeatMode(REPEAT_MODE_ALL);
                    break;
                case REPEAT_MODE_ALL:
                    ForegroundService.player.setRepeatMode(REPEAT_MODE_ONE);
                    break;
                case REPEAT_MODE_ONE:
                    ForegroundService.player.setRepeatMode(REPEAT_MODE_OFF);
                    break;
            }
            setRepeatMode();
        });
        shuffleControl.setOnClickListener(v -> {
            ForegroundService.player.setShuffleModeEnabled(!ForegroundService.player.getShuffleModeEnabled());
            setShuffleMode();
        });
    }

    private void setPlayPause(boolean play) {
        if (play) {
            playpauseControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_play));
            miniPlaypause.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_play));
        } else {
            playpauseControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
            miniPlaypause.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
        }
        ForegroundService.player.setPlayWhenReady(!play);
    }

    private void setRepeatMode() {
        switch (ForegroundService.player.getRepeatMode()) {
            case REPEAT_MODE_OFF:
                repeatControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_repeat_off));
                break;
            case REPEAT_MODE_ALL:
                repeatControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_icon_repeat_all));
                break;
            case REPEAT_MODE_ONE:
                repeatControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_repeat_one));
                break;
        }

    }

    private void setShuffleMode() {
        if (ForegroundService.player.getShuffleModeEnabled()) {
            shuffleControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_shuffle_off));

        } else {
            shuffleControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_shuffle_on));
        }
    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        String stateString;
        setPlayPause(!ForegroundService.player.getPlayWhenReady());
        switch (playbackState) {
            case ExoPlayer.STATE_IDLE:
                Log.d("Tag", "onPlayerStateChanged: ");
                stateString = "ExoPlayer.STATE_IDLE      -";
                break;
            case ExoPlayer.STATE_BUFFERING:
                stateString = "ExoPlayer.STATE_BUFFERING -";
                break;
            case ExoPlayer.STATE_READY:
                stateString = "ExoPlayer.STATE_READY     -";
                break;
            case ExoPlayer.STATE_ENDED:
                stateString = "ExoPlayer.STATE_ENDED     -";
                break;
            default:
                stateString = "UNKNOWN_STATE             -";
                break;
        }
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        setViews(queue.get(ForegroundService.player.getCurrentWindowIndex()));

    }

    @Override
    public void onScrubStart(TimeBar timeBar, long position) {
        curTimeChronometer.setBase(SystemClock.elapsedRealtime() - position);
        ForegroundService.player.setPlayWhenReady(false);
    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {
        curTimeChronometer.setBase(SystemClock.elapsedRealtime() - position);
        ForegroundService.player.setPlayWhenReady(false);
    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        if (!canceled) {
            ForegroundService.player.seekTo(position);
        }
        ForegroundService.player.setPlayWhenReady(true);

    }




}
