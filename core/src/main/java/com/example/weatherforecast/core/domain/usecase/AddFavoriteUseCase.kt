package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.SearchCity
import javax.inject.Inject


class AddFavoriteUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(
        item: SearchCity,
        alias: String? = null,
        note: String? = null
    ): Long =
        repository.addFavorite(item, alias, note)
}
