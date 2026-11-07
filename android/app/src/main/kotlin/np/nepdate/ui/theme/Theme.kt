package np.nepdate.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import np.nepdate.data.Look

// A Material 3 scheme from the marigold seed #E8A33D with warm neutrals and a slate tertiary
// (design system §2). The "Paper" look is the light scheme.
internal val LightColors =
    lightColorScheme(
        primary = Color(0xFF7C5800),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDEA8),
        onPrimaryContainer = Color(0xFF271900),
        secondary = Color(0xFF6C5C3F),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF6E0BB),
        onSecondaryContainer = Color(0xFF251A04),
        tertiary = Color(0xFF3B6383),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD2E4FF),
        onTertiaryContainer = Color(0xFF001D35),
        error = Color(0xFFBA1A1A),
        background = Color(0xFFFFF8F3),
        onBackground = Color(0xFF1F1B16),
        surface = Color(0xFFFFF8F3),
        onSurface = Color(0xFF1F1B16),
        surfaceVariant = Color(0xFFEFE0CF),
        onSurfaceVariant = Color(0xFF4F4539),
        outline = Color(0xFF817567),
        outlineVariant = Color(0xFFD4C4B5),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFFBF2EA),
        surfaceContainer = Color(0xFFF5ECE3),
        surfaceContainerHigh = Color(0xFFEFE6DD),
        surfaceContainerHighest = Color(0xFFE9E1D8),
        inverseSurface = Color(0xFF34302A),
        inverseOnSurface = Color(0xFFF9EFE7),
        inversePrimary = Color(0xFFF5BF62),
    )

internal val DarkColors =
    darkColorScheme(
        primary = Color(0xFFF5BF62),
        onPrimary = Color(0xFF422C00),
        primaryContainer = Color(0xFF5E4200),
        onPrimaryContainer = Color(0xFFFFDEA8),
        secondary = Color(0xFFD9C4A0),
        onSecondary = Color(0xFF3B2F15),
        secondaryContainer = Color(0xFF4F4532),
        onSecondaryContainer = Color(0xFFF6E0BB),
        tertiary = Color(0xFFA4C9F0),
        onTertiary = Color(0xFF03314F),
        tertiaryContainer = Color(0xFF22486A),
        onTertiaryContainer = Color(0xFFD2E4FF),
        error = Color(0xFFFFB4AB),
        background = Color(0xFF17130E),
        onBackground = Color(0xFFEBE1D9),
        surface = Color(0xFF17130E),
        onSurface = Color(0xFFEBE1D9),
        surfaceVariant = Color(0xFF4F4539),
        onSurfaceVariant = Color(0xFFD4C4B5),
        outline = Color(0xFF9C8E80),
        outlineVariant = Color(0xFF4F4539),
        surfaceContainerLowest = Color(0xFF110E09),
        surfaceContainerLow = Color(0xFF1F1B16),
        surfaceContainer = Color(0xFF231F1A),
        surfaceContainerHigh = Color(0xFF2E2924),
        surfaceContainerHighest = Color(0xFF39342E),
        inverseSurface = Color(0xFFEBE1D9),
        inverseOnSurface = Color(0xFF34302A),
        inversePrimary = Color(0xFF7C5800),
    )

/** The shape scale: rounder than the baseline, as the Expressive style asks. */
internal val NepdateShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

/** Whether [look] resolves to the dark scheme, given the system setting. */
@Composable
internal fun Look.isDark(): Boolean =
    when (this) {
        Look.SYSTEM -> isSystemInDarkTheme()
        Look.PAPER -> false
        Look.DARK -> true
    }

/**
 * The app theme. Paper and Night are the brand schemes; wallpaper colours (Android 12+) are an
 * opt-in setting. [HolidayLight] and [TodayLight] stay fixed in every scheme.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NepdateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            typography = NepdateTypography,
            shapes = NepdateShapes,
            content = content,
        )
    }
}

/**
 * The brand colours for one theme. The design spike test uses this function so its contrast check
 * sees the same values as the running app.
 *
 * @return the holiday, fill, today, event and dimmed colours and the holiday and today containers
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
        holidayContainer = if (darkTheme) HolidayContainerDark else HolidayContainerLight,
        onHolidayContainer = if (darkTheme) OnHolidayContainerDark else OnHolidayContainerLight,
        onToday = OnToday,
    )
}
