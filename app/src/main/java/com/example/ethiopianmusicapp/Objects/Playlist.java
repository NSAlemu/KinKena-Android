package com.example.ethiopianmusicapp.Objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Playlist implements Serializable {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("subtitle")
    @Expose
    private String subtitle;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("songs")
    @Expose
    private List<Song> songs;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void deleteSong(int i) {
        songs.remove(i);
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", songs=" + songs +
                '}';
    }
}

