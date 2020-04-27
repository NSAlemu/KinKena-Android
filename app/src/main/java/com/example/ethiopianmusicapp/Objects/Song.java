package com.example.ethiopianmusicapp.Objects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Song implements Serializable {

    @SerializedName("artistId")
    private Integer artistId;
    @SerializedName("artistName")
    private String artistName;
    @SerializedName("songId")
    private Integer songId;
    @SerializedName("songName")
    private String songName;
    @SerializedName("playtime")
    private Integer playtime;
    @SerializedName("songLikes")
    private Integer songLikes;
    @SerializedName("songAccess")
    private Integer songAccess;
    @SerializedName("albumName")
    private String albumName;
    @SerializedName("vendorUrl")
    private String vendorUrl;
    @SerializedName("thumbnail")
    private String thumbnail;

    public Integer getArtistId() {
        return artistId;
    }

    public void setArtistId(Integer artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Integer getSongId() {
        return songId;
    }

    public void setSongId(Integer songId) {
        this.songId = songId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public Integer getPlaytime() {
        return playtime;
    }

    public void setPlaytime(Integer playtime) {
        this.playtime = playtime;
    }

    public Integer getSongLikes() {
        return songLikes;
    }

    public void setSongLikes(Integer songLikes) {
        this.songLikes = songLikes;
    }

    public Integer getSongAccess() {
        return songAccess;
    }

    public void setSongAccess(Integer songAccess) {
        this.songAccess = songAccess;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getVendorUrl() {
        return vendorUrl;
    }

    public void setVendorUrl(String vendorUrl) {
        this.vendorUrl = vendorUrl;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        return "Song{" +
                "artistName='" + artistName + '\'' +
                ", songName='" + songName + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                '}';
    }
}