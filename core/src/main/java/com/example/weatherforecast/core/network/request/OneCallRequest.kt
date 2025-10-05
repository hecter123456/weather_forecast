package com.example.weatherforecast.core.network.request

data class OneCallRequest(
    val lat: Double,
    val lon: Double,
    val units: String = "metric",
    val exclude: String = "minutely,hourly,alerts",
)
