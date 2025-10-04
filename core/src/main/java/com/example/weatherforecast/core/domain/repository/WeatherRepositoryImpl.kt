package com.example.weatherforecast.core.domain.repository

import com.example.weatherforecast.core.model.City
import com.example.weatherforecast.core.model.DailyForecast
import com.example.weatherforecast.core.model.LocalData
import com.example.weatherforecast.core.model.TodayForecast
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

    override suspend fun getCurrentWeather(city: City): TodayForecast {
        return datasource.fetchOneCall(city = city).current.let {
            TodayForecast(
                cityName = city.name,
                dateEpochSeconds = it.dt,
                temperatureC = it.temp,
                condition = it.weather.first().description,
                windKph = it.windSpeed,
                precipitationChance = it.pressure,
            )
        }
    }

    override suspend fun getDailyWeather(city: City): List<DailyForecast?> {
        return datasource.fetchOneCall(city = city).daily.map {
            it?.let {
                DailyForecast(
                    dateEpochSeconds = it.dt,
                    minTempC = it.temp.min,
                    maxTempC = it.temp.max,
                    condition = it.weather.first().description,
                )
            }
        }
    }

}
