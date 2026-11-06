package np.nepdate.ui.upcoming

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.ShapeBadge
import np.nepdate.ui.common.SunnyShape
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.monthTitle
import np.nepdate.ui.common.segmentShape
import np.nepdate.ui.day.relativeDayLabel
import np.nepdate.ui.theme.LocalNepdateColors

/** An entry with its position in the full list, which the row test tags use. */
internal data class IndexedEntry(
    val index: Int,
    val entry: UpcomingEntry,
)

/** The entries of one BS month after search and filter. */
internal data class VisibleGroup(
    val year: Int,
    val month: Int,
    val entries: List<IndexedEntry>,
)

/** True when [query] is part of the event name in either language, ignoring case. */
internal fun UpcomingEntry.matches(query: String): Boolean =
    query.isEmpty() ||
        name.english.contains(query, ignoreCase = true) ||
        name.nepali.contains(query, ignoreCase = true)

/** Which entries the festivals list shows. */
internal enum class ParvaFilter { ALL, HOLIDAYS }

/** The groups of [list] whose entries match [query] and [filter]; empty months are left out. */
internal fun visibleGroups(
    list: UpcomingList,
    query: String,
    filter: ParvaFilter,
): List<VisibleGroup> {
    var index = 0
    return list.groups.mapNotNull { group ->
        val kept =
            group.entries.mapNotNull { entry ->
                val indexed = IndexedEntry(index++, entry)
                indexed.takeIf { keep(group.entries, entry, query, filter) }
            }
        if (kept.isEmpty()) null else VisibleGroup(group.year, group.month, kept)
    }
}

// The holiday flag belongs to the day, so on a holiday every event shares it. The holiday filter keeps
// the first event of the day, which is the holiday itself, and leaves out observances that share it.
private fun keep(
    monthEntries: List<UpcomingEntry>,
    entry: UpcomingEntry,
    query: String,
    filter: ParvaFilter,
): Boolean =
    entry.matches(query) &&
        (filter == ParvaFilter.ALL || entry.isHoliday && monthEntries.first { it.date == entry.date } === entry)

/** Month names and digits the festival rows need, read once per composition. */
internal class ParvaStrings(
    val digits: Digits,
    private val bsMonthNames: List<String>,
    private val adMonthNames: List<String>,
) {
    fun bsLong(date: NepaliDate): String = bsLong(date, bsMonthNames[date.monthValue - 1], digits)

    fun adDate(date: NepaliDate): String = adDate(date, adMonthNames, digits)

    fun monthTitle(
        year: Int,
        month: Int,
    ): String = monthTitle(bsMonthNames[month - 1], year, digits)

    companion object {
        @Composable
        fun current(digits: Digits): ParvaStrings =
            ParvaStrings(
                digits = digits,
                bsMonthNames = stringArrayResource(R.array.bs_month_long).toList(),
                adMonthNames = stringArrayResource(R.array.ad_month_long).toList(),
            )
    }
}

/**
 * Month headers and segmented rows for [groups]. Shared with the search screen, which passes its own
 * row [container] colour and the [highlight] text to draw bold.
 */
internal fun LazyListScope.parvaGroups(
    groups: List<VisibleGroup>,
    lang: Lang,
    today: NepaliDate,
    strings: ParvaStrings,
    container: @Composable () -> Color,
    highlight: String?,
    onSelect: (NepaliDate) -> Unit,
    tagPrefix: String = "upcoming_entry_",
) {
    groups.forEach { group ->
        item(key = "month_${group.year}_${group.month}") {
            Text(
                text = strings.monthTitle(group.year, group.month),
                modifier =
                    Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
                        .semantics { heading() },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        itemsIndexed(
            items = group.entries,
            key = { _, item -> "${item.entry.date.toIsoString()}_${item.index}" },
        ) { position, item ->
            ParvaRow(
                entry = item.entry,
                lang = lang,
                isToday = item.entry.date == today,
                strings = strings,
                container = container(),
                highlight = highlight,
                shape = segmentShape(position, group.entries.size, outer = 20.dp),
                onClick = { onSelect(item.entry.date) },
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 1.dp)
                        .testTag("$tagPrefix${item.index}"),
            )
        }
    }
}

/** One festival: the date tile, the name and dates, and how far it is from today. */
@Composable
private fun ParvaRow(
    entry: UpcomingEntry,
    lang: Lang,
    isToday: Boolean,
    strings: ParvaStrings,
    container: Color,
    highlight: String?,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNepdateColors.current
    val name = entry.name.name(lang).inDigits(strings.digits)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clip(shape)
                .background(container)
                .clickable(onClick = onClick)
                .padding(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateTile(
            date = entry.date,
            isHoliday = entry.isHoliday,
            isToday = isToday,
            lang = lang,
            digits = strings.digits,
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = boldMatch(name, highlight),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (entry.isHoliday) FontWeight.Bold else FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.upcoming_date, strings.bsLong(entry.date), strings.adDate(entry.date)),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = relativeDayLabel(entry.daysFromToday).inDigits(strings.digits),
            color = if (entry.isHoliday) colors.holiday else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** The 48 dp date tile: holiday tint, the marigold Sunny for today, or a neutral tile. */
@Composable
private fun DateTile(
    date: NepaliDate,
    isHoliday: Boolean,
    isToday: Boolean,
    lang: Lang,
    digits: Digits,
) {
    val colors = LocalNepdateColors.current
    val (shape, container, content) =
        when {
            isToday -> Triple(SunnyShape, colors.today, colors.onToday)
            isHoliday -> Triple(TileShape, colors.holidayContainer, colors.onHolidayContainer)
            date.dayOfWeek == Weekday.SATURDAY ->
                Triple(TileShape, MaterialTheme.colorScheme.surfaceContainerHighest, colors.holiday)
            else ->
                Triple(
                    TileShape,
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    MaterialTheme.colorScheme.onSurface,
                )
        }
    ShapeBadge(shape = shape, color = container, size = 48.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString().inDigits(digits),
                color = content,
                fontSize = 19.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = date.dayOfWeek.shortName(lang),
                color = content,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

/** [text] with the first case-insensitive match of [highlight] drawn extra bold. */
internal fun boldMatch(
    text: String,
    highlight: String?,
): AnnotatedString {
    val start = if (highlight.isNullOrEmpty()) -1 else text.indexOf(highlight, ignoreCase = true)
    val builder = AnnotatedString.Builder(text)
    if (start >= 0 && highlight != null) {
        builder.addStyle(SpanStyle(fontWeight = FontWeight.ExtraBold), start, start + highlight.length)
    }
    return builder.toAnnotatedString()
}

private val TileShape = RoundedCornerShape(14.dp)
