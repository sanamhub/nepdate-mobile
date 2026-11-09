package np.nepdate.ui.day

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.CalendarInfo
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.DayMark
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.theme.LocalNepdateColors

/** The public holiday chip and, when today is known, how far the date is from today. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayDetailChips(
    date: NepaliDate,
    info: CalendarInfo?,
    today: NepaliDate?,
    digits: Digits,
) {
    val holiday = info?.isPublicHoliday == true
    if (!holiday && today == null) return
    val colors = LocalNepdateColors.current
    FlowRow(
        modifier = Modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (holiday) {
            Row(
                modifier =
                    Modifier
                        .heightIn(min = 32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.holidayContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DayMark(holiday = true, color = colors.onHolidayContainer)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.public_holiday),
                    color = colors.onHolidayContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        if (today != null) {
            Text(
                text = relativeDayLabel(today.daysUntil(date).toInt()).inDigits(digits),
                modifier =
                    Modifier
                        .heightIn(min = 32.dp)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

/**
 * How far [days] from today is, for example `Today`, `Tomorrow`, `in 5 days` or `3 days ago`. The
 * caller rewrites the digits with [inDigits].
 */
@Composable
internal fun relativeDayLabel(days: Int): String =
    when {
        days == 0 -> stringResource(R.string.month_today)
        days == 1 -> stringResource(R.string.upcoming_tomorrow)
        days == -1 -> stringResource(R.string.day_yesterday)
        days > 1 -> pluralStringResource(R.plurals.upcoming_in_days, days, days)
        else -> pluralStringResource(R.plurals.day_days_ago, -days, -days)
    }
