package np.nepdate.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The holiday cell draws the fill over the screen background, not over a surface container. This
 * test uses the same `holidayFillOn` and `contrastRatio` functions as the cell, so a change to the
 * alpha or the surface fails here.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class HolidayCellContrastTest {
    @Test
    fun lightTheme() {
        assertCellContrast(
            scheme =
                lightColorScheme(
                    background = Color(0xFFFFFBFE),
                    surface = Color(0xFFFFFBFE),
                ),
            darkTheme = false,
        )
    }

    @Test
    fun darkTheme() {
        assertCellContrast(
            scheme =
                darkColorScheme(
                    background = Color(0xFF1C1B1F),
                    surface = Color(0xFF1C1B1F),
                ),
            darkTheme = true,
        )
    }

    @Test
    fun dynamicTheme() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertCellContrast(
            scheme = dynamicLightColorScheme(context),
            darkTheme = false,
        )
    }

    private fun assertCellContrast(
        scheme: ColorScheme,
        darkTheme: Boolean,
    ) {
        val colors = nepdateColorsFor(darkTheme, scheme)
        val cellFill = colors.holidayFillOn(scheme.background)
        val ratio = contrastRatio(colors.holiday, cellFill)
        assertTrue(
            "holiday text on the cell fill is $ratio:1, below 4.5:1",
            ratio >= 4.5f,
        )
    }
}
