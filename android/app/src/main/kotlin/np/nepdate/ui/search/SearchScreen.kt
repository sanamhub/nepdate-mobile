package np.nepdate.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.Patro
import io.github.sanamhub.nepdate.toNepaliDate
import np.nepdate.R
import np.nepdate.data.Settings
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.upcoming.IndexedEntry
import np.nepdate.ui.upcoming.ParvaStrings
import np.nepdate.ui.upcoming.UpcomingEntry
import np.nepdate.ui.upcoming.VisibleGroup
import np.nepdate.ui.upcoming.matches
import np.nepdate.ui.upcoming.parvaGroups
import np.nepdate.ui.upcoming.upcoming
import java.time.DateTimeException
import java.time.LocalDate

/**
 * The full-screen search view: festivals and events across the whole data coverage, matched by name
 * in English or Nepali, and a typed date (BS or AD) to jump straight to.
 */
@Composable
internal fun SearchScreen(
    today: NepaliDate,
    settings: Settings,
    onBack: () -> Unit,
    onOpenDate: (NepaliDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val digits = settings.digits.resolve(lang)
    val strings = ParvaStrings.current(digits)
    var query by rememberSaveable { mutableStateOf("") }
    val trimmed = query.trim()
    val allEntries = remember(today) { coverageEntries(today) }
    val jumpDate = remember(trimmed) { parseJumpDate(trimmed) }
    val results =
        remember(allEntries, trimmed) { if (trimmed.isEmpty()) emptyList() else searchGroups(allEntries, trimmed) }
    val open: (NepaliDate) -> Unit = { date ->
        rememberRecent(trimmed)
        onOpenDate(date)
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .statusBarsPadding()
                .imePadding(),
    ) {
        SearchField(query = query, onQueryChange = { query = it }, onBack = onBack, onSubmit = { jumpDate?.let(open) })
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("search_results"),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            if (trimmed.isEmpty() && RecentSearches.queries.isNotEmpty()) {
                item(key = "recent") { RecentSearchChips(onPick = { query = it }) }
            }
            item(key = "jump") {
                JumpToDateCard(
                    date = jumpDate,
                    strings = strings,
                    onOpen = { jumpDate?.let(open) },
                )
            }
            if (trimmed.isNotEmpty() && jumpDate == null) {
                item(key = "count") { ResultCount(results.sumOf { it.entries.size }, trimmed, strings) }
            }
            parvaGroups(
                groups = results,
                lang = lang,
                today = today,
                strings = strings,
                container = { MaterialTheme.colorScheme.surfaceContainerLowest },
                highlight = trimmed,
                onSelect = open,
                tagPrefix = "search_result_",
            )
        }
    }
}

/** The search row: back, the focused text field and clear. */
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
) {
    val hint = stringResource(R.string.home_search)
    val backLabel = stringResource(R.string.search_back)
    val clearLabel = stringResource(R.string.parva_clear_search)
    val focus = remember { FocusRequester() }
    val textStyle =
        MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Normal,
        )
    LaunchedEffect(Unit) { focus.requestFocus() }
    Row(
        modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = backLabel }) {
            SymbolIcon(icon = R.drawable.ic_sym_arrow_back, tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(4.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier =
                Modifier
                    .weight(1f)
                    .focusRequester(focus)
                    .semantics { contentDescription = hint }
                    .testTag("search_input"),
            textStyle = textStyle,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
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
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentSearchChips(onPick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.search_recent),
            modifier = Modifier.padding(start = 4.dp).semantics { heading() },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RecentSearches.queries.forEach { recent ->
                Row(
                    modifier =
                        Modifier
                            .heightIn(min = 48.dp)
                            .clickable { onPick(recent) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .heightIn(min = 32.dp)
                                .border(
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    RoundedCornerShape(8.dp),
                                ).padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SymbolIcon(
                            icon = R.drawable.ic_sym_history,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            size = 18.dp,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = recent,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

/** The tertiary card that explains date jumping, and jumps when the query is a date. */
@Composable
private fun JumpToDateCard(
    date: NepaliDate?,
    strings: ParvaStrings,
    onOpen: () -> Unit,
) {
    val content = MaterialTheme.colorScheme.onTertiaryContainer
    Row(
        modifier =
            Modifier
                .padding(start = 16.dp, end = 16.dp, top = 10.dp)
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .then(if (date != null) Modifier.clickable(onClick = onOpen) else Modifier)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("search_jump"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SymbolIcon(icon = R.drawable.ic_sym_event, tint = content)
        Spacer(Modifier.width(14.dp))
        Column {
            if (date != null) {
                Text(
                    text = stringResource(R.string.search_jump_open, strings.bsLong(date)),
                    color = content,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = strings.adDate(date), color = content, style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    text = stringResource(R.string.search_jump_title),
                    color = content,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text =
                        stringResource(
                            R.string.search_jump_example,
                            BS_EXAMPLE.inDigits(strings.digits),
                            AD_EXAMPLE.inDigits(strings.digits),
                        ),
                    color = content,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ResultCount(
    count: Int,
    query: String,
    strings: ParvaStrings,
) {
    Text(
        text =
            if (count == 0) {
                stringResource(R.string.parva_no_results, query)
            } else {
                pluralStringResource(R.plurals.search_results, count, count).inDigits(strings.digits)
            },
        modifier =
            Modifier
                .padding(start = 20.dp, end = 20.dp, top = 18.dp)
                .semantics { heading() },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
}

/** Recent searches for this process only; nothing is written to storage. */
private object RecentSearches {
    val queries = mutableStateListOf<String>()
}

private fun rememberRecent(query: String) {
    if (query.isEmpty() || parseJumpDate(query) != null) return
    RecentSearches.queries.remove(query)
    RecentSearches.queries.add(0, query)
    while (RecentSearches.queries.size > MAX_RECENT) RecentSearches.queries.removeAt(RecentSearches.queries.lastIndex)
}

/** Every event in the data coverage, with its distance from [today] (negative in the past). */
private fun coverageEntries(today: NepaliDate): List<UpcomingEntry> =
    upcoming(Patro.coverage.start, limit = Int.MAX_VALUE)
        .entries
        .map { it.copy(daysFromToday = today.daysUntil(it.date).toInt()) }

/** The entries whose name contains [query], grouped by BS month. */
internal fun searchGroups(
    entries: List<UpcomingEntry>,
    query: String,
): List<VisibleGroup> =
    entries
        .withIndex()
        .filter { it.value.matches(query) }
        .map { IndexedEntry(it.index, it.value) }
        .groupBy { it.entry.date.year to it.entry.date.monthValue }
        .map { (key, items) -> VisibleGroup(key.first, key.second, items) }

/**
 * The date in [text] when it is written as year-month-day, in ASCII or Devanagari digits. Years after
 * [BS_YEAR_FLOOR] are read as BS, earlier years as AD; `2083-07-15` is BS and `2026-11-01` is AD.
 *
 * @return the BS date, or null when the text is not a valid date in range
 */
internal fun parseJumpDate(text: String): NepaliDate? {
    val ascii = text.map { if (it in '०'..'९') '0' + (it - '०') else it }.joinToString("")
    val match = DATE_PATTERN.matchEntire(ascii.trim()) ?: return null
    val (year, month, day) = match.destructured.toList().map { it.toInt() }
    return if (year > BS_YEAR_FLOOR) {
        NepaliDate.ofOrNull(year, month, day)
    } else {
        try {
            LocalDate.of(year, month, day).toNepaliDate()
        } catch (_: DateTimeException) {
            null
        } catch (_: NepDateException) {
            null
        }
    }
}

private val DATE_PATTERN = Regex("""(\d{4})\s*[-/.]\s*(\d{1,2})\s*[-/.]\s*(\d{1,2})""")
private const val BS_YEAR_FLOOR = 2040
private const val MAX_RECENT = 5
private const val BS_EXAMPLE = "2083-07-15"
private const val AD_EXAMPLE = "2026-11-01"
