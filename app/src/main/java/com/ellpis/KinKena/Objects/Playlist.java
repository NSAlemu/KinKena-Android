package com.ellpis.KinKena.Objects;

import com.google.firebase.firestore.DocumentId;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Playlist implements Serializable {

    @DocumentId
    String id;
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

    private String ownerID;
    private String ownerUsername;
    @SerializedName("privacy")
    @Expose
    private boolean privacy;
    private boolean fromFirebase = true;

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

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String playlistLength() {
        int songLength = 0;
        for (Song song : getSongs()) {
            songLength += song.getPlaytime();
        }
        if (songLength < 3600) {
            return String.format(Locale.US, "%02d:%02d",
                    TimeUnit.SECONDS.toMinutes(songLength) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.SECONDS.toSeconds(songLength) % TimeUnit.MINUTES.toSeconds(1));
        }
        return String.format(Locale.US, "%01d:%02d:%02d", TimeUnit.SECONDS.toHours(songLength),
                TimeUnit.SECONDS.toMinutes(songLength) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.SECONDS.toSeconds(songLength) % TimeUnit.MINUTES.toSeconds(1));
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public boolean isFromFirebase() {
        return fromFirebase;
    }

    public void setFromFirebase(boolean fromFirebase) {
        this.fromFirebase = fromFirebase;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isPrivacy() {
        return privacy;
    }

    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    @Override
    public String toString() {
        return "Playlist{\n" +
                "id='" + id + '\'' +
                ",\n title='" + title + '\'' +
                ",\n subtitle='" + subtitle + '\'' +
                ",\n thumbnail='" + thumbnail + '\'' +
                ",\n songs=" + songs +
                ",\n ownerID='" + ownerID + '\'' +
                ",\n ownerUsername='" + ownerUsername + '\'' +
                ",\n privacy=" + privacy +
                ",\n fromFirebase=" + fromFirebase +
                '}';
    }
}

