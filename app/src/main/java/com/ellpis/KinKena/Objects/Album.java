package com.ellpis.KinKena.Objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Album {

    @SerializedName("albumName")
    @Expose
    private String albumName;
    @SerializedName("albumPurl")
    @Expose
    private String albumPurl;
    @SerializedName("songs")
    @Expose
    private List<Song> songs = new ArrayList<>();
    @SerializedName("cnt")
    @Expose
    private Integer cnt;

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumPurl() {
        return albumPurl;
    }

    public void setAlbumPurl(String albumPurl) {
        this.albumPurl = albumPurl;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public Integer getCnt() {
        return cnt;
    }

    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }

}