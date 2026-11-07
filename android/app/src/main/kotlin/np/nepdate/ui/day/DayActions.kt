package np.nepdate.ui.day

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.CalendarInfo
import np.nepdate.R
import np.nepdate.data.Settings
import np.nepdate.ui.common.GroupPosition
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.groupShape
import np.nepdate.ui.common.weekdayName
import java.time.ZoneId

/** Add to calendar, share and copy as one connected button group. */
@Composable
internal fun DayDetailActions(
    date: NepaliDate,
    info: CalendarInfo?,
    lang: Lang,
    settings: Settings,
    bsMonthNames: List<String>,
    onCopied: () -> Unit = {},
) {
    val context = LocalContext.current
    val publicHoliday = stringResource(R.string.public_holiday)
    val title = eventTitle(date, info, lang, publicHoliday, settings, bsMonthNames)
    val adMonthNames = stringArrayResource(R.array.ad_month_long).toList()
    val weekdayAd =
        stringResource(
            R.string.day_weekday_ad,
            weekdayName(date, lang),
            adDate(date, adMonthNames, settings.digits),
        )
    val chooserTitle = stringResource(R.string.day_share_chooser)
    val shareText =
        buildList {
            add(bsLong(date, bsMonthNames[date.monthValue - 1], settings.digits))
            add(weekdayAd)
            info?.events?.forEach { add(it.name(lang)) }
        }.joinToString("\n")
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ActionButton(
            label = stringResource(R.string.day_add_to_calendar),
            icon = R.drawable.ic_sym_event_available,
            position = GroupPosition.START,
            primary = true,
            onClick = { openCalendar(context, date, title) },
            modifier = Modifier.weight(PRIMARY_WEIGHT),
        )
        ActionButton(
            label = stringResource(R.string.day_share),
            icon = R.drawable.ic_sym_share,
            position = GroupPosition.MIDDLE,
            primary = false,
            onClick = { shareDate(context, shareText, chooserTitle) },
            modifier = Modifier.weight(1f),
        )
        ActionButton(
            label = stringResource(R.string.day_copy_date),
            icon = R.drawable.ic_sym_content_copy,
            position = GroupPosition.END,
            primary = false,
            onClick = {
                copyDate(context, date, settings, bsMonthNames)
                onCopied()
            },
            modifier = Modifier.weight(1f),
        )
    }
}

/** One button of the action group: the symbol above a label that may wrap to two lines. */
@Composable
private fun ActionButton(
    label: String,
    @DrawableRes icon: Int,
    position: GroupPosition,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Pressing squares the button a little, the Expressive press feedback, then it springs back.
    val shapes =
        ButtonDefaults.shapes(
            shape = groupShape(position, outer = 28.dp, inner = 8.dp),
            pressedShape = RoundedCornerShape(PRESSED_CORNER),
        )
    val padding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    val buttonModifier = modifier.fillMaxHeight().heightIn(min = 64.dp)
    val content: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SymbolIcon(icon = icon, size = 20.dp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
    if (primary) {
        Button(onClick = onClick, shapes = shapes, modifier = buttonModifier, contentPadding = padding) { content() }
    } else {
        FilledTonalButton(
            onClick = onClick,
            shapes = shapes,
            modifier = buttonModifier,
            contentPadding = padding,
        ) { content() }
    }
}

private fun eventTitle(
    date: NepaliDate,
    info: CalendarInfo?,
    lang: Lang,
    publicHoliday: String,
    settings: Settings,
    bsMonthNames: List<String>,
): String =
    info?.events?.firstOrNull()?.name(lang)
        ?: if (info?.isPublicHoliday == true) {
            publicHoliday
        } else {
            bsLong(date, bsMonthNames[date.monthValue - 1], settings.digits)
        }

private fun openCalendar(
    context: Context,
    date: NepaliDate,
    title: String,
) {
    try {
        context.startActivity(CalendarInsert.intent(date, title, ZoneId.systemDefault()))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.day_calendar_unavailable, Toast.LENGTH_SHORT).show()
    }
}

/** Shares the date as plain text through the system chooser; it needs no permission. */
private fun shareDate(
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

private fun copyDate(
    context: Context,
    date: NepaliDate,
    settings: Settings,
    bsMonthNames: List<String>,
) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    val text = bsLong(date, bsMonthNames[date.monthValue - 1], settings.digits)
    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.day_copy_label), text))
    Toast.makeText(context, R.string.day_copied, Toast.LENGTH_SHORT).show()
}

private const val PRIMARY_WEIGHT = 1.4f
private val PRESSED_CORNER = 12.dp
