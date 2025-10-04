package com.example.weatherforecast.core.network.datasource

import com.example.weatherforecast.core.model.City
import com.example.weatherforecast.core.network.model.OneCallResponse
import com.example.weatherforecast.core.network.retrofit.OpenWeatherApi
import javax.inject.Inject

class WeatherNetworkDataSourceImpl @Inject constructor(
    private val api: OpenWeatherApi,
    private val apiKey: String
): WeatherNetworkDataSource {

    override suspend fun fetchOneCall(city: City): OneCallResponse = api.oneCall(lat = city.lat, lon = city.lon, apiKey = apiKey)
}
