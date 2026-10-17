package np.nepdate.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Design system §2. The brand colours do not follow dynamic colour; their meaning is cultural.
val HolidayLight = Color(0xFFC8102E)
val HolidayDark = Color(0xFFFF6B7A)
val TodayLight = Color(0xFF003893)
val TodayDark = Color(0xFF8FB3FF)

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

val LocalNepdateColors =
    staticCompositionLocalOf {
        NepdateColors(
            holiday = HolidayLight,
            holidayFill = HolidayLight.copy(alpha = 0.10f),
            today = TodayLight,
            event = Color(0xFF49454F),
            dimmed = Color(0xFF79747E),
        )
    }
