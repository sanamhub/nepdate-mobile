package np.nepdate.ui.converter

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.weekdayName

/** The live result: the date in the other calendar, its weekday and how far it is from today. */
@Composable
internal fun ResultCard(
    conversion: Conversion,
    direction: ConvertDirection,
    lang: Lang,
    digits: Digits,
    onShowInPatro: ((NepaliDate) -> Unit)?,
    onCopied: () -> Unit = {},
) {
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    val adMonthNames = stringArrayResource(R.array.ad_month_long).toList()
    val toAd = direction == ConvertDirection.BS_TO_AD
    val label = stringResource(if (toAd) R.string.converter_in_ad else R.string.converter_in_bs)
    val bsText = bsLong(conversion.bs, bsMonthNames[conversion.bs.monthValue - 1], digits)
    val adText = adDate(conversion.ad, adMonthNames, digits)
    val main = if (toAd) adText else bsText
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
        ResultActions(
            date = conversion.bs,
            copyText = main,
            shareText = stringResource(R.string.upcoming_date, bsText, adText),
            onShowInPatro = onShowInPatro,
            onCopied = onCopied,
        )
    }
}

/** The result card's buttons: open the date in the month view, copy the result and share both dates. */
@Composable
private fun ResultActions(
    date: NepaliDate,
    copyText: String,
    shareText: String,
    onShowInPatro: ((NepaliDate) -> Unit)?,
    onCopied: () -> Unit,
) {
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.day_share_chooser)
    Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onShowInPatro != null) {
            Button(
                onClick = { onShowInPatro(date) },
                shapes = ButtonDefaults.shapes(),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                SymbolIcon(icon = R.drawable.ic_sym_calendar_month, size = 18.dp)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.converter_show_in_patro), maxLines = 1)
            }
        }
        ResultIconButton(R.drawable.ic_sym_content_copy, stringResource(R.string.day_copy_date), "converter_copy") {
            copyText(context, copyText)
            onCopied()
        }
        ResultIconButton(R.drawable.ic_sym_share, stringResource(R.string.day_share), "converter_share") {
            shareText(context, shareText, chooserTitle)
        }
    }
}

@Composable
private fun ResultIconButton(
    @DrawableRes icon: Int,
    label: String,
    tag: String,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        shapes = IconButtonDefaults.shapes(),
        modifier = Modifier.testTag(tag).semantics { contentDescription = label },
    ) {
        SymbolIcon(icon = icon, size = 20.dp)
    }
}

private fun shareText(
    context: Context,
    text: String,
    chooserTitle: String,
) {
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    try {
        context.startActivity(Intent.createChooser(send, chooserTitle))
    } catch (_: ActivityNotFoundException) {
        // A chooser always resolves on a real device; nothing to do if it does not.
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
