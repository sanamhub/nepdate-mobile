package np.nepdate.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
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
import io.github.sanamhub.nepdate.NepaliDate

/** The Glance state keys for W-03's shown month. */
internal val SHOWN_YEAR_KEY = intPreferencesKey("shown_year")
internal val SHOWN_MONTH_KEY = intPreferencesKey("shown_month")

internal val YEAR_PARAM = ActionParameters.Key<Int>("year")
internal val MONTH_PARAM = ActionParameters.Key<Int>("month")

/** The shown month, falling back to [today] when no navigation state is stored. */
internal fun shownMonth(
    preferences: Preferences,
    today: NepaliDate,
): Pair<Int, Int> {
    val year = preferences[SHOWN_YEAR_KEY]
    val month = preferences[SHOWN_MONTH_KEY]
    return if (year != null && month != null && isValidShownMonth(year, month)) {
        year to month
    } else {
        today.year to today.monthValue
    }
}

/** Whether [year]-[month] is inside the library's BS range and the 12 BS months. */
internal fun isValidShownMonth(
    year: Int,
    month: Int,
): Boolean = year in NepaliDate.MIN.year..NepaliDate.MAX.year && month in 1..MONTHS_PER_YEAR

/** The month one step before or after [year]-[month], or null at the library's range edge. */
internal fun monthStep(
    year: Int,
    month: Int,
    delta: Int,
): Pair<Int, Int>? {
    val nextMonth = month + delta
    return when {
        nextMonth == 0 && year == NepaliDate.MIN.year -> null
        nextMonth == MONTHS_PER_YEAR + 1 && year == NepaliDate.MAX.year -> null
        nextMonth == 0 -> (year - 1) to MONTHS_PER_YEAR
        nextMonth == MONTHS_PER_YEAR + 1 -> (year + 1) to 1
        else -> year to nextMonth
    }
}

/** The target of the previous/next callbacks (W-03 AC1). */
internal fun monthNavigation(
    model: MonthWidgetModel,
    delta: Int,
): Pair<Int, Int>? = monthStep(model.year, model.month, delta)

/**
 * Applies a previous/next/Today tap to the Glance state and then asks the widget to redraw. The
 * update lambda is injectable so the state transition can be tested without a session.
 */
internal suspend fun applyMonthNavigation(
    context: Context,
    glanceId: GlanceId,
    year: Int,
    month: Int,
    update: suspend (Context, GlanceId) -> Unit,
) {
    if (!isValidShownMonth(year, month)) return
    androidx.glance.appwidget.state.updateAppWidgetState(context, glanceId) { preferences ->
        preferences[SHOWN_YEAR_KEY] = year
        preferences[SHOWN_MONTH_KEY] = month
    }
    update(context, glanceId)
}

/** The previous/next/today callback. Parameters carry the target month computed at render time. */
internal class MonthNavigationAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val year = parameters[YEAR_PARAM] ?: return
        val month = parameters[MONTH_PARAM] ?: return
        applyMonthNavigation(context, glanceId, year, month) { ctx, id ->
            MonthGlanceWidget().update(ctx, id)
        }
    }
}

/** W-03 Month: compact grid, no bitmaps, previous/next/today inside the widget. */
@Composable
internal fun MonthWidgetContent(
    model: MonthWidgetModel,
    opacity: WidgetOpacity,
) {
    val size = LocalSize.current
    val dense = size.height <= 190.dp
    val large = size.height >= 300.dp
    val showAd = size.height >= 210.dp
    val titleSize = if (large) 13.sp else 10.sp
    val cellSize =
        if (large) {
            12.sp
        } else if (dense) {
            9.sp
        } else {
            10.sp
        }

    Column(
        modifier =
            widgetRootOpacity(opacity)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MonthHeaderRow(model, titleSize)
        if (!dense) MonthWeekdayHeader(model, large)
        MonthWeekGrid(model, cellSize, showAd)
    }
}

@Composable
private fun MonthHeaderRow(
    model: MonthWidgetModel,
    titleSize: TextUnit,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = model.title,
            modifier =
                GlanceModifier
                    .defaultWeight()
                    .semantics { testTag = "month_title" },
            style =
                TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = titleSize,
                    fontWeight = FontWeight.Medium,
                ),
            maxLines = 1,
        )
        MonthNavigationButton(
            label = "‹",
            description = model.previousLabel,
            tag = "month_previous",
            target = monthNavigation(model, -1),
        )
        if (!model.isCurrentMonth) {
            MonthNavigationButton(
                label = model.todayLabel,
                description = model.todayLabel,
                tag = "month_today",
                target = model.today.year to model.today.monthValue,
            )
        }
        MonthNavigationButton(
            label = "›",
            description = model.nextLabel,
            tag = "month_next",
            target = monthNavigation(model, 1),
        )
    }
}

@Composable
private fun MonthWeekdayHeader(
    model: MonthWidgetModel,
    large: Boolean,
) {
    val holiday = holidayProvider()
    Row(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 1.dp),
    ) {
        model.weekdayHeaders.forEachIndexed { index, name ->
            Text(
                text = name,
                modifier = GlanceModifier.defaultWeight(),
                style =
                    TextStyle(
                        color = if (index == SATURDAY_INDEX) holiday else GlanceTheme.colors.onSurfaceVariant,
                        fontSize = if (large) 8.sp else 7.sp,
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

@Composable
private fun MonthNavigationButton(
    label: String,
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
    val modifier =
        GlanceModifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .then(if (action != null) GlanceModifier.clickable(action) else GlanceModifier)
            .semantics {
                contentDescription = description
                testTag = tag
            }
    Text(
        text = label,
        modifier = modifier,
        style =
            TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
        maxLines = 1,
    )
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
    val todayColor = todayProvider().getColor(context)
    val fill = if (cell.isHoliday) holidayColor().copy(alpha = HOLIDAY_FILL_ALPHA) else Color.Transparent
    val clickable =
        GlanceModifier
            .then(if (action != null) GlanceModifier.clickable(action) else GlanceModifier)
            .semantics {
                if (date != null) testTag = "month_day_${date.toIsoString()}"
            }
    if (cell.isToday) {
        Column(
            modifier =
                GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight(),
        ) {
            Box(modifier = GlanceModifier.fillMaxWidth().height(2.dp).background(todayColor)) {}
            Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                Box(modifier = GlanceModifier.width(2.dp).fillMaxHeight().background(todayColor)) {}
                Box(
                    modifier =
                        GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .background(fill)
                            .then(clickable),
                    contentAlignment = Alignment.Center,
                ) {
                    MonthDayText(cell, fontSize, showAd)
                }
                Box(modifier = GlanceModifier.width(2.dp).fillMaxHeight().background(todayColor)) {}
            }
            Box(modifier = GlanceModifier.fillMaxWidth().height(2.dp).background(todayColor)) {}
        }
    } else {
        Box(
            modifier =
                GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .padding(1.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = GlanceModifier.fillMaxSize().background(fill).then(clickable),
                contentAlignment = Alignment.Center,
            ) {
                MonthDayText(cell, fontSize, showAd)
            }
        }
    }
}

private const val DAYS_PER_WEEK = 7
private const val SATURDAY_INDEX = 6
private const val MONTHS_PER_YEAR = 12
private const val HOLIDAY_FILL_ALPHA = 0.16f
