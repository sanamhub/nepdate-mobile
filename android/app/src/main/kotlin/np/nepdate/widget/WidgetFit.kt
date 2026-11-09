package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.glance.LocalContext

/**
 * Glance cannot measure text, so a face plans its lines with these estimates and leaves out a line
 * that would not fit instead of cutting it with an ellipsis. The factors are deliberately generous
 * for the bold Latin and Devanagari faces the widgets use, and both follow the system font size.
 */
internal class WidgetFit(
    private val fontScale: Float,
) {
    /** The width [text] takes at [fontSize]. */
    fun width(
        text: String,
        fontSize: TextUnit,
    ): Dp = (text.length * fontSize.value * fontScale * CHAR_WIDTH).dp

    /** True when [text] fits on one line of [available] width. */
    fun fits(
        text: String,
        fontSize: TextUnit,
        available: Dp,
    ): Boolean = width(text, fontSize) <= available

    /** The height one line of text takes at [fontSize]. */
    fun line(fontSize: TextUnit): Dp = (fontSize.value * fontScale * LINE_HEIGHT).dp

    /** The height of a pill: one line plus its vertical padding and the gap above it. */
    fun pill(fontSize: TextUnit): Dp = line(fontSize) + PILL_CHROME
}

@Composable
internal fun widgetFit(): WidgetFit = WidgetFit(LocalContext.current.resources.configuration.fontScale)

/**
 * The type scale of a face whose text column is [textWidth] wide and [height] tall: it grows with
 * the height, as [typeScale] does, but never past what the column width can hold.
 */
internal fun columnScale(
    height: Dp,
    textWidth: Dp,
    widthAtOne: Dp,
): Float = minOf(typeScale(height), textWidth / widthAtOne).coerceIn(MIN_COLUMN_SCALE, MAX_COLUMN_SCALE)

private const val CHAR_WIDTH = 0.58f
private const val LINE_HEIGHT = 1.3f
private val PILL_CHROME = 10.dp
private const val MIN_COLUMN_SCALE = 0.85f
private const val MAX_COLUMN_SCALE = 1.6f
