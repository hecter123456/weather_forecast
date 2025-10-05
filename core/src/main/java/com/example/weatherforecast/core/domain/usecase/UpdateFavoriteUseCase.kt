package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import javax.inject.Inject

class UpdateFavoriteUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(id: Long, alias: String?, note: String?) =
        repository.updateFavorite(id, alias, note)
}
