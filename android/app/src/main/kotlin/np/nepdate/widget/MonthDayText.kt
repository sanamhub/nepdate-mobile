package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import np.nepdate.R

/**
 * One W-03 day. A public holiday gets a soft tile in the holiday colour (the old solid red tile was
 * too heavy), today the Sunny shape; the marker under the numeral tells a holiday from an event.
 */
@Composable
internal fun RowScope.MonthDayCell(
    cell: MonthWidgetCell,
    fontSize: TextUnit,
    showAd: Boolean,
) {
    val context = LocalContext.current
    val date = cell.date
    val softTile = cell.isHoliday && cell.isInMonth && !cell.isToday
    Box(
        modifier =
            GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .then(
                    if (softTile) holidayTile() else GlanceModifier,
                ).then(
                    if (date != null) {
                        GlanceModifier.clickable(actionStartActivity(widgetIntent(context, date)))
                    } else {
                        GlanceModifier
                    },
                ).semantics { if (date != null) testTag = "month_day_${date.toIsoString()}" },
        contentAlignment = Alignment.Center,
    ) {
        if (cell.isToday) SunnyShape(GlanceModifier.fillMaxSize())
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = cell.bsDay,
                style =
                    TextStyle(
                        color = dayInk(cell),
                        fontSize = fontSize,
                        fontWeight = if (cell.isInMonth) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                    ),
                maxLines = 1,
            )
            if (showAd && cell.isInMonth) {
                Text(
                    text = cell.adDay,
                    style =
                        TextStyle(
                            color = if (cell.isToday) onTodayProvider() else GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                        ),
                    maxLines = 1,
                )
            }
            if (cell.isInMonth) DayMarker(cell.isHoliday, cell.hasEvents, onToday = cell.isToday)
        }
    }
}

@Composable
private fun dayInk(cell: MonthWidgetCell): ColorProvider =
    when {
        !cell.isInMonth -> GlanceTheme.colors.onSurfaceVariant
        cell.isToday -> onTodayProvider()
        cell.isHoliday || cell.isSaturday -> holidayProvider()
        else -> GlanceTheme.colors.onSurface
    }

/** The soft holiday tile behind a day; the red numeral on it still clears 4.5:1. */
@Composable
internal fun holidayTile(): GlanceModifier =
    GlanceModifier.softShapeBackground(R.drawable.widget_shape_cell, holidayProvider(), HOLIDAY_TILE_ALPHA)

private const val HOLIDAY_TILE_ALPHA = 0.14f
