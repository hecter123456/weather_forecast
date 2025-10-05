package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.DailyForecast
import com.example.weatherforecast.core.model.SearchCity
import javax.inject.Inject

class GetDailyForecastUseCase @Inject constructor(private val repo: WeatherRepository) {
    suspend operator fun invoke(city: SearchCity): List<DailyForecast?> {
        val cities = repo.getCities()
        return repo.getDailyWeather(city)
    }
}