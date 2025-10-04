package com.example.weatherforecast.core.network.datasource

import com.example.weatherforecast.core.model.City
import com.example.weatherforecast.core.network.model.OneCallResponse

interface WeatherNetworkDataSource {
    suspend fun fetchOneCall(city: City): OneCallResponse
}