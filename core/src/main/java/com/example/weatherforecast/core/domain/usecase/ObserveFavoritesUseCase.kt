package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.FavoriteCity
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase(private val repository: WeatherRepository) {
    operator fun invoke(): Flow<List<FavoriteCity>> = repository.observeFavorites()
}
