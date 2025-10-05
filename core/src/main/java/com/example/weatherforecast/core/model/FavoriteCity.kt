package com.example.weatherforecast.core.model

data class FavoriteCity(
    val id: Long,
    val name: String,
    val alias: String?,
    val note: String?,
    val country: String?,
    val state: String?,
    val lat: Double,
    val lon: Double,
)