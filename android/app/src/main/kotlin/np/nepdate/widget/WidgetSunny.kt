package np.nepdate.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import np.nepdate.R

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

private const val NUMERAL_SCALE = 0.44f
