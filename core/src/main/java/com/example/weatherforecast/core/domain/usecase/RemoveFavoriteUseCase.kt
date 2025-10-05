package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import javax.inject.Inject

class RemoveFavoriteUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(id: Long) = repository.removeFavorite(id)
}
