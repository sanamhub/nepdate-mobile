package np.nepdate.ui.month

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import np.nepdate.ui.common.FlowerShape
import np.nepdate.ui.common.ShapeBadge
import np.nepdate.ui.common.SoftBurstShape
import np.nepdate.ui.common.SunnyShape
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.weekdayName
import np.nepdate.ui.theme.LocalNepdateColors
import np.nepdate.ui.upcoming.upcoming

/**
 * The today card's place above the month: it grows in on the current month and folds away on any
 * other, on the expressive spatial spring, so the grid below slides rather than jumps.
 */
@Composable
internal fun ColumnScope.TodayHeroSlot(
    visible: Boolean,
    today: NepaliDate,
    lang: Lang,
    digits: Digits,
    onOpenDay: (NepaliDate) -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = visible,
        enter =
            expandVertically(motion.defaultSpatialSpec(), expandFrom = Alignment.Top) +
                scaleIn(motion.defaultSpatialSpec(), initialScale = ENTER_SCALE) +
                fadeIn(motion.defaultEffectsSpec()),
        exit =
            shrinkVertically(motion.fastSpatialSpec(), shrinkTowards = Alignment.Top) +
                fadeOut(motion.fastEffectsSpec()),
    ) {
        TodayHero(
            today = today,
            lang = lang,
            digits = digits,
            onOpenDay = onOpenDay,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
}

/**
 * The today card at the top of the patro: the BS day on the Sunny shape, the weekday, tithi, AD
 * date, the day's first event and the countdown to the next public holiday. One tinted container,
 * the only hero on the screen, with a quiet marigold flower in its corner.
 */
@Composable
internal fun TodayHero(
    today: NepaliDate,
    lang: Lang,
    digits: Digits,
    onOpenDay: (NepaliDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val info = remember(today) { if (today in Patro.coverage) Patro.info(today) else null }
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { onOpenDay(today) },
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 56.dp, y = (-64).dp)
                    .size(184.dp)
                    .background(onContainer.copy(alpha = DECORATION_ALPHA), FlowerShape),
        )
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeroDate(today = today, tithi = info?.tithi?.name(lang), lang = lang, digits = digits)
            HeroChips(
                today = today,
                event = info?.events?.firstOrNull()?.name(lang),
                lang = lang,
                digits = digits,
                onOpenDay = onOpenDay,
            )
        }
    }
}

/** The BS day on the Sunny shape, with the weekday, tithi and AD date beside it. */
@Composable
private fun HeroDate(
    today: NepaliDate,
    tithi: String?,
    lang: Lang,
    digits: Digits,
) {
    val colors = LocalNepdateColors.current
    val adMonths = stringArrayResource(R.array.ad_month_long).toList()
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    Row(verticalAlignment = Alignment.CenterVertically) {
        ShapeBadge(shape = SunnyShape, color = colors.today, size = 92.dp) {
            Text(
                text = today.dayOfMonth.toString().inDigits(digits),
                color = colors.onToday,
                fontSize = 40.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.displaySmall,
            )
        }
        Spacer(Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_today_tag),
                color = onContainer.copy(alpha = SOFT_ALPHA),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = weekdayName(today, lang),
                color = onContainer,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            tithi?.let {
                Text(text = it, color = onContainer, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = adDate(today, adMonths, digits),
                color = onContainer.copy(alpha = SOFT_ALPHA),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** The day's first event and the countdown to the next public holiday, as chips. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroChips(
    today: NepaliDate,
    event: String?,
    lang: Lang,
    digits: Digits,
    onOpenDay: (NepaliDate) -> Unit,
) {
    val colors = LocalNepdateColors.current
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val chip = onContainer.copy(alpha = CHIP_ALPHA)
    val nextHoliday =
        remember(today) { upcoming(today, limit = HOLIDAY_LOOKAHEAD).entries.firstOrNull { it.isHoliday } }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        event?.let {
            HeroChip(text = it.inDigits(digits), container = chip, content = onContainer) {
                SymbolIcon(icon = R.drawable.ic_sym_celebration_filled, tint = onContainer, size = 18.dp)
            }
        }
        nextHoliday?.let { entry ->
            HeroChip(
                text = nextHolidayText(entry.name.name(lang).inDigits(digits), entry.daysFromToday, digits),
                container = chip,
                content = onContainer,
                onClick = { onOpenDay(entry.date) },
            ) {
                // The holiday red rides on a small soft burst, so the chip keeps the card's calm tone.
                ShapeBadge(shape = SoftBurstShape, color = colors.holiday, size = 14.dp) {}
            }
        }
    }
}

@Composable
private fun nextHolidayText(
    name: String,
    days: Int,
    digits: Digits,
): String =
    when (days) {
        0 -> stringResource(R.string.home_next_holiday_today, name)
        1 -> stringResource(R.string.home_next_holiday_tomorrow, name)
        else -> stringResource(R.string.home_next_holiday_days, name, days.toString().inDigits(digits))
    }

@Composable
private fun HeroChip(
    text: String,
    container: Color,
    content: Color,
    onClick: (() -> Unit)? = null,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .then(if (onClick != null) Modifier.minimumInteractiveComponentSize() else Modifier)
                .heightIn(min = 40.dp)
                .clip(CircleShape)
                .background(container)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(Modifier.width(8.dp))
        Text(text = text, color = content, style = MaterialTheme.typography.labelLarge, maxLines = 2)
    }
}

private const val HOLIDAY_LOOKAHEAD = 60
private const val ENTER_SCALE = 0.94f
private const val SOFT_ALPHA = 0.8f
private const val CHIP_ALPHA = 0.1f
private const val DECORATION_ALPHA = 0.07f
