package com.example.ethiopianmusicapp.Retrofit;

import com.example.ethiopianmusicapp.Objects.Featured;
import com.example.ethiopianmusicapp.Objects.Playlist;
import com.example.ethiopianmusicapp.Objects.Song;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MusicRetrofit {

    @GET("books/v1/volumes/{songID}")
    Call<Song> getBook(@Path("songID") String bookId);

    @GET("/json/list/search.php")
    Call<Playlist> getSearch(@Query("q") String searchQuery);

    @GET("/json/{firstPath}/{secondPath}")
    Call<Playlist> getCategory(@Path("firstPath") String firstPath, @Path("secondPath") String secondPath);

    @GET("/json/featured/all.json")
    Call<List<Featured>> getFeatured();
}
