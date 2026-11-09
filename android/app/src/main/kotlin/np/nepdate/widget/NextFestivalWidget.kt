package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import np.nepdate.R

/** The face colours of the Next festival widget: holidays on the holiday container. */
private data class FestivalFace(
    val background: ColorProvider,
    val ink: ColorProvider,
)

/**
 * The Next festival widget: days left as a big numeral that grows with the widget, then the day's
 * holidays and festivals, as many as the height holds with "+N" for the rest, and the BS date with
 * its weekday. Public holidays sit on the holiday container, other festivals on the secondary
 * container. Two connected arrows step day by day and stop at either end; [FestivalStepAction]
 * stores the position per widget and the midnight refresh clears it.
 */
@Composable
internal fun NextFestivalWidgetContent(
    model: NextFestivalModel,
    opacity: WidgetOpacity,
    minimal: Boolean = false,
) {
    val context = LocalContext.current
    val face =
        if (model.isHoliday) {
            FestivalFace(holidayContainerProvider(), onHolidayContainerProvider())
        } else {
            FestivalFace(GlanceTheme.colors.secondaryContainer, GlanceTheme.colors.onSecondaryContainer)
        }
    val label = context.getString(R.string.widget2_next_festival_name)
    Box(
        modifier =
            widgetRoot(opacity, face.background)
                .clickable(actionStartActivity(widgetIntent(context, model.entry?.date)))
                .semantics {
                    contentDescription = label
                    testTag = "next_festival_widget"
                },
    ) {
        when {
            model.entry == null -> EmptyFace(model.emptyText, face.ink)
            LocalSize.current.height < COMPACT_HEIGHT -> NextFestivalCompact(model, face, minimal)
            else -> NextFestivalFull(model, face, minimal)
        }
    }
}

@Composable
private fun NextFestivalFull(
    model: NextFestivalModel,
    face: FestivalFace,
    minimal: Boolean,
) {
    val entry = model.entry ?: return
    val size = LocalSize.current
    val scale = typeScale(minOf(size.width, size.height))
    val fit = widgetFit()
    // A busy day gives the count a little less height, so its names get the room.
    val share = if (model.names.size > 1) BUSY_NUMBER_SHARE else NUMBER_SHARE
    val numberSize = (size.height.value * share).coerceIn(MIN_NUMBER, MAX_NUMBER).sp
    val nameSize = 16.sp * scale.coerceAtMost(MAX_NAME_SCALE)
    val dateHeight = if (minimal) 0.dp else fit.line(12.sp * scale)
    // "Today" is drawn smaller than a numeral, so it takes less height.
    val countHeight = fit.line(if (model.unit.isEmpty()) numberSize * WORD_SHARE else numberSize)
    val room = size.height - FULL_VERTICAL - ARROW_ROW - countHeight - dateHeight
    val lines = (room / fit.line(nameSize)).toInt().coerceIn(1, MAX_NAME_LINES)
    val names = fitEventNames(model.names, fit, nameSize, size.width - FULL_HORIZONTAL, lines, model::more)
    Column(modifier = GlanceModifier.fillMaxSize().padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 14.dp)) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().height(ARROW_ROW),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = model.kind,
                modifier = GlanceModifier.defaultWeight(),
                style =
                    TextStyle(
                        color = face.ink,
                        fontSize = WidgetType.label * scale,
                        fontWeight = WidgetType.labelWeight,
                    ),
                maxLines = 2,
            )
            // A two-cell face is too narrow for the label and the arrows side by side.
            if (!minimal && size.width >= ARROWS_IN_FULL) FestivalArrows(model, face)
        }
        Spacer(GlanceModifier.defaultWeight())
        DaysLeft(model, face.ink, numberSize = numberSize)
        Text(
            text = names.ifEmpty { entry.name },
            style = TextStyle(color = face.ink, fontSize = nameSize, fontWeight = WidgetType.emphasisWeight),
            maxLines = lines,
        )
        if (!minimal) {
            Text(text = model.dateLine, style = TextStyle(color = face.ink, fontSize = 12.sp * scale), maxLines = 1)
        }
    }
}

/**
 * The one-row face. Wide enough, it puts the count, the name and the arrows in a row; a two-cell
 * face stacks the count over the name, so a long count such as "Today" never pushes the name out.
 */
@Composable
private fun NextFestivalCompact(
    model: NextFestivalModel,
    face: FestivalFace,
    minimal: Boolean,
) {
    val entry = model.entry ?: return
    val width = LocalSize.current.width
    val fit = widgetFit()
    if (width < COMPACT_ROW_WIDTH) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val roomy = LocalSize.current.height >= ROOMY_STACK
            DaysLeft(model, face.ink, numberSize = if (roomy) 34.sp else 22.sp)
            val nameSize = if (roomy) 15.sp else 13.sp
            val lines = if (roomy) 2 else 1
            Text(
                text =
                    fitEventNames(
                        model.names,
                        fit,
                        nameSize,
                        width - 28.dp,
                        lines,
                        model::more,
                    ).ifEmpty { entry.name },
                style =
                    TextStyle(
                        color = face.ink,
                        fontSize = if (roomy) 15.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = if (roomy) 2 else 1,
            )
        }
        return
    }
    Row(
        modifier = GlanceModifier.fillMaxSize().padding(start = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DaysLeft(model, face.ink, numberSize = 30.sp)
        val textWidth = width - COMPACT_ROW_CHROME - if (!minimal && width >= ARROWS_IN_COMPACT) ARROWS_WIDTH else 0.dp
        Text(
            text = fitEventNames(model.names, fit, 14.sp, textWidth, 2, model::more).ifEmpty { entry.name },
            modifier = GlanceModifier.defaultWeight().padding(start = 8.dp),
            style = TextStyle(color = face.ink, fontSize = 14.sp, fontWeight = FontWeight.Bold),
            maxLines = 2,
        )
        if (!minimal && width >= ARROWS_IN_COMPACT) FestivalArrows(model, face)
    }
}

@Composable
private fun DaysLeft(
    model: NextFestivalModel,
    ink: ColorProvider,
    numberSize: TextUnit,
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = model.days,
            modifier = GlanceModifier.semantics { testTag = "next_festival_days" },
            // "Today" is a word, not a count, so it gets a smaller size than the numeral.
            style =
                TextStyle(
                    color = ink,
                    fontSize = if (model.unit.isEmpty()) numberSize * WORD_SHARE else numberSize,
                    fontWeight = FontWeight.Bold,
                ),
            maxLines = 1,
        )
        if (model.unit.isNotEmpty()) {
            Text(
                text = model.unit,
                modifier = GlanceModifier.padding(start = 4.dp, bottom = (numberSize.value * UNIT_LIFT).dp),
                style =
                    TextStyle(
                        color = ink,
                        fontSize = (numberSize.value * UNIT_SHARE).sp,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
            )
        }
    }
}

/**
 * The connected arrow pair, filled with the face's ink and drawn in the face colour: the inverse
 * of the face, so the controls clear 3:1 against it and the arrows 4.5:1 against the fill.
 */
@Composable
private fun FestivalArrows(
    model: NextFestivalModel,
    face: FestivalFace,
) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconControl(
            spec =
                ControlSpec(
                    icon = R.drawable.ic_sym_chevron_left,
                    description = context.getString(R.string.widget2_previous_festival),
                    tag = "next_festival_previous",
                    action = model.previousIndex?.let(::stepAction),
                ),
            style = arrowStyle(R.drawable.widget_shape_start, face),
        )
        Spacer(GlanceModifier.width(2.dp))
        IconControl(
            spec =
                ControlSpec(
                    icon = R.drawable.ic_sym_chevron_right,
                    description = context.getString(R.string.widget2_following_festival),
                    tag = "next_festival_next",
                    action = model.nextIndex?.let(::stepAction),
                ),
            style = arrowStyle(R.drawable.widget_shape_end, face),
        )
    }
}

private fun arrowStyle(
    shape: Int,
    face: FestivalFace,
) = ControlStyle(shape, fill = face.ink, ink = face.background, width = 36.dp, height = 32.dp)

private fun stepAction(index: Int) =
    actionRunCallback<FestivalStepAction>(actionParametersOf(FESTIVAL_INDEX_PARAM to index))

@Composable
private fun EmptyFace(
    text: String,
    ink: ColorProvider,
) {
    Box(modifier = GlanceModifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(text = text, style = TextStyle(color = ink, fontSize = 14.sp), maxLines = 3)
    }
}

private val COMPACT_HEIGHT = 100.dp
private val FULL_VERTICAL = 18.dp
private val FULL_HORIZONTAL = 24.dp
private const val MAX_NAME_LINES = 4
private val COMPACT_ROW_CHROME = 90.dp
private val ARROWS_WIDTH = 74.dp
private val ARROWS_IN_COMPACT = 220.dp
private val ARROWS_IN_FULL = 160.dp
private val COMPACT_ROW_WIDTH = 170.dp
private val ARROW_ROW = 48.dp
private val ROOMY_STACK = 84.dp
private const val NUMBER_SHARE = 0.32f
private const val BUSY_NUMBER_SHARE = 0.26f
private const val MAX_NAME_SCALE = 1.5f
private const val MIN_NUMBER = 36f
private const val MAX_NUMBER = 96f
private const val UNIT_SHARE = 0.32f
private const val UNIT_LIFT = 0.16f
private const val WORD_SHARE = 0.55f
