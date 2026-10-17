package np.nepdate.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors =
    lightColorScheme(
        primary = TodayLight,
        onPrimary = Color.White,
        background = Color(0xFFFFFBFE),
        surface = Color(0xFFFFFBFE),
    )

private val DarkColors =
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

    val brandColors =
        NepdateColors(
            holiday = if (darkTheme) HolidayDark else HolidayLight,
            holidayFill =
                (if (darkTheme) HolidayDark else HolidayLight).copy(
                    alpha = if (darkTheme) 0.16f else 0.10f,
                ),
            today = if (darkTheme) TodayDark else TodayLight,
            event = colorScheme.onSurfaceVariant,
            dimmed = colorScheme.onSurfaceVariant,
        )

    CompositionLocalProvider(LocalNepdateColors provides brandColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NepdateTypography,
            content = content,
        )
    }
}
