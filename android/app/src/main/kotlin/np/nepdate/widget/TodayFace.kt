package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import np.nepdate.R

/** The strings one face of the Today widget draws: the BS face or the AD face. */
private data class TodayFace(
    val top: String,
    val day: String,
    val weekday: String,
    val sub: String,
    val subIsHoliday: Boolean,
    val oneLine: String,
)

private fun TodayWidgetModel.face(showAd: Boolean): TodayFace =
    if (showAd) {
        TodayFace(
            top = adMonthYear,
            day = adDay,
            weekday = weekday,
            sub = bsShortLine,
            subIsHoliday = false,
            oneLine = "$weekday, $adDay $adMonthYear · $bsShortLine",
        )
    } else {
        TodayFace(
            top = bsMonthYear,
            day = bsDay,
            weekday = weekday,
            sub = holidayName ?: adShortLine,
            subIsHoliday = holidayName != null,
            oneLine = oneLine,
        )
    }

/**
 * W-01 Today. The numeral sits on the marigold Sunny shape on a primary container face. A round
 * button flips the face between the BS and the AD date; the choice is saved per widget by
 * [TodayFaceAction]. 2x2 stacks the face, 4x2 puts the numeral beside the text, 4x1 is one line.
 *
 * @param showAd true to draw the AD face
 */
@Composable
internal fun TodayWidgetContent(
    model: TodayWidgetModel,
    opacity: WidgetOpacity,
    showAd: Boolean = false,
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
        when {
            isOneLineSize() -> TodayFaceOneLine(model, face)
            isSmallSize() -> TodayFaceStacked(model, face)
            else -> TodayFaceWide(model, face)
        }
        if (!isOneLineSize()) FlipButton(showAd)
    }
}

@Composable
private fun TodayFaceStacked(
    model: TodayWidgetModel,
    face: TodayFace,
) {
    val size = LocalSize.current
    val sunny = (minOf(size.width, size.height) - STACKED_CHROME).coerceIn(MIN_SUNNY, MAX_SUNNY)
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopLabel(model, face, fontSize = 14.sp)
        Box(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            contentAlignment = Alignment.Center,
        ) {
            SunnyNumeral(face.day, sunny)
        }
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (size.width < WIDE_ENOUGH_FOR_FULL_WEEKDAY) model.weekdayShort else face.weekday,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = weekdayColor(model), fontSize = 12.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
            )
            SubLine(face, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TodayFaceWide(
    model: TodayWidgetModel,
    face: TodayFace,
) {
    val size = LocalSize.current
    val sunny = (size.height - WIDE_CHROME).coerceIn(MIN_SUNNY, MAX_SUNNY)
    Row(
        modifier = GlanceModifier.fillMaxSize().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SunnyNumeral(face.day, sunny)
        Column(modifier = GlanceModifier.defaultWeight().padding(start = 12.dp, end = 36.dp)) {
            TopLabel(model, face, fontSize = 16.sp)
            Text(
                text = face.weekday,
                style = TextStyle(color = weekdayColor(model), fontSize = 13.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
            )
            SubLine(face, fontSize = 12.sp)
            model.nextHoliday?.let { next ->
                Text(
                    text = "${next.name} · ${next.relative}",
                    modifier = GlanceModifier.padding(top = 2.dp),
                    style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer, fontSize = 11.sp),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TodayFaceOneLine(
    model: TodayWidgetModel,
    face: TodayFace,
) {
    Row(
        modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SunnyNumeral(face.day, ONE_LINE_SUNNY)
        Text(
            text = face.oneLine,
            modifier = GlanceModifier.defaultWeight().padding(start = 10.dp),
            style =
                TextStyle(
                    color =
                        if (model.isHoliday ||
                            model.isSaturday
                        ) {
                            holidayProvider()
                        } else {
                            GlanceTheme.colors.onPrimaryContainer
                        },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
            maxLines = 2,
        )
    }
}

@Composable
private fun TopLabel(
    model: TodayWidgetModel,
    face: TodayFace,
    fontSize: TextUnit,
) {
    // The end padding keeps the label clear of the flip button in the corner.
    Row(modifier = GlanceModifier.fillMaxWidth().padding(end = 36.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = face.top,
            style =
                TextStyle(
                    color = GlanceTheme.colors.onPrimaryContainer,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                ),
            maxLines = 1,
        )
        model.nepalLabel?.let {
            Text(
                text = " · $it",
                style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer, fontSize = 10.sp),
                maxLines = 1,
            )
        }
    }
}

/** The holiday name on a holiday container pill, or the other calendar's date as plain text. */
@Composable
private fun SubLine(
    face: TodayFace,
    fontSize: TextUnit,
) {
    val modifier =
        if (face.subIsHoliday) {
            GlanceModifier
                .shapeBackground(R.drawable.widget_shape_pill, holidayContainerProvider())
                .padding(horizontal = 8.dp, vertical = 2.dp)
        } else {
            GlanceModifier
        }
    Text(
        text = face.sub,
        modifier = modifier,
        style =
            TextStyle(
                color = if (face.subIsHoliday) onHolidayContainerProvider() else GlanceTheme.colors.onPrimaryContainer,
                fontSize = fontSize,
                fontWeight = if (face.subIsHoliday) FontWeight.Medium else FontWeight.Normal,
            ),
        maxLines = 1,
    )
}

@Composable
private fun weekdayColor(model: TodayWidgetModel): ColorProvider =
    if (model.isHoliday || model.isSaturday) holidayProvider() else GlanceTheme.colors.onPrimaryContainer

/** The round flip button: 36 dp visible, inside a 48 dp touch target. */
@Composable
private fun FlipButton(showAd: Boolean) {
    val context = LocalContext.current
    val description = context.getString(if (showAd) R.string.widget2_show_bs else R.string.widget2_show_ad)
    Box(
        modifier =
            GlanceModifier
                .padding(4.dp)
                .size(TOUCH_TARGET)
                .clickable(actionRunCallback<TodayFaceAction>())
                .semantics {
                    contentDescription = description
                    testTag = "today_flip"
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = GlanceModifier.size(36.dp).shapeBackground(R.drawable.widget_shape_pill, chipProvider()),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_sym_swap_horiz),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
            )
        }
    }
}

/**
 * A numeral on the marigold Sunny shape. Glance cannot draw paths, so the shape is the
 * `widget_sunny` vector tinted with the day or night today colour.
 */
@Composable
internal fun SunnyNumeral(
    text: String,
    size: Dp,
    fontSize: TextUnit = (size.value * NUMERAL_SCALE).sp,
) {
    Box(modifier = GlanceModifier.size(size), contentAlignment = Alignment.Center) {
        SunnyShape(GlanceModifier.fillMaxSize())
        Text(
            text = text,
            style =
                TextStyle(
                    color = onTodayProvider(),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            maxLines = 1,
        )
    }
}

/** The Sunny shape alone, scaled to fit [modifier]'s bounds. */
@Composable
internal fun SunnyShape(modifier: GlanceModifier) {
    Image(
        provider = ImageProvider(R.drawable.widget_sunny),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(todayProvider()),
    )
}

internal val TOUCH_TARGET = 48.dp
private val MIN_SUNNY = 40.dp
private val MAX_SUNNY = 112.dp
private val ONE_LINE_SUNNY = 44.dp
private val STACKED_CHROME = 70.dp
private val WIDE_CHROME = 24.dp
private val WIDE_ENOUGH_FOR_FULL_WEEKDAY = 150.dp
private const val NUMERAL_SCALE = 0.44f
