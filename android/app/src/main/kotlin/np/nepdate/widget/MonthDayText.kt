package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle

/** The numerals, AD day and event dot inside one W-03 cell. */
@Composable
internal fun MonthDayText(
    cell: MonthWidgetCell,
    fontSize: TextUnit,
    showAd: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = cell.bsDay,
            style =
                TextStyle(
                    color =
                        when {
                            !cell.isInMonth -> GlanceTheme.colors.onSurfaceVariant
                            cell.isHoliday -> holidayProvider()
                            cell.isToday -> todayProvider()
                            cell.isSaturday -> holidayProvider()
                            else -> GlanceTheme.colors.onSurface
                        },
                    fontSize = fontSize,
                    fontWeight = if (cell.isToday) FontWeight.Medium else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                ),
            maxLines = 1,
        )
        if (showAd && cell.isInMonth) {
            Text(
                text = cell.adDay,
                style =
                    TextStyle(
                        color = if (cell.isHoliday) holidayProvider() else GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 6.sp,
                        textAlign = TextAlign.Center,
                    ),
                maxLines = 1,
            )
        }
        if (cell.hasEvents) {
            Box(
                modifier =
                    GlanceModifier
                        .padding(top = 1.dp)
                        .size(2.dp)
                        .background(GlanceTheme.colors.onSurfaceVariant),
            ) {}
        }
    }
}
