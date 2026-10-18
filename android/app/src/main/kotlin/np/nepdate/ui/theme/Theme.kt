package np.nepdate.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

internal val LightColors =
    lightColorScheme(
        primary = TodayLight,
        onPrimary = Color.White,
        background = Color(0xFFFFFBFE),
        surface = Color(0xFFFFFBFE),
    )

internal val DarkColors =
    darkColorScheme(
        primary = TodayDark,
        onPrimary = Color(0xFF001A41),
        background = Color(0xFF1C1B1F),
        surface = Color(0xFF1C1B1F),
    )

/**
 * The app theme. Surfaces follow the platform (dynamic colour on Android 12+); [HolidayLight] and
 * [TodayLight] stay fixed in both modes.
 */
@Composable
fun NepdateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme ->
                dynamicDarkColorScheme(context)
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                dynamicLightColorScheme(context)
            darkTheme -> DarkColors
            else -> LightColors
        }

    val brandColors = nepdateColorsFor(darkTheme, colorScheme)

    CompositionLocalProvider(LocalNepdateColors provides brandColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NepdateTypography,
            content = content,
        )
    }
}

/**
 * The brand colours for one theme. The design spike test uses this function so its contrast check
 * sees the same values as the running app.
 *
 * @return the holiday, fill, today, event and dimmed colours
 */
internal fun nepdateColorsFor(
    darkTheme: Boolean,
    colorScheme: ColorScheme,
): NepdateColors {
    val holiday = if (darkTheme) HolidayDark else HolidayLight
    return NepdateColors(
        holiday = holiday,
        holidayFill =
            holiday.copy(
                alpha = if (darkTheme) HOLIDAY_FILL_DARK_ALPHA else HOLIDAY_FILL_LIGHT_ALPHA,
            ),
        today = if (darkTheme) TodayDark else TodayLight,
        event = colorScheme.onSurfaceVariant,
        dimmed = colorScheme.onSurfaceVariant,
    )
}
