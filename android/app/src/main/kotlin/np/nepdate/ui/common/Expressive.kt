package np.nepdate.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The corner shape of one row in a segmented list group (Android 16 Settings style): large outer
 * corners on the first and last row, small corners where rows meet.
 */
internal fun segmentShape(
    index: Int,
    count: Int,
    outer: Dp = 24.dp,
    inner: Dp = 4.dp,
): Shape =
    when {
        count == 1 -> RoundedCornerShape(outer)
        index == 0 -> RoundedCornerShape(topStart = outer, topEnd = outer, bottomStart = inner, bottomEnd = inner)
        index == count - 1 ->
            RoundedCornerShape(
                topStart = inner,
                topEnd = inner,
                bottomStart = outer,
                bottomEnd = outer,
            )
        else -> RoundedCornerShape(inner)
    }

/** Where a button sits in a connected button group. */
internal enum class GroupPosition { START, MIDDLE, END, ALONE }

/** The shape of a button in a horizontal connected button group. */
internal fun groupShape(
    position: GroupPosition,
    outer: Dp = 20.dp,
    inner: Dp = 6.dp,
): Shape =
    when (position) {
        GroupPosition.START ->
            RoundedCornerShape(
                CornerSize(outer),
                CornerSize(inner),
                CornerSize(inner),
                CornerSize(outer),
            )
        GroupPosition.END ->
            RoundedCornerShape(
                CornerSize(inner),
                CornerSize(outer),
                CornerSize(outer),
                CornerSize(inner),
            )
        GroupPosition.MIDDLE -> RoundedCornerShape(inner)
        GroupPosition.ALONE -> RoundedCornerShape(outer)
    }

/** A Material Symbols icon from the app's drawables (only the symbols the app uses ship). */
@Composable
internal fun SymbolIcon(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = modifier.size(size),
        tint = if (tint == Color.Unspecified) androidx.compose.material3.LocalContentColor.current else tint,
    )
}

/** A filled shape with content centred on it: the Sunny today marker, the Cookie date tile. */
@Composable
internal fun ShapeBadge(
    shape: Shape,
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.size(size).clip(shape).background(color),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/**
 * The holiday or event mark in an icon's place: the soft burst for a public holiday and the diamond
 * for an event, the same marks the month grid and the today card draw. [size] is the icon slot; the
 * mark sits centred in it so text beside it lines up as it would beside an icon.
 */
@Composable
internal fun DayMark(
    holiday: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (holiday) {
            Box(Modifier.size(size * HOLIDAY_MARK).clip(SoftBurstShape).background(color))
        } else {
            Box(Modifier.size(size * EVENT_MARK).clip(DiamondShape).background(color))
        }
    }
}

private const val HOLIDAY_MARK = 0.8f
private const val EVENT_MARK = 0.55f
