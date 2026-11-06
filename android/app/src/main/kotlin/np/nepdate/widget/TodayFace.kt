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
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
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
    val day: String,
    val weekday: String,
    val detail: String,
    val pill: String?,
    val pillIsHoliday: Boolean,
)

private fun TodayWidgetModel.face(showAd: Boolean): TodayFace {
    val pill = holidayName ?: eventName
    return if (showAd) {
        TodayFace(adMonthYear, adDay, weekday, joinDots(bsShortLine, tithi), pill, holidayName != null)
    } else {
        TodayFace(bsMonthYear, bsDay, weekday, joinDots(tithi, adLine), pill, holidayName != null)
    }
}

private fun joinDots(vararg parts: String): String = parts.filter { it.isNotEmpty() }.joinToString(" · ")

/**
 * W-01 Today. The numeral sits on the marigold Sunny shape on a primary container face, and every
 * size scales its type with the widget: a larger widget gets a larger numeral and weekday, not just
 * more padding. A round button flips the face between the BS and the AD date; the choice is saved
 * per widget by [TodayFaceAction]. The rest of the face opens the app on today.
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
        val stacked = LocalSize.current.width < WIDE_FACE
        if (stacked) TodayFaceStacked(model, face) else TodayFaceWide(model, face)
        FlipButton(showAd, if (stacked) STACKED_FLIP else WIDE_FLIP)
    }
}

@Composable
private fun TodayFaceStacked(
    model: TodayWidgetModel,
    face: TodayFace,
) {
    val size = LocalSize.current
    val scale = typeScale(minOf(size.width, size.height))
    val sunny = (minOf(size.width, size.height) - STACKED_CHROME * scale).coerceIn(MIN_SUNNY, MAX_SUNNY)
    Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            // A narrow face drops the year: the month name alone always fits beside the flip button.
            text = if (size.width < FULL_DETAIL_WIDTH) face.top.substringBeforeLast(" ") else face.top,
            modifier = GlanceModifier.fillMaxWidth().padding(end = STACKED_FLIP_CLEARANCE),
            style = faceStyle(14.sp * scale, FontWeight.Bold),
            maxLines = 1,
        )
        Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight(), contentAlignment = Alignment.Center) {
            SunnyNumeral(face.day, sunny)
        }
        Text(
            text = face.weekday,
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(color = weekdayColor(model), fontSize = 15.sp * scale, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        if (face.pill != null && size.height >= PILL_HEIGHT) {
            FacePill(face.pill, face.pillIsHoliday, 12.sp * scale)
        } else {
            Text(
                // A narrow face keeps only the first part (the tithi, or the BS date on the AD face),
                // so a 2x2 widget never cuts a date mid-word.
                text = if (size.width < FULL_DETAIL_WIDTH) face.detail.substringBefore(" · ") else face.detail,
                modifier = GlanceModifier.fillMaxWidth(),
                style = faceStyle(12.sp * scale),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TodayFaceWide(
    model: TodayWidgetModel,
    face: TodayFace,
) {
    val size = LocalSize.current
    val scale = typeScale(size.height)
    val sunny = (size.height - WIDE_CHROME).coerceIn(MIN_SUNNY, MAX_SUNNY * scale)
    Row(modifier = GlanceModifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        SunnyNumeral(face.day, sunny)
        Column(modifier = GlanceModifier.defaultWeight().padding(start = 14.dp)) {
            // Only the top line shares its row with the flip button, so only it keeps clear of it.
            Text(
                text = face.top,
                modifier = GlanceModifier.padding(end = FLIP_CLEARANCE),
                style = faceStyle(14.sp * scale, FontWeight.Bold),
                maxLines = 1,
            )
            Text(
                text = face.weekday,
                style = TextStyle(color = weekdayColor(model), fontSize = 24.sp * scale, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
            WideFooter(model, face, scale, roomy = size.height >= WIDE_PILL_HEIGHT)
        }
    }
}

/**
 * The lines under the weekday. A short face has room for one more line: today's holiday or event
 * pill when there is one, otherwise the detail line. A taller face shows the detail and then the
 * pill, or the next holiday.
 */
@Composable
private fun WideFooter(
    model: TodayWidgetModel,
    face: TodayFace,
    scale: Float,
    roomy: Boolean,
) {
    if (!roomy && face.pill != null) {
        FacePill(face.pill, face.pillIsHoliday, 12.sp * scale)
        return
    }
    Text(text = face.detail, style = faceStyle(13.sp * scale), maxLines = 1)
    val next = model.nextHoliday
    when {
        !roomy -> Unit
        face.pill != null -> FacePill(face.pill, face.pillIsHoliday, 12.sp * scale)
        next != null ->
            Text(
                text = "${next.name} · ${next.relative}",
                modifier = GlanceModifier.padding(top = 2.dp),
                style = faceStyle(12.sp * scale, FontWeight.Medium),
                maxLines = 1,
            )
    }
}

/** Today's holiday on the holiday container, or today's event on the surface, as a small pill. */
@Composable
private fun FacePill(
    text: String,
    isHoliday: Boolean,
    fontSize: TextUnit,
) {
    Text(
        text = text,
        modifier =
            GlanceModifier
                .padding(top = 4.dp)
                .shapeBackground(
                    R.drawable.widget_shape_pill,
                    if (isHoliday) holidayContainerProvider() else GlanceTheme.colors.surface,
                ).padding(horizontal = 10.dp, vertical = 3.dp),
        style =
            TextStyle(
                color = if (isHoliday) onHolidayContainerProvider() else GlanceTheme.colors.onSurface,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
            ),
        maxLines = 1,
    )
}

@Composable
private fun faceStyle(
    fontSize: TextUnit,
    weight: FontWeight = FontWeight.Normal,
): TextStyle = TextStyle(color = GlanceTheme.colors.onPrimaryContainer, fontSize = fontSize, fontWeight = weight)

@Composable
private fun weekdayColor(model: TodayWidgetModel): ColorProvider =
    if (model.isHoliday || model.isSaturday) holidayProvider() else GlanceTheme.colors.onPrimaryContainer

/**
 * The flip button: a filled primary circle with the on-primary icon. The old button tinted a white
 * shape with a 12% white tint, which `setColorFilter` blends instead of replacing, so it drew nearly
 * white on the dark face. Primary on primary container keeps the icon above 4.5:1 and the button
 * above 3:1 in light, dark and wallpaper colours.
 */
@Composable
private fun FlipButton(
    showAd: Boolean,
    width: Dp,
) {
    val context = LocalContext.current
    val description = context.getString(if (showAd) R.string.widget2_show_bs else R.string.widget2_show_ad)
    Box(modifier = GlanceModifier.padding(4.dp)) {
        IconControl(
            spec =
                ControlSpec(
                    icon = R.drawable.ic_sym_swap_horiz,
                    description = description,
                    tag = "today_flip",
                    action = actionRunCallback<TodayFaceAction>(),
                ),
            style =
                ControlStyle(
                    shape = R.drawable.widget_shape_pill,
                    fill = GlanceTheme.colors.primary,
                    ink = GlanceTheme.colors.onPrimary,
                    width = width,
                    height = width,
                ),
        )
    }
}

internal val TOUCH_TARGET = 48.dp
private val WIDE_FACE = 200.dp
private val PILL_HEIGHT = 150.dp
private val FLIP_CLEARANCE = 40.dp
private val STACKED_FLIP_CLEARANCE = 32.dp
private val WIDE_FLIP = 36.dp
private val STACKED_FLIP = 28.dp
private val FULL_DETAIL_WIDTH = 160.dp
private val WIDE_PILL_HEIGHT = 130.dp
private val MIN_SUNNY = 40.dp
private val MAX_SUNNY = 120.dp
private val STACKED_CHROME = 72.dp
private val WIDE_CHROME = 24.dp
