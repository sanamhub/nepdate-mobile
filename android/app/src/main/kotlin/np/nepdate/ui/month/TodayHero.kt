package np.nepdate.ui.month

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.Patro
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.ShapeBadge
import np.nepdate.ui.common.SunnyShape
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.weekdayName
import np.nepdate.ui.theme.LocalNepdateColors
import np.nepdate.ui.upcoming.upcoming

/**
 * The today card at the top of the patro: the BS day on the Sunny shape, the weekday, tithi, AD
 * date, the day's first event and the countdown to the next public holiday. One tinted container,
 * the only hero on the screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TodayHero(
    today: NepaliDate,
    lang: Lang,
    digits: Digits,
    onOpenDay: (NepaliDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNepdateColors.current
    val info = remember(today) { if (today in Patro.coverage) Patro.info(today) else null }
    val nextHoliday = remember(today) { upcoming(today, limit = HOLIDAY_LOOKAHEAD).entries.firstOrNull { it.isHoliday } }
    val adMonths = stringArrayResource(R.array.ad_month_long).toList()
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { onOpenDay(today) }
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShapeBadge(shape = SunnyShape, color = colors.today, size = 96.dp) {
                Text(
                    text = today.dayOfMonth.toString().inDigits(digits),
                    color = colors.onToday,
                    fontSize = 44.sp,
                    lineHeight = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_today_tag),
                    color = onContainer.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = weekdayName(today, lang),
                    color = onContainer,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                info?.tithi?.let { tithi ->
                    Text(text = tithi.name(lang), color = onContainer, style = MaterialTheme.typography.bodyLarge)
                }
                Text(
                    text = adDate(today, adMonths, digits),
                    color = onContainer.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            info?.events?.firstOrNull()?.let { event ->
                HeroChip(
                    text = event.name(lang).inDigits(digits),
                    container = onContainer.copy(alpha = 0.08f),
                    content = onContainer,
                    icon = R.drawable.ic_sym_celebration_filled,
                )
            }
            nextHoliday?.let { entry ->
                val name = entry.name.name(lang).inDigits(digits)
                val text =
                    when (entry.daysFromToday) {
                        0 -> stringResource(R.string.home_next_holiday_today, name)
                        1 -> stringResource(R.string.home_next_holiday_tomorrow, name)
                        else ->
                            stringResource(
                                R.string.home_next_holiday_days,
                                name,
                                entry.daysFromToday.toString().inDigits(digits),
                            )
                    }
                HeroChip(
                    text = text,
                    container = colors.holidayContainer,
                    content = colors.onHolidayContainer,
                    icon = null,
                    onClick = { onOpenDay(entry.date) },
                )
            }
        }
    }
}

@Composable
private fun HeroChip(
    text: String,
    container: Color,
    content: Color,
    icon: Int?,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier
                .heightIn(min = 36.dp)
                .clip(CircleShape)
                .background(container)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            SymbolIcon(icon = icon, tint = content, size = 16.dp)
            Spacer(Modifier.width(6.dp))
        }
        Text(text = text, color = content, style = MaterialTheme.typography.labelLarge, maxLines = 2)
    }
}

private const val HOLIDAY_LOOKAHEAD = 60
