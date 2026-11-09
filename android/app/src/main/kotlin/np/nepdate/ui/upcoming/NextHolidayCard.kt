package np.nepdate.ui.upcoming

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.Lang
import np.nepdate.R
import np.nepdate.ui.common.DayMark
import np.nepdate.ui.common.ShapeBadge
import np.nepdate.ui.common.SoftBurstShape
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.weekdayName
import np.nepdate.ui.day.relativeDayLabel
import np.nepdate.ui.theme.LocalNepdateColors

/**
 * The holiday-tinted card with the countdown to the next public holiday. The number sits on the
 * Soft Burst, the same mark the today card uses for the next holiday, so the two read as one idea.
 */
@Composable
internal fun NextHolidayCard(
    entry: UpcomingEntry,
    lang: Lang,
    strings: ParvaStrings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNepdateColors.current
    val content = colors.onHolidayContainer
    Row(
        modifier =
            modifier
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(colors.holidayContainer)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .testTag("parva_next_holiday"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Countdown(days = entry.daysFromToday, strings = strings)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text =
                    stringResource(
                        if (entry.daysFromToday == 0) R.string.parva_holiday_today else R.string.parva_next_holiday,
                    ),
                color = content.copy(alpha = LABEL_ALPHA),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = entry.name.name(lang).inDigits(strings.digits),
                color = content,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text =
                    stringResource(
                        R.string.day_weekday_ad,
                        weekdayName(entry.date, lang),
                        strings.bsLong(entry.date),
                    ),
                color = content,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** The days left on the Soft Burst, with the unit under it. */
@Composable
private fun Countdown(
    days: Int,
    strings: ParvaStrings,
) {
    val colors = LocalNepdateColors.current
    Column(modifier = Modifier.widthIn(min = 84.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        ShapeBadge(shape = SoftBurstShape, color = colors.onHolidayContainer, size = 84.dp) {
            // "0 days" reads as a bug, so a holiday that is today gets the celebration mark instead.
            if (days == 0) {
                DayMark(holiday = true, color = colors.holidayContainer, size = 36.dp)
            } else {
                Text(
                    text = days.toString().inDigits(strings.digits),
                    color = colors.holidayContainer,
                    fontSize = 34.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
        Text(
            text = if (days == 0) relativeDayLabel(0) else pluralStringResource(R.plurals.parva_days_left, days),
            modifier = Modifier.padding(top = 6.dp),
            color = colors.onHolidayContainer,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}

private const val LABEL_ALPHA = 0.8f
