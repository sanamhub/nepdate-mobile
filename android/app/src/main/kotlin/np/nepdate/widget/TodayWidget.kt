package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
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
 * W-01 Today. The 2x2 layout stacks the hero numeral and the date; 4x2 puts the next holiday on
 * the right; 4x1 becomes one line. The holiday mark is a fill plus an underline at every opacity
 * (design-system H1).
 */
@Composable
internal fun TodayWidgetContent(
    model: TodayWidgetModel,
    opacity: WidgetOpacity,
) {
    val context = LocalContext.current
    val tap = actionStartActivity(widgetIntent(context))
    val label = context.getString(R.string.widget_today_name)
    Box(
        modifier =
            widgetRootOpacity(opacity)
                .clickable(tap)
                .semantics {
                    contentDescription = label
                    testTag = "today_widget"
                },
    ) {
        when {
            isOneLineSize() -> TodayOneLine(model)
            isSmallSize() -> TodayStacked(model, compact = true)
            else -> TodaySideBySide(model, compact = false, showNext = true)
        }
    }
}

@Composable
private fun TodayOneLine(model: TodayWidgetModel) {
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = model.oneLine,
                style =
                    TextStyle(
                        color =
                            if (model.isHoliday ||
                                model.isSaturday
                            ) {
                                holidayProvider()
                            } else {
                                GlanceTheme.colors.onSurface
                            },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    ),
                maxLines = 1,
            )
            if (model.isHoliday) {
                HolidayMark()
            }
        }
    }
}

@Composable
private fun TodaySideBySide(
    model: TodayWidgetModel,
    compact: Boolean,
    showNext: Boolean,
    tiny: Boolean = false,
) {
    Row(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .padding(if (compact) 6.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Hero(model, compact, tiny)
        Column(
            modifier =
                GlanceModifier
                    .defaultWeight()
                    .padding(start = if (compact) 6.dp else 10.dp, end = 4.dp),
        ) {
            Text(
                text = model.monthName,
                style =
                    TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = if (compact) 11.sp else 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                maxLines = 1,
            )
            Text(
                text = if (compact) model.weekdayShort else model.weekday,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = if (compact) 9.sp else 12.sp),
                maxLines = 1,
            )
            DateOrHoliday(model, compact = compact, short = true, tiny = tiny)
        }
        if (showNext) {
            model.nextHoliday?.let { next ->
                Column(
                    modifier = GlanceModifier.width(72.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = next.name,
                        style = TextStyle(color = holidayProvider(), fontSize = 9.sp, fontWeight = FontWeight.Medium),
                        maxLines = 2,
                    )
                    Text(
                        text = next.relative,
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 8.sp),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayStacked(
    model: TodayWidgetModel,
    compact: Boolean,
    showNext: Boolean = true,
    tiny: Boolean = false,
) {
    Column(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Hero(model, compact, tiny)
        Text(
            text = "${model.monthName} · ${model.weekdayShort}",
            style =
                TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize =
                        if (compact) {
                            9.sp
                        } else {
                            13.sp
                        },
                    fontWeight = FontWeight.Medium,
                ),
            maxLines = 1,
        )
        DateOrHoliday(model, compact = compact, short = true, tiny = tiny)
        if (showNext) {
            model.nextHoliday?.let { next ->
                Text(
                    text = "${next.name}\n${next.relative}",
                    style = TextStyle(color = holidayProvider(), fontSize = 6.5.sp, fontWeight = FontWeight.Medium),
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun Hero(
    model: TodayWidgetModel,
    compact: Boolean,
    tiny: Boolean = false,
) {
    val holiday = model.isHoliday
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                GlanceModifier
                    .then(
                        if (holiday) {
                            GlanceModifier.background(holidayColor().copy(alpha = HOLIDAY_FILL_ALPHA))
                        } else {
                            GlanceModifier
                        },
                    ).padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = model.bsDay,
                style =
                    TextStyle(
                        color = if (holiday || model.isSaturday) holidayProvider() else GlanceTheme.colors.onSurface,
                        fontSize =
                            if (tiny) {
                                28.sp
                            } else if (compact) {
                                32.sp
                            } else {
                                52.sp
                            },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                maxLines = 1,
            )
        }
        if (holiday) HolidayMark()
        model.nepalLabel?.let {
            Text(
                text = it,
                style =
                    TextStyle(
                        color = todayProvider(),
                        fontSize =
                            if (tiny) {
                                6.sp
                            } else if (compact) {
                                7.sp
                            } else {
                                8.sp
                            },
                        fontWeight = FontWeight.Medium,
                    ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DateOrHoliday(
    model: TodayWidgetModel,
    compact: Boolean = false,
    short: Boolean = false,
    tiny: Boolean = false,
) {
    Text(
        text = model.holidayName ?: if (short) model.adShortLine else model.adLine,
        style =
            TextStyle(
                color = if (model.holidayName != null) holidayProvider() else GlanceTheme.colors.onSurfaceVariant,
                fontSize =
                    if (tiny) {
                        7.sp
                    } else if (compact) {
                        8.sp
                    } else {
                        8.5.sp
                    },
                textAlign = TextAlign.Center,
            ),
        maxLines = if (model.holidayName != null) 2 else 1,
    )
}

@Composable
private fun HolidayMark() {
    Box(
        modifier =
            GlanceModifier
                .padding(top = 1.dp)
                .width(24.dp)
                .height(2.dp)
                .background(holidayColor()),
    ) {}
}

/**
 * W-02 Today and upcoming. The left half is W-01, the right half the next three entries. If fewer
 * than three remain, the coverage footer ends the right side (W-02 AC2).
 */
@Composable
internal fun UpcomingWidgetContent(
    model: UpcomingWidgetModel,
    opacity: WidgetOpacity,
) {
    val context = LocalContext.current
    if (isOneLineSize() || isSmallSize()) {
        Box(
            modifier =
                widgetRootOpacity(opacity)
                    .clickable(actionStartActivity(widgetIntent(context)))
                    .semantics { testTag = "upcoming_widget" },
        ) {
            if (isOneLineSize()) TodayOneLine(model.today) else TodayStacked(model.today, compact = true)
        }
        return
    }
    Row(
        modifier =
            widgetRootOpacity(opacity)
                .padding(10.dp),
    ) {
        Box(
            modifier =
                GlanceModifier
                    .width(104.dp)
                    .fillMaxHeight()
                    .clickable(actionStartActivity(widgetIntent(context)))
                    .semantics { testTag = "upcoming_today" },
        ) {
            TodayStacked(model.today, compact = true, showNext = false, tiny = true)
        }
        Box(
            modifier =
                GlanceModifier
                    .padding(start = 10.dp, end = 8.dp)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(GlanceTheme.colors.outline),
        ) {}
        UpcomingEntries(model, context)
    }
}

@Composable
private fun androidx.glance.layout.RowScope.UpcomingEntries(
    model: UpcomingWidgetModel,
    context: android.content.Context,
) {
    Column(modifier = GlanceModifier.defaultWeight().padding(start = 10.dp)) {
        model.entries.forEachIndexed { index, entry ->
            Column(
                modifier =
                    GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity(widgetIntent(context, entry.date)))
                        .padding(vertical = 1.dp)
                        .semantics { testTag = "upcoming_entry_$index" },
            ) {
                Text(
                    text = entry.name,
                    style =
                        TextStyle(
                            color = if (entry.isHoliday) holidayProvider() else GlanceTheme.colors.onSurface,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    maxLines = 1,
                )
                if (entry.isHoliday) {
                    Box(
                        modifier =
                            GlanceModifier
                                .padding(top = 1.dp)
                                .width(16.dp)
                                .height(1.dp)
                                .background(holidayColor()),
                    ) {}
                }
                Text(
                    text = entry.relative,
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 7.sp),
                    maxLines = 1,
                )
            }
        }
        if (model.entries.isEmpty() && model.dataEnds == null) {
            Text(
                text = context.getString(R.string.upcoming_empty),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 8.sp),
                maxLines = 2,
            )
        }
        model.dataEnds?.let { footer ->
            Text(
                text = footer,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 7.sp),
                maxLines = 2,
            )
        }
    }
}

private const val HOLIDAY_FILL_ALPHA = 0.16f
