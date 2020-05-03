package com.ellpis.KinKena.Objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SearchArtist {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("bio")
    @Expose
    private String bio;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("songCnt")
    @Expose
    private Integer songCnt;
    @SerializedName("albumCnt")
    @Expose
    private Integer albumCnt;
    @SerializedName("likeCnt")
    @Expose
    private Integer likeCnt;
    @SerializedName("index")
    @Expose
    private Integer index;
    @SerializedName("objectID")
    @Expose
    private String objectID;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Integer getSongCnt() {
        return songCnt;
    }

    public void setSongCnt(Integer songCnt) {
        this.songCnt = songCnt;
    }

    public Integer getAlbumCnt() {
        return albumCnt;
    }

    public void setAlbumCnt(Integer albumCnt) {
        this.albumCnt = albumCnt;
    }

    public Integer getLikeCnt() {
        return likeCnt;
    }

    public void setLikeCnt(Integer likeCnt) {
        this.likeCnt = likeCnt;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

}

