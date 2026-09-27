package com.example.nasaapod;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("planetary/apod")
    Call<ApodResponse> getApod(
            @Query("api_key") String apiKey,
            @Query("date") String date,
            @Query("thumbs") boolean thumbs
    );
}
