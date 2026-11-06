package np.nepdate.widget

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
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
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
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
 * W-03 Month: a compact grid on the surface, with a connected Previous / Today / Next group in the
 * header that changes the shown month in place. Today's numeral sits on the Sunny shape, holidays
 * on the holiday container, and Saturdays are drawn in the holiday colour.
 */
@Composable
internal fun MonthWidgetContent(
    model: MonthWidgetModel,
    opacity: WidgetOpacity,
) {
    val size = LocalSize.current
    val dense = size.height <= 190.dp
    val large = size.height >= 300.dp
    val showAd = size.height >= 210.dp
    val cellSize =
        if (large) {
            15.sp
        } else if (dense) {
            11.sp
        } else {
            13.sp
        }

    Column(
        modifier =
            widgetRoot(opacity, GlanceTheme.colors.surface)
                .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MonthHeaderRow(model, showSpan = !dense, wide = size.width >= WIDE_HEADER)
        if (!dense) MonthWeekdayHeader(model)
        MonthWeekGrid(model, cellSize, showAd)
    }
}

@Composable
private fun MonthHeaderRow(
    model: MonthWidgetModel,
    showSpan: Boolean,
    wide: Boolean,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = model.title,
                modifier = GlanceModifier.semantics { testTag = "month_title" },
                style =
                    TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = if (wide) 18.sp else 16.sp,
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
        val arrowWidth = if (wide) 48.dp else 40.dp
        MonthNavigationButton(
            shape = R.drawable.widget_shape_start,
            icon = R.drawable.ic_sym_chevron_left,
            width = arrowWidth,
            description = model.previousLabel,
            tag = "month_previous",
            target = monthNavigation(model, -1),
        )
        Spacer(GlanceModifier.width(2.dp))
        MonthTodayButton(model, asText = wide)
        Spacer(GlanceModifier.width(2.dp))
        MonthNavigationButton(
            shape = R.drawable.widget_shape_end,
            icon = R.drawable.ic_sym_chevron_right,
            width = arrowWidth,
            description = model.nextLabel,
            tag = "month_next",
            target = monthNavigation(model, 1),
        )
    }
}

@Composable
private fun MonthWeekdayHeader(model: MonthWidgetModel) {
    val holiday = holidayProvider()
    Row(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 2.dp),
    ) {
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
            week.forEach { cell ->
                MonthDayCell(
                    cell = cell,
                    fontSize = cellSize,
                    showAd = showAd,
                )
            }
        }
    }
}

/** One end of the connected group: a 36 dp tall chip inside a 48 dp tall touch target. */
@Composable
private fun MonthNavigationButton(
    @DrawableRes shape: Int,
    @DrawableRes icon: Int,
    width: Dp,
    description: String,
    tag: String,
    target: Pair<Int, Int>?,
) {
    val action =
        target?.let {
            actionRunCallback<MonthNavigationAction>(
                actionParametersOf(YEAR_PARAM to it.first, MONTH_PARAM to it.second),
            )
        }
    Box(
        modifier =
            GlanceModifier
                .width(width)
                .height(TOUCH_TARGET)
                .then(if (action != null) GlanceModifier.clickable(action) else GlanceModifier)
                .semantics {
                    contentDescription = description
                    testTag = tag
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                GlanceModifier
                    .width(width)
                    .height(BUTTON_HEIGHT)
                    .shapeBackground(shape, surfaceContainerHighestProvider()),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp),
                colorFilter =
                    ColorFilter.tint(
                        if (action != null) GlanceTheme.colors.onSurface else GlanceTheme.colors.outline,
                    ),
            )
        }
    }
}

/**
 * The middle of the connected group, filled with the primary colour. Narrow widgets draw the today
 * icon instead of the word, so the month title keeps its room.
 */
@Composable
private fun MonthTodayButton(
    model: MonthWidgetModel,
    asText: Boolean,
) {
    val action =
        actionRunCallback<MonthNavigationAction>(
            actionParametersOf(YEAR_PARAM to model.today.year, MONTH_PARAM to model.today.monthValue),
        )
    val description = model.todayLabel
    Box(
        modifier =
            GlanceModifier
                .height(TOUCH_TARGET)
                .clickable(action)
                .semantics {
                    contentDescription = description
                    testTag = "month_today"
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                GlanceModifier
                    .height(BUTTON_HEIGHT)
                    .then(if (asText) GlanceModifier.padding(horizontal = 12.dp) else GlanceModifier.width(40.dp))
                    .shapeBackground(R.drawable.widget_shape_4, GlanceTheme.colors.primary),
            contentAlignment = Alignment.Center,
        ) {
            if (asText) {
                Text(
                    text = model.todayLabel,
                    style =
                        TextStyle(
                            color = GlanceTheme.colors.onPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    maxLines = 1,
                )
            } else {
                Image(
                    provider = ImageProvider(R.drawable.ic_sym_today),
                    contentDescription = null,
                    modifier = GlanceModifier.size(20.dp),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimary),
                )
            }
        }
    }
}

@Composable
private fun RowScope.MonthDayCell(
    cell: MonthWidgetCell,
    fontSize: TextUnit,
    showAd: Boolean,
) {
    val context = LocalContext.current
    val date = cell.date
    val action = date?.let { actionStartActivity(widgetIntent(context, it)) }
    Box(
        modifier =
            GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .padding(1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .then(
                        if (cell.isHoliday && cell.isInMonth) {
                            GlanceModifier.shapeBackground(R.drawable.widget_shape_10, holidayContainerProvider())
                        } else {
                            GlanceModifier
                        },
                    ).then(if (action != null) GlanceModifier.clickable(action) else GlanceModifier)
                    .semantics {
                        if (date != null) testTag = "month_day_${date.toIsoString()}"
                    },
            contentAlignment = Alignment.Center,
        ) {
            if (cell.isToday) SunnyShape(GlanceModifier.fillMaxSize().padding(1.dp))
            MonthDayText(cell, fontSize, showAd)
        }
    }
}

private val BUTTON_HEIGHT = 36.dp
private val WIDE_HEADER = 300.dp
private const val DAYS_PER_WEEK = 7
private const val SATURDAY_INDEX = 6
