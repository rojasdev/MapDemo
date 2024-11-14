package com.rhix.apidemo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface WeatherApiServiceTwo {
    @GET("weather")
    Call<WeatherResponseTwo> getWeather(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    // Method for Nominatim reverse geocoding
    @Headers("User-Agent: MyAndroidApp/1.0 (your-email@example.com)")
    @GET("reverse")
    Call<NominatimResponse> reverseGeocodeWithNominatim(
            @Query("lat") double latitude,
            @Query("lon") double longitude,
            @Query("format") String format
    );
}

