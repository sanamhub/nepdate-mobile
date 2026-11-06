package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import np.nepdate.R

/**
 * W-03 Month: the BS month as a grid on the surface, with a connected Previous / Today / Next group
 * in the header that changes the shown month in place. Today sits on the Sunny shape; a public
 * holiday gets a soft holiday tile, a red numeral and a short red bar; any other event a round dot.
 */
@Composable
internal fun MonthWidgetContent(
    model: MonthWidgetModel,
    opacity: WidgetOpacity,
) {
    val size = LocalSize.current
    val dense = size.height <= 190.dp
    val large = size.height >= 300.dp
    val cellSize =
        when {
            large -> 17.sp
            dense -> 13.sp
            else -> 15.sp
        }
    Column(
        modifier =
            widgetRoot(opacity, GlanceTheme.colors.surface)
                .padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MonthHeaderRow(model, showSpan = !dense, wide = size.width >= WIDE_HEADER)
        if (!dense) MonthWeekdayHeader(model)
        MonthWeekGrid(model, cellSize, showAd = large)
    }
}

@Composable
private fun MonthHeaderRow(
    model: MonthWidgetModel,
    showSpan: Boolean,
    wide: Boolean,
) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                GlanceModifier
                    .defaultWeight()
                    .clickable(actionStartActivity(widgetIntent(context, model.firstDay)))
                    .semantics { testTag = "month_header" },
        ) {
            Text(
                text = model.title,
                modifier = GlanceModifier.semantics { testTag = "month_title" },
                style =
                    TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = if (wide) 20.sp else 17.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
            )
            if (showSpan && model.adSpan.isNotEmpty()) {
                Text(
                    text = model.adSpan,
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
                    maxLines = 1,
                )
            }
        }
        MonthNavigationGroup(model, wide)
    }
}

/** The connected Previous / Today / Next group (M3 Expressive button group). */
@Composable
private fun MonthNavigationGroup(
    model: MonthWidgetModel,
    wide: Boolean,
) {
    val arrowWidth = if (wide) 44.dp else 38.dp
    val arrowFill = surfaceContainerHighestProvider()
    val ink = GlanceTheme.colors.onSurface
    IconControl(
        spec =
            ControlSpec(
                icon = R.drawable.ic_sym_chevron_left,
                description = model.previousLabel,
                tag = "month_previous",
                action = monthNavigation(model, -1)?.let { monthAction(it.first, it.second) },
            ),
        style = ControlStyle(R.drawable.widget_shape_start, arrowFill, ink, width = arrowWidth),
    )
    Spacer(GlanceModifier.width(2.dp))
    MonthTodayButton(model, asText = wide)
    Spacer(GlanceModifier.width(2.dp))
    IconControl(
        spec =
            ControlSpec(
                icon = R.drawable.ic_sym_chevron_right,
                description = model.nextLabel,
                tag = "month_next",
                action = monthNavigation(model, 1)?.let { monthAction(it.first, it.second) },
            ),
        style = ControlStyle(R.drawable.widget_shape_end, arrowFill, ink, width = arrowWidth),
    )
}

private fun monthAction(
    year: Int,
    month: Int,
) = actionRunCallback<MonthNavigationAction>(actionParametersOf(YEAR_PARAM to year, MONTH_PARAM to month))

/**
 * The middle of the group. Away from the current month it is filled with the primary colour so the
 * way back stands out; on the current month it drops to the secondary container.
 */
@Composable
private fun MonthTodayButton(
    model: MonthWidgetModel,
    asText: Boolean,
) {
    val fill = if (model.isCurrentMonth) GlanceTheme.colors.secondaryContainer else GlanceTheme.colors.primary
    val ink = if (model.isCurrentMonth) GlanceTheme.colors.onSecondaryContainer else GlanceTheme.colors.onPrimary
    val description = model.todayLabel
    Box(
        modifier =
            GlanceModifier
                .height(TOUCH_TARGET)
                .clickable(monthAction(model.today.year, model.today.monthValue))
                .semantics {
                    contentDescription = description
                    testTag = "month_today"
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                GlanceModifier
                    .height(36.dp)
                    .then(if (asText) GlanceModifier.padding(horizontal = 14.dp) else GlanceModifier.width(40.dp))
                    .shapeBackground(R.drawable.widget_shape_4, fill),
            contentAlignment = Alignment.Center,
        ) {
            if (asText) {
                Text(
                    text = model.todayLabel,
                    style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
            } else {
                Image(
                    provider = ImageProvider(R.drawable.ic_sym_today),
                    contentDescription = null,
                    modifier = GlanceModifier.size(20.dp),
                    colorFilter = ColorFilter.tint(ink),
                )
            }
        }
    }
}

@Composable
private fun MonthWeekdayHeader(model: MonthWidgetModel) {
    val holiday = holidayProvider()
    Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 2.dp, bottom = 2.dp)) {
        model.weekdayHeaders.forEachIndexed { index, name ->
            Text(
                text = name,
                modifier = GlanceModifier.defaultWeight(),
                style =
                    TextStyle(
                        color = if (index == SATURDAY_INDEX) holiday else GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ColumnScope.MonthWeekGrid(
    model: MonthWidgetModel,
    cellSize: TextUnit,
    showAd: Boolean,
) {
    model.cells.chunked(DAYS_PER_WEEK).forEach { week ->
        Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
            week.forEach { cell -> MonthDayCell(cell, cellSize, showAd) }
        }
    }
}

private val WIDE_HEADER = 300.dp
private const val DAYS_PER_WEEK = 7
private const val SATURDAY_INDEX = 6
