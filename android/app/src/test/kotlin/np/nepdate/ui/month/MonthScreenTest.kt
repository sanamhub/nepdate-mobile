package np.nepdate.ui.month

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** A1-03 and the Compose parts of MR-01 and MR-10. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
class MonthScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = NepaliDate.of(2083, 6, 20)
    private val english = Settings(digits = Digits.ENGLISH)

    @Test
    fun mr01Ac5NextAndPreviousChangeTheMonth() {
        setMonthScreen()

        composeRule.onNodeWithText("Asoj 2083").assertExists()
        composeRule.onNodeWithContentDescription("Next month").performClick()
        waitForText("Kartik 2083")
        composeRule.onNodeWithContentDescription("Previous month").performClick()
        waitForText("Asoj 2083")
    }

    @Test
    fun mr01Ac5SwipeChangesTheMonth() {
        setMonthScreen()

        composeRule.onNodeWithTag("month_pager").performTouchInput { swipeLeft() }
        waitForText("Kartik 2083")
        composeRule.onNodeWithTag("month_pager").performTouchInput { swipeRight() }
        waitForText("Asoj 2083")
    }

    @Test
    fun widgetDeepLinkOpensTheDaySheet() {
        val date = NepaliDate.of(2083, 6, 25)
        setMonthScreen(openDate = date, openDateKey = 1)

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Asoj 25, 2083").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Ghatasthapana Brata").assertExists()
    }

    @Test
    fun mr01Ac5TodayButtonOnlyAppearsOffTheCurrentMonth() {
        setMonthScreen()

        composeRule.onNodeWithText("Today").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Next month").performClick()
        waitForText("Kartik 2083")
        composeRule.onNodeWithText("Today").assertExists().performClick()
        waitForText("Asoj 2083")
        composeRule.onNodeWithText("Today").assertDoesNotExist()
    }

    @Test
    fun mr01Ac6PickerCoversTheRangeAndJumps() {
        setMonthScreen()
        composeRule.onNodeWithContentDescription("Choose month and year").performClick()

        composeRule.onNodeWithTag("month_picker_year").performTextReplacement("2084")
        composeRule.onNodeWithTag("month_option_1").performClick()
        composeRule.onNodeWithText("Go").performClick()
        waitForText("Baisakh 2084")

        composeRule.onNodeWithContentDescription("Choose month and year").performClick()
        composeRule.onNodeWithTag("month_picker_year").performTextReplacement("1900")
        composeRule.onNodeWithText("Go").assertIsNotEnabled()
        composeRule.onNodeWithTag("month_picker_year").performTextReplacement("2199")
        composeRule.onNodeWithText("Go").assertIsEnabled()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Choose month and year").assertDoesNotExist()
    }

    @Test
    fun mr01Ac6PickerUsesTheProductMonthNames() {
        setMonthScreen()
        composeRule.onNodeWithContentDescription("Choose month and year").performClick()

        composeRule.onNodeWithTag("month_option_6").assertTextEquals("Asoj")
        composeRule.onNodeWithTag("month_option_3").assertTextEquals("Asar")
        composeRule.onNodeWithTag("month_option_1").assertTextEquals("Baisakh")
    }

    @Test
    fun mr10Ac4PickerHidesTheMonthAndHasNoUnlabelledControls() {
        setMonthScreen()
        composeRule.onNodeWithContentDescription("Choose month and year").performClick()

        composeRule.onNodeWithContentDescription("Next month").assertDoesNotExist()
        assertTrue(composeRule.onAllNodesWithTag("month_cell_in_2083-06-20").fetchSemanticsNodes().isEmpty())
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().forEach { node ->
            val config = node.config
            val labelled =
                config.contains(SemanticsProperties.Text) ||
                    config.contains(SemanticsProperties.ContentDescription) ||
                    node.children.any { it.config.contains(SemanticsProperties.Text) }
            assertTrue("an unlabelled click target in the picker: $config", labelled)
        }

        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithContentDescription("Next month").assertExists()
    }

    @Test
    fun mr01Ac7DimmedCellHasNoClickAction() {
        setMonthScreen()
        val leading = NepaliDate.of(2083, 6, 1).minusDays(1)

        composeRule
            .onNodeWithTag("month_cell_out_${leading.toIsoString()}")
            .assertHasNoClickAction()
    }

    @Test
    fun mr10Ac1Asoj25LabelMatchesTheRequirement() {
        setMonthScreen()

        composeRule
            .onNodeWithContentDescription(
                "Asoj 25, 2083, Sunday, 11 October 2026, public holiday, Ghatasthapana Brata",
            ).assertExists()
    }

    @Test
    fun mr10Ac1TodayHasAStateDescriptionWithoutChangingTheSpokenLabel() {
        setMonthScreen()
        val today =
            composeRule
                .onNodeWithTag("month_cell_in_2083-06-20")
                .fetchSemanticsNode()
                .config

        assertEquals("Today", today[SemanticsProperties.StateDescription])
        assertFalse(
            "the state description stays out of the spoken label",
            today[SemanticsProperties.ContentDescription].joinToString(", ").contains("Today"),
        )
        assertFalse(
            composeRule
                .onNodeWithTag("month_cell_in_2083-06-21")
                .fetchSemanticsNode()
                .config
                .contains(SemanticsProperties.StateDescription),
        )
    }

    @Test
    @Config(qualifiers = "ne-w360dp-h640dp-xhdpi")
    fun mr10Ac1TodayStateDescriptionUsesTheNepaliCopy() {
        setMonthScreen()

        assertEquals(
            "आज",
            composeRule
                .onNodeWithTag("month_cell_in_2083-06-20")
                .fetchSemanticsNode()
                .config[SemanticsProperties.StateDescription],
        )
    }

    @Test
    fun mr06Ac2NepalTimeAppearsInTheHeaderWhenTheZonesDiffer() {
        setMonthScreen(nepalTime = "10:17")

        composeRule.onNodeWithText("Nepal time · 10:17").assertExists()
    }

    @Test
    fun settingsIconOpensTheSettingsScreen() {
        var opened = false
        setMonthScreen(onSettings = { opened = true })

        composeRule.onNodeWithContentDescription("Settings").performClick()

        assertTrue(opened)
    }

    @Test
    fun firstGridIsReportedOnce() {
        var reports = 0
        setMonthScreen(onFirstGridDrawn = { reports++ })

        composeRule.waitForIdle()

        assertEquals(1, reports)
    }

    @Test
    fun mr10Ac2GridAtNormalFontScale() {
        setMonthScreen()

        composeRule.onAllNodesWithTag("month_grid").onFirst().assertExists()
        assertTrue(composeRule.onAllNodesWithTag("month_list").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun mr10Ac2ListAtDoubleFontScale() {
        setMonthScreen(fontScale = 2f)

        composeRule.onAllNodesWithTag("month_list").onFirst().assertExists()
        assertTrue(composeRule.onAllNodesWithTag("month_grid").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun mr10Ac4EveryClickableIsAtLeast48Dp() {
        setMonthScreen()
        composeRule.onNodeWithContentDescription("Next month").performClick()
        waitForText("Kartik 2083")

        composeRule.onNodeWithContentDescription("Previous month").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithContentDescription("Next month").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithContentDescription("Choose month and year").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithText("Today").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("month_cell_in_2083-07-20").assertHeightIsAtLeast(48.dp)

        val density =
            ApplicationProvider
                .getApplicationContext<Context>()
                .resources.displayMetrics.density
        val clickables =
            composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().filter {
                it.boundsInRoot.height >
                    0f
            }
        assertTrue("no visible clickable elements found", clickables.isNotEmpty())
        with(Density(density)) {
            clickables.forEach { node ->
                val height = node.boundsInRoot.height.toDp()
                assertTrue("a clickable is $height high, below 48 dp", height >= 48.dp)
            }
        }
    }

    private fun setMonthScreen(
        settings: Settings = english,
        fontScale: Float = 1f,
        nepalTime: String? = null,
        openDate: NepaliDate? = null,
        openDateKey: Int = 0,
        onSettings: (() -> Unit)? = null,
        onFirstGridDrawn: () -> Unit = {},
    ) {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = density.density, fontScale = fontScale),
            ) {
                NepdateTheme(darkTheme = false, dynamicColor = false) {
                    MonthScreen(
                        initialYear = 2083,
                        initialMonth = 6,
                        today = today,
                        settings = settings,
                        nepalTime = nepalTime,
                        openDate = openDate,
                        openDateKey = openDateKey,
                        onSettings = onSettings,
                        onFirstGridDrawn = onFirstGridDrawn,
                    )
                }
            }
        }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
