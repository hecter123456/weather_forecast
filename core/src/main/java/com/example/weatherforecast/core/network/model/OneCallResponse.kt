package com.example.weatherforecast.core.network.model

data class OneCallResponse(
    var alerts: List<Alert?>,
    var current: Current,
    var daily: List<Daily?>,
    var hourly: List<Hourly?>,
    var lat: Double,
    var lon: Double,
    var minutely: List<Minutely?>,
    var timezone: String,
    var timezone_offset: Int
) {
    data class Alert(
        var description: String,
        var end: Int,
        var event: String,
        var sender_name: String,
        var start: Int,
        var tags: List<Any?>
    )

    data class Current(
        var clouds: Int,
        var dew_point: Double,
        var dt: Int,
        var feels_like: Double,
        var humidity: Int,
        var pressure: Int,
        var sunrise: Int,
        var sunset: Int,
        var temp: Double,
        var uvi: Double,
        var visibility: Int,
        var weather: List<Weather>,
        var wind_deg: Int,
        var wind_gust: Double,
        var wind_speed: Double
    ) {
        data class Weather(
            var description: String,
            var icon: String,
            var id: Int,
            var main: String
        )
    }

    data class Daily(
        var clouds: Int,
        var dew_point: Double,
        var dt: Int,
        var feels_like: FeelsLike,
        var humidity: Int,
        var moon_phase: Double,
        var moonrise: Int,
        var moonset: Int,
        var pop: Double,
        var pressure: Int,
        var rain: Double,
        var summary: String,
        var sunrise: Int,
        var sunset: Int,
        var temp: Temp,
        var uvi: Double,
        var weather: List<Weather>,
        var wind_deg: Int,
        var wind_gust: Double,
        var wind_speed: Double
    ) {
        data class FeelsLike(
            var day: Double,
            var eve: Double,
            var morn: Double,
            var night: Double
        )

        data class Temp(
            var day: Double,
            var eve: Double,
            var max: Double,
            var min: Double,
            var morn: Double,
            var night: Double
        )

        data class Weather(
            var description: String,
            var icon: String,
            var id: Int,
            var main: String
        )
    }

    data class Hourly(
        var clouds: Int,
        var dew_point: Double,
        var dt: Int,
        var feels_like: Double,
        var humidity: Int,
        var pop: Double,
        var pressure: Int,
        var temp: Double,
        var uvi: Int,
        var visibility: Int,
        var weather: List<Weather>,
        var wind_deg: Int,
        var wind_gust: Double,
        var wind_speed: Double
    ) {
        data class Weather(
            var description: String,
            var icon: String,
            var id: Int,
            var main: String
        )
    }

    data class Minutely(
        var dt: Int,
        var precipitation: Int
    )
}