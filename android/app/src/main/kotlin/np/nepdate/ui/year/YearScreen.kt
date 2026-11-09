package np.nepdate.ui.year

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.common.GroupPosition
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.groupShape
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.month.PICKER_FIRST_YEAR
import np.nepdate.ui.month.PICKER_LAST_YEAR
import np.nepdate.ui.month.monthLayout
import np.nepdate.ui.settings.rememberHaptics
import np.nepdate.ui.theme.LocalNepdateColors

/**
 * The year at a glance: twelve mini months in a 3 by 4 grid that fills the screen. Saturdays are
 * red, public holidays sit on the holiday container and today is marigold. Tapping a month opens it.
 *
 * @param initialYear the BS year to open on; clamped to the library range
 * @param onOpenMonth called with the BS year and month (1..12) of the tapped tile
 */
@Composable
internal fun YearScreen(
    initialYear: Int,
    today: NepaliDate,
    settings: Settings,
    onBack: () -> Unit,
    onOpenMonth: (year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val digits = settings.digits.resolve(lang)
    var year by rememberSaveable { mutableIntStateOf(initialYear.coerceIn(PICKER_FIRST_YEAR, PICKER_LAST_YEAR)) }
    val haptics = rememberHaptics(settings.haptics)
    BackHandler(onBack = onBack)
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        YearTopBar(
            year = year,
            digits = digits,
            onBack = onBack,
            onPrevious = {
                haptics.tick()
                year--
            },
            onNext = {
                haptics.tick()
                year++
            },
        )
        Legend(digits = digits, saturday = Weekday.SATURDAY.displayName(lang))
        YearPages(
            year = year,
            today = today,
            settings = settings,
            digits = digits,
            onOpenMonth = onOpenMonth,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun YearTopBar(
    year: Int,
    digits: Digits,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val backLabel = stringResource(R.string.year_back)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 72.dp)
                .padding(start = 4.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            shapes = IconButtonDefaults.shapes(),
            modifier = Modifier.semantics { contentDescription = backLabel },
        ) {
            SymbolIcon(icon = R.drawable.ic_sym_arrow_back, tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            text = year.toString().inDigits(digits),
            modifier =
                Modifier
                    .weight(1f)
                    .testTag("year_title")
                    .semantics { heading() },
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            YearStep(
                label = stringResource(R.string.year_previous),
                icon = R.drawable.ic_sym_chevron_left,
                shape = groupShape(GroupPosition.START),
                enabled = year > PICKER_FIRST_YEAR,
                onClick = onPrevious,
            )
            YearStep(
                label = stringResource(R.string.year_next),
                icon = R.drawable.ic_sym_chevron_right,
                shape = groupShape(GroupPosition.END),
                enabled = year < PICKER_LAST_YEAR,
                onClick = onNext,
            )
        }
    }
}

/** One half of the connected previous/next year group; pressing rounds its inner corners out. */
@Composable
private fun YearStep(
    label: String,
    icon: Int,
    shape: Shape,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        shapes = IconButtonDefaults.shapes(shape = shape, pressedShape = RoundedCornerShape(PRESSED_CORNER)),
        colors =
            IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        modifier = Modifier.size(width = 52.dp, height = 48.dp).semantics { contentDescription = label },
    ) {
        SymbolIcon(icon = icon, size = 22.dp)
    }
}

@Composable
private fun Legend(
    digits: Digits,
    saturday: String,
) {
    val colors = LocalNepdateColors.current
    Row(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(text = stringResource(R.string.month_today)) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(colors.today))
        }
        LegendItem(text = stringResource(R.string.public_holiday)) {
            Box(Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(colors.holidayContainer))
        }
        LegendItem(text = saturday) {
            Text(
                text = "1".inDigits(digits),
                color = colors.holiday,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun LegendItem(
    text: String,
    swatch: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        swatch()
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

/** The twelve months of [year]; a new year slides in from the side it was asked for. */
@Composable
private fun YearPages(
    year: Int,
    today: NepaliDate,
    settings: Settings,
    digits: Digits,
    onOpenMonth: (year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = year,
        modifier = modifier,
        transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            val enter =
                slideInHorizontally(motion.defaultSpatialSpec()) { it / SLIDE_FRACTION * direction } +
                    fadeIn(motion.defaultEffectsSpec())
            val exit =
                slideOutHorizontally(motion.defaultSpatialSpec()) { -it / SLIDE_FRACTION * direction } +
                    fadeOut(motion.fastEffectsSpec())
            enter.togetherWith(exit)
        },
        label = "year",
    ) { shownYear ->
        // Twelve layouts, one Patro lookup per day, computed once per year. The tithi is not drawn,
        // so it is switched off to skip that work.
        val months =
            remember(shownYear, today) {
                val plain = settings.copy(showTithi = false)
                (1..MONTHS).map { monthLayout(shownYear, it, plain, today) }
            }
        MonthTiles(
            months = months,
            today = today,
            digits = digits,
            onOpenMonth = onOpenMonth,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private const val MONTHS = 12
private const val SLIDE_FRACTION = 4
private val PRESSED_CORNER = 14.dp
