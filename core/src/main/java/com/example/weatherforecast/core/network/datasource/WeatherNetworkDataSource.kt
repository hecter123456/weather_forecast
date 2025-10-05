package com.example.weatherforecast.core.network.datasource

import com.example.weatherforecast.core.network.request.OneCallRequest
import com.example.weatherforecast.core.network.response.GeoDirectItem
import com.example.weatherforecast.core.network.response.OneCallResponse

interface WeatherNetworkDataSource {
    suspend fun fetchOneCall(request: OneCallRequest): OneCallResponse

    suspend fun geocodeDirect(query: String, limit: Int): List<GeoDirectItem>

    suspend fun reverseGeocode(lat: Double, lon: Double, limit: Int): List<GeoDirectItem>
}