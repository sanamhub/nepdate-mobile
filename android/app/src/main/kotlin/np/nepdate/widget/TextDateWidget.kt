package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import np.nepdate.R

/**
 * The Date text widget: today's BS date as plain type with no card or shape, for a clean home
 * screen. The month and day are the large line, the weekday and the tithi the small one. It starts
 * with no background, so its ink is white like a launcher clock; with a background it uses the
 * surface colours. A minimal widget keeps the large line only.
 */
@Composable
internal fun TextDateContent(
    model: TodayWidgetModel,
    opacity: WidgetOpacity,
    minimal: Boolean = false,
) {
    val context = LocalContext.current
    val size = LocalSize.current
    val fit = widgetFit()
    val inner = size.width - TEXT_PADDING * 2
    val scale = columnScale(size.height * TEXT_HEIGHT_BOOST, inner, TEXT_WIDTH_AT_ONE)
    val onWallpaper = opacity == WidgetOpacity.TRANSPARENT
    val ink = if (onWallpaper) ColorProvider(Color.White) else GlanceTheme.colors.onSurface
    val soft = if (onWallpaper) ColorProvider(SOFT_WHITE) else GlanceTheme.colors.onSurfaceVariant
    val parts = listOf(model.weekday, model.tithi).filter { it.isNotEmpty() }
    val detail =
        if (fit.fits(parts.joinToString(" · "), DETAIL * scale, inner)) {
            parts.joinToString(" · ")
        } else {
            parts.firstOrNull().orEmpty()
        }
    val roomForDetail = size.height - TEXT_VERTICAL * 2 - fit.line(TITLE * scale) >= fit.line(DETAIL * scale)
    Column(
        modifier =
            widgetRoot(opacity, GlanceTheme.colors.surface)
                .clickable(actionStartActivity(widgetIntent(context)))
                .semantics {
                    contentDescription = "${context.getString(R.string.widget4_text_name)}, ${model.bsLongLine}"
                    testTag = "text_widget"
                }.padding(horizontal = TEXT_PADDING, vertical = TEXT_VERTICAL),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${model.monthName} ${model.bsDay}",
            style = TextStyle(color = ink, fontSize = TITLE * scale, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        if (!minimal && roomForDetail && detail.isNotEmpty()) {
            Text(
                text = detail,
                style =
                    TextStyle(
                        color = if (model.isHoliday || model.isSaturday) holidayOn(onWallpaper) else soft,
                        fontSize = DETAIL * scale,
                        fontWeight = FontWeight.Medium,
                    ),
                maxLines = 1,
            )
        }
    }
}

/** The holiday ink: the brand red on a card, a light red that stays legible on a wallpaper. */
@Composable
private fun holidayOn(onWallpaper: Boolean): ColorProvider =
    if (onWallpaper) ColorProvider(WALLPAPER_HOLIDAY) else holidayProvider()

private val TEXT_PADDING = 14.dp
private val TEXT_VERTICAL = 6.dp
private const val TEXT_HEIGHT_BOOST = 1.3f
private val TEXT_WIDTH_AT_ONE = 150.dp
private val TITLE = 24.sp
private val DETAIL = 14.sp
private val SOFT_WHITE = Color(0xE6FFFFFF)
private val WALLPAPER_HOLIDAY = Color(0xFFFFB4AB)
