package np.nepdate.ui.upcoming

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.common.weekdayName
import np.nepdate.ui.day.DayDetailSheet
import np.nepdate.ui.day.relativeDayLabel
import np.nepdate.ui.theme.LocalNepdateColors

/**
 * The festivals tab (MR-03), "Parva": every holiday and event from today to the end of the data,
 * grouped by BS month, with a search field, a holiday filter and the next public holiday on top.
 */
@Composable
fun UpcomingScreen(
    today: NepaliDate,
    settings: Settings,
    modifier: Modifier = Modifier,
    onSettings: (() -> Unit)? = null,
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

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("upcoming_list"),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item(key = "header") {
            ParvaHeader(
                query = query,
                onQueryChange = { query = it },
                filter = filter,
                onFilterChange = { filter = it },
                onSettings = onSettings,
            )
        }
        if (query.isBlank() && filter == ParvaFilter.ALL && nextHoliday != null) {
            item(key = "next_holiday") {
                NextHolidayCard(
                    entry = nextHoliday,
                    lang = lang,
                    strings = strings,
                    onClick = { selected = nextHoliday.date },
                )
            }
        }
        parvaGroups(
            groups = shown,
            lang = lang,
            today = today,
            strings = strings,
            container = { MaterialTheme.colorScheme.surfaceContainer },
            highlight = null,
            onSelect = { selected = it },
        )
        item(key = "footer") {
            ParvaFooter(list = list, nothingShown = shown.isEmpty(), query = query.trim(), strings = strings)
        }
    }

    selected?.let { date ->
        DayDetailSheet(
            date = date,
            settings = displaySettings,
            onDismiss = { selected = null },
            today = today,
        )
    }
}

/** The title, the settings button, the search field and the filter chips. */
@Composable
private fun ParvaHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    filter: ParvaFilter,
    onFilterChange: (ParvaFilter) -> Unit,
    onSettings: (() -> Unit)?,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.parva_title),
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .semantics { heading() },
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
            )
            if (onSettings != null) {
                val settingsLabel = stringResource(R.string.settings_title)
                IconButton(onClick = onSettings, modifier = Modifier.semantics { contentDescription = settingsLabel }) {
                    SymbolIcon(icon = R.drawable.ic_sym_settings, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Box(Modifier.padding(end = 8.dp)) {
            ParvaSearchField(query = query, onQueryChange = onQueryChange)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ParvaChip(
                label = stringResource(R.string.parva_filter_all),
                selected = filter == ParvaFilter.ALL,
                onClick = { onFilterChange(ParvaFilter.ALL) },
            )
            ParvaChip(
                label = stringResource(R.string.parva_filter_holidays),
                selected = filter == ParvaFilter.HOLIDAYS,
                onClick = { onFilterChange(ParvaFilter.HOLIDAYS) },
            )
        }
    }
}

/** The 56 dp search bar: a full-radius pill with the search symbol and a clear button. */
@Composable
private fun ParvaSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val hint = stringResource(R.string.parva_search_hint)
    val clearLabel = stringResource(R.string.parva_clear_search)
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(start = 18.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SymbolIcon(icon = R.drawable.ic_sym_search, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { contentDescription = hint }
                    .testTag("parva_search"),
            textStyle = textStyle,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = hint,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = textStyle,
                            maxLines = 1,
                        )
                    }
                    inner()
                }
            },
        )
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.semantics { contentDescription = clearLabel },
            ) {
                SymbolIcon(icon = R.drawable.ic_sym_close, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Spacer(Modifier.width(14.dp))
        }
    }
}

@Composable
private fun ParvaChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon =
            if (selected) {
                { SymbolIcon(icon = R.drawable.ic_sym_check, size = FilterChipDefaults.IconSize) }
            } else {
                null
            },
    )
}

/** The holiday-tinted card with the big countdown to the next public holiday. */
@Composable
private fun NextHolidayCard(
    entry: UpcomingEntry,
    lang: Lang,
    strings: ParvaStrings,
    onClick: () -> Unit,
) {
    val colors = LocalNepdateColors.current
    val content = colors.onHolidayContainer
    val days = entry.daysFromToday
    Row(
        modifier =
            Modifier
                .padding(start = 16.dp, end = 16.dp, top = 14.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.holidayContainer)
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("parva_next_holiday"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Countdown(days = days, digits = strings.digits, color = content)
        Spacer(Modifier.width(18.dp))
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(content.copy(alpha = DIVIDER_ALPHA)),
        )
        Spacer(Modifier.width(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.parva_next_holiday),
                color = content.copy(alpha = LABEL_ALPHA),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = entry.name.name(lang).inDigits(strings.digits),
                color = content,
                style = MaterialTheme.typography.headlineSmall,
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

/** The big number of days left, with its unit below. */
@Composable
private fun Countdown(
    days: Int,
    digits: Digits,
    color: Color,
) {
    Column(modifier = Modifier.widthIn(min = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = days.toString().inDigits(digits),
            color = color,
            fontSize = 57.sp,
            lineHeight = 60.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = if (days == 0) relativeDayLabel(0) else pluralStringResource(R.plurals.parva_days_left, days),
            color = color,
            style = MaterialTheme.typography.labelLarge,
        )
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

private const val DIVIDER_ALPHA = 0.2f
private const val LABEL_ALPHA = 0.8f
