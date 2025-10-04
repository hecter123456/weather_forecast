package com.example.weatherforecast.core.model

data class TodayForecast(
    val cityName: String,
    val dateEpochSeconds: Long,
    val temperatureC: Double,
    val condition: String,
    val precipitationChance: Long,
    val windKph: Double,
)
