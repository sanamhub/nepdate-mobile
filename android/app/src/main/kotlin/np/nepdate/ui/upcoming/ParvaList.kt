package np.nepdate.ui.upcoming

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
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
import np.nepdate.ui.day.relativeDayLabel
import np.nepdate.ui.theme.LocalNepdateColors

/** An entry with its position in the full list, which the row test tags use. */
internal data class IndexedEntry(
    val index: Int,
    val entry: UpcomingEntry,
)

/**
 * The entries of one BS month after search and filter. [holidays] counts the public holidays the
 * month has left from today, whatever the filter shows; search results leave it at zero.
 */
internal data class VisibleGroup(
    val year: Int,
    val month: Int,
    val entries: List<IndexedEntry>,
    val holidays: Int = 0,
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
        val holidays =
            group.entries
                .filter { it.isHoliday }
                .distinctBy { it.date }
                .size
        if (kept.isEmpty()) null else VisibleGroup(group.year, group.month, kept, holidays)
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

    fun monthName(month: Int): String = bsMonthNames[month - 1]

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

/** How the festival rows look and behave; shared by the festivals tab and the search screen. */
internal class ParvaRowStyle(
    val container: @Composable () -> Color,
    val highlight: String?,
    val tagPrefix: String = "upcoming_entry_",
    val onOpenMonth: ((year: Int, month: Int) -> Unit)? = null,
)

/**
 * Month headers and segmented rows for [groups]. Rows and headers animate into place when the
 * search or filter changes the list.
 */
internal fun LazyListScope.parvaGroups(
    groups: List<VisibleGroup>,
    lang: Lang,
    today: NepaliDate,
    strings: ParvaStrings,
    style: ParvaRowStyle,
    onSelect: (NepaliDate) -> Unit,
) {
    groups.forEach { group ->
        item(key = "month_${group.year}_${group.month}") {
            ParvaMonthHeader(
                group = group,
                strings = strings,
                onOpenMonth = style.onOpenMonth,
                modifier = Modifier.animateItem(),
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
                style = style,
                shapes = ListItemDefaults.segmentedShapes(position, group.entries.size),
                onClick = { onSelect(item.entry.date) },
                modifier =
                    Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp, vertical = 1.dp)
                        .testTag("${style.tagPrefix}${item.index}"),
            )
        }
    }
}

/** One festival: the date tile, the name, the AD date, and how far it is from today. */
@Composable
private fun ParvaRow(
    entry: UpcomingEntry,
    lang: Lang,
    isToday: Boolean,
    strings: ParvaStrings,
    style: ParvaRowStyle,
    shapes: ListItemShapes,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNepdateColors.current
    val name = entry.name.name(lang).inDigits(strings.digits)
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        modifier = modifier.fillMaxWidth().heightIn(min = 72.dp),
        colors =
            ListItemDefaults.segmentedColors(
                containerColor = style.container(),
                supportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        leadingContent = {
            DateTile(entry.date, entry.isHoliday, isToday, lang, strings.digits)
        },
        supportingContent = {
            Text(text = strings.adDate(entry.date), maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            Text(
                text = relativeDayLabel(entry.daysFromToday).inDigits(strings.digits),
                color = if (entry.isHoliday) colors.holiday else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
        },
    ) {
        Text(
            text = boldMatch(name, style.highlight),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (entry.isHoliday) FontWeight.Bold else FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
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
