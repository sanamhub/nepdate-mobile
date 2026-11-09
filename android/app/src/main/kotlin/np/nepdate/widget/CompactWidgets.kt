package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
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
            if (side < CHIP_WITH_MONTH) {
                // One cell has no room for a legible month, so it draws the numeral alone.
                SunnyNumeral(model.bsDay, side - 8.dp)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = model.monthName,
                        style = chipStyle(12.sp * typeScale(side), FontWeight.Bold),
                        maxLines = 1,
                    )
                    SunnyNumeral(model.bsDay, (side - CHIP_LABEL * typeScale(side)).coerceIn(28.dp, 120.dp))
                }
            }
        }
    }
}

/**
 * The Date and tithi strip (4x1): today's numeral on the Sunny shape, then the weekday and the BS
 * month on one line and the tithi with the AD date under it. A strip too short for two lines keeps
 * one, and a part that does not fit is left out rather than cut. Today's holidays and events are
 * pills: a tall strip gives them their own rows under the date, a wide one-row strip ends with the
 * first of them and a "+N" for the rest. A minimal strip keeps the weekday and the month only. A
 * tap opens the app on today.
 */
@Composable
internal fun DateStripContent(
    model: TodayWidgetModel,
    opacity: WidgetOpacity,
    minimal: Boolean = false,
) {
    val context = LocalContext.current
    val plan = stripPlan(model, LocalSize.current, widgetFit(), minimal)
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
        SunnyNumeral(model.bsDay, plan.sunny)
        Column(modifier = GlanceModifier.defaultWeight().padding(start = 12.dp)) {
            Text(
                text = plan.first,
                style =
                    TextStyle(
                        color = weekdayInk(model),
                        fontSize = 15.sp * plan.scale,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
            )
            if (plan.second.isNotEmpty()) {
                Text(
                    text = plan.second,
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp * plan.scale),
                    maxLines = 1,
                )
            }
            StripPills(plan.below)
        }
        StripPills(plan.trailing, GlanceModifier.padding(start = TRAILING_START, bottom = 4.dp))
    }
}

/** What a Date and tithi strip draws at its size: the text lines, their scale and the pills. */
private data class StripPlan(
    val sunny: Dp,
    val scale: Float,
    val first: String,
    val second: String,
    val below: List<List<EventPill>>,
    val trailing: List<List<EventPill>>,
)

private fun stripPlan(
    model: TodayWidgetModel,
    size: DpSize,
    fit: WidgetFit,
    minimal: Boolean,
): StripPlan {
    val names = if (minimal) emptyList() else model.eventNames.ifEmpty { listOfNotNull(model.holidayName) }
    val pillsBelow = names.isNotEmpty() && size.height >= STRIP_PILLS_BELOW
    val sunny = (size.height - 12.dp).coerceIn(32.dp, if (pillsBelow) 72.dp else 64.dp)
    val column = size.width - sunny - STRIP_CHROME
    val trailing =
        if (!pillsBelow && size.width >= STRIP_PILL_WIDTH) {
            packEventPills(names, model.isHoliday, fit, STRIP_PILL, TRAILING_PILLS, 1, model::more)
        } else {
            emptyList()
        }
    val trailingWidth =
        trailing.firstOrNull().orEmpty().fold(0.dp) { sum, pill -> sum + eventPillWidth(pill.text, fit, STRIP_PILL) }
    val text = column - trailingWidth - if (trailing.isEmpty()) 0.dp else TRAILING_START
    val scale = columnScale(size.height * STRIP_TO_FACE, text, STRIP_WIDTH_AT_ONE).coerceAtMost(MAX_STRIP_SCALE)
    val twoLines = !minimal && size.height >= STRIP_TWO_LINES
    val second = if (twoLines) fitted(fit, text, 12.sp * scale, model.tithi, model.adLine) else ""
    val below =
        if (pillsBelow) {
            val textHeight = fit.line(15.sp * scale) + if (second.isNotEmpty()) fit.line(12.sp * scale) else 0.dp
            val rows = ((size.height - STRIP_VERTICAL - textHeight) / fit.pill(STRIP_PILL)).toInt().coerceIn(1, 2)
            packEventPills(names, model.isHoliday, fit, STRIP_PILL, column, rows, model::more)
        } else {
            emptyList()
        }
    val first = fitted(fit, text, 15.sp * scale, model.weekday, model.bsMonthYear)
    return StripPlan(sunny, scale, first, second, below, trailing)
}

/** The strip's pills on the secondary container, which stands out from the strip's surface. */
@Composable
private fun StripPills(
    rows: List<List<EventPill>>,
    modifier: GlanceModifier = GlanceModifier,
) {
    EventPillRows(
        rows,
        STRIP_PILL,
        modifier,
        container = GlanceTheme.colors.secondaryContainer,
        content = GlanceTheme.colors.onSecondaryContainer,
    )
}

/** [parts] joined with dots, dropping parts from the end until the line fits in [width]. */
private fun fitted(
    fit: WidgetFit,
    width: Dp,
    fontSize: TextUnit,
    vararg parts: String,
): String {
    val present = parts.filter { it.isNotEmpty() }
    for (count in present.size downTo 1) {
        val line = present.take(count).joinToString(" · ")
        if (fit.fits(line, fontSize, width)) return line
    }
    return present.firstOrNull().orEmpty()
}

@Composable
private fun chipStyle(
    fontSize: TextUnit,
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
private val CHIP_WITH_MONTH = 90.dp
private val STRIP_PILL_WIDTH = 320.dp
private val STRIP_PILLS_BELOW = 84.dp
private val STRIP_VERTICAL = 8.dp
private val TRAILING_PILLS = 150.dp
private val TRAILING_START = 8.dp
private val STRIP_PILL = WidgetType.label
private const val MAX_STRIP_SCALE = 1.25f
private val STRIP_CHROME = 32.dp
private val STRIP_TWO_LINES = 50.dp
private val STRIP_WIDTH_AT_ONE = 190.dp
private const val STRIP_TO_FACE = 1.6f
