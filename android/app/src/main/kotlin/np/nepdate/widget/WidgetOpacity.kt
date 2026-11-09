package np.nepdate.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import np.nepdate.R
import np.nepdate.ui.theme.HolidayDark
import np.nepdate.ui.theme.HolidayLight
import np.nepdate.ui.theme.TodayDark
import np.nepdate.ui.theme.TodayLight
import androidx.glance.color.ColorProvider as dayNightColor

/** The background choice every widget offers in its configuration screen. */
internal enum class WidgetOpacity(
    val key: String,
    val alpha: Float,
) {
    SOLID("solid", 1f),
    SEVENTY("seventy", SEVENTY_PERCENT_ALPHA),
    TRANSPARENT("transparent", 0f),
    ;

    companion object {
        fun fromKey(key: String?): WidgetOpacity = entries.firstOrNull { it.key == key } ?: SOLID
    }
}

internal val WIDGET_OPACITY_KEY = stringPreferencesKey("widget_opacity")

/** True when the placed widget shows only the date, without its detail lines and controls. */
internal val WIDGET_MINIMAL_KEY = booleanPreferencesKey("widget_minimal")

private const val SEVENTY_PERCENT_ALPHA = 0.7f

/** The responsive sizes W-03 renders at. */
internal val MONTH_WIDGET_SIZES: Set<DpSize> =
    setOf(
        DpSize(250.dp, 180.dp),
        DpSize(250.dp, 250.dp),
        DpSize(320.dp, 320.dp),
    )

/** The sizes the Festivals list renders at: 4x2, a taller 4x2, 4x3 and a wide 5x4. */
internal val FESTIVALS_WIDGET_SIZES: Set<DpSize> =
    setOf(
        DpSize(250.dp, 110.dp),
        DpSize(250.dp, 190.dp),
        DpSize(320.dp, 250.dp),
        DpSize(400.dp, 320.dp),
    )

/** The sizes the Week widget renders at: one row and two rows, at four and five cells wide. */
internal val WEEK_WIDGET_SIZES: Set<DpSize> =
    setOf(
        DpSize(250.dp, 50.dp),
        DpSize(250.dp, 110.dp),
        DpSize(320.dp, 50.dp),
        DpSize(320.dp, 110.dp),
    )

/** The brand colours stay fixed; the surface follows dynamic colour (design system §2). */
@Composable
internal fun holidayProvider(): ColorProvider = dayNightColor(day = HolidayLight, night = HolidayDark)

@Composable
internal fun todayProvider(): ColorProvider = dayNightColor(day = TodayLight, night = TodayDark)

/**
 * The widget root: [color] at the configured opacity with the widget corner, the system radius on
 * Android 12+ and 28 dp before. A solid background keeps the day and night colour provider; a
 * translucent one is resolved for the current configuration, because alpha needs a concrete colour.
 */
@Composable
internal fun widgetRoot(
    opacity: WidgetOpacity,
    color: ColorProvider,
): GlanceModifier {
    val base = GlanceModifier.fillMaxSize().appWidgetBackground()
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val fill =
                when (opacity) {
                    WidgetOpacity.SOLID -> color
                    else -> ColorProvider(color.getColor(LocalContext.current).copy(alpha = opacity.alpha))
                }
            base.cornerRadius(android.R.dimen.system_app_widget_background_radius).background(fill)
        }
        // Before Android 12 the corner comes from a tinted shape, and a tinted shape takes its
        // alpha from the image, never from the tint (see softShapeBackground).
        opacity == WidgetOpacity.SOLID -> base.shapeBackground(R.drawable.widget_shape_28, color)
        else -> base.softShapeBackground(R.drawable.widget_shape_28, color, opacity.alpha)
    }
}
