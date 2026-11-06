package np.nepdate.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import np.nepdate.data.RecentSearchStore
import np.nepdate.data.Settings
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.upcoming.IndexedEntry
import np.nepdate.ui.upcoming.ParvaRowStyle
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
    recentStore: RecentSearchStore? = null,
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val strings = ParvaStrings.current(settings.digits.resolve(lang))
    var query by rememberSaveable { mutableStateOf("") }
    val trimmed = query.trim()
    val allEntries = remember(today) { coverageEntries(today) }
    val jumpDate = remember(trimmed) { parseJumpDate(trimmed) }
    val results =
        remember(allEntries, trimmed) { if (trimmed.isEmpty()) emptyList() else searchGroups(allEntries, trimmed) }
    val recent = rememberRecentSearches(recentStore)
    val rowColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val style = ParvaRowStyle(container = { rowColor }, highlight = trimmed, tagPrefix = "search_result_")
    val open: (NepaliDate) -> Unit = { date ->
        if (parseJumpDate(trimmed) == null) recent.add(trimmed)
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
            if (trimmed.isEmpty() && recent.queries.isNotEmpty()) {
                item(key = "recent") {
                    RecentSearchChips(recent = recent, onPick = { query = it }, modifier = Modifier.animateItem())
                }
            }
            item(key = "jump") {
                JumpToDateCard(date = jumpDate, strings = strings, onOpen = { jumpDate?.let(open) })
            }
            if (trimmed.isNotEmpty() && jumpDate == null) {
                item(key = "count") { ResultCount(results.sumOf { it.entries.size }, trimmed, strings) }
            }
            parvaGroups(groups = results, lang = lang, today = today, strings = strings, style = style, onSelect = open)
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
        IconButton(
            onClick = onBack,
            shapes = IconButtonDefaults.shapes(),
            modifier = Modifier.semantics { contentDescription = backLabel },
        ) {
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
        if (query.isNotEmpty()) ClearButton(label = clearLabel, onClear = { onQueryChange("") })
    }
}

@Composable
private fun ClearButton(
    label: String,
    onClear: () -> Unit,
) {
    IconButton(
        onClick = onClear,
        shapes = IconButtonDefaults.shapes(),
        modifier = Modifier.semantics { contentDescription = label },
    ) {
        SymbolIcon(icon = R.drawable.ic_sym_close, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
private const val BS_EXAMPLE = "2083-07-15"
private const val AD_EXAMPLE = "2026-11-01"
