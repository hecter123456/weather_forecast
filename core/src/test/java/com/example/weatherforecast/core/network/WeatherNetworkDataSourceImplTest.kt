package com.example.weatherforecast.core.network

import com.example.weatherforecast.core.network.datasource.WeatherNetworkDataSourceImpl
import com.example.weatherforecast.core.network.request.OneCallRequest
import com.example.weatherforecast.core.network.response.Current
import com.example.weatherforecast.core.network.response.Daily
import com.example.weatherforecast.core.network.response.FeelsLike
import com.example.weatherforecast.core.network.response.GeoDirectItem
import com.example.weatherforecast.core.network.response.OneCallResponse
import com.example.weatherforecast.core.network.response.Temp
import com.example.weatherforecast.core.network.response.Weather
import com.example.weatherforecast.core.network.retrofit.OpenWeatherApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class WeatherNetworkDataSourceImplTest {

    private val api: OpenWeatherApi = mock()
    private val apiKey = "test-key-123"
    private val dataSource = WeatherNetworkDataSourceImpl(api = api, apiKey = apiKey)

    // --- Helpers to build minimal, valid One Call payloads for assertions ---
    private fun oneCallResponse(
        tempC: Double = 26.0,
        windMs: Double = 3.0,
        condition: String = "Clouds",
        days: Int = 7
    ): OneCallResponse {
        val weather = listOf(
            Weather(
                id = 800,
                main = condition,
                description = "desc",
                icon = "01d"
            )
        )
        val current = Current(
            dt = 1_000L, sunrise = 900L, sunset = 1_800L,
            temp = tempC, feelsLike = tempC - 1,
            pressure = 1013L, humidity = 60L, dewPoint = 18.0,
            uvi = 5.0, clouds = 30L, visibility = 10_000L,
            windSpeed = windMs, windDeg = 180L, windGust = windMs + 1,
            weather = weather
        )
        val daily = (0 until days).map { i ->
            Daily(
                dt = 2_000L + i,
                sunrise = 1_800L + i,
                sunset = 2_500L + i,
                moonrise = 1_600L + i,
                moonset = 2_700L + i,
                moonPhase = 0.5,
                summary = "day $i",
                temp = Temp(
                    day = 26.0,
                    min = 20.0,
                    max = 30.0,
                    night = 22.0,
                    eve = 25.0,
                    morn = 21.0
                ),
                feelsLike = FeelsLike(day = 26.0, night = 22.0, eve = 25.0, morn = 21.0),
                pressure = 1010L,
                humidity = 60L,
                dewPoint = 18.0,
                windSpeed = 3.0,
                windDeg = 90L,
                windGust = 4.0,
                weather = weather,
                clouds = 30L,
                pop = 0.1,
                uvi = 5.0,
                rain = null
            )
        }
        return OneCallResponse(
            lat = 25.0, lon = 121.5, timezone = "Asia/Taipei", timezoneOffset = 8 * 3600L,
            current = current, daily = daily
        )
    }

    // ------------------ fetchOneCall ------------------

    @Test
    fun `fetchOneCall forwards parameters and returns API result`() = runTest {
        val req = OneCallRequest(
            lat = 25.033, lon = 121.5654,
            units = "metric",
            exclude = "minutely,alerts"
        )
        val apiResult = oneCallResponse()

        whenever(
            api.oneCall(
                lat = req.lat,
                lon = req.lon,
                apiKey = apiKey,
                units = req.units,
                exclude = req.exclude
            )
        ).thenReturn(apiResult)

        val result = dataSource.fetchOneCall(req)

        assertEquals(apiResult, result)
        verify(api).oneCall(
            lat = req.lat, lon = req.lon,
            apiKey = apiKey, units = req.units, exclude = req.exclude
        )
    }

    @Test
    fun `fetchOneCall propagates API exception`() = runTest {
        val req = OneCallRequest(
            lat = 1.0,
            lon = 2.0,
            units = "metric",
            exclude = "minutely,hourly,alerts"
        )
        whenever(api.oneCall(any(), any(), any(), any(), any())).thenThrow(RuntimeException("boom"))

        var thrown: RuntimeException? = null
        try {
            dataSource.fetchOneCall(req)
        } catch (e: RuntimeException) {
            thrown = e
        }
        assertNotNull(thrown)
        assertEquals("boom", thrown!!.message)
    }

    // ------------------ geocodeDirect ------------------

    @Test
    fun `geocodeDirect forwards parameters and returns API result`() = runTest {
        val list = listOf(
            GeoDirectItem(
                name = "Taipei",
                lat = 25.033,
                lon = 121.5654,
                country = "TW",
                state = null
            ),
            GeoDirectItem(name = "Tainan", lat = 22.99, lon = 120.21, country = "TW", state = null)
        )
        whenever(api.geocodeDirect(query = "tai", limit = 3, apiKey = apiKey)).thenReturn(list)

        val result = dataSource.geocodeDirect(query = "tai", limit = 3)

        assertEquals(list, result)
        verify(api).geocodeDirect(query = "tai", limit = 3, apiKey = apiKey)
    }

    @Test
    fun `geocodeDirect propagates API exception`() = runTest {
        whenever(api.geocodeDirect(query = any(), limit = any(), apiKey = any()))
            .thenThrow(IllegalStateException("down"))

        var thrown: IllegalStateException? = null
        try {
            dataSource.geocodeDirect(query = "a", limit = 1)
        } catch (e: IllegalStateException) {
            thrown = e
        }
        assertNotNull(thrown)
        assertEquals("down", thrown!!.message)
    }

    // ------------------ reverseGeocode ------------------

    @Test
    fun `reverseGeocode forwards parameters and returns API result`() = runTest {
        val list = listOf(
            GeoDirectItem(
                name = "Taipei",
                lat = 25.0,
                lon = 121.0,
                country = "TW",
                state = "Taipei"
            )
        )
        whenever(api.reverseGeocode(lat = 25.0, lon = 121.0, limit = 1, apiKey = apiKey))
            .thenReturn(list)

        val result = dataSource.reverseGeocode(lat = 25.0, lon = 121.0, limit = 1)

        assertEquals(list, result)
        verify(api).reverseGeocode(lat = 25.0, lon = 121.0, limit = 1, apiKey = apiKey)
    }

    @Test
    fun `reverseGeocode propagates API exception`() = runTest {
        whenever(api.reverseGeocode(lat = any(), lon = any(), limit = any(), apiKey = any()))
            .thenThrow(RuntimeException("oops"))

        var thrown: RuntimeException? = null
        try {
            dataSource.reverseGeocode(0.0, 0.0, 1)
        } catch (e: RuntimeException) {
            thrown = e
        }
        assertNotNull(thrown)
        assertEquals("oops", thrown!!.message)
    }
}
