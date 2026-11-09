package np.nepdate.widget

import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight

/**
 * The Material 3 type roles the widgets use, since Glance cannot read the app's typography. Sizes
 * follow the M3 scale; weights follow it too, so only numerals and the weekday headline are bold,
 * titles and labels are medium and names in lists are regular. That keeps a hierarchy on a widget
 * instead of every line shouting in bold.
 */
internal object WidgetType {
    /** A widget's own title, such as "Festivals" or the month: M3 title large, medium weight. */
    val title = 18.sp
    val titleWeight = FontWeight.Medium

    /** A list row's names: M3 body medium. A holiday's names take [emphasisWeight]. */
    val body = 14.sp
    val bodyWeight = FontWeight.Normal
    val emphasisWeight = FontWeight.Medium

    /** Pills, captions and the relative day: M3 label medium. */
    val label = 12.sp
    val labelWeight = FontWeight.Medium

    /** Weekday initials over a numeral: M3 label small. */
    val labelSmall = 11.sp
}
