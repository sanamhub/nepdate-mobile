package np.nepdate.ui.month

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.patro.Named
import io.github.sanamhub.nepdate.patro.Patro
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * MR-01 AC8: every tithi name in the data fits a grid cell on a 360 dp screen without truncation.
 * Native graphics, so the text is measured with real fonts rather than Robolectric's stand-in metrics.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
class TithiLineTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mr01Ac8EveryTithiNameFitsACell() {
        val tithis: List<Named> = Patro.coverage.mapNotNull { Patro.info(it).tithi }.distinct()
        val layouts = mutableMapOf<String, TextLayoutResult>()
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    for (tithi in tithis) {
                        for (lang in Lang.entries) {
                            Box(Modifier.width(CELL_TEXT_WIDTH)) {
                                TithiLine(tithi, lang, Color.Black) { layouts[tithi.name(lang)] = it }
                            }
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()

        assertTrue("expected every tithi from Pratipadha to Aunsi", tithis.size >= 15)
        assertEquals(tithis.flatMap { t -> Lang.entries.map { t.name(it) } }.toSet(), layouts.keys)
        layouts.forEach { (name, layout) ->
            assertFalse("$name overflows its cell", layout.hasVisualOverflow)
            assertTrue("$name is below the minimum size", layout.layoutInput.style.fontSize >= TITHI_MIN_SIZE)
            if (' ' !in name && name.any { it in 'ऀ'..'ॿ' }) {
                assertEquals("$name must not be broken inside a word", 1, layout.lineCount)
            }
        }
    }

    private companion object {
        // (360 dp - 2 × 8 dp grid padding) / 7 columns - 2 × 2 dp cell padding.
        val CELL_TEXT_WIDTH = 45.dp
    }
}
