package com.ellpis.KinKena.Objects;

import com.google.gson.annotations.SerializedName;

public class Featured {

    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("songCnt")
    private String songCnt;
    @SerializedName("artistId")
    private String artistId;
    @SerializedName("thumbnail")
    private String thumbnail;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSongCnt() {
        return songCnt;
    }

    public void setSongCnt(String songCnt) {
        this.songCnt = songCnt;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }


}
