package np.nepdate.ui.month

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * MR-10 AC2 at the size the plan names: 360x640 dp, a six-row month, tithi on. The grid holds at
 * 1.0 and 1.15; at 1.3 the second tithi line clips, so [LIST_FONT_SCALE] is 1.3. The list takes
 * over from there and has no fixed cell height to clip against.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
class GridFitTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = NepaliDate.of(2081, 1, 15)
    private val tithiOn =
        Settings(
            digits = Digits.ENGLISH,
            showTithi = true,
        )

    @Test
    fun gridAt100() = assertGrid(1f)

    @Test
    fun gridAt115() = assertGrid(1.15f)

    @Test
    fun listAt130() = assertList(1.3f)

    @Test
    fun listAt145() = assertList(1.45f)

    @Test
    fun cellContentFitsAt100() = assertCellContent(1f)

    @Test
    fun cellContentFitsAt115() = assertCellContent(1.15f)

    @Test
    fun thresholdComesFromTheMeasurement() {
        assertEquals(1.3f, LIST_FONT_SCALE)
    }

    private fun assertGrid(fontScale: Float) {
        setMonthScreen(fontScale)
        assertTrue(composeRule.onAllNodesWithTag("month_grid").fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithTag("month_list").fetchSemanticsNodes().isEmpty())
    }

    private fun assertList(fontScale: Float) {
        setMonthScreen(fontScale)
        assertTrue(composeRule.onAllNodesWithTag("month_list").fetchSemanticsNodes().isNotEmpty())
        assertTrue(composeRule.onAllNodesWithTag("month_grid").fetchSemanticsNodes().isEmpty())
    }

    private fun assertCellContent(fontScale: Float) {
        // The cell is 49x89 dp at 1.0 and 49x88.5 dp at 1.15 on the measured screen. Use the smaller
        // of the two and the inner 2 dp padding, so a pass covers both scales.
        val cell = monthLayout(2083, 6, tithiOn, today).cells.first { it.isInMonth && it.bsDay == 9 }
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = density.density, fontScale = fontScale),
            ) {
                NepdateTheme(darkTheme = false, dynamicColor = false) {
                    Box(
                        modifier =
                            Modifier
                                .testTag("fit_cell")
                                .width(49.dp)
                                .height(88.dp),
                    ) {
                        Box(Modifier.fillMaxSize().padding(2.dp)) {
                            DayCellContent(
                                cell = cell,
                                isSelected = false,
                                digits = Digits.ENGLISH,
                                lang = Lang.ENGLISH,
                                showTithi = true,
                                ink = dayInk(cell, false),
                            )
                        }
                    }
                }
            }
        }

        val parent = composeRule.onNodeWithTag("fit_cell").fetchSemanticsNode().boundsInRoot
        listOf("9", "25", "Chaturdashi").forEach { text ->
            val child =
                composeRule
                    .onAllNodesWithText(text, useUnmergedTree = true)
                    .onFirst()
                    .fetchSemanticsNode()
                    .boundsInRoot
            assertTrue("$text at $fontScale is outside the cell: $child vs $parent", parent.contains(child))
        }
    }

    private fun setMonthScreen(fontScale: Float) {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = density.density, fontScale = fontScale),
            ) {
                NepdateTheme(darkTheme = false, dynamicColor = false) {
                    MonthScreen(
                        initialYear = 2081,
                        initialMonth = 1,
                        today = today,
                        settings = tithiOn,
                    )
                }
            }
        }
    }

    private fun Rect.contains(other: Rect): Boolean =
        other.left >= left - 0.5f &&
            other.top >= top - 0.5f &&
            other.right <= right + 0.5f &&
            other.bottom <= bottom + 0.5f
}
