package com.example.weatherforecast.feature.searchCity

import app.cash.turbine.test
import com.example.weatherforecast.core.domain.usecase.SaveSelectedCityUseCase
import com.example.weatherforecast.core.domain.usecase.SearchCitiesUseCase
import com.example.weatherforecast.core.model.SearchCity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SearchCityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchCitiesUseCase: SearchCitiesUseCase = mock()
    private val saveSelectedCityUseCase: SaveSelectedCityUseCase = mock()

    private fun vm() = SearchCityViewModel(
        searchCitiesUseCase = searchCitiesUseCase,
        saveSelectedCityUseCase = saveSelectedCityUseCase
    )

    @Test
    fun `initial state is empty query and empty results`() = runTest {
        val vm = vm()
        assertEquals("", vm.query.value)
        assertTrue(vm.results.value.isEmpty())

        // (Optional) Flow assertion for initial emission
        vm.results.test {
            assertTrue(awaitItem().isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `onQueryChange with length less than 2 does not search`() = runTest {
        val vm = vm()

        vm.onQueryChange("T")
        advanceTimeBy(400)  // pass debounce
        advanceUntilIdle()

        verify(searchCitiesUseCase, never()).invoke(any(), any())
        assertTrue(vm.results.value.isEmpty())
    }

    @Test
    fun `debounced search success updates results`() = runTest {
        val vm = vm()
        val city1 = SearchCity("Taipei", "TW", null, 25.033, 121.5654)
        val city2 = SearchCity("Tainan", "TW", null, 22.99, 120.21)

        whenever(searchCitiesUseCase.invoke(eq("Ta"), eq(10)))
            .thenReturn(listOf(city1, city2))

        vm.results.test {
            // initial []
            assertTrue(awaitItem().isEmpty())

            vm.onQueryChange("Ta")
            // before 350ms -> no search executed
            advanceTimeBy(349)
            // nothing emitted yet

            // hit debounce time
            advanceTimeBy(1)
            advanceUntilIdle()

            val list = awaitItem()
            assertEquals(listOf(city1, city2), list)

            verify(searchCitiesUseCase, times(1)).invoke("Ta", 10)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `search failure keeps results empty`() = runTest {
        val vm = vm()

        whenever(searchCitiesUseCase.invoke(any(), any()))
            .thenThrow(RuntimeException("down"))

        // subscribe first to capture emissions if any
        vm.results.test {
            assertTrue(awaitItem().isEmpty())

            vm.onQueryChange("Ta")
            advanceTimeBy(350)
            advanceUntilIdle()

            // results set to empty again -> may or may not emit depending on same-value optimization;
            // so assert state directly and verify use case was called
            assertTrue(vm.results.value.isEmpty())
            verify(searchCitiesUseCase, times(1)).invoke("Ta", 10)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `rapid queries cancel previous and only last executes`() = runTest {
        val vm = vm()
        whenever(searchCitiesUseCase.invoke(any(), any()))
            .thenReturn(emptyList())

        vm.onQueryChange("Ta")
        advanceTimeBy(200) // still within debounce window of first query

        vm.onQueryChange("Tai")
        advanceTimeBy(350)
        advanceUntilIdle()

        verify(searchCitiesUseCase, never()).invoke(eq("Ta"), any())
        verify(searchCitiesUseCase, times(1)).invoke(eq("Tai"), eq(10))
    }

    @Test
    fun `onCitySelected calls SaveSelectedCityUseCase`() = runTest {
        val vm = vm()
        val chosen = SearchCity("Tokyo", "JP", null, 35.0, 139.0)

        vm.onCitySelected(chosen)
        advanceUntilIdle()

        verify(saveSelectedCityUseCase, times(1)).invoke(chosen)
    }
}

/** JUnit Rule to set a TestDispatcher as Main so viewModelScope launches run under test control. */
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
