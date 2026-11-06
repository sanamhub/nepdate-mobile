package np.nepdate.ui.upcoming

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Settings
import np.nepdate.ui.common.LocalBottomBarPadding
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.day.DayDetailSheet
import np.nepdate.ui.settings.rememberHaptics

/**
 * The festivals tab (MR-03), "Parva": every holiday and event from today to the end of the data,
 * grouped by BS month, with a search field, a holiday filter and the next public holiday on top.
 *
 * @param onOpenMonth opens a BS month (year, month 1..12) in the patro; the month buttons hide when null
 */
@Composable
fun UpcomingScreen(
    today: NepaliDate,
    settings: Settings,
    modifier: Modifier = Modifier,
    onSettings: (() -> Unit)? = null,
    onOpenMonth: ((year: Int, month: Int) -> Unit)? = null,
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val displaySettings = settings.copy(digits = settings.digits.resolve(lang))
    val list = remember(today) { upcoming(today, limit = Int.MAX_VALUE) }
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(ParvaFilter.ALL) }
    var selected by remember { mutableStateOf<NepaliDate?>(null) }
    val shown = remember(list, query, filter) { visibleGroups(list, query.trim(), filter) }
    val nextHoliday = remember(list) { list.entries.firstOrNull { it.isHoliday } }
    val strings = ParvaStrings.current(displaySettings.digits)
    val style = rememberTabRowStyle(onOpenMonth)

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("upcoming_list"),
        contentPadding = PaddingValues(bottom = LocalBottomBarPadding.current.calculateBottomPadding() + 16.dp),
    ) {
        item(key = "header") {
            val haptics = rememberHaptics(settings.haptics)
            ParvaHeader(query, { query = it }, filter, { filter = it }, haptics, onSettings)
        }
        if (query.isBlank() && filter == ParvaFilter.ALL && nextHoliday != null) {
            item(key = "next_holiday") {
                NextHolidayCard(
                    entry = nextHoliday,
                    lang = lang,
                    strings = strings,
                    onClick = { selected = nextHoliday.date },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        parvaGroups(
            groups = shown,
            lang = lang,
            today = today,
            strings = strings,
            style = style,
            onSelect = { selected = it },
        )
        item(key = "footer") {
            ParvaFooter(list = list, nothingShown = shown.isEmpty(), query = query.trim(), strings = strings)
        }
    }

    selected?.let { date ->
        DayDetailSheet(date = date, settings = displaySettings, onDismiss = { selected = null }, today = today)
    }
}

/** The festivals tab's rows: on the container colour, nothing highlighted, months open the patro. */
@Composable
private fun rememberTabRowStyle(onOpenMonth: ((year: Int, month: Int) -> Unit)?): ParvaRowStyle {
    val rowColor = MaterialTheme.colorScheme.surfaceContainer
    return remember(rowColor, onOpenMonth) {
        ParvaRowStyle(container = { rowColor }, highlight = null, onOpenMonth = onOpenMonth)
    }
}

/** The no-results message and the line that says where the holiday data ends. */
@Composable
private fun ParvaFooter(
    list: UpcomingList,
    nothingShown: Boolean,
    query: String,
    strings: ParvaStrings,
) {
    if (nothingShown) {
        val message =
            if (list.entries.isEmpty()) {
                stringResource(R.string.upcoming_empty)
            } else {
                stringResource(R.string.parva_no_results, query)
            }
        ParvaMessage(message)
    }
    list.dataEndsOn?.let { end ->
        ParvaMessage(stringResource(R.string.upcoming_data_ends, strings.bsLong(end)))
    }
}

@Composable
private fun ParvaMessage(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )
}
