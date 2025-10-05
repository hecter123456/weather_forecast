package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository

class RemoveFavoriteUseCase(private val repository: WeatherRepository) {
    suspend operator fun invoke(id: Long) = repository.removeFavorite(id)
}
