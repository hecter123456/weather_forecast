package com.example.weatherforecast.core.domain.usecase // ← change to your actual package

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.FavoriteCity
import com.example.weatherforecast.core.model.SearchCity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SaveSelectedFavoriteCityUseCaseTest {

    private val repository: WeatherRepository = mock()
    private val useCase = SaveSelectedFavoriteCityUseCase(repository)

    @Test
    fun `invokes conversion then saves selected city`() = runTest {
        val favorite = FavoriteCity(
            id = 1L,
            name = "Taipei",
            alias = "Home",
            note = null,
            country = "TW",
            state = null,
            lat = 25.033,
            lon = 121.5654
        )
        val converted = SearchCity(
            name = "Taipei",
            country = "TW",
            state = null,
            lat = 25.033,
            lon = 121.5654
        )

        whenever(repository.favoriteCityToSearchCity(favorite)).thenReturn(converted)
        // saveSelectedCity is suspend; no need to stub if it returns Unit

        useCase(favorite)

        val order = inOrder(repository)
        order.verify(repository).favoriteCityToSearchCity(favorite)
        order.verify(repository).saveSelectedCity(converted)
        order.verifyNoMoreInteractions()
    }

    @Test
    fun `propagates exception when conversion fails and does not call save`() = runTest {
        val favorite = FavoriteCity(
            id = 2L,
            name = "Nowhere",
            alias = null,
            note = null,
            country = null,
            state = null,
            lat = 0.0,
            lon = 0.0
        )

        whenever(repository.favoriteCityToSearchCity(favorite))
            .thenThrow(IllegalStateException("conversion failed"))

        var thrown: IllegalStateException? = null
        try {
            useCase(favorite)
        } catch (e: IllegalStateException) {
            thrown = e
        }

        assertNotNull(thrown)
        assertEquals("conversion failed", thrown!!.message)
        verify(repository, never()).saveSelectedCity(any())
    }

    @Test
    fun `propagates exception when saveSelectedCity fails`() = runTest {
        val favorite = FavoriteCity(
            id = 3L,
            name = "Tokyo",
            alias = null,
            note = null,
            country = "JP",
            state = null,
            lat = 35.0,
            lon = 139.0
        )
        val converted = SearchCity(
            name = "Tokyo",
            country = "JP",
            state = null,
            lat = 35.0,
            lon = 139.0
        )

        whenever(repository.favoriteCityToSearchCity(favorite)).thenReturn(converted)
        whenever(repository.saveSelectedCity(converted))
            .thenThrow(RuntimeException("datastore down"))

        var thrown: RuntimeException? = null
        try {
            useCase(favorite)
        } catch (e: RuntimeException) {
            thrown = e
        }

        assertNotNull(thrown)
        assertEquals("datastore down", thrown!!.message)

        val order = inOrder(repository)
        order.verify(repository).favoriteCityToSearchCity(favorite)
        order.verify(repository).saveSelectedCity(converted)
    }
}
