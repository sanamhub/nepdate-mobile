package np.nepdate.widget

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import np.nepdate.R

/**
 * How an icon control is drawn. [fill] and [ink] are both opaque theme roles, picked so the icon
 * clears 4.5:1 on its fill and the fill clears 3:1 on the widget face in light, dark and dynamic
 * colour (widget review item 2).
 */
internal data class ControlStyle(
    @param:DrawableRes val shape: Int,
    val fill: ColorProvider,
    val ink: ColorProvider,
    val width: Dp = 40.dp,
    val height: Dp = 36.dp,
)

/** What an icon control does and how it is announced. */
internal data class ControlSpec(
    @param:DrawableRes val icon: Int,
    val description: String,
    val tag: String,
    val action: Action?,
)

/**
 * An icon button: the visible chip from [style] inside a 48 dp tall touch target. A null action
 * draws the chip in the outline colour and leaves it inert, for the ends of the calendar range.
 */
@Composable
internal fun IconControl(
    spec: ControlSpec,
    style: ControlStyle,
) {
    val action = spec.action
    Box(
        modifier =
            GlanceModifier
                .width(style.width)
                .height(TOUCH_TARGET)
                .then(if (action != null) GlanceModifier.clickable(action) else GlanceModifier)
                .semantics {
                    contentDescription = spec.description
                    testTag = spec.tag
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                GlanceModifier
                    .width(style.width)
                    .height(style.height)
                    .shapeBackground(style.shape, style.fill),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(spec.icon),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp),
                colorFilter = ColorFilter.tint(if (action != null) style.ink else GlanceTheme.colors.outline),
            )
        }
    }
}

/**
 * The mark under a day: a short holiday-red bar for a public holiday, a round dot in the tertiary
 * colour for any other event, or an empty slot of the same height so numerals stay aligned.
 */
@Composable
internal fun DayMarker(
    isHoliday: Boolean,
    hasEvents: Boolean,
    onToday: Boolean = false,
) {
    when {
        isHoliday ->
            Box(
                modifier =
                    GlanceModifier
                        .width(HOLIDAY_BAR_WIDTH)
                        .height(MARKER_SIZE)
                        .shapeBackground(
                            R.drawable.widget_shape_pill,
                            if (onToday) onTodayProvider() else holidayProvider(),
                        ),
            ) {}
        hasEvents ->
            Box(
                modifier =
                    GlanceModifier
                        .size(MARKER_SIZE)
                        .shapeBackground(
                            R.drawable.widget_shape_pill,
                            if (onToday) onTodayProvider() else GlanceTheme.colors.tertiary,
                        ),
            ) {}
        else -> Spacer(GlanceModifier.height(MARKER_SIZE))
    }
}

/**
 * A rounded date badge for list rows: the BS day over the short weekday. Holidays sit on the
 * holiday container, other days on the highest surface container.
 */
@Composable
internal fun DateBadge(
    row: WidgetDayRow,
    size: Dp,
) {
    val ink = if (row.isHoliday) onHolidayContainerProvider() else GlanceTheme.colors.onSurface
    Column(
        modifier =
            GlanceModifier
                .size(size)
                .shapeBackground(
                    R.drawable.widget_shape_12,
                    if (row.isHoliday) holidayContainerProvider() else surfaceContainerHighestProvider(),
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.bsDay,
            style =
                TextStyle(
                    color = ink,
                    fontSize = (size.value * BADGE_DAY_SCALE).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            maxLines = 1,
        )
        if (size >= BADGE_WITH_WEEKDAY) {
            Text(
                text = row.weekdayShort,
                style =
                    TextStyle(
                        color = ink,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    ),
                maxLines = 1,
            )
        }
    }
}

/** A thin rule in the outline variant colour. */
@Composable
internal fun Rule(modifier: GlanceModifier) {
    Box(modifier = modifier.background(outlineVariantProvider())) {}
}

/**
 * How much a face's type grows with [extent]: 1x at a two-cell side (110 dp), shrinking a little
 * below it and growing up to 1.8x, so a bigger widget gets bigger numerals and words (review item 6).
 */
internal fun typeScale(extent: Dp): Float = (extent / BASE_EXTENT).coerceIn(MIN_TYPE_SCALE, MAX_TYPE_SCALE)

internal val MARKER_SIZE = 4.dp
private val HOLIDAY_BAR_WIDTH = 12.dp
private val BADGE_WITH_WEEKDAY = 38.dp
private const val BADGE_DAY_SCALE = 0.36f
private val BASE_EXTENT = 110.dp
private const val MIN_TYPE_SCALE = 0.85f
private const val MAX_TYPE_SCALE = 1.8f
