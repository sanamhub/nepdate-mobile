package np.nepdate.ui.upcoming

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.nepdate.R
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.theme.LocalNepdateColors

/**
 * The BS month above its festivals: the month and year, how many public holidays it has left, and a
 * button that opens the month in the patro when [onOpenMonth] is wired.
 */
@Composable
internal fun ParvaMonthHeader(
    group: VisibleGroup,
    strings: ParvaStrings,
    onOpenMonth: ((year: Int, month: Int) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val title = strings.monthTitle(group.year, group.month)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f, fill = false).semantics { heading() },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        if (group.holidays > 0) HolidayCount(group.holidays, strings)
        Spacer(Modifier.weight(1f))
        if (onOpenMonth != null) {
            val label = stringResource(R.string.parva_open_month, strings.monthName(group.month))
            IconButton(
                onClick = { onOpenMonth(group.year, group.month) },
                shapes = IconButtonDefaults.shapes(),
                modifier =
                    Modifier
                        .testTag("parva_open_month_${group.year}_${group.month}")
                        .semantics { contentDescription = label },
            ) {
                SymbolIcon(R.drawable.ic_sym_calendar_month, tint = MaterialTheme.colorScheme.primary, size = 22.dp)
            }
        }
    }
}

/** A small holiday-tinted pill: "2 holidays". */
@Composable
private fun HolidayCount(
    count: Int,
    strings: ParvaStrings,
) {
    val colors = LocalNepdateColors.current
    Text(
        text = pluralStringResource(R.plurals.parva_month_holidays, count, count).inDigits(strings.digits),
        modifier =
            Modifier
                .clip(CircleShape)
                .background(colors.holidayContainer)
                .padding(horizontal = 10.dp, vertical = 3.dp),
        color = colors.onHolidayContainer,
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
    )
}
