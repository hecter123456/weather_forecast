package com.example.weatherforecast.feature.weather

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.weatherforecast.core.model.DailyForecast
import com.example.weatherforecast.core.model.SearchCity
import com.example.weatherforecast.core.model.TodayForecast
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
class WeatherScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setup() {
        // Avoid NPE when some libs read Build.FINGERPRINT in JVM tests
        ShadowBuild.setFingerprint("robolectric")
    }

    private val taipei = SearchCity(
        name = "Taipei",
        country = "TW",
        state = null,
        lat = 25.033,
        lon = 121.5654
    )

    @Test
    fun cityCard_isRendered() {
        composeRule.setContent {
            MaterialTheme {
                WeatherScreen(
                    tabIndex = 0,
                    uiState = WeatherUiState.Idle,
                    city = taipei,
                    isFavorite = false,
                    setTabIndex = {},
                    toggleFavorite = {},
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithText("City: Taipei").assertIsDisplayed()
        composeRule.onNodeWithText("Country: TW").assertIsDisplayed()
        composeRule.onNodeWithText("State: null").assertIsDisplayed()
        composeRule.onNodeWithText("Lat: 25.033").assertIsDisplayed()
        composeRule.onNodeWithText("Lon: 121.5654").assertIsDisplayed()
    }

    @Test
    fun clickTabs_calls_setTabIndex() {
        var lastIndex = -1
        composeRule.setContent {
            MaterialTheme {
                WeatherScreen(
                    tabIndex = 0,
                    uiState = WeatherUiState.Idle,
                    city = taipei,
                    isFavorite = false,
                    setTabIndex = { lastIndex = it },
                    toggleFavorite = {},
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithText("This Week").performClick()
        assert(lastIndex == 1)

        composeRule.onNodeWithText("Today").performClick()
        assert(lastIndex == 0)
    }

    @Test
    fun today_notFavorite_showsAddFab_and_callsToggle() {
        var toggleCalls = 0
        val today = TodayForecast("Taipei", 1_000L, 28.0, "Clear", 0, 10.0)

        composeRule.setContent {
            MaterialTheme {
                WeatherScreen(
                    tabIndex = 0,
                    uiState = WeatherUiState.Today(today),
                    city = taipei,
                    isFavorite = false,
                    setTabIndex = {},
                    toggleFavorite = { toggleCalls++ },
                    modifier = Modifier
                )
            }
        }

        // FAB 可能在 LazyColumn 內部，需要先捲動到可視範圍
        composeRule.onNodeWithContentDescription(
            "Add to favorites",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed().performClick()

        assert(toggleCalls == 1)
    }

    @Test
    fun today_favorite_showsRemoveFab_and_callsToggle() {
        var toggleCalls = 0
        val today = TodayForecast("Taipei", 1_000L, 28.0, "Clear", 0, 10.0)

        composeRule.setContent {
            MaterialTheme {
                WeatherScreen(
                    tabIndex = 0,
                    uiState = WeatherUiState.Today(today),
                    city = taipei,
                    isFavorite = true,
                    setTabIndex = {},
                    toggleFavorite = { toggleCalls++ },
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithContentDescription(
            "Remove from favorites",
            useUnmergedTree = true
        ).performScrollTo().assertIsDisplayed().performClick()

        assert(toggleCalls == 1)
    }

    @Test
    fun weekState_showsList() {
        val week = listOf(
            DailyForecast(
                dateEpochSeconds = 10L,
                minTempC = 20.0,
                maxTempC = 30.0,
                condition = "Clouds"
            ),
            DailyForecast(
                dateEpochSeconds = 11L,
                minTempC = 18.0,
                maxTempC = 29.0,
                condition = "Rain"
            )
        )

        composeRule.setContent {
            MaterialTheme {
                WeatherScreen(
                    tabIndex = 1,
                    uiState = WeatherUiState.Week(week),
                    city = taipei,
                    isFavorite = false,
                    setTabIndex = {},
                    toggleFavorite = {},
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithText("7-Day Forecast").assertIsDisplayed()
        composeRule.onNodeWithText("Max 30.0°C").assertIsDisplayed()
        composeRule.onNodeWithText("Min 20.0°C").assertIsDisplayed()
        composeRule.onNodeWithText("Clouds").assertIsDisplayed()
        composeRule.onNodeWithText("Rain").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorText() {
        composeRule.setContent {
            MaterialTheme {
                WeatherScreen(
                    tabIndex = 0,
                    uiState = WeatherUiState.Error("boom"),
                    city = taipei,
                    isFavorite = false,
                    setTabIndex = {},
                    toggleFavorite = {},
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithText("Error: boom").assertIsDisplayed()
    }

    @Test
    fun loadingState_showsProgress() {
        composeRule.setContent {
            MaterialTheme {
                WeatherScreen(
                    tabIndex = 0,
                    uiState = WeatherUiState.Loading,
                    city = taipei,
                    isFavorite = false,
                    setTabIndex = {},
                    toggleFavorite = {},
                    modifier = Modifier
                )
            }
        }

        composeRule.onNode(
            hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)
        ).assertExists()
    }
}
