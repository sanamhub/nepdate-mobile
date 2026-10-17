package np.nepdate.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.nepdate.R
import np.nepdate.ui.theme.HeroTextStyle
import np.nepdate.ui.theme.LocalNepdateColors
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/** The three parts of the M0 design spike. */
enum class SpikePart {
    CELL,
    HERO,
    COLOUR,
}

private data class SpikeLine(
    val number: Int,
    val text: String,
)

private val spikeLines =
    listOf(
        SpikeLine(1, "० १ २ ३ ४ ५ ६ ७ ८ ९"),
        SpikeLine(2, "बैशाख जेठ असार साउन भदौ असोज"),
        SpikeLine(3, "कार्तिक मंसिर पुष माघ फागुन चैत"),
        SpikeLine(4, "आइतवार सोमवार मङ्गलवार बुधवार बिहिवार शुक्रवार शनिवार"),
        SpikeLine(5, "कि की कु कू के कै को कौ कं कः"),
        SpikeLine(6, "क्ष ज्ञ श्र त्र द्ध द्व ह्म ट्ट"),
        SpikeLine(7, "धर्म कर्म पूर्णिमा चतुर्दशी"),
        SpikeLine(8, "पञ्चमी षष्ठी अष्टमी प्रतिपदा द्वितीया"),
        SpikeLine(9, "नयाँ वर्ष · गणतन्त्र दिवस · बिस्का जात्रा"),
        SpikeLine(10, "२०८३ असोज ८ गते, बिहिवार"),
        SpikeLine(11, "Ashoj 8, 2083 · 2026-09-24 · 2:05 PM"),
    )

private val smallSpikeStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)

/**
 * Renders the Devanagari strings or the colour tokens for the M0 spike. The screenshot test
 * captures this composable; the debug activity shows it with a tab row.
 */
@Composable
fun DesignSpikeScreen(
    part: SpikePart,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.design_spike_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.design_spike_not_verified),
                color = LocalNepdateColors.current.holiday,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(12.dp))

            when (part) {
                SpikePart.CELL -> DevanagariCell()
                SpikePart.HERO -> DevanagariHero()
                SpikePart.COLOUR -> ColourTokens()
            }
        }
    }
}

@Composable
private fun DevanagariCell() {
    Text(
        text = stringResource(R.string.design_spike_small),
        style = MaterialTheme.typography.labelLarge,
    )
    spikeLines.forEach { line ->
        Text(
            text = "${line.number}. ${line.text}",
            style = smallSpikeStyle,
            maxLines = 1,
        )
    }

    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.design_spike_cell),
        style = MaterialTheme.typography.labelLarge,
    )
    spikeLines.forEach { line ->
        Text(
            text = "${line.number}. ${line.text}",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
        )
    }
}

@Composable
private fun DevanagariHero() {
    Text(
        text = stringResource(R.string.design_spike_hero),
        style = MaterialTheme.typography.labelLarge,
    )
    spikeLines.forEach { line ->
        Text(
            text = "${line.number}. ${line.text}",
            style = HeroTextStyle,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun ColourTokens() {
    val background = MaterialTheme.colorScheme.background
    val colors = LocalNepdateColors.current
    val fill = colors.holidayFill.compositeOver(background)

    Text(
        text = stringResource(R.string.design_spike_colour),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = stringResource(R.string.design_spike_measured),
        style = MaterialTheme.typography.labelLarge,
    )
    ColourRow(
        label = stringResource(R.string.design_spike_holiday),
        swatch = colors.holiday,
        ratio = contrastRatio(colors.holiday, background),
        detail = stringResource(R.string.design_spike_on_background),
    )
    ColourRow(
        label = stringResource(R.string.design_spike_fill),
        swatch = colors.holidayFill,
        ratio = contrastRatio(colors.holiday, fill),
        detail = stringResource(R.string.design_spike_on_fill),
    )
    ColourRow(
        label = stringResource(R.string.design_spike_today),
        swatch = colors.today,
        ratio = contrastRatio(colors.today, background),
        detail = stringResource(R.string.design_spike_on_background),
    )
    ColourRow(
        label = stringResource(R.string.design_spike_event),
        swatch = colors.event,
        ratio = contrastRatio(colors.event, background),
        detail = stringResource(R.string.design_spike_on_background),
    )
    ColourRow(
        label = stringResource(R.string.design_spike_dimmed),
        swatch = colors.dimmed,
        ratio = contrastRatio(colors.dimmed, background),
        detail = stringResource(R.string.design_spike_on_background),
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.design_spike_holiday_cell),
        style = MaterialTheme.typography.labelLarge,
    )
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.holidayFill),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "१५",
                    color = colors.holiday,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        Column {
            Text(
                text = stringResource(R.string.design_spike_holiday_name),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.design_spike_public_holiday),
                color = colors.holiday,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }

    Spacer(Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.design_spike_pending),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun ColourRow(
    label: String,
    swatch: Color,
    ratio: Float,
    detail: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(swatch),
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = String.format(Locale.US, "%.2f : 1 %s", ratio, detail),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private fun contrastRatio(
    foreground: Color,
    background: Color,
): Float {
    val opaqueForeground = foreground.compositeOver(background)
    val lighter = max(opaqueForeground.luminance(), background.luminance())
    val darker = min(opaqueForeground.luminance(), background.luminance())
    return (lighter + 0.05f) / (darker + 0.05f)
}
