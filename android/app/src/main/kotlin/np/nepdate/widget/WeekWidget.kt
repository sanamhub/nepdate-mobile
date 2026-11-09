package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
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
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import np.nepdate.R

/**
 * The Week widget (4x1 to 5x4): the Sunday-to-Saturday week around today, each day with its
 * weekday, numeral and holiday or event mark, today on the Sunny shape. The two-row size adds the
 * month and the next day with an event; a taller widget lists the rest of the week instead, one
 * line per day with its holidays and events, and "+N" for days that do not fit. A day opens itself
 * in the app; the rest opens the app on today.
 */
@Composable
internal fun WeekWidgetContent(
    model: WeekWidgetModel,
    opacity: WidgetOpacity,
    minimal: Boolean = false,
) {
    val context = LocalContext.current
    val height = LocalSize.current.height
    val tall = height >= TALL_WEEK
    val agenda = !minimal && height >= AGENDA_WEEK && model.agenda.isNotEmpty()
    // A one-row strip shorter than the weekday, numeral and mark stack keeps the numerals only.
    val labels = height >= LABELLED_WEEK
    Column(
        modifier =
            widgetRoot(opacity, GlanceTheme.colors.surface)
                .clickable(actionStartActivity(widgetIntent(context)))
                .semantics { testTag = "week_widget" }
                .padding(horizontal = 8.dp, vertical = if (tall) 10.dp else 4.dp),
    ) {
        if (height >= TITLED_WEEK) {
            Text(
                text = model.title,
                modifier = GlanceModifier.padding(start = 8.dp, bottom = 4.dp),
                style =
                    TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = WidgetType.title,
                        fontWeight = WidgetType.titleWeight,
                    ),
                maxLines = 1,
            )
        }
        val week =
            if (agenda) {
                GlanceModifier.fillMaxWidth().height(
                    AGENDA_WEEK_ROW,
                )
            } else {
                GlanceModifier.fillMaxWidth().defaultWeight()
            }
        Row(modifier = week) {
            model.days.forEach { day -> WeekDay(day, tall, labels, minimal) }
        }
        val next = model.next
        when {
            agenda -> WeekAgenda(model, height - AGENDA_CHROME)
            tall && !minimal && next != null -> NextLine(next)
        }
    }
}

@Composable
private fun RowScope.WeekDay(
    day: WeekDayCell,
    tall: Boolean,
    labels: Boolean,
    minimal: Boolean,
) {
    val context = LocalContext.current
    val numeral = if (tall) 36.dp else 30.dp
    Column(
        modifier =
            GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .then(
                    if (day.isHoliday && !day.isToday) holidayTile() else GlanceModifier,
                ).clickable(actionStartActivity(widgetIntent(context, day.date)))
                .semantics { testTag = "week_day_${day.date.toIsoString()}" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val red = day.isHoliday || day.isSaturday
        if (labels) WeekdayLabel(day.weekdayShort, red)
        Box(modifier = GlanceModifier.size(numeral), contentAlignment = Alignment.Center) {
            if (day.isToday) SunnyShape(GlanceModifier.size(numeral))
            Text(
                text = day.bsDay,
                style =
                    TextStyle(
                        color =
                            when {
                                day.isToday -> onTodayProvider()
                                red -> holidayProvider()
                                else -> GlanceTheme.colors.onSurface
                            },
                        fontSize = if (tall) 17.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                maxLines = 1,
            )
        }
        if (!minimal) DayMarker(day.isHoliday, day.hasEvents)
    }
}

@Composable
private fun WeekdayLabel(
    text: String,
    red: Boolean,
) {
    Text(
        text = text,
        style =
            TextStyle(
                color = if (red) holidayProvider() else GlanceTheme.colors.onSurfaceVariant,
                fontSize = WidgetType.labelSmall,
                fontWeight = WidgetType.labelWeight,
                textAlign = TextAlign.Center,
            ),
        maxLines = 1,
    )
}

/** The next day with an event, as one tappable line under the week. */
@Composable
private fun NextLine(next: WidgetDayRow) {
    val context = LocalContext.current
    Text(
        text = "${next.names} · ${next.relative}",
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .shapeBackground(R.drawable.widget_shape_pill, surfaceContainerProvider())
                .clickable(actionStartActivity(widgetIntent(context, next.date)))
                .semantics { testTag = "week_next" }
                .padding(horizontal = 12.dp, vertical = 6.dp),
        style =
            TextStyle(
                color = if (next.isHoliday) holidayProvider() else GlanceTheme.colors.onSurface,
                fontSize = WidgetType.body,
                fontWeight = WidgetType.bodyWeight,
            ),
        maxLines = 1,
    )
}

/**
 * The rest of the week as compact lines, "24  Govardhan Puja, Mha Puja", the day in the holiday ink
 * when it is one. Lines fill [height]; the days that do not fit end the last line as "+N".
 */
@Composable
private fun ColumnScope.WeekAgenda(
    model: WeekWidgetModel,
    height: Dp,
) {
    val context = LocalContext.current
    val fit = widgetFit()
    val slots = (height / fit.line(AGENDA_TEXT)).toInt().coerceAtLeast(1)
    val shown = if (model.agenda.size > slots) model.agenda.take(slots - 1) else model.agenda
    Column(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .padding(top = 6.dp)
                .shapeBackground(R.drawable.widget_shape_12, surfaceContainerProvider())
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        shown.forEach { row -> AgendaLine(row) }
        if (shown.size < model.agenda.size) {
            Text(
                text = model.more(model.agenda.size - shown.size),
                modifier = GlanceModifier.padding(start = AGENDA_DAY),
                style =
                    TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = AGENDA_TEXT,
                        fontWeight = FontWeight.Medium,
                    ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AgendaLine(row: WidgetDayRow) {
    val context = LocalContext.current
    val ink = if (row.isHoliday) holidayProvider() else GlanceTheme.colors.onSurface
    Row(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity(widgetIntent(context, row.date)))
                .semantics { testTag = "week_agenda_${row.date.toIsoString()}" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.bsDay,
            modifier = GlanceModifier.width(AGENDA_DAY),
            style = TextStyle(color = ink, fontSize = AGENDA_TEXT, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        Text(
            text = row.names,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(color = ink, fontSize = AGENDA_TEXT),
            maxLines = 1,
        )
    }
}

private val TALL_WEEK = 100.dp
private val AGENDA_WEEK = 160.dp
private val AGENDA_WEEK_ROW = 64.dp

/** Below this the month title gives way, so a two-row week keeps its days and the next line. */
private val TITLED_WEEK = 150.dp

/** The padding, title, week row and agenda padding above and around the agenda lines. */
private val AGENDA_CHROME = 128.dp
private val AGENDA_TEXT = 13.sp
private val AGENDA_DAY = 30.dp
private val LABELLED_WEEK = 76.dp
