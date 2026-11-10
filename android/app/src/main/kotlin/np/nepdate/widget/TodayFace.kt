package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import np.nepdate.R

/** The strings one face of the Today widget draws: the BS face or the AD face. */
private data class TodayFace(
    val top: String,
    val narrowTop: String,
    val day: String,
    val weekday: String,
    val details: List<String>,
    val events: List<String>,
    val firstIsHoliday: Boolean,
)

private fun TodayWidgetModel.face(showAd: Boolean): TodayFace {
    // A holiday without a named event still gets its pill.
    val events = eventNames.ifEmpty { listOfNotNull(holidayName) }
    // The next holiday comes last so that it is the first line dropped when the face runs short.
    val next = nextHoliday?.let { "${it.name} ${it.relative}" }.orEmpty()
    return if (showAd) {
        TodayFace(adMonthYear, adMonthShort, adDay, weekday, details(bsShortLine, tithi, next), events, isHoliday)
    } else {
        TodayFace(bsMonthYear, monthName, bsDay, weekday, details(tithi, adLine, next), events, isHoliday)
    }
}

private fun details(vararg parts: String): List<String> = parts.filter { it.isNotEmpty() }

/** What a face has room for once its fixed lines are placed. */
private data class FaceExtras(
    val pills: List<List<EventPill>>,
    val details: List<String>,
)

private val NO_EXTRAS = FaceExtras(emptyList(), emptyList())

/**
 * Picks the optional lines in their order of importance (today's holidays and events, the tithi,
 * the other calendar's date) while they fit in [height], and only the ones that fit in [width].
 * The first row of pills comes before the details; any height left after them takes more rows of
 * pills, up to [MAX_PILL_ROWS], and names that still do not fit become a "+N" pill.
 */
private fun TodayFace.extras(
    model: TodayWidgetModel,
    fit: WidgetFit,
    width: Dp,
    height: Dp,
    scale: Float,
): FaceExtras {
    var left = height
    val pillRow = fit.pill(PILL_SIZE * scale)
    var pillRows = if (events.isNotEmpty() && pillRow <= left) 1 else 0
    left -= pillRow * pillRows
    val shown = mutableListOf<String>()
    for (line in details) {
        val lineHeight = fit.line(DETAIL_SIZE * scale)
        if (lineHeight > left) break
        if (fit.fits(line, DETAIL_SIZE * scale, width)) {
            shown += line
            left -= lineHeight
        }
    }
    if (pillRows > 0) pillRows += ((left / pillRow).toInt()).coerceIn(0, MAX_PILL_ROWS - 1)
    val pills = packEventPills(events, firstIsHoliday, fit, PILL_SIZE * scale, width, pillRows, model::more)
    return FaceExtras(pills, shown)
}

/**
 * W-01 Today. The numeral sits on the marigold Sunny shape on a primary container face and the text
 * is start aligned in one column, so every size reads the same way: weekday first, then the month,
 * then whatever else fits (today's holiday or event, the tithi, the other calendar's date). The type
 * grows with the widget but never past what its text column can hold, so nothing is cut with an
 * ellipsis. A round button flips between the BS and the AD face; [TodayFaceAction] saves the choice
 * per widget. A minimal widget keeps only the numeral, the weekday and the month.
 *
 * @param showAd true to draw the AD face
 */
@Composable
internal fun TodayWidgetContent(
    model: TodayWidgetModel,
    opacity: WidgetOpacity,
    showAd: Boolean = false,
    minimal: Boolean = false,
) {
    val context = LocalContext.current
    val label = context.getString(R.string.widget_today_name)
    val face = model.face(showAd)
    Box(
        modifier =
            widgetRoot(opacity, GlanceTheme.colors.primaryContainer)
                .clickable(actionStartActivity(widgetIntent(context)))
                .semantics {
                    contentDescription = label
                    testTag = "today_widget"
                },
        contentAlignment = Alignment.TopEnd,
    ) {
        if (LocalSize.current.width < WIDE_FACE) {
            TodayFaceStacked(model, face, minimal)
        } else {
            TodayFaceWide(model, face, minimal)
        }
        if (!minimal) FlipButton(showAd)
    }
}

/** Up to three cells wide: the numeral in the top corner, the text under it. */
@Composable
private fun TodayFaceStacked(
    model: TodayWidgetModel,
    face: TodayFace,
    minimal: Boolean,
) {
    val size = LocalSize.current
    val fit = widgetFit()
    val inner = size.width - FACE_PADDING * 2
    val scale = columnScale(minOf(size.width, size.height), inner, STACKED_WIDTH_AT_ONE).coerceAtMost(MAX_STACKED_SCALE)
    val text = fit.line(WEEKDAY_SIZE * scale) + fit.line(TOP_SIZE * scale) + STACK_GAP
    // The numeral gives way first on a short face, so the weekday and the month always fit.
    val sunny =
        minOf(minOf(size.width, size.height) * STACKED_SUNNY_SHARE, size.height - FACE_PADDING * 2 - text)
            .coerceIn(MIN_SUNNY, MAX_STACKED_SUNNY)
    val top = if (fit.fits(face.top, TOP_SIZE * scale, inner)) face.top else face.narrowTop
    val fixed = sunny + text
    val extras =
        if (minimal) NO_EXTRAS else face.extras(model, fit, inner, size.height - FACE_PADDING * 2 - fixed, scale)
    Column(modifier = GlanceModifier.fillMaxSize().padding(FACE_PADDING)) {
        SunnyNumeral(face.day, sunny)
        Spacer(GlanceModifier.defaultWeight())
        WeekdayLine(face.weekday, WEEKDAY_SIZE * scale, weekdayColor(model), model.nepalLabel, scale)
        FaceText(top, TOP_SIZE * scale, faceInk(), FontWeight.Medium)
        FaceExtraLines(extras, scale)
    }
}

/** Four or more cells wide: the numeral on the start side, the text column beside it. */
@Composable
private fun TodayFaceWide(
    model: TodayWidgetModel,
    face: TodayFace,
    minimal: Boolean,
) {
    val size = LocalSize.current
    val fit = widgetFit()
    val sunny = minOf(size.height - FACE_PADDING * 2, size.width * WIDE_SUNNY_SHARE).coerceIn(MIN_SUNNY, MAX_WIDE_SUNNY)
    val column = size.width - FACE_PADDING * 2 - sunny - COLUMN_GAP
    val scale = columnScale(size.height, column, WIDE_WIDTH_AT_ONE)
    // Only the top line shares its row with the flip button, so only it keeps clear of it.
    val topWidth = column - if (minimal) 0.dp else FLIP_CLEARANCE
    val top = if (fit.fits(face.top, TOP_SIZE * scale, topWidth)) face.top else face.narrowTop
    val fixed = fit.line(WEEKDAY_SIZE * WIDE_WEEKDAY * scale) + fit.line(TOP_SIZE * scale)
    val extras =
        if (minimal) NO_EXTRAS else face.extras(model, fit, column, size.height - FACE_PADDING * 2 - fixed, scale)
    Row(modifier = GlanceModifier.fillMaxSize().padding(FACE_PADDING), verticalAlignment = Alignment.CenterVertically) {
        SunnyNumeral(face.day, sunny)
        Column(modifier = GlanceModifier.defaultWeight().padding(start = COLUMN_GAP)) {
            Text(
                text = top,
                modifier = GlanceModifier.padding(end = if (minimal) 0.dp else FLIP_CLEARANCE),
                style = faceStyle(TOP_SIZE * scale, FontWeight.Medium),
                maxLines = 1,
            )
            WeekdayLine(face.weekday, WEEKDAY_SIZE * WIDE_WEEKDAY * scale, weekdayColor(model), model.nepalLabel, scale)
            FaceExtraLines(extras, scale)
        }
    }
}

@Composable
private fun FaceExtraLines(
    extras: FaceExtras,
    scale: Float,
) {
    extras.details.forEach { FaceText(it, DETAIL_SIZE * scale, faceInk(), FontWeight.Normal) }
    EventPillRows(extras.pills, PILL_SIZE * scale)
}

@Composable
private fun FaceText(
    text: String,
    fontSize: TextUnit,
    color: ColorProvider,
    weight: FontWeight,
) {
    Text(
        text = text,
        modifier = GlanceModifier.fillMaxWidth(),
        style = TextStyle(color = color, fontSize = fontSize, fontWeight = weight),
        maxLines = 1,
    )
}

@Composable
private fun faceInk(): ColorProvider = GlanceTheme.colors.onPrimaryContainer

@Composable
private fun faceStyle(
    fontSize: TextUnit,
    weight: FontWeight = FontWeight.Normal,
): TextStyle = TextStyle(color = faceInk(), fontSize = fontSize, fontWeight = weight)

@Composable
private fun weekdayColor(model: TodayWidgetModel): ColorProvider =
    if (model.isHoliday || model.isSaturday) holidayProvider() else GlanceTheme.colors.onPrimaryContainer

internal val TOUCH_TARGET = 48.dp
private val WIDE_FACE = 200.dp
private val FACE_PADDING = 14.dp
private val COLUMN_GAP = 16.dp
private val STACK_GAP = 6.dp
private val FLIP_CLEARANCE = 36.dp
private val MIN_SUNNY = 32.dp
private val MAX_STACKED_SUNNY = 84.dp
private val MAX_WIDE_SUNNY = 136.dp
private const val STACKED_SUNNY_SHARE = 0.34f
private const val MAX_STACKED_SCALE = 1.25f
private const val WIDE_SUNNY_SHARE = 0.34f
private const val WIDE_WEEKDAY = 1.35f
private val STACKED_WIDTH_AT_ONE = 100.dp
private val WIDE_WIDTH_AT_ONE = 130.dp
private val WEEKDAY_SIZE = 18.sp
private val TOP_SIZE = 13.sp
private val DETAIL_SIZE = 12.sp
private val PILL_SIZE = 12.sp
private const val MAX_PILL_ROWS = 3
