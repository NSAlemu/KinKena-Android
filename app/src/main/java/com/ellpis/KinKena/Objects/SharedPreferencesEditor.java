package com.ellpis.KinKena.Objects;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SharedPreferencesEditor {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    void setUpSharedPreferences(){
//        if(prefs==null){
//            prefs = getActivity().getSharedPreferences("search_history", Context.MODE_PRIVATE);
//            editor = prefs.edit();
//        }
    }
    public void addToList(Song song) {
        Gson gson = new Gson();

        List<Song> prefSongs = getList();
        prefSongs.add(song);
        String json = gson.toJson(prefSongs);
        set("search", json);
    }

    private List<Song> getList() {
        Gson gson = new Gson();
        Type typeOfT = new TypeToken<List<Song>>() {
        }.getType();
        ;
        return gson.fromJson(prefs.getString("search", "[]"), typeOfT);

    }

    public void set(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }
}
