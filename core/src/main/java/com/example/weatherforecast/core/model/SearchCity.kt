package com.example.weatherforecast.core.model

data class SearchCity(
    val name: String,
    val country: String?,
    val state: String?,
    val lat: Double,
    val lon: Double,
)
