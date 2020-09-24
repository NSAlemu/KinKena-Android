package com.ellpis.KinKena;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ellpis.KinKena.Objects.Utility;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


public class ForegroundService extends Service {
    public static SimpleExoPlayer player;
    private PlayerNotificationManager playerNotificationManager;


    public ForegroundService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TAG", "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        playerNotificationManager = new PlayerNotificationManager(
                this, getString(R.string.player_channel_name), 5,
                createMediaDescriptionAdapter(),
                new PlayerNotificationManager.NotificationListener() {
                    @Override
                    public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {

                        startForeground(notificationId, notification);
                    }

                    @Override
                    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                        if (dismissedByUser) {
                            // Do what the app wants to do when dismissed by the user,
                            // like calling stopForeground(true); or stopSelf();
                        }
                    }
                });
        playerNotificationManager.setMediaSessionToken(MusicPlayerSheet.mediaSessionConnector.mediaSession.getSessionToken());
        playerNotificationManager.setSmallIcon(R.drawable.kinkena_logo_mini);
        playerNotificationManager.setPlayer(player);
        Log.d("TAG", "onStartCommand: ");
        return START_NOT_STICKY;
    }

    private PlayerNotificationManager.MediaDescriptionAdapter createMediaDescriptionAdapter() {
        return new PlayerNotificationManager.MediaDescriptionAdapter() {
            @Override
            public CharSequence getCurrentContentTitle(Player player) {
                return MusicPlayerSheet.queue.get(player.getCurrentWindowIndex()).getSongName();
            }

            @Nullable
            @Override
            public PendingIntent createCurrentContentIntent(Player player) {
                return PendingIntent.getActivity(getApplication(), 0,
                        new Intent(getApplication(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            }

            @Nullable
            @Override
            public CharSequence getCurrentContentText(Player player) {
                return MusicPlayerSheet.queue.get(player.getCurrentWindowIndex()).getArtistName();
            }

            @Nullable
            @Override
            public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                Utility.getImageLinkMini(  MusicPlayerSheet.queue.get(player.getCurrentWindowIndex()).getThumbnail(), link -> {
                    Picasso.get().load(link).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            callback.onBitmap(bitmap);

                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });

                });
                return MusicPlayerSheet.bitmap;
            }
        };
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        playerNotificationManager.setPlayer(null);
        stopSelf();
        Log.d("TAG", "onDestroy: ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("TAG", "onBind: ");
        return null;
    }


}
