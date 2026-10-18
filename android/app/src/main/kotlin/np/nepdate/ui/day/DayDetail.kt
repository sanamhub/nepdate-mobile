package np.nepdate.ui.day

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.CalendarInfo
import io.github.sanamhub.nepdate.patro.Patro
import np.nepdate.R
import np.nepdate.data.Settings
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.bsLongNepali
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.weekdayName
import np.nepdate.ui.theme.LocalNepdateColors
import np.nepdate.ui.theme.holidayFillOn
import java.time.ZoneId

/** The modal day sheet from MR-02. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailSheet(
    date: NepaliDate,
    settings: Settings,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        DayDetailContent(date = date, settings = settings, onClose = onDismiss)
    }
}

/**
 * The content of the day sheet. It is separate from [DayDetailSheet] so screenshots and UI tests
 * can render it without a dialog window.
 */
@Composable
fun DayDetailContent(
    date: NepaliDate,
    settings: Settings,
    onClose: () -> Unit,
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    val adMonthNames = stringArrayResource(R.array.ad_month_long).toList()
    val info = if (date in Patro.coverage) Patro.info(date) else null
    val background = MaterialTheme.colorScheme.background

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("day_detail"),
    ) {
        DayDetailHeader(date = date, lang = lang, settings = settings, bsMonthNames = bsMonthNames, onClose = onClose)
        Text(
            text =
                stringResource(
                    R.string.day_weekday_ad,
                    weekdayName(date, lang),
                    adDate(date, adMonthNames, settings.digits),
                ),
            color = LocalNepdateColors.current.dimmed,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (info == null) {
            Text(
                text = stringResource(R.string.day_no_data),
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            DayDetailFacts(info = info, lang = lang, background = background)
        }
        DayDetailButtons(
            date = date,
            info = info,
            lang = lang,
            settings = settings,
            bsMonthNames = bsMonthNames,
        )
    }
}

@Composable
private fun DayDetailHeader(
    date: NepaliDate,
    lang: Lang,
    settings: Settings,
    bsMonthNames: List<String>,
    onClose: () -> Unit,
) {
    val closeLabel = stringResource(R.string.day_close)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bsLong(date, bsMonthNames[date.monthValue - 1], settings.digits),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
            )
            if (lang == Lang.ENGLISH) {
                Text(
                    text = bsLongNepali(date, settings.digits),
                    color = LocalNepdateColors.current.dimmed,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        IconButton(
            onClick = onClose,
            modifier =
                Modifier
                    .size(48.dp)
                    .semantics { contentDescription = closeLabel },
        ) {
            Icon(imageVector = Icons.Filled.Close, contentDescription = null)
        }
    }
}

@Composable
private fun DayDetailFacts(
    info: CalendarInfo,
    lang: Lang,
    background: Color,
) {
    val colors = LocalNepdateColors.current
    info.tithi?.let { tithi ->
        Text(
            text = stringResource(R.string.day_tithi),
            modifier = Modifier.padding(top = 16.dp),
            color = colors.dimmed,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = tithi.name(lang),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    if (info.isPublicHoliday) {
        Surface(
            modifier = Modifier.padding(top = 16.dp),
            shape = RoundedCornerShape(8.dp),
            color = colors.holidayFillOn(background),
        ) {
            Text(
                text = stringResource(R.string.public_holiday),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = colors.holiday,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
    if (info.events.isNotEmpty()) {
        Text(
            text = stringResource(R.string.day_events),
            modifier = Modifier.padding(top = 16.dp),
            color = colors.dimmed,
            style = MaterialTheme.typography.labelLarge,
        )
        info.events.forEachIndexed { index, event ->
            Text(
                text = event.name(lang),
                modifier = Modifier.testTag("day_event_$index"),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun DayDetailButtons(
    date: NepaliDate,
    info: CalendarInfo?,
    lang: Lang,
    settings: Settings,
    bsMonthNames: List<String>,
) {
    val context = LocalContext.current
    val publicHoliday = stringResource(R.string.public_holiday)
    val title = eventTitle(date, info, lang, publicHoliday, settings, bsMonthNames)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { openCalendar(context, date, title) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
        ) {
            Text(stringResource(R.string.day_add_to_calendar), maxLines = 1)
        }
        OutlinedButton(
            onClick = { copyDate(context, date, settings, bsMonthNames) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
        ) {
            Text(stringResource(R.string.day_copy_date), maxLines = 1)
        }
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
