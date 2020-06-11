package com.ellpis.KinKena.Repository;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.ellpis.KinKena.MainActivity;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.Objects.Song;
import com.ellpis.KinKena.SongDownloadService;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DownloadsRepository {
    public static boolean isDownloaded(Playlist playlist, Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        Set<String> playlistSet = sharedPref.getStringSet(playlist.getId(), new HashSet<>());
        return !playlistSet.isEmpty();
    }

    public static void downloadPlaylist(Playlist playlist, Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Set<String> playlistSet = new HashSet<>();
        if (!sharedPref.getStringSet(playlist.getId(), new HashSet<>()).isEmpty()) {
            return;
        }
        for (Song song : playlist.getSongs()) {
            playlistSet.add(song.getSongId() + "");
            addToAllDownloads(song.getSongId()+"", activity);
        }
        editor.putStringSet(playlist.getId(), playlistSet);
        editor.apply();
    }

    public static void deleteDownload(Playlist playlist, Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Set<String> playlistSet = new HashSet<>();
        for (Song song : playlist.getSongs()) {
            playlistSet.remove(song.getSongId() + "");
            removeFromAllDownloads(song.getSongId()+"", activity);
        }
        editor.remove(playlist.getId());
        editor.apply();
    }

    public static void updateDownloadedPlaylist(Playlist playlist, Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Set<String> playlistSet = sharedPref.getStringSet(playlist.getId(), new HashSet<>());
        if (playlistSet.isEmpty()) {
            return;
        }
        for (Song song : playlist.getSongs()) {
            if (!playlistSet.contains(song.getId() + "")) {
                playlistSet.add(song.getSongId() + "");
                addToAllDownloads(song.getId(), activity);
            }
        }
        Set<String> tempSet = new HashSet<>();
        tempSet.addAll(playlistSet);
        for (Song song : playlist.getSongs()) {
            tempSet.remove(song.getId()+"");
        }
        Iterator itr = tempSet.iterator();
        while (itr.hasNext()) {
            String songID = itr.next() + "";
            playlistSet.remove(songID);
            removeFromAllDownloads(songID, activity);
        }
        editor.putStringSet(playlist.getId(), playlistSet);
        editor.apply();
    }

    public static int addToAllDownloads(String songId, Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences("All_downloads", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int songDownloadCount = sharedPref.getInt(songId, 0);
        if (songDownloadCount == 0) {
            downloadSong(songId);
        }
        Log.e("TAG", "removeFromAllDownloads:started at "+songDownloadCount);
        editor.putInt(songId, ++songDownloadCount);
        Log.e("TAG", "removeFromAllDownloads:ended at  "+songDownloadCount);
        editor.apply();
        return songDownloadCount;
    }

    public static int removeFromAllDownloads(String songId, Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences("All_downloads", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int songDownloadCount = sharedPref.getInt(songId, 0);
        Log.e("TAG", "removeFromAllDownloads:started at "+songDownloadCount);
        editor.putInt(songId, --songDownloadCount);
        Log.e("TAG", "removeFromAllDownloads:ended at  "+songDownloadCount);
        if (songDownloadCount == 0) {
            deleteSong(songId);
        }
        editor.apply();
        return songDownloadCount;
    }

    private static void downloadSong(String songId) {
        Uri songUri = Uri.parse("http://www.arifzefen.com/json/playSong.php?id=" + songId);
        if (MainActivity.songDownloadApplication.getDownloadTracker().downloadExists(songUri)) {
            return;
        }
        DownloadRequest downloadRequest = new DownloadRequest(
                songId + "",
                DownloadRequest.TYPE_PROGRESSIVE,
                songUri,
                /* streamKeys= */ Collections.emptyList(),
                /* customCacheKey= */ null,
                "".getBytes());

        DownloadService.sendAddDownload(
                MainActivity.context.getApplicationContext(),
                SongDownloadService.class,
                downloadRequest,
                /* foreground= */ true);
    }

    private static void deleteSong(String songId) {
        DownloadService.sendRemoveDownload(
                MainActivity.context.getApplicationContext(),
                SongDownloadService.class,
                songId + "",
                /* foreground= */ true
        );
    }
}
