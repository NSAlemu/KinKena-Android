package com.example.ethiopianmusicapp.Repository;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.ethiopianmusicapp.Objects.Playlist;
import com.example.ethiopianmusicapp.Objects.Song;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistRepository {
    public static final String PLAYLIST_REPOSITORY_ID="playlist_repository";
    private static final String PLAYLIST_NAMES_REPOSITORY_ID="playlist_names_repository";
    List<String> playlistRepositoryNames = new ArrayList<>();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    static Map<String, Playlist> playlistMap = new HashMap<>();
    public void addToPlaylist(String playlistName, Song song) {
        Gson gson = new Gson();

        Playlist p =playlistMap.get(playlistName);
        p.addSong(song);
        playlistMap.put(playlistName, p);
        String json = gson.toJson(playlistMap.get(playlistName));
        setPlaylist(playlistName, json);
    }

    public List<Playlist> getList() {
        List<Playlist> playlists = new ArrayList<>();
        for (Map.Entry<String,Playlist> entry : playlistMap.entrySet())
            playlists.add(entry.getValue());
        return playlists;

    }
    public Playlist getPlaylist(String name) {

        return playlistMap.get(name);

    }

    public void deleteSong(String playlistName, int position){
        Gson gson = new Gson();

        playlistMap.get(playlistName).deleteSong(position);
        String json = gson.toJson(playlistMap.get(playlistName));
        setPlaylist(playlistName, json);
    }
    public void setPlaylist(String key, String value) {
        editor.putString(key, value).commit();
    }
    public void setPlaylistName(String value) {
        editor.putString(PLAYLIST_NAMES_REPOSITORY_ID, value).commit();
    }

    public boolean createPlaylist(String name){
        if(playlistMap.containsKey(name)){
            return false;
        }
        Gson gson = new Gson();
        Playlist playlist = new Playlist();
        playlist.setTitle(name);
        playlist.setSubtitle("");
        playlist.setThumbnail("");
        playlist.setSongs(new ArrayList<>());

        String json = gson.toJson(playlist);
        setPlaylist(name, json);

        gson = new Gson();
        playlistRepositoryNames.add(name);
        json = gson.toJson(playlistRepositoryNames);
        setPlaylistName(json);

        return true;
    }
    public PlaylistRepository (Activity activity){
        if(editor==null){

            Log.e("ssasad",activity.toString());
            prefs = activity.getSharedPreferences(PLAYLIST_REPOSITORY_ID, Context.MODE_PRIVATE);
            editor = prefs.edit();
        }
        Gson gson = new Gson();
        Type typeOfT = new TypeToken<List<String>>() {
        }.getType();
        playlistRepositoryNames= gson.fromJson(prefs.getString(PLAYLIST_NAMES_REPOSITORY_ID, ""), typeOfT);
        if(playlistRepositoryNames==null){
            playlistRepositoryNames = new ArrayList<>();
        }
        for(String playlistName:playlistRepositoryNames){
            gson = new Gson();
            typeOfT = new TypeToken<Playlist>() {
            }.getType();
            Playlist playlist = gson.fromJson(prefs.getString(playlistName, "[]"), typeOfT);
            playlistMap.put(playlistName, playlist);
        }
    }



}
