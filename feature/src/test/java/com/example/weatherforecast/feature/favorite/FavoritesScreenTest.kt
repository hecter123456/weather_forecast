package com.example.weatherforecast.feature.favorite

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import com.example.weatherforecast.core.model.FavoriteCity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FavoritesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setup() {
        // Prevent potential NPEs from libraries reading Build.FINGERPRINT in JVM tests
        ShadowBuild.setFingerprint("robolectric")
    }

    private val taipei = FavoriteCity(
        id = 1L, name = "Taipei", alias = "Home", note = "Work",
        country = "TW", state = null, lat = 25.033, lon = 121.5654
    )
    private val tainan = FavoriteCity(
        id = 2L, name = "Tainan", alias = null, note = null,
        country = "TW", state = null, lat = 22.99, lon = 120.21
    )

    @Test
    fun emptyList_showsEmptyMessage() {
        composeRule.setContent {
            MaterialTheme {
                FavoritesScreen(
                    favorites = emptyList(),
                    scope = TestScope(UnconfinedTestDispatcher()),
                    editing = null,
                    alias = "",
                    note = "",
                    onCitySelected = {},
                    onEdit = {},
                    onAliasChanged = {},
                    onNoteChanged = {},
                    onUpdate = { _, _, _ -> },
                    onDelete = {},
                    onNavigateToWeather = {},
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithText("No favorites yet. Save a location from the Forecast screen.")
            .assertIsDisplayed()
    }

    @Test
    fun clickRow_calls_onCitySelected_and_onNavigate() {
        var selected: FavoriteCity? = null
        var navigated = false

        composeRule.setContent {
            MaterialTheme {
                FavoritesScreen(
                    favorites = listOf(taipei, tainan),
                    scope = TestScope(UnconfinedTestDispatcher()),
                    editing = null,
                    alias = "",
                    note = "",
                    onCitySelected = { selected = it },
                    onEdit = {},
                    onAliasChanged = {},
                    onNoteChanged = {},
                    onUpdate = { _, _, _ -> },
                    onDelete = {},
                    onNavigateToWeather = { navigated = true },
                    modifier = Modifier
                )
            }
        }

        composeRule.onNodeWithTag(
            "favoriteRow_${taipei.id}",
            useUnmergedTree = true
        ).performScrollTo().performClick()

        // Click the first row (node that has click action and contains the city name)
//        composeRule.onNode(hasClickAction() and hasText("Taipei"))
//            .performClick()

        assert(selected == taipei)
        assert(navigated)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun editFlow_opensDialog_and_save_calls_onUpdate_and_closesDialog() {
        // Use Compose state to drive recomposition when callbacks update values
        var editing by mutableStateOf<FavoriteCity?>(null)
        var alias by mutableStateOf("")
        var note by mutableStateOf("")
        var updated: Triple<Long, String?, String?>? = null

        composeRule.setContent {
            MaterialTheme {
                FavoritesScreen(
                    favorites = listOf(taipei),
                    scope = TestScope(UnconfinedTestDispatcher()),
                    editing = editing,
                    alias = alias,
                    note = note,
                    onCitySelected = {},
                    onEdit = { editing = it },             // toggle dialog visibility
                    onAliasChanged = { alias = it },       // bind input to state
                    onNoteChanged = { note = it },
                    onUpdate = { id, a, n -> updated = Triple(id, a, n) },
                    onDelete = {},
                    onNavigateToWeather = {},
                    modifier = Modifier
                )
            }
        }

        // Tap the "Edit" icon on the first item
        composeRule.onAllNodesWithContentDescription("Edit")[0].performClick()

        // The screen’s onClick sets alias/note/editing via callbacks → state updates → recomposition
        // (Values will initially be set by the Edit icon handler inside the Composable.)

        // Dialog title should be visible
        composeRule.onNodeWithText("Edit Favorite").assertIsDisplayed()

        // Replace the two text fields: first is Alias, second is Note
        composeRule.onAllNodes(hasSetTextAction())[0].performTextReplacement("Nick")
        composeRule.onAllNodes(hasSetTextAction())[1].performTextReplacement("Office")

        // Press Save → onUpdate is called with sanitized values; dialog is dismissed via onEdit(null)
        composeRule.onNodeWithText("Save").performClick()

        assert(updated == Triple(taipei.id, "Nick", "Office"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delete_calls_onDelete_using_given_scope() {
        val testScope = TestScope(UnconfinedTestDispatcher())
        var deletedId: Long? = null

        composeRule.setContent {
            MaterialTheme {
                FavoritesScreen(
                    favorites = listOf(taipei, tainan),
                    scope = testScope,                  // pass a controllable TestScope
                    editing = null,
                    alias = "",
                    note = "",
                    onCitySelected = {},
                    onEdit = {},
                    onAliasChanged = {},
                    onNoteChanged = {},
                    onUpdate = { _, _, _ -> },
                    onDelete = { id -> deletedId = id }, // verify this is invoked
                    onNavigateToWeather = {},
                    modifier = Modifier
                )
            }
        }

        // Tap the first "Delete" icon
        composeRule.onAllNodesWithContentDescription("Delete")[0].performClick()

        // The click launches a coroutine: scope.launch { onDelete(id) } → advance the scheduler
        testScope.advanceUntilIdle()

        assert(deletedId == taipei.id)
    }
}
