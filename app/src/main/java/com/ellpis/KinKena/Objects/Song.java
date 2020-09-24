package com.ellpis.KinKena.Objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Song implements Serializable, Parcelable {

    @SerializedName("artistId")
    private Integer artistId;
    @SerializedName("artistName")
    private String artistName;
    @SerializedName("songId")
    private Integer songId;
    @SerializedName("songName")
    private String songName;
    @SerializedName(value = "playtime", alternate = "songPlaytime")
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.artistId);
        dest.writeString(this.artistName);
        dest.writeValue(this.songId);
        dest.writeString(this.songName);
        dest.writeValue(this.playtime);
        dest.writeValue(this.songLikes);
        dest.writeValue(this.songAccess);
        dest.writeString(this.albumName);
        dest.writeString(this.vendorUrl);
        dest.writeString(this.thumbnail);
    }

    public Song() {
    }

    protected Song(Parcel in) {
        this.artistId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.artistName = in.readString();
        this.songId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.songName = in.readString();
        this.playtime = (Integer) in.readValue(Integer.class.getClassLoader());
        this.songLikes = (Integer) in.readValue(Integer.class.getClassLoader());
        this.songAccess = (Integer) in.readValue(Integer.class.getClassLoader());
        this.albumName = in.readString();
        this.vendorUrl = in.readString();
        this.thumbnail = in.readString();
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}