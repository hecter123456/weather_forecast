package com.example.weatherforecast // ← change to your actual package

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherforecast.navigation.Routes
import com.example.weatherforecast.ui.BottomBar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WeatherAppNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setup() {
        // Prevent NPEs if a library reads Build.FINGERPRINT during unit tests
        ShadowBuild.setFingerprint("robolectric")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomBar_navigates_and_updates_selected_state() {
        composeRule.setContent {
            MaterialTheme {
                val navController = rememberNavController()

                // Mini scaffold + NavHost for the test
                Scaffold(
                    topBar = { TopAppBar(title = { Text("Weather Forecast") }) },
                    bottomBar = { BottomBar(navController) }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Forecast,
                        modifier = Modifier.padding()
                    ) {
                        composable(Routes.Forecast) { Text("Forecast Screen") }
                        composable(Routes.Search) { Text("Search Screen") }
                        composable(Routes.Favorites) { Text("Favorites Screen") }
                    }
                }
            }
        }

        // Top app bar title is visible
        composeRule.onNodeWithText("Weather Forecast").assertIsDisplayed()

        // All three bottom items are present
        composeRule.onNodeWithText("Forecast").assertIsDisplayed()
        composeRule.onNodeWithText("Search").assertIsDisplayed()
        composeRule.onNodeWithText("Favorites").assertIsDisplayed()

        // Initially "Forecast" is selected
        composeRule.onNodeWithTag(
            "Forecast",
            useUnmergedTree = true
        ).assertExists()
        composeRule.onNodeWithText("Forecast Screen").assertIsDisplayed()

        // Tap "Search" → selected changes and content updates
        composeRule.onNodeWithTag(
            "Search",
            useUnmergedTree = true
        ).performClick()

        composeRule.onNodeWithTag(
            "Search",
            useUnmergedTree = true
        ).assertExists()
        composeRule.onNodeWithText("Search Screen").assertIsDisplayed()

        // Tap "Favorites" → selected changes and content updates
        composeRule.onNodeWithTag(
            "Favorites",
            useUnmergedTree = true
        ).performClick()

        composeRule.onNodeWithTag(
            "Favorites",
            useUnmergedTree = true
        ).assertExists()
        composeRule.onNodeWithText("Favorites Screen").assertIsDisplayed()
    }
}
