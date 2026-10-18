package np.nepdate.widget

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import np.nepdate.ui.theme.HolidayDark
import np.nepdate.ui.theme.HolidayLight
import np.nepdate.ui.theme.TodayDark
import np.nepdate.ui.theme.TodayLight
import androidx.glance.color.ColorProvider as dayNightColor

/** The one configuration choice each widget offers (widgets.md, quality bar). */
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

private const val SEVENTY_PERCENT_ALPHA = 0.7f

/** The three responsive sizes W-01 and W-02 render at. */
internal val TODAY_WIDGET_SIZES: Set<DpSize> =
    setOf(
        DpSize(110.dp, 110.dp),
        DpSize(250.dp, 70.dp),
        DpSize(250.dp, 110.dp),
    )

/** The three responsive sizes W-03 renders at. */
internal val MONTH_WIDGET_SIZES: Set<DpSize> =
    setOf(
        DpSize(250.dp, 180.dp),
        DpSize(250.dp, 250.dp),
        DpSize(320.dp, 320.dp),
    )

/** The brand colours stay fixed; the surface follows dynamic colour (design system §2). */
@Composable
internal fun holidayProvider(): ColorProvider = dayNightColor(day = HolidayLight, night = HolidayDark)

@Composable
internal fun todayProvider(): ColorProvider = dayNightColor(day = TodayLight, night = TodayDark)

@Composable
internal fun holidayColor(): Color {
    val context = LocalContext.current
    val night =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    return if (night) HolidayDark else HolidayLight
}

/** The widget background at the configured opacity, with the system corner radius. */
@Composable
internal fun widgetRootOpacity(opacity: WidgetOpacity): GlanceModifier {
    val surface = GlanceTheme.colors.background.getColor(LocalContext.current)
    return GlanceModifier
        .fillMaxSize()
        .appWidgetBackground()
        .background(surface.copy(alpha = opacity.alpha))
}

/** Whether the current widget is the one-line 4x1 size. */
@Composable
internal fun isOneLineSize(): Boolean {
    val size = LocalSize.current
    return size.width >= 200.dp && size.height < 90.dp
}

/** Whether the current widget is the compact 2x2 size. */
@Composable
internal fun isSmallSize(): Boolean = LocalSize.current.width < 200.dp
