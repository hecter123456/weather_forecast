package com.example.weatherforecast.core.network.retrofit

import com.example.weatherforecast.core.network.model.OneCallResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApi {
    // One Call (3.0): current + daily. Requires API key.
    @GET("data/3.0/onecall")
    suspend fun oneCall(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("exclude") exclude: String = "minutely,hourly,alerts",
        @Query("appid") apiKey: String,
    ): OneCallResponse
}