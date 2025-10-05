package com.example.weatherforecast.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OneCallResponse (
    val lat: Double,
    val lon: Double,
    val timezone: String,

    @SerialName("timezone_offset")
    val timezoneOffset: Long,

    val current: Current,
    val daily: List<Daily>
)

@Serializable
data class Current (
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val temp: Double,

    @SerialName("feels_like")
    val feelsLike: Double,

    val pressure: Long,
    val humidity: Long,

    @SerialName("dew_point")
    val dewPoint: Double,

    val uvi: Double,
    val clouds: Long,
    val visibility: Long,

    @SerialName("wind_speed")
    val windSpeed: Double,

    @SerialName("wind_deg")
    val windDeg: Long,

    @SerialName("wind_gust")
    val windGust: Double,

    val weather: List<Weather>
)

@Serializable
data class Weather (
    val id: Long,
    val main: String,
    val description: String,
    val icon: String
)

@Serializable
data class Daily (
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val moonrise: Long,
    val moonset: Long,

    @SerialName("moon_phase")
    val moonPhase: Double,

    val summary: String,
    val temp: Temp,

    @SerialName("feels_like")
    val feelsLike: FeelsLike,

    val pressure: Long,
    val humidity: Long,

    @SerialName("dew_point")
    val dewPoint: Double,

    @SerialName("wind_speed")
    val windSpeed: Double,

    @SerialName("wind_deg")
    val windDeg: Long,

    @SerialName("wind_gust")
    val windGust: Double,

    val weather: List<Weather>,
    val clouds: Long,
    val pop: Double,
    val uvi: Double,
    val rain: Double? = null
)

@Serializable
data class FeelsLike (
    val day: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

@Serializable
data class Temp (
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)