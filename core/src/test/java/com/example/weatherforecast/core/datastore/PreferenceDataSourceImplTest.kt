package com.example.weatherforecast.core.datastore

import app.cash.turbine.test
import com.example.weatherforecast.core.datastore.datasource.PreferencesDataSourceImpl
import com.example.weatherforecast.core.model.LocalData
import com.example.weatherforecast.core.model.SearchCity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PreferencesDataSourceImplTest {

    @Test
    fun `observeSelectedCity emits Default then updates from datastore and null maps to Default`() =
        runTest {
            // Arrange
            val datastore: PrefsDataStore = mock()
            val selectedFlow = MutableSharedFlow<SearchCity?>(replay = 1)
            whenever(datastore.selectedCity).thenReturn(selectedFlow)

            val dataSource = PreferencesDataSourceImpl(
                datastore = datastore,
                coroutineScope = backgroundScope    // provided by runTest
            )

            val tokyo = SearchCity("Tokyo", "JP", null, 35.0, 139.0)

            // Act + Assert
            dataSource.observeSelectedCity().test {
                // Initial value comes from in-memory cache = DefaultCity
                assertEquals(LocalData.DefaultCity, awaitItem())

                // When datastore emits a city, cache reflects it immediately
                selectedFlow.emit(tokyo)
                assertEquals(tokyo, awaitItem())

                // When datastore emits null, cache maps it back to DefaultCity
                selectedFlow.emit(null)
                assertEquals(LocalData.DefaultCity, awaitItem())

                cancelAndConsumeRemainingEvents()
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `saveSelectedCity updates cache immediately and persists`() = runTest {
        val selectedFlow = MutableSharedFlow<SearchCity?>(replay = 1)
        selectedFlow.tryEmit(null)


        val datastore: PrefsDataStore = mock()
        whenever(datastore.selectedCity).thenReturn(selectedFlow)

        val dataSource = PreferencesDataSourceImpl(
            datastore = datastore,
            coroutineScope = backgroundScope
        )
        val taipei = SearchCity("Taipei", "TW", null, 25.033, 121.5654)

        dataSource.observeSelectedCity().test {
            // Initial Default
            assertEquals(LocalData.DefaultCity, awaitItem())

            // Act: this sets cache first, then calls datastore.setSelectedCity
            dataSource.saveSelectedCity(taipei)
//
//          // In case any persistence runs on the queue (not needed here, but safe):
            advanceUntilIdle()
            verify(datastore).setSelectedCity(taipei)
//
            cancelAndConsumeRemainingEvents()
        }
    }
}
