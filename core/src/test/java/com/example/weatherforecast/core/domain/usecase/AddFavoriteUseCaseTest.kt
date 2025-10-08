package com.example.weatherforecast.core.domain.usecase

import com.example.weatherforecast.core.domain.repository.WeatherRepository
import com.example.weatherforecast.core.model.SearchCity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AddFavoriteUseCaseTest {

    @Test
    fun `invoke forwards to repository with alias and note`() = runTest {
        // Arrange
        val repo = mock<WeatherRepository>()
        val useCase = AddFavoriteUseCase(repo)
        val city =
            SearchCity(name = "Taipei", country = "TW", state = null, lat = 25.0330, lon = 121.5654)

        whenever(repo.addFavorite(city, "home", "nice weather")).thenReturn(7L)

        // Act
        val result = useCase(item = city, alias = "home", note = "nice weather")

        // Assert
        assertEquals(7L, result)
        verify(repo).addFavorite(city, "home", "nice weather")
    }

    @Test
    fun `invoke uses default nulls when alias and note not provided`() = runTest {
        // Arrange
        val repo = mock<WeatherRepository>()
        val useCase = AddFavoriteUseCase(repo)
        val city = SearchCity(name = "Tokyo", country = "JP", state = null, lat = 35.0, lon = 139.0)

        whenever(repo.addFavorite(city, null, null)).thenReturn(42L)

        // Act
        val result = useCase(item = city)

        // Assert
        assertEquals(42L, result)
        verify(repo).addFavorite(city, null, null)
    }
}
