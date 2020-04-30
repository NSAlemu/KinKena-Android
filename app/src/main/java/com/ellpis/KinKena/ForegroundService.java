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
import androidx.palette.graphics.Palette;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ForegroundService extends Service {
    public static SimpleExoPlayer player;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
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
                this, "5465362", 5,
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
        playerNotificationManager.setSmallIcon(R.mipmap.ic_launcher);
        playerNotificationManager.setPlayer(player);
//        startForeground(1,  MusicPlayerSheet.notification);
        //do heavy work on a background thread
        //stopSelf();
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
                return null;
            }

            @Nullable
            @Override
            public CharSequence getCurrentContentText(Player player) {
                return MusicPlayerSheet.queue.get(player.getCurrentWindowIndex()).getArtistName();
            }

            @Nullable
            @Override
            public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                Picasso.get().load("http://www.arifzefen.com" + MusicPlayerSheet.queue.get(player.getCurrentWindowIndex()).getThumbnail()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            public void onGenerated(Palette p) {
                             callback.onBitmap(bitmap);
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
                return MusicPlayerSheet.bitmap;
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "onDestroy: ");
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("TAG", "onBind: ");
        return null;
    }

}
