package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.DailyForecast
import javax.inject.Inject

class GetDailyForecastUseCase @Inject constructor(private val repo: WeatherRepository) {
    suspend operator fun invoke(cityId: String): List<DailyForecast?> {
        val cities = repo.getCities()
        return cities.firstOrNull { it.id == cityId }?.let {
            repo.getDailyWeather(it)
        }?:  error("Unknown cityId: $cityId")
    }
}