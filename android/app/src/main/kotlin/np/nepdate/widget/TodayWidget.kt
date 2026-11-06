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
 * the Sunny shape; the right side lists the next days that have a holiday or an event, one row per
 * day. Every part opens the app: the block and the empty space on today, a row on its own day.
 */
@Composable
internal fun UpcomingWidgetContent(
    model: UpcomingWidgetModel,
    opacity: WidgetOpacity,
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
        TodayBlock(model.today, context)
        UpcomingEntries(model, context)
    }
}

/** The left block: month, numeral, weekday and tithi, sized from the widget height. */
@Composable
private fun TodayBlock(
    today: TodayWidgetModel,
    context: Context,
) {
    val height = LocalSize.current.height
    val scale = typeScale(height)
    val sunny = (height - BLOCK_CHROME * scale).coerceIn(MIN_SUNNY, MAX_SUNNY)
    val ink = GlanceTheme.colors.onPrimaryContainer
    Column(
        modifier =
            GlanceModifier
                .width(sunny + BLOCK_PADDING)
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
            text = today.bsMonthYear.substringBeforeLast(" "),
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
            text = today.weekday,
            style =
                TextStyle(
                    color = if (today.isHoliday || today.isSaturday) holidayProvider() else ink,
                    fontSize = 13.sp * scale,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            maxLines = 1,
        )
        if (today.tithi.isNotEmpty() && height >= TITHI_HEIGHT) {
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
) {
    val height = LocalSize.current.height
    val rowHeight = if (height >= ROOMY) 48.dp else 40.dp
    val fitting = ((height - LIST_CHROME) / (rowHeight + ROW_GAP)).toInt().coerceIn(1, UPCOMING_MAX_ROWS)
    Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight().padding(start = 10.dp, end = 4.dp)) {
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
        model.entries.take(fitting).forEachIndexed { index, row ->
            if (index > 0) Spacer(GlanceModifier.height(ROW_GAP))
            UpcomingRow(row, index, rowHeight, context)
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

@Composable
private fun UpcomingRow(
    row: WidgetDayRow,
    index: Int,
    height: Dp,
    context: Context,
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
            Text(
                text = row.relative,
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp),
                maxLines = 1,
            )
        }
    }
}

private val BLOCK_CHROME = 70.dp
private val BLOCK_PADDING = 18.dp
private val MIN_SUNNY = 36.dp
private val MAX_SUNNY = 112.dp
private val ROOMY = 170.dp
private val LIST_CHROME = 40.dp
private val ROW_GAP = 4.dp
private val TITHI_HEIGHT = 130.dp
