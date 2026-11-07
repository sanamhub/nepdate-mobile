package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import np.nepdate.R

/**
 * The Date chip (1x1, up to 2x2): the BS month over today's numeral on the Sunny shape. From two
 * cells wide the weekday joins it beside the numeral. A tap opens the app on today.
 */
@Composable
internal fun DateChipContent(
    model: TodayWidgetModel,
    opacity: WidgetOpacity,
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val label = context.getString(R.string.widget3_chip_name)
    Box(
        modifier =
            widgetRoot(opacity, GlanceTheme.colors.surface)
                .clickable(actionStartActivity(widgetIntent(context)))
                .semantics {
                    contentDescription = "$label, ${model.bsLongLine}"
                    testTag = "chip_widget"
                }.padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (size.width >= CHIP_WIDE && size.width > size.height) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SunnyNumeral(model.bsDay, (size.height - 12.dp).coerceIn(32.dp, 96.dp))
                Column(modifier = GlanceModifier.padding(start = 10.dp)) {
                    Text(text = model.monthName, style = chipStyle(15.sp, FontWeight.Bold), maxLines = 1)
                    Text(
                        text = model.weekday,
                        style = TextStyle(color = weekdayInk(model), fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        maxLines = 1,
                    )
                }
            }
        } else {
            val side = minOf(size.width, size.height)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = model.monthName, style = chipStyle(12.sp * typeScale(side), FontWeight.Bold), maxLines = 1)
                SunnyNumeral(model.bsDay, (side - CHIP_LABEL).coerceIn(28.dp, 120.dp))
            }
        }
    }
}

/**
 * The Date and tithi strip (4x1): today's numeral on the Sunny shape, then the weekday and the BS
 * date on one line and the tithi with the AD date under it. A wide strip adds today's holiday or
 * event as a pill at the end. A tap opens the app on today.
 */
@Composable
internal fun DateStripContent(
    model: TodayWidgetModel,
    opacity: WidgetOpacity,
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val scale = typeScale(size.height * STRIP_TO_FACE)
    val pill = model.holidayName ?: model.eventName
    Row(
        modifier =
            widgetRoot(opacity, GlanceTheme.colors.surface)
                .clickable(actionStartActivity(widgetIntent(context)))
                .semantics {
                    contentDescription = context.getString(R.string.widget3_strip_name)
                    testTag = "strip_widget"
                }.padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SunnyNumeral(model.bsDay, (size.height - 10.dp).coerceIn(32.dp, 64.dp))
        Column(modifier = GlanceModifier.defaultWeight().padding(start = 12.dp)) {
            Text(
                text = "${model.weekday} · ${model.bsLongLine}",
                style = TextStyle(color = weekdayInk(model), fontSize = 15.sp * scale, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
            Text(
                text = listOf(model.tithi, model.adLine).filter { it.isNotEmpty() }.joinToString(" · "),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp * scale),
                maxLines = 1,
            )
        }
        if (pill != null && size.width >= STRIP_PILL_WIDTH) StripPill(pill, model.isHoliday)
    }
}

@Composable
private fun StripPill(
    text: String,
    isHoliday: Boolean,
) {
    Text(
        text = text,
        modifier =
            GlanceModifier
                .padding(start = 8.dp)
                .shapeBackground(
                    R.drawable.widget_shape_pill,
                    if (isHoliday) holidayContainerProvider() else GlanceTheme.colors.secondaryContainer,
                ).padding(horizontal = 10.dp, vertical = 4.dp),
        style =
            TextStyle(
                color = if (isHoliday) onHolidayContainerProvider() else GlanceTheme.colors.onSecondaryContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        maxLines = 1,
    )
}

@Composable
private fun chipStyle(
    fontSize: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight,
): TextStyle =
    TextStyle(
        color = GlanceTheme.colors.onSurface,
        fontSize = fontSize,
        fontWeight = weight,
        textAlign = TextAlign.Center,
    )

@Composable
private fun weekdayInk(model: TodayWidgetModel): ColorProvider =
    if (model.isHoliday || model.isSaturday) holidayProvider() else GlanceTheme.colors.onSurface

private val CHIP_WIDE = 100.dp
private val CHIP_LABEL = 22.dp
private val STRIP_PILL_WIDTH = 320.dp
private const val STRIP_TO_FACE = 1.6f
