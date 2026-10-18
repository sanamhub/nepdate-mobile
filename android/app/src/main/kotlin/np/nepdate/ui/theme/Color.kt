package np.nepdate.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

// Design system §2. The brand colours do not follow dynamic colour; their meaning is cultural.
val HolidayLight = Color(0xFFC8102E)
val HolidayDark = Color(0xFFFF6B7A)
val TodayLight = Color(0xFF003893)
val TodayDark = Color(0xFF8FB3FF)

internal const val HOLIDAY_FILL_LIGHT_ALPHA = 0.10f
internal const val HOLIDAY_FILL_DARK_ALPHA = 0.16f
private const val CONTRAST_OFFSET = 0.05f

/**
 * Brand colours that Material's colour scheme does not carry. [holidayFill] is the holiday colour
 * at 10 % in light and 16 % in dark, drawn behind a holiday numeral (rule H1).
 */
@Immutable
data class NepdateColors(
    val holiday: Color,
    val holidayFill: Color,
    val today: Color,
    val event: Color,
    val dimmed: Color,
)

/**
 * The holiday fill as it appears over [surface]. The cell draws this exact colour and the contrast
 * check measures the same value, so the drawing and the measurement cannot drift.
 *
 * @return [holidayFill] composited over [surface]
 */
internal fun NepdateColors.holidayFillOn(surface: Color): Color = holidayFill.compositeOver(surface)

/**
 * The WCAG contrast ratio between [foreground] and [background].
 *
 * @return the ratio from 1.0 to 21.0
 */
internal fun contrastRatio(
    foreground: Color,
    background: Color,
): Float {
    val opaqueForeground = foreground.compositeOver(background)
    val lighter = max(opaqueForeground.luminance(), background.luminance())
    val darker = min(opaqueForeground.luminance(), background.luminance())
    return (lighter + CONTRAST_OFFSET) / (darker + CONTRAST_OFFSET)
}

val LocalNepdateColors =
    staticCompositionLocalOf {
        NepdateColors(
            holiday = HolidayLight,
            holidayFill = HolidayLight.copy(alpha = HOLIDAY_FILL_LIGHT_ALPHA),
            today = TodayLight,
            event = Color(0xFF49454F),
            dimmed = Color(0xFF49454F),
        )
    }
