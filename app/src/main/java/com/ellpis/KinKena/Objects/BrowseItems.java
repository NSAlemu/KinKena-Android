package com.ellpis.KinKena.Objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BrowseItems implements Comparable<BrowseItems> {
    String id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("position")
    @Expose
    private int position;
    @SerializedName("type")
    @Expose
    private int type;
    @SerializedName("playlists")
    @Expose
    private List<MiniPlaylist> playlists;

    public BrowseItems() {
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<MiniPlaylist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<MiniPlaylist> playlists) {
        this.playlists = playlists;
    }

    @Override
    public String toString() {
        return "BrowseItems{" +
                "playlists=" + title +
                '}';
    }

    @Override
    public int compareTo(BrowseItems o) {
        return this.getPosition()-o.getPosition();
    }
}
