package com.example.weatherforecast.core.network.retrofit

import com.example.weatherforecast.core.network.response.GeoDirectItem
import com.example.weatherforecast.core.network.response.OneCallResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApi {
    // One Call (3.0): current + daily. Requires API key.
    @GET("data/3.0/onecall")
    suspend fun oneCall(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("exclude") exclude: String,
        @Query("appid") apiKey: String,
    ): OneCallResponse

    @GET("geo/1.0/direct")
    suspend fun geocodeDirect(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String,
    ): List<GeoDirectItem>

    @GET("geo/1.0/reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<GeoDirectItem>
}