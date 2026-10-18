package np.nepdate.design

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Records the M0 design-spike screenshots. They are not a pass: the human checks them on a device
 * (docs/product/spike-results.md).
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w900dp-h900dp-xhdpi")
class DesignSpikeScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun devanagariCellLight() {
        capture("design-spike-devanagari-cell-light", SpikePart.CELL, darkTheme = false)
    }

    @Test
    @Config(qualifiers = "+night")
    fun devanagariCellDark() {
        capture("design-spike-devanagari-cell-dark", SpikePart.CELL, darkTheme = true)
    }

    @Test
    @Config(qualifiers = "ne-w900dp-h900dp-xhdpi")
    fun devanagariCellNepali() {
        capture("design-spike-devanagari-cell-ne", SpikePart.CELL, darkTheme = false)
    }

    @Test
    @Config(qualifiers = "w1800dp-h1000dp-xhdpi")
    fun devanagariHeroLight() {
        capture("design-spike-devanagari-hero-light", SpikePart.HERO, darkTheme = false)
    }

    @Test
    @Config(qualifiers = "w1800dp-h1000dp-xhdpi")
    fun devanagariHeroDark() {
        capture("design-spike-devanagari-hero-dark", SpikePart.HERO, darkTheme = true)
    }

    @Test
    @Config(qualifiers = "ne-w1800dp-h1000dp-xhdpi")
    fun devanagariHeroNepali() {
        capture("design-spike-devanagari-hero-ne", SpikePart.HERO, darkTheme = false)
    }

    @Test
    fun colourLight() {
        capture("design-spike-colour-light", SpikePart.COLOUR, darkTheme = false)
    }

    @Test
    @Config(qualifiers = "+night")
    fun colourDark() {
        capture("design-spike-colour-dark", SpikePart.COLOUR, darkTheme = true)
    }

    @Test
    @Config(qualifiers = "ne-w900dp-h900dp-xhdpi")
    fun colourNepali() {
        capture("design-spike-colour-ne", SpikePart.COLOUR, darkTheme = false)
    }

    @Test
    fun colourDynamic() {
        capture(
            "design-spike-colour-dynamic",
            SpikePart.COLOUR,
            darkTheme = false,
            dynamicColor = true,
        )
    }

    private fun capture(
        name: String,
        part: SpikePart,
        darkTheme: Boolean,
        dynamicColor: Boolean = false,
    ) {
        composeRule.setContent {
            NepdateTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                DesignSpikeScreen(part = part)
            }
        }
        composeRule.onRoot().captureRoboImage("$name.png")
    }
}
