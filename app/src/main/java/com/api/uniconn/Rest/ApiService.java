package com.api.uniconn.Rest;

import com.api.uniconn.Rest.Models.NearbySearch;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Rakshit on 7/10/2016.
 */

public interface ApiService {


    @GET("maps/api/place/nearbysearch/json")
    Call<NearbySearch> getGetOutputFormats(
            @Query("location") String location,
            @Query("types") String types,
            @Query("rankby") String radius1,
            @Query("key") String Key);

    @GET("maps/api/place/nearbysearch/json")
    Call<NearbySearch> getNextPage(
            @Query("location") String location,
            @Query("types") String types,
            @Query("rankby") String radius1,
            @Query("key") String Key,
            @Query("pagetoken") String token);

}
