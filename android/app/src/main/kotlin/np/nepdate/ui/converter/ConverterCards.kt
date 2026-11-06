package np.nepdate.ui.converter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.DateDiff
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.GroupPosition
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.groupShape
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.weekdayName
import kotlin.math.abs

/** The live result: the date in the other calendar, its weekday and how far it is from today. */
@Composable
internal fun ResultCard(
    conversion: Conversion,
    direction: ConvertDirection,
    lang: Lang,
    digits: Digits,
    onShowInPatro: ((NepaliDate) -> Unit)?,
) {
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    val adMonthNames = stringArrayResource(R.array.ad_month_long).toList()
    val label: String
    val main: String
    when (direction) {
        ConvertDirection.BS_TO_AD -> {
            label = stringResource(R.string.converter_in_ad)
            main = adDate(conversion.ad, adMonthNames, digits)
        }
        ConvertDirection.AD_TO_BS -> {
            label = stringResource(R.string.converter_in_bs)
            main = bsLong(conversion.bs, bsMonthNames[conversion.bs.monthValue - 1], digits)
        }
    }
    val sub =
        stringResource(
            R.string.converter_weekday_ad,
            weekdayName(conversion.bs, lang),
            relativeText(conversion.daysFromToday, digits),
        )
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            color = onContainer.copy(alpha = 0.8f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = main,
            modifier =
                Modifier
                    .testTag("converter_result")
                    .semantics { liveRegion = LiveRegionMode.Polite },
            color = onContainer,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = sub,
            modifier = Modifier.testTag("converter_result_detail"),
            color = onContainer,
            style = MaterialTheme.typography.bodyLarge,
        )
        ResultActions(date = conversion.bs, text = main, onShowInPatro = onShowInPatro)
    }
}

/** The result card's buttons: open the date in the month view, and copy the converted text. */
@Composable
private fun ResultActions(
    date: NepaliDate,
    text: String,
    onShowInPatro: ((NepaliDate) -> Unit)?,
) {
    val context = LocalContext.current
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    Row(
        modifier = Modifier.padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onShowInPatro != null) {
            Button(
                onClick = { onShowInPatro(date) },
                contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(),
            ) {
                SymbolIcon(icon = R.drawable.ic_sym_calendar_month, size = 18.dp)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.converter_show_in_patro), maxLines = 1)
            }
        }
        val copyLabel = stringResource(R.string.day_copy_date)
        IconButton(
            onClick = { copyText(context, text) },
            modifier =
                Modifier
                    .testTag("converter_copy")
                    .semantics { contentDescription = copyLabel },
            colors = IconButtonDefaults.iconButtonColors(containerColor = onContainer.copy(alpha = 0.08f)),
        ) {
            SymbolIcon(icon = R.drawable.ic_sym_content_copy, tint = onContainer, size = 18.dp)
        }
    }
}

/** The years, months and days between two dates, with the total day count in a pill. */
@Composable
internal fun BetweenCard(
    comparison: Comparison,
    today: NepaliDate,
    direction: ConvertDirection,
    digits: Digits,
    onFrom: (NepaliDate) -> Unit,
    onTo: (NepaliDate) -> Unit,
) {
    var editing by remember { mutableStateOf<BetweenEnd?>(null) }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.converter_between_title),
            modifier =
                Modifier
                    .padding(start = 4.dp)
                    .semantics { heading() },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        BetweenDates(
            comparison = comparison,
            today = today,
            direction = direction,
            digits = digits,
            onEdit = { editing = it },
        )
        BetweenSummary(diff = comparison.diff, digits = digits)
    }
    editing?.let { end ->
        BetweenDialog(
            end = end,
            comparison = comparison,
            direction = direction,
            digits = digits,
            onDismiss = { editing = null },
            onConfirm = { date ->
                if (end == BetweenEnd.FROM) onFrom(date) else onTo(date)
                editing = null
            },
        )
    }
}

private enum class BetweenEnd { FROM, TO }

/** The two tappable ends of the comparison, shown in the calendar the wheel reads from. */
@Composable
private fun BetweenDates(
    comparison: Comparison,
    today: NepaliDate,
    direction: ConvertDirection,
    digits: Digits,
    onEdit: (BetweenEnd) -> Unit,
) {
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    val adMonthNames = stringArrayResource(R.array.ad_month_long).toList()
    val display: (NepaliDate) -> String = { date ->
        when (direction) {
            ConvertDirection.BS_TO_AD -> bsLong(date, bsMonthNames[date.monthValue - 1], digits)
            ConvertDirection.AD_TO_BS -> adDate(date, adMonthNames, digits)
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        DateTile(
            label = stringResource(R.string.converter_between_from),
            value = display(comparison.from),
            shape = groupShape(GroupPosition.START, outer = 16.dp, inner = 4.dp),
            tag = "converter_from",
            onClick = { onEdit(BetweenEnd.FROM) },
            modifier = Modifier.weight(1f),
        )
        DateTile(
            label =
                stringResource(
                    if (comparison.to == today) R.string.converter_between_to_today else R.string.converter_between_to,
                ),
            value = display(comparison.to),
            shape = groupShape(GroupPosition.END, outer = 16.dp, inner = 4.dp),
            tag = "converter_to",
            onClick = { onEdit(BetweenEnd.TO) },
            modifier = Modifier.weight(1f),
        )
    }
}

/** The difference in years, months and days, and the total day count in a pill. */
@Composable
private fun BetweenSummary(
    diff: DateDiff,
    digits: Digits,
) {
    Row(
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = differenceText(diff, digits),
            modifier =
                Modifier
                    .weight(1f)
                    .testTag("converter_comparison"),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        val total = abs(diff.totalDays).toInt()
        Text(
            text = pluralStringResource(R.plurals.converter_days, total, total).inDigits(digits),
            modifier =
                Modifier
                    .heightIn(min = 28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("converter_total_days"),
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

/** The wheel dialog for the end of the comparison being edited. */
@Composable
private fun BetweenDialog(
    end: BetweenEnd,
    comparison: Comparison,
    direction: ConvertDirection,
    digits: Digits,
    onDismiss: () -> Unit,
    onConfirm: (NepaliDate) -> Unit,
) {
    DatePickDialog(
        title =
            stringResource(
                if (end == BetweenEnd.FROM) R.string.converter_choose_first else R.string.converter_choose_second,
            ),
        initial = if (end == BetweenEnd.FROM) comparison.from else comparison.to,
        direction = direction,
        digits = digits,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
private fun DateTile(
    label: String,
    value: String,
    shape: Shape,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .heightIn(min = 56.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable(role = Role.Button, onClick = onClick)
                .testTag(tag)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
        )
    }
}

private fun copyText(
    context: Context,
    text: String,
) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.day_copy_label), text))
    Toast.makeText(context, R.string.day_copied, Toast.LENGTH_SHORT).show()
}

@Composable
private fun relativeText(
    days: Int,
    digits: Digits,
): String =
    when {
        days == 0 -> stringResource(R.string.converter_relative_today)
        days == 1 -> stringResource(R.string.converter_relative_tomorrow)
        days == -1 -> stringResource(R.string.converter_relative_yesterday)
        days > 0 -> pluralStringResource(R.plurals.converter_days_from_today, days, days)
        else -> pluralStringResource(R.plurals.converter_days_before_today, -days, -days)
    }.inDigits(digits)

@Composable
private fun differenceText(
    diff: DateDiff,
    digits: Digits,
): String =
    listOf(
        pluralStringResource(R.plurals.converter_years, diff.years, diff.years),
        pluralStringResource(R.plurals.converter_months, diff.months, diff.months),
        pluralStringResource(R.plurals.converter_days, diff.days, diff.days),
    ).joinToString(", ").inDigits(digits)
