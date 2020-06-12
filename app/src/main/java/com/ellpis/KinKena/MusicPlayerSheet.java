package com.ellpis.KinKena;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
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
import androidx.media.session.MediaButtonReceiver;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ellpis.KinKena.Adapters.QueueAdapter;
import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.Objects.Utility;
import com.ellpis.KinKena.helper.OnStartDragListener;
import com.ellpis.KinKena.helper.SimpleItemTouchHelperCallback;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;
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
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_ALL;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_OFF;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_ONE;
import static com.google.android.exoplayer2.Player.STATE_READY;


public class MusicPlayerSheet extends Fragment implements TimeBar.OnScrubListener,
        Player.EventListener, OnStartDragListener, QueueAdapter.QueueItemClickListener {

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
    @BindView(R.id.queue_container)
    ConstraintLayout queueBackground;
    @BindView(R.id.card_queue_rv)
    RecyclerView queueRV;
    public static ConcatenatingMediaSource concatenatedSource;
    private boolean shuffled;
    public static ArrayList<Song> queue;
    static Bitmap bitmap;
    String ArifzefenSongPath = "http://www.arifzefen.com/json/playSong.php?id=";
    DefaultTrackSelector trackSelector;
    private MediaReceiver mediaReceiver;
    public static MediaSessionConnector mediaSessionConnector;
    private static int headSetHookClicks;
    private AudioManager audioManager;
    private QueueAdapter queueAdapter;
    private ItemTouchHelper mItemTouchHelper;
    public static List<TrackChangeListener> mTrackChangeListener = new ArrayList<>();

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
                        MusicPlayerSheet.bitmap = bitmap;
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


        exoSeekBar.addListener(this);
    }


    private ConcatenatingMediaSource getConcatenatingMediaSource() {
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
        ProgressiveMediaSource progressiveMediaSource;
        if (true) {
            CacheDataSourceFactory dataSourceFactory = new CacheDataSourceFactory(
                    MainActivity.songDownloadApplication.getDownloadCache(),
                    () -> {
                        HttpDataSource dataSource =
                                new DefaultHttpDataSource("exoplayer-codelab");
                        // Set a custom authentication request header.
                        dataSource.setRequestProperty("Cookies", MainActivity.arifzefenCookie);
                        dataSource.setRequestProperty("Referer", "http://www.arifzefen.com/");
                        return dataSource;

                    });
            progressiveMediaSource = new ProgressiveMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(uri);
        } else {
            progressiveMediaSource = new ProgressiveMediaSource.Factory(
                    () -> {
                        HttpDataSource dataSource =
                                new DefaultHttpDataSource("exoplayer-codelab");
                        // Set a custom authentication request header.
                        dataSource.setRequestProperty("Cookies", MainActivity.arifzefenCookie);
                        dataSource.setRequestProperty("Referer", "http://www.arifzefen.com/");
                        return dataSource;

                    })
                    .setTag(playbackPosition)
                    .createMediaSource(uri);
        }


        return progressiveMediaSource;
    }

    private void initializePlayer() {
        trackSelector = new DefaultTrackSelector(getContext());
        ForegroundService.player = new SimpleExoPlayer.Builder(getContext())
                .setTrackSelector(trackSelector)
                .build();
        ConcatenatingMediaSource concatenatingMediaSource = getConcatenatingMediaSource();
        ForegroundService.player.setPlayWhenReady(playWhenReady);
        ForegroundService.player.seekTo(currentWindow, playbackPosition);
        ForegroundService.player.prepare(concatenatingMediaSource, false, false);
        ForegroundService.player.setAudioAttributes(audioFocus(), true);
        ForegroundService.player.setWakeMode(C.WAKE_MODE_NETWORK);
        setViews(queue.get(ForegroundService.player.getCurrentWindowIndex()));
        mediaSession(queue.get(ForegroundService.player.getCurrentWindowIndex()));
        setupSeek();
        playpauseControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
        miniPlaypause.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_pause));
        ForegroundService.player.addListener(this);
        setupControls();
        setupQueue();
        sendMediaStyleNotification();
        setRepeatMode();
        setShuffleMode();
        noiseControl();
    }


    private void mediaSession(Song song) {

        PlaybackStateCompat playbackStateCompat = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1, SystemClock.elapsedRealtime())
                .build();
        ComponentName mediaButtonReceiver = new ComponentName(getActivity(), MediaButtonReceiver.class);
        MediaSessionCompat mMediaSession = new MediaSessionCompat(getActivity(), "MyMediasession", mediaButtonReceiver, null);
        mMediaSession.setPlaybackState(playbackStateCompat);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setActive(true);
        mediaSessionConnector = new MediaSessionConnector(mMediaSession);
        mediaSessionConnector.setPlayer(ForegroundService.player);
        mediaSessionConnector.setMediaMetadataProvider(player -> new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, queue.get(player.getCurrentWindowIndex()).getSongName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, queue.get(player.getCurrentWindowIndex()).getArtistName())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, MusicPlayerSheet.bitmap)
                .build());
        mediaSessionConnector.setMediaButtonEventHandler(setupMediaControls());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
        getContext().stopService(serviceIntent);
        releasePlayer();
        getActivity().unregisterReceiver(mediaReceiver);
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
        setPreviousNextControl();
        switch (ForegroundService.player.getRepeatMode()) {
            case REPEAT_MODE_OFF:
                repeatControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_repeat_off));
                break;
            case REPEAT_MODE_ALL:
                repeatControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_repeat_all));
                break;
            case REPEAT_MODE_ONE:
                repeatControl.setImageDrawable(getActivity().getDrawable(R.drawable.exo_controls_repeat_one));
                break;
        }

    }

    private void setPreviousNextControl() {
        if (ForegroundService.player.hasNext()) {
            nextControl.setAlpha(1f);
        } else {
            nextControl.setAlpha(0.3f);
        }
        if (ForegroundService.player.hasPrevious()) {
            previousControl.setAlpha(1f);
        } else {
            previousControl.setAlpha(0.3f);
        }
    }

    private void setShuffleMode() {
        setPreviousNextControl();
        if (ForegroundService.player.getShuffleModeEnabled()) {
            shuffleControl.setAlpha(1f);
        } else {
            shuffleControl.setAlpha(0.3f);
        }
    }

    private MediaSessionConnector.MediaButtonEventHandler setupMediaControls() {

        return (player, controlDispatcher, mediaButtonEvent) -> {
            KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            int keyCode = keyEvent.getKeyCode();
            Log.e("keycode", keyCode + "");
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    nextControl.performClick();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:

                    previousControl.performClick();
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    headSetHookClicks++;
                    Handler handler = new Handler();
                    Runnable r = () -> {
                        if (headSetHookClicks == 2) {
                            nextControl.performClick();
                        }
                        headSetHookClicks = 0;
                    };
                    if (headSetHookClicks == 1) {
                        handler.postDelayed(r, 500);
                    }
                    break;
                default:
                    // If another key is pressed within double tap timeout, consider the pending
                    // pending play/pause as a single tap to handle media keys in order.
                    break;
            }
            return false;
        };
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        String stateString;
        setPlayPause(!ForegroundService.player.getPlayWhenReady());
        Log.e(TAG, "onPlayerStateChanged: " + playbackState);
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

    private AudioAttributes audioFocus() {
        return new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build();

    }

    private void noiseControl() {
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mediaReceiver = new MediaReceiver();
        getActivity().registerReceiver(mediaReceiver, filter);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        setPreviousNextControl();
        setViews(queue.get(ForegroundService.player.getCurrentWindowIndex()));
        queueAdapter.notifyDataSetChanged();
        for(TrackChangeListener trackChangeListener:mTrackChangeListener){
            trackChangeListener.trackChanged();
        }
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

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onQueueItemClick(View view, int position) {
        ForegroundService.player.seekTo(position, 0);
    }

    class MediaReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(final Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (ForegroundService.player != null && ForegroundService.player.isPlaying()) {
                    ForegroundService.player.setPlayWhenReady(false);
                }
            }


        }
    }

    private void setupQueue() {
        queueAdapter = new QueueAdapter(queue);
        queueAdapter.setClickListener(this);
        queueRV.setAdapter(queueAdapter);
        queueRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemTouchHelper.Callback callback = enableSwipeToDeleteAndUndo();
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(queueRV);
        getView().findViewById(R.id.music_player_queue_close).setOnClickListener(v -> {
            queueBackground.setVisibility(View.GONE);
        });
        getView().findViewById(R.id.exoplayer_queue).setOnClickListener(v -> {
            queueBackground.setVisibility(View.VISIBLE);
        });
    }

    private SimpleItemTouchHelperCallback enableSwipeToDeleteAndUndo() {
        return new SimpleItemTouchHelperCallback(queueAdapter, getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final Song item = queue.get(position);

                queue.remove(position);
                queueAdapter.notifyItemRemoved(position);

                Snackbar snackbar = Snackbar.make(getView(), "Song removed from Queue", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        queue.add(position, item);
                        queueAdapter.notifyItemInserted(position);
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        };
    }

    public static String getCurrentSongID() {
        if(ForegroundService.player==null){
            return null;
        }
        return queue.get(ForegroundService.player.getCurrentWindowIndex()).getSongId() + "";
    }
    public interface TrackChangeListener{
        void trackChanged();
    }
    public static void addTrackChangeListener(TrackChangeListener trackChangeListener) {
        mTrackChangeListener.add(trackChangeListener);
    }
    public static void removeTrackChangeListener(TrackChangeListener trackChangeListener) {
        mTrackChangeListener.remove(trackChangeListener);
    }

    public void addSongToQueue(Song song) {
        int pos = ForegroundService.player.getCurrentWindowIndex() + 1;
        queue.add(pos, song);
        queueAdapter.notifyItemInserted(pos);
        concatenatedSource.addMediaSource(pos, buildMediaSource(Uri.parse(ArifzefenSongPath + song.getSongId()), song.getSongId()));
    }

    public void download(Song song) {
        DownloadRequest downloadRequest = new DownloadRequest(
                song.getId(),
                DownloadRequest.TYPE_PROGRESSIVE,
                Uri.parse(ArifzefenSongPath + song.getSongId()),
                /* streamKeys= */ Collections.emptyList(),
                /* customCacheKey= */ null,
                null);
        DownloadService.sendAddDownload(
                getContext(),
                SongDownloadService.class,
                downloadRequest,
                /* foreground= */ false);
    }

}
