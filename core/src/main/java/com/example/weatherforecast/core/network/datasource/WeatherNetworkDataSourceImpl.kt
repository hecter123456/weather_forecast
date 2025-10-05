package com.example.weatherforecast.core.network.datasource

import com.example.weatherforecast.core.network.request.OneCallRequest
import com.example.weatherforecast.core.network.response.GeoDirectItem
import com.example.weatherforecast.core.network.response.OneCallResponse
import com.example.weatherforecast.core.network.retrofit.OpenWeatherApi
import javax.inject.Inject

class WeatherNetworkDataSourceImpl @Inject constructor(
    private val api: OpenWeatherApi,
    private val apiKey: String,
) : WeatherNetworkDataSource {

    override suspend fun fetchOneCall(request: OneCallRequest): OneCallResponse =
        api.oneCall(
            lat = request.lat,
            lon = request.lon,
            apiKey = apiKey,
            units = request.units,
            exclude = request.exclude
        )

    override suspend fun geocodeDirect(
        query: String,
        limit: Int
    ): List<GeoDirectItem> =
        api.geocodeDirect(query = query, limit = limit, apiKey = apiKey)

    override suspend fun reverseGeocode(
        lat: Double,
        lon: Double,
        limit: Int
    ): List<GeoDirectItem> =
        api.reverseGeocode(lat = lat, lon = lon, limit = limit, apiKey = apiKey)

}
