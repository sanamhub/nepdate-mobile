package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import np.nepdate.R

/** One pill in a widget's event rows: a name, or the "+N" that stands for the names left out. */
internal data class EventPill(
    val text: String,
    val isHoliday: Boolean,
    val isMore: Boolean = false,
)

/**
 * Packs a day's [names] into at most [maxRows] rows of pills that fit in [width]. Names keep their
 * order (the public holiday first); a name wider than a row gets a row to itself, where the text
 * view ends it with an ellipsis. When names are left over, the rows end with a "+N" pill ([addMore]).
 *
 * @param holidayFirst true when the first name is the day's public holiday
 * @param more the localised "+N" label for N names left out
 */
internal fun packEventPills(
    names: List<String>,
    holidayFirst: Boolean,
    fit: WidgetFit,
    fontSize: TextUnit,
    width: Dp,
    maxRows: Int,
    more: (Int) -> String,
): List<List<EventPill>> {
    if (names.isEmpty() || maxRows <= 0) return emptyList()

    fun pillWidth(text: String): Dp = eventPillWidth(text, fit, fontSize)
    val rows = mutableListOf<MutableList<EventPill>>()
    var used = 0.dp
    for ((index, name) in names.withIndex()) {
        val wide = minOf(pillWidth(name), width)
        if (rows.isEmpty() || used + wide > width) {
            if (rows.size == maxRows) break
            rows += mutableListOf<EventPill>()
            used = 0.dp
        }
        rows.last() += EventPill(name, holidayFirst && index == 0)
        used += wide
    }
    val left = names.size - rows.sumOf { it.size }
    if (left > 0 && rows.isNotEmpty()) addMore(rows, used, left, width, maxRows, ::pillWidth, more)
    return rows.filter { it.isNotEmpty() }
}

/**
 * Ends [rows] with the "+N" pill for [left] names. Names come off the end of the last row until it
 * fits beside them, but never the row's only name: then the "+N" takes a row of its own, or is left
 * out when no row is left.
 */
private fun addMore(
    rows: MutableList<MutableList<EventPill>>,
    usedWidth: Dp,
    leftOver: Int,
    width: Dp,
    maxRows: Int,
    pillWidth: (String) -> Dp,
    more: (Int) -> String,
) {
    val last = rows.last()
    var used = usedWidth
    var left = leftOver
    while (last.size > 1 && used + pillWidth(more(left)) > width) {
        used -= pillWidth(last.removeAt(last.lastIndex).text)
        left++
    }
    val label = EventPill(more(left), isHoliday = false, isMore = true)
    when {
        used + pillWidth(label.text) <= width -> last += label
        rows.size < maxRows -> rows += mutableListOf(label)
    }
}

/**
 * A day's [names] as one line of text that fits in [width] over [lines] lines: as many names as fit,
 * joined with commas, then "+N" for the rest. Empty when not even the first name fits.
 */
internal fun fitEventNames(
    names: List<String>,
    fit: WidgetFit,
    fontSize: TextUnit,
    width: Dp,
    lines: Int,
    more: (Int) -> String,
): String {
    val room = width * lines
    for (count in names.size downTo 1) {
        val shown = names.take(count).joinToString(", ")
        val line = if (count < names.size) "$shown ${more(names.size - count)}" else shown
        if (fit.width(line, fontSize) <= room) return line
    }
    return ""
}

/** The width one pill with [text] takes, with the gap after it. */
internal fun eventPillWidth(
    text: String,
    fit: WidgetFit,
    fontSize: TextUnit,
): Dp = fit.width(text, fontSize) + PILL_PADDING * 2 + PILL_GAP

/**
 * Draws packed [rows]: the holiday on the holiday container, the rest on [container], which must
 * differ from the colour the widget draws them on.
 */
@Composable
internal fun EventPillRows(
    rows: List<List<EventPill>>,
    fontSize: TextUnit,
    modifier: GlanceModifier = GlanceModifier,
    container: ColorProvider = GlanceTheme.colors.surface,
    content: ColorProvider = GlanceTheme.colors.onSurface,
) {
    if (rows.isEmpty()) return
    Column(modifier = modifier) {
        rows.forEach { row ->
            Row(modifier = GlanceModifier.padding(top = PILL_ROW_GAP)) {
                row.forEachIndexed { index, pill ->
                    if (index > 0) Spacer(GlanceModifier.width(PILL_GAP))
                    EventPillView(pill, fontSize, container, content)
                }
            }
        }
    }
}

@Composable
private fun EventPillView(
    pill: EventPill,
    fontSize: TextUnit,
    plainContainer: ColorProvider,
    plainContent: ColorProvider,
) {
    val container = if (pill.isHoliday) holidayContainerProvider() else plainContainer
    val content = if (pill.isHoliday) onHolidayContainerProvider() else plainContent
    Text(
        text = pill.text,
        modifier =
            GlanceModifier
                .shapeBackground(R.drawable.widget_shape_pill, container)
                .padding(horizontal = PILL_PADDING, vertical = 3.dp),
        style =
            TextStyle(
                color = content,
                fontSize = fontSize,
                fontWeight = WidgetType.labelWeight,
            ),
        maxLines = 1,
    )
}

private val PILL_PADDING = 10.dp
private val PILL_GAP = 6.dp
private val PILL_ROW_GAP = 4.dp
