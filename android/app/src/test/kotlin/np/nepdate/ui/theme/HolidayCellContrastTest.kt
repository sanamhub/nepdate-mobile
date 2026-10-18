package np.nepdate.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * MR-10 AC3: every colour pair of the design system reaches 4.5:1. The schemes are the ones
 * `NepdateTheme` uses, and the cell fill comes from the same `holidayFillOn` and `contrastRatio`
 * functions as the cell, so a change to a scheme, an alpha or the surface fails here.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class HolidayCellContrastTest {
    @Test
    fun lightTheme() = assertContrast(LightColors, darkTheme = false)

    @Test
    fun darkTheme() = assertContrast(DarkColors, darkTheme = true)

    @Test
    fun dynamicLightTheme() = assertContrast(dynamicLightColorScheme(context()), darkTheme = false)

    @Test
    fun dynamicDarkTheme() = assertContrast(dynamicDarkColorScheme(context()), darkTheme = true)

    private fun context(): Context = ApplicationProvider.getApplicationContext()

    private fun assertContrast(
        scheme: ColorScheme,
        darkTheme: Boolean,
    ) {
        val colors = nepdateColorsFor(darkTheme, scheme)
        val background = scheme.background
        val pairs =
            mapOf(
                "holiday on the cell fill" to contrastRatio(colors.holiday, colors.holidayFillOn(background)),
                "holiday on the background" to contrastRatio(colors.holiday, background),
                "today on the background" to contrastRatio(colors.today, background),
                "dimmed on the background" to contrastRatio(colors.dimmed, background),
            )
        for ((pair, ratio) in pairs) {
            assertTrue("$pair is $ratio:1, below 4.5:1", ratio >= MIN_CONTRAST)
        }
    }

    private companion object {
        const val MIN_CONTRAST = 4.5f
    }
}
