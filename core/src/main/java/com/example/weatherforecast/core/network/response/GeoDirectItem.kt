package com.example.weatherforecast.core.network.response

import kotlinx.serialization.Serializable

@Serializable
data class GeoDirectItem(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String? = null,
    val state: String? = null
)
