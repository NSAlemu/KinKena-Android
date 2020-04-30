package com.ellpis.KinKena.Retrofit;

import com.ellpis.KinKena.Objects.Artist;
import com.ellpis.KinKena.Objects.Featured;
import com.ellpis.KinKena.Objects.Playlist;
import com.ellpis.KinKena.Objects.Song;

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

    @GET("/json/artist/{artistID}")
    Call<Artist> getArtist(@Path("artistID") String artistID);
}
