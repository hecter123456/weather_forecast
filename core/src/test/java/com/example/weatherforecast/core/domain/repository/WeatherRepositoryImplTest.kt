package com.example.weatherforecast.core.domain.repository

import app.cash.turbine.test
import com.example.weatherforecast.core.datastore.datasource.PreferencesDataSource
import com.example.weatherforecast.core.model.FavoriteCity
import com.example.weatherforecast.core.model.SearchCity
import com.example.weatherforecast.core.network.datasource.WeatherNetworkDataSource
import com.example.weatherforecast.core.network.request.OneCallRequest
import com.example.weatherforecast.core.network.response.Current
import com.example.weatherforecast.core.network.response.Daily
import com.example.weatherforecast.core.network.response.FeelsLike
import com.example.weatherforecast.core.network.response.GeoDirectItem
import com.example.weatherforecast.core.network.response.OneCallResponse
import com.example.weatherforecast.core.network.response.Temp
import com.example.weatherforecast.core.network.response.Weather
import com.example.weatherforecast.core.room.FavoriteCityDao
import com.example.weatherforecast.core.room.FavoriteCityEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class WeatherRepositoryImplTest {

    private val networkDataSource: WeatherNetworkDataSource = mock()
    private val favoriteCityDao: FavoriteCityDao = mock()

    // In-memory flow to simulate selected city persistence for tests.
    private val selectedCityFlow = MutableSharedFlow<SearchCity>(replay = 1)
    private val preferencesDataSource = object : PreferencesDataSource {
        override fun observeSelectedCity() = selectedCityFlow
        override suspend fun saveSelectedCity(city: SearchCity) {
            selectedCityFlow.emit(city)
        }
    }

    private val repository = WeatherRepositoryImpl(
        datasource = networkDataSource,
        favoriteCityDao = favoriteCityDao,
        preferencesDataSource = preferencesDataSource
    )

    private fun oneCall(
        tempC: Double = 26.5,
        windMs: Double = 3.0,
        condition: String = "Clouds",
        days: Int = 7
    ): OneCallResponse {
        val weatherList = listOf(
            Weather(
                id = 800L,
                main = condition,
                description = "test $condition",
                icon = "01d"
            )
        )

        val current = Current(
            dt = 1_000L,
            sunrise = 900L,
            sunset = 1_800L,
            temp = tempC,
            feelsLike = tempC - 1,
            pressure = 1013L,
            humidity = 70L,
            dewPoint = 18.0,
            uvi = 5.0,
            clouds = 40L,
            visibility = 10_000L,
            windSpeed = windMs,   // m/s
            windDeg = 180L,
            windGust = windMs + 1.0,
            weather = weatherList
        )

        val daily = (0 until days).map { i ->
            Daily(
                dt = 2_000L + i,
                sunrise = 1_800L + i,
                sunset = 2_500L + i,
                moonrise = 1_600L + i,
                moonset = 2_700L + i,
                moonPhase = 0.5,
                summary = "Day $i summary",
                temp = Temp(
                    day = 26.0 + i,
                    min = 20.0 + i,
                    max = 30.0 + i,
                    night = 22.0 + i,
                    eve = 25.0 + i,
                    morn = 21.0 + i
                ),
                feelsLike = FeelsLike(
                    day = 26.0 + i,
                    night = 22.0 + i,
                    eve = 25.0 + i,
                    morn = 21.0 + i
                ),
                pressure = 1010L + i,
                humidity = 60L + i,
                dewPoint = 18.0 + i,
                windSpeed = 3.0 + i,
                windDeg = 90L + i,
                windGust = 4.0 + i,
                weather = weatherList,
                clouds = 30L + i,
                pop = 0.1 * i,
                uvi = 5.0 + i,
                rain = if (i % 2 == 0) null else 1.2
            )
        }

        return OneCallResponse(
            lat = 25.0,
            lon = 121.5,
            timezone = "Asia/Taipei",
            timezoneOffset = 8 * 3600L,
            current = current,
            daily = daily
        )
    }

    // ---------- getCurrentWeather / getDailyWeather ----------

    @Test
    fun `getCurrentWeather maps fields from oneCall`() = runTest {
        val city = SearchCity("Taipei", "TW", null, 25.033, 121.5654)
        whenever(networkDataSource.fetchOneCall(OneCallRequest(city.lat, city.lon)))
            .thenReturn(oneCall(tempC = 30.0, windMs = 5.0, condition = "Clear"))

        val result = repository.getCurrentWeather(city)

        assertEquals("Taipei", result.cityName)
        assertEquals(30.0, result.temperatureC, 0.0001)
        assertEquals("Clear", result.condition)
        // 風速 m/s → kph
        assertEquals(18.0, result.windKph, 0.0001)
        verify(networkDataSource).fetchOneCall(OneCallRequest(lat = city.lat, lon = city.lon))
    }

    @Test
    fun `getDailyWeather maps to 7 days list`() = runTest {
        val city = SearchCity("Taipei", "TW", null, 25.033, 121.5654)
        whenever(networkDataSource.fetchOneCall(OneCallRequest(city.lat, city.lon)))
            .thenReturn(oneCall(days = 9)) // Repo 會 take(7)

        val days = repository.getDailyWeather(city)

        assertEquals(7, days.size)
        assertEquals("N/A" != days.first()?.condition, true) // 有 condition
        verify(networkDataSource).fetchOneCall(OneCallRequest(city.lat, city.lon))
    }

    // ---------- Geocoding ----------

    @Test
    fun `searchCities maps geocode results`() = runTest {
        whenever(networkDataSource.geocodeDirect("tai", 3)).thenReturn(
            listOf(
                GeoDirectItem(
                    name = "Taipei",
                    lat = 25.033,
                    lon = 121.5654,
                    country = "TW",
                    state = null
                ),
                GeoDirectItem(
                    name = "Tainan",
                    lat = 22.99,
                    lon = 120.21,
                    country = "TW",
                    state = null
                ),
            )
        )

        val list = repository.searchCities("tai", 3)

        assertEquals(2, list.size)
        assertEquals("Taipei", list[0].name)
        assertEquals(121.5654, list[0].lon, 0.0001)
        verify(networkDataSource).geocodeDirect("tai", 3)
    }

    @Test
    fun `reverseGeocode maps results`() = runTest {
        whenever(networkDataSource.reverseGeocode(25.0, 121.0, 1)).thenReturn(
            listOf(
                GeoDirectItem(
                    name = "Taipei",
                    lat = 25.0,
                    lon = 121.0,
                    country = "TW",
                    state = "Taipei"
                )
            )
        )

        val list = repository.reverseGeocode(25.0, 121.0, 1)

        assertEquals(1, list.size)
        assertEquals("Taipei", list[0].name)
        assertEquals("TW", list[0].country)
        verify(networkDataSource).reverseGeocode(25.0, 121.0, 1)
    }

    // ---------- Favorites CRUD + Observe ----------

    @Test
    fun `observeFavorites maps entities to domain`() = runTest {
        whenever(favoriteCityDao.observeAll()).thenReturn(
            flowOf(
                listOf(
                    FavoriteCityEntity(
                        id = 1, name = "Taipei", alias = "Home", note = null,
                        country = "TW", state = null, lat = 25.033, lon = 121.5654
                    ),
                    FavoriteCityEntity(
                        id = 2, name = "Tokyo", alias = null, note = "Trip",
                        country = "JP", state = null, lat = 35.0, lon = 139.0
                    )
                )
            )
        )

        repository.observeFavorites().test {
            val first = awaitItem()
            assertEquals(2, first.size)
            assertEquals("Home", first[0].alias)
            assertEquals("JP", first[1].country)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `addFavorite converts SearchCity to entity and returns id`() = runTest {
        val city = SearchCity("Taipei", "TW", null, 25.033, 121.5654)
        whenever(favoriteCityDao.insert(any())).thenReturn(99L)

        val id = repository.addFavorite(city, alias = "fav", note = "nice")

        assertEquals(99L, id)
        argumentCaptor<FavoriteCityEntity>().apply {
            verify(favoriteCityDao).insert(capture())
            assertEquals("Taipei", firstValue.name)
            assertEquals("fav", firstValue.alias)
            assertEquals(121.5654, firstValue.lon, 0.0001)
        }
    }

    @Test
    fun `removeFavorite by id calls dao`() = runTest {
        repository.removeFavorite(5L)
        verify(favoriteCityDao).deleteById(5L)
    }

    @Test
    fun `updateFavorite passes alias note and timestamp`() = runTest {
        repository.updateFavorite(7L, alias = "A", note = "N")
        verify(favoriteCityDao).updateFields(eq(7L), eq("A"), eq("N"), any())
    }

    @Test
    fun `observeIsFavorite emits booleans from count`() = runTest {
        val countFlow = MutableSharedFlow<Int>(replay = 1)
        whenever(favoriteCityDao.countByIdentity("Taipei", "TW", null)).thenReturn(countFlow)

        val city = SearchCity("Taipei", "TW", null, 25.0, 121.0)

        repository.observeIsFavorite(city).test {
            countFlow.emit(0)
            assertEquals(false, awaitItem())
            countFlow.emit(1)
            assertEquals(true, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `removeFavorite by identity calls dao`() = runTest {
        val city = SearchCity("Taipei", "TW", null, 25.033, 121.5654)
        repository.removeFavorite(city)
        verify(favoriteCityDao).deleteByIdentity("Taipei", "TW", null)
    }

    // ---------- Selected City (DataStore bind) ----------

    @Test
    fun `observeSelectedCity replays latest value from preferences`() = runTest {
        val taipei = SearchCity("Taipei", "TW", null, 25.033, 121.5654)
        selectedCityFlow.emit(taipei)

        repository.observeSelectedCity().test {
            assertEquals(taipei, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `saveSelectedCity emits value`() = runTest {
        val tokyo = SearchCity("Tokyo", "JP", null, 35.0, 139.0)
        repository.observeSelectedCity().test {
            repository.saveSelectedCity(tokyo)
            assertEquals(tokyo, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    // ---------- Mapping helpers ----------

    @Test
    fun `favoriteCityToSearchCity maps fields correctly`() {
        val favorite = FavoriteCity(
            id = 1, name = "Tainan", alias = null, note = null,
            country = "TW", state = null, lat = 22.99, lon = 120.21
        )
        val search = repository.favoriteCityToSearchCity(favorite)
        assertEquals("Tainan", search.name)
        assertEquals(120.21, search.lon, 0.0001)
    }

    // ---------- Exception propagation (network error) ----------

    @Test
    fun `getCurrentWeather propagates exception from network layer`() = runTest {
        val city = SearchCity("Taipei", "TW", null, 25.033, 121.5654)
        whenever(networkDataSource.fetchOneCall(any()))
            .thenThrow(RuntimeException("network down"))

        var thrown: RuntimeException? = null
        try {
            repository.getCurrentWeather(city)
        } catch (e: RuntimeException) {
            thrown = e
        }

        assertNotNull(thrown)
        assertEquals("network down", thrown?.message)
    }
}
