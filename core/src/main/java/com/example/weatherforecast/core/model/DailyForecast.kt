package com.example.weatherforecast.core.model

data class DailyForecast(
    val dateEpochSeconds: Long,
    val minTempC: Double,
    val maxTempC: Double,
    val condition: String,
)
