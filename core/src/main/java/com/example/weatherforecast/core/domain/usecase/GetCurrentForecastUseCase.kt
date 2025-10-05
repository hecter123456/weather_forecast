package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.SearchCity
import com.example.weatherforecast.core.model.TodayForecast
import javax.inject.Inject

class GetCurrentForecastUseCase @Inject constructor(private val repo: WeatherRepository) {
    suspend operator fun invoke(city: SearchCity): TodayForecast {
        return repo.getCurrentWeather(city)
    }
}