package np.nepdate.ui.search

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import np.nepdate.ui.upcoming.upcoming
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** MR-13: search by name and the landing state before a query. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w360dp-h800dp-xhdpi")
class SearchScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = NepaliDate.of(2083, 6, 20)
    private val settings = Settings(digits = Digits.ENGLISH)

    @Test
    fun searchGroupsKeepOnlyTheNamesThatMatch() {
        val groups = searchGroups(entries = entries, query = "ghatas")

        assertTrue(groups.isNotEmpty())
        assertTrue(
            groups.all { group ->
                group.entries
                    .all { entry ->
                        val name = entry.entry.name.english
                        name.contains("Ghatasthapana", ignoreCase = true)
                    }
            },
        )
    }

    @Test
    fun searchMatchesNepaliNamesToo() {
        val fulpati = entries.first { it.name.nepali.contains("फूलपाती") }
        val groups = searchGroups(entries = entries, query = "फूलपाती")

        assertTrue(groups.isNotEmpty())
        val group = groups.first()
        val entry = group.entries.first().entry
        assertEquals(fulpati.name.english, entry.name.english)
    }

    @Test
    fun theLandingStateHasTheDateCardsAndNoRecentChips() {
        setSearch()

        composeRule.onNodeWithTag("search_jump").assertExists()
        composeRule.onNodeWithTag("search_results").assertExists()
        composeRule.onNodeWithText("Recent searches").assertDoesNotExist()
    }

    @Test
    fun typingANameShowsTheMatchingFestival() {
        setSearch()

        composeRule.onNodeWithTag("search_input").performTextInput("Ghatasthapana")

        composeRule.onAllNodesWithText("Ghatasthapana Brata").onFirst().assertExists()
        composeRule.onNodeWithText("Recent searches").assertDoesNotExist()
    }

    @Test
    fun theRecentChipsShowTheWordsTheStoreKeeps() {
        val recent =
            RecentSearches(
                queries = listOf("Dashain", "Tihar"),
                add = {},
                clear = {},
            )
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                RecentSearchChips(recent = recent, onPick = {})
            }
        }

        composeRule.onNodeWithText("Recent searches").assertExists()
        composeRule.onNodeWithText("Dashain").assertExists()
        composeRule.onNodeWithText("Tihar").assertExists()
    }

    private fun setSearch() {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                SearchScreen(
                    today = today,
                    settings = settings,
                    onBack = {},
                    onOpenDate = {},
                )
            }
        }
    }

    private val entries by lazy {
        upcoming(NepaliDate.of(2077, 1, 1), limit = Int.MAX_VALUE).entries
    }
}
