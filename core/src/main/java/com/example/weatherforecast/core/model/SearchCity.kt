package com.example.weatherforecast.core.model

data class SearchCity(
    val name: String,
    val country: String? = null,
    val state: String? = null,
    val lat: Double,
    val lon: Double,
)
