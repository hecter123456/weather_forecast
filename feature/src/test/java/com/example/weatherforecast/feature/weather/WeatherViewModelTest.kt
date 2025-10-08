package com.example.weatherforecast.feature.weather

import app.cash.turbine.test
import com.example.weatherforecast.core.domain.usecase.AddFavoriteUseCase
import com.example.weatherforecast.core.domain.usecase.GetCurrentForecastUseCase
import com.example.weatherforecast.core.domain.usecase.GetDailyForecastUseCase
import com.example.weatherforecast.core.domain.usecase.ObserveIsFavoriteByIdentityUseCase
import com.example.weatherforecast.core.domain.usecase.ObserveSelectedCityUseCase
import com.example.weatherforecast.core.domain.usecase.RemoveFavoriteByIdentityUseCase
import com.example.weatherforecast.core.model.DailyForecast
import com.example.weatherforecast.core.model.LocalData
import com.example.weatherforecast.core.model.SearchCity
import com.example.weatherforecast.core.model.TodayForecast
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mocks for use cases
    private val getToday: GetCurrentForecastUseCase = mock()
    private val getWeek: GetDailyForecastUseCase = mock()
    private val addFavorite: AddFavoriteUseCase = mock()
    private val observeIsFavoriteByIdentity: ObserveIsFavoriteByIdentityUseCase = mock()
    private val removeFavoriteByIdentity: RemoveFavoriteByIdentityUseCase = mock()
    private val observeSelectedCityUseCase: ObserveSelectedCityUseCase = mock()

    private fun createViewModel(
        selectedCityFlow: MutableSharedFlow<SearchCity> = MutableSharedFlow(replay = 1),
    ): WeatherViewModel {
        // default city emitted for initial stateIn
        selectedCityFlow.tryEmit(LocalData.DefaultCity)
        whenever(observeSelectedCityUseCase.invoke()).thenReturn(selectedCityFlow)
        return WeatherViewModel(
            getToday = getToday,
            getWeek = getWeek,
            addFavorite = addFavorite,
            observeIsFavoriteByIdentity = observeIsFavoriteByIdentity,
            removeFavoriteByIdentity = removeFavoriteByIdentity,
            observeSelectedCityUseCase = observeSelectedCityUseCase
        )
    }

    @Test
    fun `city has DefaultCity initially`() = runTest {
        val vm = createViewModel()

        vm.city.test {
            assertEquals(LocalData.DefaultCity, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `setTabIndex updates state`() = runTest {
        val vm = createViewModel()
        vm.tabIndex.test {
            // initial 0
            assertEquals(0, awaitItem())
            vm.setTabIndex(1)
            assertEquals(1, awaitItem())
            vm.setTabIndex(2)
            assertEquals(2, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `startObservingFavoriteByIdentity reflects favorite changes`() = runTest {
        val vm = createViewModel()
        val identity = LocalData.DefaultCity

        val favFlow = MutableSharedFlow<Boolean>(replay = 1)
        whenever(observeIsFavoriteByIdentity.invoke(identity)).thenReturn(favFlow)

        vm.isFavorite.test {
            // initial false
            assertEquals(false, awaitItem())

            vm.startObservingFavoriteByIdentity()

            favFlow.tryEmit(true)
            assertEquals(true, awaitItem())

            favFlow.tryEmit(false)
            assertEquals(false, awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite adds when not favorite`() = runTest {
        val vm = createViewModel()
        val identity = LocalData.DefaultCity

        // Not observing fav -> remains false by default, so toggle should add
        vm.toggleFavorite()
        advanceUntilIdle()

        verify(addFavorite, times(1)).invoke(identity)
        verify(removeFavoriteByIdentity, never()).invoke(any())
    }

    @Test
    fun `toggleFavorite removes when already favorite`() = runTest {
        val vm = createViewModel()
        val identity = LocalData.DefaultCity

        // Wire favorite observer so we can flip it to true
        val favFlow = MutableSharedFlow<Boolean>(replay = 1)
        whenever(observeIsFavoriteByIdentity.invoke(identity)).thenReturn(favFlow)
        vm.startObservingFavoriteByIdentity()

        favFlow.tryEmit(true) // set as favorite
        advanceUntilIdle()

        vm.toggleFavorite()
        advanceUntilIdle()

        verify(removeFavoriteByIdentity, times(1)).invoke(identity)
        verify(addFavorite, never()).invoke(identity)
    }

    @Test
    fun `loadToday emits Loading then Today on success`() = runTest {
        val vm = createViewModel()
        val identity = LocalData.DefaultCity
        val today = TodayForecast(
            cityName = identity.name,
            dateEpochSeconds = 1_000L,
            temperatureC = 28.0,
            condition = "Clear",
            precipitationChance = 0,
            windKph = 10.0
        )
        whenever(getToday.invoke(identity)).thenReturn(today)

        vm.uiState.test {
            // initial Idle
            assertTrue(awaitItem() is WeatherUiState.Idle)

            vm.loadToday()
            // Loading
            assertTrue(awaitItem() is WeatherUiState.Loading)
            // Today
            val s = awaitItem()
            assertTrue(s is WeatherUiState.Today)
            s as WeatherUiState.Today
            assertEquals(today, s.data)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `loadToday emits Loading then Error on failure`() = runTest {
        val vm = createViewModel()
        val identity = LocalData.DefaultCity
        whenever(getToday.invoke(identity)).thenThrow(RuntimeException("boom"))

        vm.uiState.test {
            assertTrue(awaitItem() is WeatherUiState.Idle)

            vm.loadToday()
            assertTrue(awaitItem() is WeatherUiState.Loading)
            val s = awaitItem()
            assertTrue(s is WeatherUiState.Error)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `loadWeek emits Loading then Week on success`() = runTest {
        val vm = createViewModel()
        val identity = LocalData.DefaultCity
        val week = listOf(
            DailyForecast(
                dateEpochSeconds = 1L, minTempC = 20.0, maxTempC = 30.0, condition = "Clouds"
            )
        )
        whenever(getWeek.invoke(identity)).thenReturn(week)

        vm.uiState.test {
            assertTrue(awaitItem() is WeatherUiState.Idle)

            vm.loadWeek()
            assertTrue(awaitItem() is WeatherUiState.Loading)
            val s = awaitItem()
            assertTrue(s is WeatherUiState.Week)
            s as WeatherUiState.Week
            assertEquals(week, s.data)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `loadWeek emits Loading then Error on failure`() = runTest {
        val vm = createViewModel()
        val identity = LocalData.DefaultCity
        whenever(getWeek.invoke(identity)).thenThrow(IllegalStateException("down"))

        vm.uiState.test {
            assertTrue(awaitItem() is WeatherUiState.Idle)

            vm.loadWeek()
            assertTrue(awaitItem() is WeatherUiState.Loading)
            val s = awaitItem()
            assertTrue(s is WeatherUiState.Error)

            cancelAndConsumeRemainingEvents()
        }
    }
}

/** JUnit Rule to set TestDispatcher as Main for viewModelScope launches. */
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
