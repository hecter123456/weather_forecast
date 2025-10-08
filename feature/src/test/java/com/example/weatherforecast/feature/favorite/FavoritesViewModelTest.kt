package com.example.weatherforecast.feature.favorite

import app.cash.turbine.test
import com.example.weatherforecast.core.domain.usecase.ObserveFavoritesUseCase
import com.example.weatherforecast.core.domain.usecase.RemoveFavoriteUseCase
import com.example.weatherforecast.core.domain.usecase.SaveSelectedFavoriteCityUseCase
import com.example.weatherforecast.core.domain.usecase.UpdateFavoriteUseCase
import com.example.weatherforecast.core.model.FavoriteCity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeFavorites: ObserveFavoritesUseCase = mock()
    private val removeFavorite: RemoveFavoriteUseCase = mock()
    private val updateFavorite: UpdateFavoriteUseCase = mock()
    private val saveSelectedFavorite: SaveSelectedFavoriteCityUseCase = mock()

    private fun vm(
        upstream: MutableSharedFlow<List<FavoriteCity>> = MutableSharedFlow(replay = 1)
    ): Pair<FavoritesViewModel, MutableSharedFlow<List<FavoriteCity>>> {
        whenever(observeFavorites.invoke()).thenReturn(upstream)
        val vm = FavoritesViewModel(
            observeFavorites = observeFavorites,
            removeFavoriteUseCase = removeFavorite,
            updateFavoriteUseCase = updateFavorite,
            saveSelectedCityUseCase = saveSelectedFavorite
        )
        return vm to upstream
    }

    @Test
    fun `favorites emits initial empty list then updates from use case`() = runTest {
        val (vm, upstream) = vm()


        vm.favorites.test {
            // Initial emission from stateIn initialValue = emptyList()
            assertEquals(emptyList<FavoriteCity>(), awaitItem())

            val a = FavoriteCity(
                id = 1,
                name = "Taipei",
                alias = "Home",
                note = null,
                country = "TW",
                state = null,
                lat = 25.033,
                lon = 121.5654
            )
            val b = FavoriteCity(
                id = 2,
                name = "Tokyo",
                alias = null,
                note = "Trip",
                country = "JP",
                state = null,
                lat = 35.0,
                lon = 139.0
            )

            // Emit from upstream after subscription
            upstream.emit(listOf(a, b))
            assertEquals(listOf(a, b), awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `delete calls RemoveFavoriteUseCase with id`() = runTest {
        val (vm, _) = vm()

        vm.delete(42L)
        advanceUntilIdle()

        verify(removeFavorite).invoke(42L)
    }

    @Test
    fun `update calls UpdateFavoriteUseCase with fields`() = runTest {
        val (vm, _) = vm()

        vm.update(id = 7L, alias = "Work", note = "Office")
        advanceUntilIdle()

        verify(updateFavorite).invoke(7L, "Work", "Office")
    }

    @Test
    fun `update accepts null alias and note`() = runTest {
        val (vm, _) = vm()

        vm.update(id = 8L, alias = null, note = null)
        advanceUntilIdle()

        verify(updateFavorite).invoke(8L, null, null)
    }

    @Test
    fun `saveSelectedCity calls SaveSelectedFavoriteCityUseCase`() = runTest {
        val (vm, _) = vm()
        val fav = FavoriteCity(
            id = 9L, name = "Tainan", alias = null, note = null,
            country = "TW", state = null, lat = 22.99, lon = 120.21
        )

        vm.saveSelectedCity(fav)
        advanceUntilIdle()

        verify(saveSelectedFavorite).invoke(fav)
    }
}

/** Sets a TestDispatcher as Main so viewModelScope launches are controlled in tests. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
