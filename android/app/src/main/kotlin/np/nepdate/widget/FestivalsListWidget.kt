package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import np.nepdate.R

/**
 * The Festivals list widget: every upcoming day with a holiday or an event, as a scrolling list of
 * up to [LIST_MAX_ROWS] days within [LIST_WINDOW_DAYS] days. The chip in the header narrows it to
 * public holidays; [HolidayFilterAction] stores that per widget. A row opens its day in the app,
 * and the header opens the app on today. A picker preview cannot draw a scrolling list, so with
 * [scrolling] off the rows that fit are laid out in a plain column instead.
 */
@Composable
internal fun FestivalsWidgetContent(
    model: FestivalsListModel,
    opacity: WidgetOpacity,
    scrolling: Boolean = true,
) {
    val context = LocalContext.current
    Column(
        modifier =
            widgetRoot(opacity, GlanceTheme.colors.surface)
                .clickable(actionStartActivity(widgetIntent(context)))
                .semantics { testTag = "festivals_widget" }
                .padding(start = 10.dp, end = 10.dp, top = 2.dp, bottom = 10.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(start = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = context.getString(R.string.widget3_festivals_title),
                modifier = GlanceModifier.defaultWeight(),
                style =
                    TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = WidgetType.title,
                        fontWeight = WidgetType.titleWeight,
                    ),
                maxLines = 1,
            )
            HolidaysOnlyChip(model.holidaysOnly)
        }
        if (model.rows.isEmpty()) {
            Text(
                text = model.emptyText,
                modifier = GlanceModifier.padding(start = 6.dp, top = 4.dp),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp),
                maxLines = 2,
            )
        } else {
            val height = LocalSize.current.height
            val lines = if (height >= TWO_LINE_ROWS) 2 else 1
            if (scrolling) {
                LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    itemsIndexed(model.rows) { index, row -> FestivalListRow(row, index, lines) }
                }
            } else {
                val fits = ((height - LIST_HEADER) / (if (lines == 2) TWO_LINE_ROW else ONE_LINE_ROW)).toInt()
                Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    model.rows.take(fits.coerceAtLeast(1)).forEachIndexed {
                        index,
                        row,
                        ->
                        FestivalListRow(row, index, lines)
                    }
                }
            }
        }
    }
}

/**
 * The filter chip: outlined when off, filled with the secondary container and a check when on. The
 * outline and the label both clear 3:1 and 4.5:1 on the surface.
 */
@Composable
private fun HolidaysOnlyChip(holidaysOnly: Boolean) {
    val context = LocalContext.current
    val description =
        context.getString(if (holidaysOnly) R.string.widget2_holidays_only_on else R.string.widget2_holidays_only_off)
    val ink = if (holidaysOnly) GlanceTheme.colors.onSecondaryContainer else GlanceTheme.colors.onSurfaceVariant
    Box(
        modifier =
            GlanceModifier
                .height(TOUCH_TARGET)
                .clickable(actionRunCallback<HolidayFilterAction>())
                .semantics {
                    contentDescription = description
                    testTag = "festivals_filter"
                },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier =
                GlanceModifier
                    .height(32.dp)
                    .then(
                        if (holidaysOnly) {
                            GlanceModifier.shapeBackground(
                                R.drawable.widget_shape_chip,
                                GlanceTheme.colors.secondaryContainer,
                            )
                        } else {
                            GlanceModifier.shapeBackground(
                                R.drawable.widget_shape_chip_outline,
                                GlanceTheme.colors.outline,
                            )
                        },
                    ).padding(start = 8.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(if (holidaysOnly) R.drawable.ic_sym_check else R.drawable.ic_sym_filter_alt),
                contentDescription = null,
                modifier = GlanceModifier.size(18.dp),
                colorFilter = ColorFilter.tint(ink),
            )
            Text(
                text = context.getString(R.string.widget2_holidays_only),
                modifier = GlanceModifier.padding(start = 4.dp),
                style = TextStyle(color = ink, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FestivalListRow(
    row: WidgetDayRow,
    index: Int,
    lines: Int,
) {
    val context = LocalContext.current
    // Today's row takes the primary container, like the today faces of the other widgets.
    val container = if (row.isToday) GlanceTheme.colors.primaryContainer else surfaceContainerProvider()
    val plainInk = if (row.isToday) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSurface
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        if (index > 0) Spacer(GlanceModifier.height(4.dp))
        Row(
            modifier =
                GlanceModifier
                    .fillMaxWidth()
                    .shapeBackground(R.drawable.widget_shape_18, container)
                    .clickable(actionStartActivity(widgetIntent(context, row.date)))
                    .semantics { testTag = "festivals_entry_$index" }
                    .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DateBadge(row, 40.dp)
            Text(
                text = row.names,
                modifier = GlanceModifier.defaultWeight().padding(horizontal = 12.dp),
                style =
                    TextStyle(
                        color = if (row.isHoliday) holidayProvider() else plainInk,
                        fontSize = WidgetType.body,
                        fontWeight =
                            if (row.isHoliday ||
                                row.isToday
                            ) {
                                WidgetType.emphasisWeight
                            } else {
                                WidgetType.bodyWeight
                            },
                    ),
                maxLines = lines,
            )
            Text(
                text = row.relative,
                style =
                    TextStyle(
                        color = if (row.isToday) plainInk else GlanceTheme.colors.onSurfaceVariant,
                        fontSize = WidgetType.label,
                        fontWeight = WidgetType.labelWeight,
                    ),
                maxLines = 1,
            )
        }
    }
}

/** A list shorter than this shows one line per row, so a short widget never clips a row. */
private val TWO_LINE_ROWS = 150.dp

/** The header and padding above the rows, and the height of one row with its gap. */
private val LIST_HEADER = 60.dp
private val ONE_LINE_ROW = 56.dp
private val TWO_LINE_ROW = 64.dp
