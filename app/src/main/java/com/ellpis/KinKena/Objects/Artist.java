package com.ellpis.KinKena.Objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Artist {

    @SerializedName("albums")
    @Expose
    private List<Album> albums = new ArrayList<>();
    @SerializedName("artistName")
    @Expose
    private String artistName;
    @SerializedName("artistId")
    @Expose
    private Integer artistId;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("artistBio")
    @Expose
    private String artistBio;
    @SerializedName("artistAlbumCnt")
    @Expose
    private Integer artistAlbumCnt;
    @SerializedName("album_cnt")
    @Expose
    private Integer albumCnt;

    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Integer getArtistId() {
        return artistId;
    }

    public void setArtistId(Integer artistId) {
        this.artistId = artistId;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getArtistBio() {
        return artistBio;
    }

    public void setArtistBio(String artistBio) {
        this.artistBio = artistBio;
    }

    public Integer getArtistAlbumCnt() {
        return artistAlbumCnt;
    }

    public void setArtistAlbumCnt(Integer artistAlbumCnt) {
        this.artistAlbumCnt = artistAlbumCnt;
    }

    public Integer getAlbumCnt() {
        return albumCnt;
    }

    public void setAlbumCnt(Integer albumCnt) {
        this.albumCnt = albumCnt;
    }

    public void setSong_sArtistData(){
        for(Album album: getAlbums()){
            String cover = album.getAlbumPurl().replaceFirst("icon","cover");
            album.setAlbumPurl(cover);
            for(Song song: album.getSongs()){
                song.setArtistId(this.artistId);
                song.setAlbumName(album.getAlbumName());
                song.setArtistName(this.artistName);
                song.setThumbnail(cover);
            }
        }
    }

}