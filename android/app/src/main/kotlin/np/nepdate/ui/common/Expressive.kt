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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * A closed wavy outline in the spirit of the Material 3 Expressive shape library: [lobes] rounded
 * bumps between [inner] and the full radius. Drawn from a sampled polar curve, so it needs no extra
 * library and costs one path per size.
 */
internal class WavyShape(
    private val lobes: Int,
    private val inner: Float,
    private val sharpness: Float = 1f,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = minOf(cx, cy)
        val steps = lobes * STEPS_PER_LOBE
        for (i in 0..steps) {
            val angle = (i.toFloat() / steps) * 2f * PI.toFloat() - (PI.toFloat() / 2f)
            val wave = (0.5f + 0.5f * cos(angle * lobes + (PI.toFloat() / 2f) * lobes)).pow(sharpness)
            val r = radius * (inner + (1f - inner) * wave)
            val x = cx + r * cos(angle)
            val y = cy + r * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }

    private companion object {
        const val STEPS_PER_LOBE = 12
    }
}

/** Twelve soft rays: the today marker. Marigold, the Tihar flower and the sun. */
internal val SunnyShape: Shape = WavyShape(lobes = 12, inner = 0.86f)

/** Nine soft lobes: the date tile of the day sheet. */
internal val CookieShape: Shape = WavyShape(lobes = 9, inner = 0.9f)

/** Four deep lobes: decoration on the first-run screen. */
internal val CloverShape: Shape = WavyShape(lobes = 4, inner = 0.62f, sharpness = 1.6f)

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
