package np.nepdate.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.Patro
import io.github.sanamhub.nepdate.toNepaliDate
import np.nepdate.R
import np.nepdate.data.RecentSearchStore
import np.nepdate.data.Settings
import np.nepdate.ui.common.DATE_DIGITS
import np.nepdate.ui.common.DateSlashTransformation
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.common.withDateSlashes
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
    val jumpDates = remember(trimmed) { parseJumpDates(trimmed) }
    val results =
        remember(allEntries, trimmed) { if (trimmed.isEmpty()) emptyList() else searchGroups(allEntries, trimmed) }
    val recent = rememberRecentSearches(recentStore)
    val rowColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val style = ParvaRowStyle(container = { rowColor }, highlight = trimmed, tagPrefix = "search_result_")
    val open: (NepaliDate) -> Unit = { date ->
        if (!jumpDates.isDate) recent.add(trimmed)
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
        SearchField(
            query = query,
            onQueryChange = { query = it },
            onBack = onBack,
            onSubmit = { jumpDates.primary?.let(open) },
        )
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
                JumpCards(dates = jumpDates, strings = strings, onOpen = open)
            }
            if (trimmed.isNotEmpty() && !jumpDates.isDate) {
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
            // A query of digits only is a date being typed, so it shows the YYYY/MM/DD slashes.
            visualTransformation =
                if (query.isNotEmpty() && query.length <= DATE_DIGITS && query.all { it.isDigit() }) {
                    DateSlashTransformation
                } else {
                    VisualTransformation.None
                },
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

/**
 * The date cards. A typed date such as `2081-05-10` is valid in BS and in AD, so it gets one card
 * for each reading; a date that only one calendar can read gets one card; anything else gets the
 * card that explains date jumping.
 */
@Composable
private fun JumpCards(
    dates: JumpDates,
    strings: ParvaStrings,
    onOpen: (NepaliDate) -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        dates.bs?.let { date ->
            JumpCard(
                title = stringResource(R.string.search_jump_open_bs, strings.bsLong(date)),
                detail = strings.adDate(date),
                tag = "search_jump_bs",
                onClick = { onOpen(date) },
            )
        }
        dates.ad?.let { date ->
            JumpCard(
                title = stringResource(R.string.search_jump_open_ad, strings.adDate(date)),
                detail = strings.bsLong(date),
                tag = "search_jump_ad",
                onClick = { onOpen(date) },
            )
        }
        if (!dates.isDate) {
            JumpCard(
                title = stringResource(R.string.search_jump_title),
                detail =
                    stringResource(
                        R.string.search_jump_example,
                        BS_EXAMPLE.inDigits(strings.digits),
                        AD_EXAMPLE.inDigits(strings.digits),
                    ),
                tag = "search_jump",
                onClick = null,
            )
        }
    }
}

/** One rounded card of [JumpCards]: the date it opens, or the explainer when [onClick] is null. */
@Composable
private fun JumpCard(
    title: String,
    detail: String,
    tag: String,
    onClick: (() -> Unit)?,
) {
    val content = MaterialTheme.colorScheme.onTertiaryContainer
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SymbolIcon(icon = R.drawable.ic_sym_event, tint = content)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                color = content,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(text = detail, color = content, style = MaterialTheme.typography.bodyMedium)
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
 * The two readings of a typed date: the same year-month-day once as a BS date and once as an AD
 * date. `2081-05-10` is a real day in both calendars, so it has both; a date only one calendar can
 * read, such as a BS day past the end of the Nepali year that came first, has only that one.
 */
internal data class JumpDates(
    val bs: NepaliDate?,
    val ad: NepaliDate?,
) {
    /** True when [text] read as a date in either calendar. */
    val isDate: Boolean get() = bs != null || ad != null

    /** The date the keyboard's Search action opens: the BS reading when the text has both. */
    val primary: NepaliDate? get() = bs ?: ad
}

/**
 * Reads [text] as a year-month-day date, in ASCII or Devanagari digits, and returns both readings
 * that the library can make of it. Eight digits typed without separators (`20830722`) read as
 * YYYYMMDD.
 *
 * @return the BS reading and the AD reading, either of which is null when that calendar cannot read it
 */
internal fun parseJumpDates(text: String): JumpDates {
    val ascii = text.map { if (it in '०'..'९') '0' + (it - '०') else it }.joinToString("").trim()
    val spaced = if (ascii.length == DATE_DIGITS && ascii.all { it.isDigit() }) withDateSlashes(ascii) else ascii
    val match = DATE_PATTERN.matchEntire(spaced) ?: return JumpDates(null, null)
    val (year, month, day) = match.destructured.toList().map { it.toInt() }
    return JumpDates(bs = NepaliDate.ofOrNull(year, month, day), ad = adDateOf(year, month, day))
}

/** The BS date that [year]-[month]-[day] falls on in AD, or null outside the library's range. */
private fun adDateOf(
    year: Int,
    month: Int,
    day: Int,
): NepaliDate? =
    try {
        LocalDate.of(year, month, day).toNepaliDate()
    } catch (_: DateTimeException) {
        null
    } catch (_: NepDateException) {
        null
    }

private val DATE_PATTERN = Regex("""(\d{4})\s*[-/.]\s*(\d{1,2})\s*[-/.]\s*(\d{1,2})""")
private const val BS_EXAMPLE = "2083-07-15"
private const val AD_EXAMPLE = "2026-11-01"
