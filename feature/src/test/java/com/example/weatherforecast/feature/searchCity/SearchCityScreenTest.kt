package com.example.feature.presentation // ← change to your actual package

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.weatherforecast.core.model.SearchCity
import com.example.weatherforecast.feature.searchCity.SearchCityScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SearchCityScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setup() {
        // Some libraries read Build.FINGERPRINT during JVM tests; give it a safe value.
        ShadowBuild.setFingerprint("robolectric")
    }

    private val taipei =
        SearchCity(name = "Taipei", country = "TW", state = null, lat = 25.033, lon = 121.5654)
    private val tainan =
        SearchCity(name = "Tainan", country = "TW", state = null, lat = 22.99, lon = 120.21)

    @Test
    fun typing_updates_query_and_shows_no_results_when_list_is_empty() {
        // Use Compose state to drive the controlled TextField value via onQueryChange.
        composeRule.setContent {
            MaterialTheme {
                var query by remember { mutableStateOf("") }

                SearchCityScreen(
                    query = query,
                    results = emptyList(),                 // no results
                    onQueryChange = { query = it },        // write back to state → recomposition
                    onCitySelected = {},
                    onNavigateToWeather = {},
                    modifier = Modifier
                )
            }
        }

        // Find the OutlinedTextField (editable node) and type text.
        composeRule.onNode(hasSetTextAction())
            .performTextInput("Ta")

        // Assert the TextField actually displays the typed text.
        composeRule.onNode(hasSetTextAction())
            .assertTextContains("Ta")

        // With a non-blank query and an empty results list, the screen shows "No results".
        composeRule.onNodeWithText("No results").assertIsDisplayed()
    }

    @Test
    fun list_shows_results_and_clicking_a_row_calls_both_callbacks() {
        var selected: SearchCity? = null
        var navigated = false

        composeRule.setContent {
            MaterialTheme {
                SearchCityScreen(
                    query = "Ta",
                    results = listOf(taipei, tainan),
                    onQueryChange = {},
                    onCitySelected = { selected = it },
                    onNavigateToWeather = { navigated = true },
                    modifier = Modifier
                )
            }
        }

        // Click the row that contains "Taipei".
        // We match a node that both has text and is clickable (the row container).
        composeRule.onNode(hasClickAction() and hasText("Taipei"))
            .performClick()

        // Verify both callbacks fired with the expected item.
        assert(selected == taipei)
        assert(navigated)
    }
}
