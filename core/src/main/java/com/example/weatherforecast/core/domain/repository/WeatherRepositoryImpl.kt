package com.example.weatherforecast.core.domain.repository

import com.example.weatherforecast.core.model.City
import com.example.weatherforecast.core.model.LocalData
import com.example.weatherforecast.core.network.datasource.WeatherNetworkDataSource
import com.example.weatherforecast.core.network.datasource.WeatherNetworkDataSourceImpl
import com.example.weatherforecast.core.network.model.OneCallResponse
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val datasource: WeatherNetworkDataSource
) : WeatherRepository {
    override suspend fun getCities(): List<City> {
        return LocalData.cities
    }

    override suspend fun getCurrentWeather(city: City): OneCallResponse {
        return datasource.fetchOneCall(city = city)
    }

}
