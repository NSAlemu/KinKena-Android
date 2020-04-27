package com.example.ethiopianmusicapp.Repository;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.ethiopianmusicapp.Objects.Song;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public  class SearchRepository {
    public static final String SEARCH_REPOSITORY_ID="search_repository";
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    public void addToList(Song song) {
        Gson gson = new Gson();

        List<Song> prefSongs = getList();
        prefSongs.add(song);
        String json = gson.toJson(prefSongs);
        set("search", json);
    }

    public List<Song> getList() {
        Gson gson = new Gson();
        Type typeOfT = new TypeToken<List<Song>>() {
        }.getType();
        return gson.fromJson(prefs.getString("search", "[]"), typeOfT);

    }

    public void clearSearchPref() {
        editor.clear().commit();
    }

    public void set(String key, String value) {
        editor.putString(key, value).commit();
    }

    public SearchRepository (Activity activity){
        if(editor==null){
            prefs = activity.getSharedPreferences(SEARCH_REPOSITORY_ID, Context.MODE_PRIVATE);
            editor = prefs.edit();
        }
    }
}
