package np.nepdate.ui.upcoming

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Settings
import np.nepdate.ui.common.ScreenHeader
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.monthTitle
import np.nepdate.ui.common.resolve
import np.nepdate.ui.day.DayDetailSheet
import np.nepdate.ui.month.HolidayUnderline
import np.nepdate.ui.theme.LocalNepdateColors

/** The Upcoming tab (MR-03): the next 20 events, grouped by BS month. */
@Composable
fun UpcomingScreen(
    today: NepaliDate,
    settings: Settings,
    modifier: Modifier = Modifier,
    onSettings: (() -> Unit)? = null,
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val displaySettings = settings.copy(digits = settings.digits.resolve(lang))
    val list = remember(today, settings) { upcoming(today) }
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    val adMonthNames = stringArrayResource(R.array.ad_month_long).toList()
    var selected by remember { mutableStateOf<NepaliDate?>(null) }
    val title = stringResource(R.string.upcoming_title)

    UpcomingListContent(
        list = list,
        title = title,
        settings = displaySettings,
        lang = lang,
        bsMonthNames = bsMonthNames,
        adMonthNames = adMonthNames,
        onSelect = { selected = it },
        onSettings = onSettings,
        modifier = modifier,
    )

    selected?.let { date ->
        DayDetailSheet(
            date = date,
            settings = displaySettings,
            onDismiss = { selected = null },
        )
    }
}

@Composable
private fun UpcomingListContent(
    list: UpcomingList,
    title: String,
    settings: Settings,
    lang: Lang,
    bsMonthNames: List<String>,
    adMonthNames: List<String>,
    onSelect: (NepaliDate) -> Unit,
    onSettings: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("upcoming_list"),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item(key = "header") { ScreenHeader(title = title, onSettings = onSettings) }
        if (list.entries.isEmpty()) {
            item(key = "empty") { UpcomingEmpty() }
        }
        list.groups.forEach { group ->
            item(key = "month_${group.year}_${group.month}") {
                UpcomingGroupHeader(group, settings, bsMonthNames)
            }
            items(
                items = group.entries,
                key = { entry -> "${entry.date.toIsoString()}_${entry.name.english}" },
            ) { entry ->
                UpcomingRow(
                    entry = entry,
                    index = list.entries.indexOf(entry),
                    settings = settings,
                    lang = lang,
                    bsMonthNames = bsMonthNames,
                    monthNames = adMonthNames,
                    onClick = { onSelect(entry.date) },
                )
            }
        }
        list.dataEndsOn?.let { end ->
            item(key = "data_end") { UpcomingFooter(end, settings, bsMonthNames) }
        }
    }
}

@Composable
private fun UpcomingEmpty() {
    Text(
        text = stringResource(R.string.upcoming_empty),
        modifier = Modifier.padding(16.dp),
        color = LocalNepdateColors.current.dimmed,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun UpcomingGroupHeader(
    group: UpcomingGroup,
    settings: Settings,
    bsMonthNames: List<String>,
) {
    Text(
        text = monthTitle(bsMonthNames[group.month - 1], group.year, settings.digits),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        color = LocalNepdateColors.current.dimmed,
        style = MaterialTheme.typography.titleSmall,
    )
}

@Composable
private fun UpcomingFooter(
    end: NepaliDate,
    settings: Settings,
    bsMonthNames: List<String>,
) {
    Text(
        text =
            stringResource(
                R.string.upcoming_data_ends,
                bsLong(end, bsMonthNames[end.monthValue - 1], settings.digits),
            ),
        modifier = Modifier.padding(16.dp),
        color = LocalNepdateColors.current.dimmed,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun UpcomingRow(
    entry: UpcomingEntry,
    index: Int,
    settings: Settings,
    lang: Lang,
    bsMonthNames: List<String>,
    monthNames: List<String>,
    onClick: () -> Unit,
) {
    val relative = relativeLabel(entry)
    val bs = bsLong(entry.date, bsMonthNames[entry.date.monthValue - 1], settings.digits)
    val ad = adDate(entry.date, monthNames, settings.digits)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .heightIn(min = 64.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .testTag("upcoming_entry_$index")
                .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.name.name(lang),
                color =
                    if (entry.isHoliday) {
                        LocalNepdateColors.current.holiday
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                style = MaterialTheme.typography.titleMedium,
            )
            if (entry.isHoliday) {
                HolidayUnderline(width = 24.dp)
            }
            Text(
                text = stringResource(R.string.upcoming_date, bs, ad),
                color = LocalNepdateColors.current.dimmed,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = relative,
            color = LocalNepdateColors.current.dimmed,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun relativeLabel(entry: UpcomingEntry): String =
    when (entry.daysFromToday) {
        0 -> stringResource(R.string.month_today)
        1 -> stringResource(R.string.upcoming_tomorrow)
        else -> pluralStringResource(R.plurals.upcoming_in_days, entry.daysFromToday, entry.daysFromToday)
    }
