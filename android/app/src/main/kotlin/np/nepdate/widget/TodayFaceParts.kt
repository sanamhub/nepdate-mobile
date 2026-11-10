package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import np.nepdate.R

// The Today widget's controls and pill, kept apart from its layout.

/**
 * The flip button: a filled primary circle with the on-primary icon. Primary on primary container
 * keeps the icon above 4.5:1 and the button above 3:1 in light, dark and wallpaper colours.
 */
@Composable
internal fun FlipButton(showAd: Boolean) {
    val context = LocalContext.current
    val description = context.getString(if (showAd) R.string.widget2_show_bs else R.string.widget2_show_ad)
    Box(modifier = GlanceModifier.padding(6.dp)) {
        IconControl(
            spec =
                ControlSpec(
                    icon = R.drawable.ic_sym_swap_horiz,
                    description = description,
                    tag = "today_flip",
                    action = actionRunCallback<TodayFaceAction>(),
                ),
            style =
                ControlStyle(
                    shape = R.drawable.widget_shape_pill,
                    fill = GlanceTheme.colors.primary,
                    ink = GlanceTheme.colors.onPrimary,
                    width = FLIP_SIZE,
                    height = FLIP_SIZE,
                ),
        )
    }
}

private val FLIP_SIZE = 32.dp

/**
 * The weekday, with the small "Nepal" label after it when the date follows Nepal and the device is
 * in another zone (W-01 AC3). The label takes the label type role, so it reads as a note on the
 * weekday rather than as another line.
 */
@Composable
internal fun WeekdayLine(
    weekday: String,
    fontSize: TextUnit,
    color: ColorProvider,
    nepal: String?,
    scale: Float,
) {
    if (nepal == null) {
        Text(
            text = weekday,
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(color = color, fontSize = fontSize, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
    } else {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = weekday,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = color, fontSize = fontSize, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
            Text(
                text = nepal,
                modifier = GlanceModifier.padding(start = 6.dp, bottom = 2.dp),
                style =
                    TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontSize = WidgetType.label * scale,
                        fontWeight = WidgetType.labelWeight,
                    ),
                maxLines = 1,
            )
        }
    }
}
