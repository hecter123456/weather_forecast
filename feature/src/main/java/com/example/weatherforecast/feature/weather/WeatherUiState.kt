package com.example.weatherforecast.feature.weather

import com.example.weatherforecast.core.model.DailyForecast
import com.example.weatherforecast.core.model.TodayForecast
import com.example.weatherforecast.core.network.model.OneCallResponse

sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object Loading : WeatherUiState
    data class Today(val data: TodayForecast) : WeatherUiState
    data class Week(val data: List<DailyForecast?>) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}