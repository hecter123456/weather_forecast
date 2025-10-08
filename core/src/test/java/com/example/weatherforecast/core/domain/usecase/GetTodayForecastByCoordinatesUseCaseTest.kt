package com.example.weatherforecast.core.domain.usecase // ← change to your actual package

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.SearchCity
import com.example.weatherforecast.core.model.TodayForecast
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetTodayForecastByCoordinatesUseCaseTest {

    private val repository: WeatherRepository = mock()
    private val useCase = GetTodayForecastByCoordinatesUseCase(repository)

    private fun todayForecast(
        name: String = "Any",
        temp: Double = 30.0
    ) = TodayForecast(
        cityName = name,
        dateEpochSeconds = 1_000L,
        temperatureC = temp,
        condition = "Clear",
        precipitationChance = 0,
        windKph = 10.0
    )

    @Test
    fun `uses reverseGeocode name when available`() = runTest {
        val lat = 25.033
        val lon = 121.5654
        val geocoded = listOf(
            SearchCity(
                name = "Taipei",
                country = "TW",
                state = null,
                lat = lat,
                lon = lon
            )
        )
        val expected = todayForecast(name = "Taipei")

        // reverseGeocode might have (lat, lon, limit) with default param; stub the 3-arity form.
        whenever(repository.reverseGeocode(eq(lat), eq(lon), any())).thenReturn(geocoded)
        whenever(repository.getCurrentWeather(any())).thenReturn(expected)

        val result = useCase(lat, lon)

        assertEquals(expected, result)

        // Verify SearchCity passed to getCurrentWeather has the geocoded name and correct coords
        val captor = argumentCaptor<SearchCity>()
        verify(repository).getCurrentWeather(captor.capture())
        val passed = captor.firstValue
        assertEquals("Taipei", passed.name)
        assertEquals(lat, passed.lat, 0.0)
        assertEquals(lon, passed.lon, 0.0)
    }

    @Test
    fun `defaults name to Unknown when reverseGeocode returns empty`() = runTest {
        val lat = 35.0
        val lon = 139.0
        val expected = todayForecast(name = "Unknown")

        whenever(repository.reverseGeocode(eq(lat), eq(lon), any())).thenReturn(emptyList())
        whenever(repository.getCurrentWeather(any())).thenReturn(expected)

        val result = useCase(lat, lon)

        assertEquals(expected, result)

        val captor = argumentCaptor<SearchCity>()
        verify(repository).getCurrentWeather(captor.capture())
        val passed = captor.firstValue
        assertEquals("Unknown", passed.name)
        assertEquals(lat, passed.lat, 0.0)
        assertEquals(lon, passed.lon, 0.0)
    }

    @Test
    fun `defaults name to Unknown when reverseGeocode throws`() = runTest {
        val lat = 1.23
        val lon = 4.56
        val expected = todayForecast(name = "Unknown")

        whenever(
            repository.reverseGeocode(
                eq(lat),
                eq(lon),
                any()
            )
        ).thenThrow(RuntimeException("down"))
        whenever(repository.getCurrentWeather(any())).thenReturn(expected)

        val result = useCase(lat, lon)

        assertEquals(expected, result)

        val captor = argumentCaptor<SearchCity>()
        verify(repository).getCurrentWeather(captor.capture())
        val passed = captor.firstValue
        assertEquals("Unknown", passed.name)
        assertEquals(lat, passed.lat, 0.0)
        assertEquals(lon, passed.lon, 0.0)
    }
}
