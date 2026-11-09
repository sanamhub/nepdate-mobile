package np.nepdate.widget

import android.content.Context
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
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
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
 * W-02 Today and upcoming. Today sits in a primary container block on the left, with the numeral on
 * the Sunny shape; the right side names today's own holidays and events first, in a tinted note,
 * then lists the next days that have a holiday or an event, one row per day. Every part opens the
 * app: the block, the note and the empty space on today, a row on its own day.
 */
@Composable
internal fun UpcomingWidgetContent(
    model: UpcomingWidgetModel,
    opacity: WidgetOpacity,
    minimal: Boolean = false,
) {
    val context = LocalContext.current
    val label = context.getString(R.string.widget_upcoming_name)
    Row(
        modifier =
            widgetRoot(opacity, GlanceTheme.colors.surface)
                .clickable(actionStartActivity(widgetIntent(context)))
                .semantics {
                    contentDescription = label
                    testTag = "upcoming_widget"
                }.padding(8.dp),
    ) {
        TodayBlock(model.today, context, minimal)
        UpcomingEntries(model, context, minimal)
    }
}

/**
 * The left block: month, numeral, weekday and tithi. Its width is a share of the widget, and its
 * type grows with the height only as far as that width holds, so the weekday is never cut.
 */
@Composable
private fun TodayBlock(
    today: TodayWidgetModel,
    context: Context,
    minimal: Boolean,
) {
    val size = LocalSize.current
    val fit = widgetFit()
    val width = (size.width * BLOCK_SHARE).coerceIn(MIN_BLOCK, MAX_BLOCK)
    val text = width - BLOCK_PADDING
    val scale = columnScale(size.height, text, BLOCK_WIDTH_AT_ONE)
    val chrome = fit.line(12.sp * scale) + fit.line(13.sp * scale) + BLOCK_VERTICAL
    val showTithi =
        !minimal &&
            today.tithi.isNotEmpty() &&
            fit.fits(today.tithi, 11.sp * scale, text) &&
            size.height - chrome - fit.line(11.sp * scale) >= MIN_SUNNY + BLOCK_PADDING
    val sunny =
        (size.height - chrome - (if (showTithi) fit.line(11.sp * scale) else 0.dp) - BLOCK_PADDING)
            .coerceIn(MIN_SUNNY, minOf(MAX_SUNNY, text))
    val ink = GlanceTheme.colors.onPrimaryContainer
    Column(
        modifier =
            GlanceModifier
                .width(width)
                .fillMaxHeight()
                .shapeBackground(R.drawable.widget_shape_20, GlanceTheme.colors.primaryContainer)
                .clickable(actionStartActivity(widgetIntent(context)))
                .semantics { testTag = "upcoming_today" }
                .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            // The block is only as wide as the numeral, so it names the month without the year.
            text = today.monthName,
            style =
                TextStyle(
                    color = ink,
                    fontSize = 12.sp * scale,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            maxLines = 1,
        )
        SunnyNumeral(today.bsDay, sunny)
        Text(
            text = if (fit.fits(today.weekday, 13.sp * scale, text)) today.weekday else today.weekdayShort,
            style =
                TextStyle(
                    color = if (today.isHoliday || today.isSaturday) holidayProvider() else ink,
                    fontSize = 13.sp * scale,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            maxLines = 1,
        )
        if (showTithi) {
            Text(
                text = today.tithi,
                style = TextStyle(color = ink, fontSize = 11.sp * scale, textAlign = TextAlign.Center),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RowScope.UpcomingEntries(
    model: UpcomingWidgetModel,
    context: Context,
    minimal: Boolean,
) {
    val height = LocalSize.current.height
    val rowHeight = if (height >= ROOMY) 48.dp else 40.dp
    val layout = upcomingLayout(height, rowHeight, model.todayRow.takeUnless { minimal }, widgetFit())
    Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight().padding(start = 10.dp, end = 4.dp)) {
        model.todayRow.takeUnless { minimal }?.let { TodayNote(it, layout, context) }
        if (layout.title) {
            Text(
                text = context.getString(R.string.widget3_upcoming_title),
                modifier = GlanceModifier.padding(start = 2.dp, top = 2.dp, bottom = 4.dp),
                style =
                    TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
            )
        }
        if (layout.rows == 0) return@Column
        model.entries.take(layout.rows).forEachIndexed { index, row ->
            if (index > 0) Spacer(GlanceModifier.height(ROW_GAP))
            UpcomingRow(row, index, rowHeight, context, minimal)
        }
        val footer =
            model.dataEnds ?: context.getString(R.string.upcoming_empty).takeIf { model.entries.isEmpty() }
        footer?.let {
            Text(
                text = it,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp),
                maxLines = 2,
            )
        }
    }
}

/** How today's note is drawn: over two lines with a caption, or as one line. */
private enum class NoteStyle { FULL, COMPACT }

/** The right side's plan: the note's style, whether the list has its title, and how many rows fit. */
private data class UpcomingLayout(
    val note: NoteStyle,
    val title: Boolean,
    val rows: Int,
)

/**
 * Today's holidays come first. A tall widget gives them the two-line note and keeps the list with
 * its title; a short one gives them one line and drops the title, since each row already says how
 * far away it is. Without anything today the list has the whole side, as before.
 */
private fun upcomingLayout(
    height: Dp,
    rowHeight: Dp,
    todayRow: WidgetDayRow?,
    fit: WidgetFit,
): UpcomingLayout {
    val room = height - ROOT_PADDING
    val slot = rowHeight + ROW_GAP
    val full = fit.line(NOTE_CAPTION) + fit.line(NOTE_NAMES) * NOTE_LINES + NOTE_CHROME
    val compact = fit.line(NOTE_NAMES) + COMPACT_NOTE_CHROME

    fun rows(
        space: Dp,
        min: Int,
    ) = (space / slot).toInt().coerceIn(min, UPCOMING_MAX_ROWS)
    return when {
        todayRow == null -> UpcomingLayout(NoteStyle.FULL, title = true, rows = rows(room - TITLE_HEIGHT, 1))
        room - full - TITLE_HEIGHT >= slot ->
            UpcomingLayout(NoteStyle.FULL, title = true, rows = rows(room - full - TITLE_HEIGHT, 1))
        else -> UpcomingLayout(NoteStyle.COMPACT, title = false, rows = rows(room - compact, 0))
    }
}

/**
 * Today's holidays and events on a tinted rounded block, so they read as part of today and not as
 * one more upcoming day: "Today" over up to two lines of names, or "Today" beside them when the
 * widget is short. A holiday's names take the holiday ink.
 */
@Composable
private fun TodayNote(
    row: WidgetDayRow,
    layout: UpcomingLayout,
    context: Context,
) {
    val ink = GlanceTheme.colors.onSecondaryContainer
    val names =
        TextStyle(
            color = if (row.isHoliday) holidayProvider() else ink,
            fontSize = NOTE_NAMES,
            fontWeight = FontWeight.Bold,
        )
    val modifier =
        GlanceModifier
            .fillMaxWidth()
            .shapeBackground(R.drawable.widget_shape_12, GlanceTheme.colors.secondaryContainer)
            .clickable(actionStartActivity(widgetIntent(context, row.date)))
            .semantics { testTag = "upcoming_today_note" }
    when (layout.note) {
        NoteStyle.FULL ->
            Column(modifier = modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(
                    text = row.relative,
                    style = TextStyle(color = ink, fontSize = NOTE_CAPTION, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
                Text(text = row.names, style = names, maxLines = NOTE_LINES)
            }
        NoteStyle.COMPACT ->
            Row(
                modifier = modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = row.relative,
                    modifier = GlanceModifier.padding(end = 6.dp),
                    style = TextStyle(color = ink, fontSize = NOTE_CAPTION, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
                Text(
                    text = row.names,
                    modifier = GlanceModifier.defaultWeight(),
                    style = names,
                    maxLines = 1,
                )
            }
    }
    Spacer(GlanceModifier.height(NOTE_GAP))
}

@Composable
private fun UpcomingRow(
    row: WidgetDayRow,
    index: Int,
    height: Dp,
    context: Context,
    minimal: Boolean,
) {
    Row(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .height(height)
                .clickable(actionStartActivity(widgetIntent(context, row.date)))
                .semantics { testTag = "upcoming_entry_$index" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateBadge(row, height - 4.dp)
        Column(modifier = GlanceModifier.defaultWeight().padding(start = 10.dp)) {
            Text(
                text = row.names,
                style =
                    TextStyle(
                        color = if (row.isHoliday) holidayProvider() else GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
            )
            if (!minimal) {
                Text(
                    text = row.relative,
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
                    maxLines = 1,
                )
            }
        }
    }
}

private const val BLOCK_SHARE = 0.32f
private val MIN_BLOCK = 84.dp
private val MAX_BLOCK = 132.dp
private val BLOCK_PADDING = 12.dp
private val BLOCK_VERTICAL = 28.dp
private val BLOCK_WIDTH_AT_ONE = 80.dp
private val MIN_SUNNY = 36.dp
private val MAX_SUNNY = 112.dp
private val ROOMY = 170.dp
private val ROOT_PADDING = 16.dp
private val TITLE_HEIGHT = 24.dp
private val ROW_GAP = 4.dp
private val NOTE_CAPTION = 11.sp
private val NOTE_NAMES = 13.sp
private const val NOTE_LINES = 2
private val NOTE_GAP = 6.dp

/** The note's vertical padding and the gap under it, over two lines and on one. */
private val NOTE_CHROME = 18.dp
private val COMPACT_NOTE_CHROME = 14.dp
