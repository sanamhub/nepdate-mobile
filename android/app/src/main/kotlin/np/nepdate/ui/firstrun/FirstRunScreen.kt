package np.nepdate.ui.firstrun

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.data.Look
import np.nepdate.data.Settings
import np.nepdate.data.TodayProvider
import np.nepdate.ui.common.CloverShape
import np.nepdate.ui.common.CookieShape
import np.nepdate.ui.common.GroupPosition
import np.nepdate.ui.common.ShapeBadge
import np.nepdate.ui.common.SunnyShape
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.bsLongNepali
import np.nepdate.ui.common.groupShape
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.settings.AppLanguage
import np.nepdate.ui.theme.DarkColors
import np.nepdate.ui.theme.LightColors
import np.nepdate.ui.theme.LocalNepdateColors
import java.time.Clock
import java.util.Locale

/**
 * The first-run welcome: today's date on the marigold Sunny shape, the promise, the language (Android
 * 13 and later, through the per-app locale) and the look. A look applies at once through [onLook],
 * so the screen previews it live; [onDone] opens the patro.
 */
@Composable
internal fun FirstRunScreen(
    settings: Settings,
    onLook: (Look) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember(settings.dateFollows) { TodayProvider(Clock.systemDefaultZone()).today(settings.dateFollows) }
    val digits = settings.digits.resolve(languageFor(LocalConfiguration.current.locales[0]))
    val canPickLanguage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .systemBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
                .testTag("first_run_screen"),
    ) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Collage(today, digits)
            Text(
                text = stringResource(R.string.firstrun_title),
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp).semantics { heading() },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
            )
            Text(
                text =
                    stringResource(
                        if (canPickLanguage) R.string.firstrun_lead else R.string.firstrun_lead_look_only,
                    ),
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (canPickLanguage) {
                LanguageChoice(today, modifier = Modifier.padding(top = 20.dp))
            }
            LookChoice(settings.look, onLook, modifier = Modifier.padding(top = 12.dp))
            Spacer(Modifier.height(16.dp))
        }
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(64.dp).testTag("first_run_done"),
            shape = RoundedCornerShape(32.dp),
        ) {
            Text(
                text = stringResource(R.string.firstrun_open),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
            )
        }
    }
}

/** Decoration: today's BS day and month on Sunny, with Cookie and Clover shapes around it. */
@Composable
private fun Collage(
    today: NepaliDate,
    digits: Digits,
) {
    val colors = LocalNepdateColors.current
    val scheme = MaterialTheme.colorScheme
    val months = stringArrayResource(R.array.bs_month_long)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(236.dp)
                .padding(top = 8.dp, bottom = 4.dp)
                .clearAndSetSemantics {},
    ) {
        ShapeBadge(CookieShape, scheme.tertiaryContainer, 120.dp, Modifier.offset(x = 6.dp, y = 18.dp)) {}
        ShapeBadge(
            CloverShape,
            colors.holidayContainer,
            96.dp,
            Modifier.align(Alignment.TopEnd).offset(x = (-10).dp),
        ) {}
        ShapeBadge(
            CookieShape,
            scheme.secondaryContainer,
            78.dp,
            Modifier.align(Alignment.BottomEnd).offset(x = (-36).dp, y = (-6).dp),
        ) {}
        ShapeBadge(SunnyShape, colors.today, 176.dp, Modifier.align(Alignment.Center)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = today.dayOfMonth.toString().inDigits(digits),
                    color = colors.onToday,
                    fontSize = 64.sp,
                    lineHeight = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = months[today.monthValue - 1],
                    color = colors.onToday,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** Nepali or English as a connected two-button group; the chosen one turns into a full pill. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun LanguageChoice(
    today: NepaliDate,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val systemLanguage = configuration.locales[0].language
    var selected by remember { mutableStateOf(AppLanguage.current(context) ?: systemLanguage) }
    val englishMonths =
        remember(context, configuration) {
            val english = Configuration(configuration).apply { setLocale(Locale.ENGLISH) }
            context.createConfigurationContext(english).resources.getStringArray(R.array.bs_month_long)
        }
    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        LanguageButton(
            name = stringResource(R.string.settings_language_nepali),
            sample = bsLongNepali(today),
            selected = selected == NEPALI,
            position = GroupPosition.START,
        ) {
            selected = NEPALI
            AppLanguage.set(context, NEPALI)
        }
        LanguageButton(
            name = stringResource(R.string.settings_language_english),
            sample = bsLong(today, englishMonths[today.monthValue - 1], Digits.ENGLISH),
            selected = selected != NEPALI,
            position = GroupPosition.END,
        ) {
            selected = ENGLISH
            AppLanguage.set(context, ENGLISH)
        }
    }
}

@Composable
private fun RowScope.LanguageButton(
    name: String,
    sample: String,
    selected: Boolean,
    position: GroupPosition,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (selected) scheme.primary else scheme.surfaceContainerHighest
    val content = if (selected) scheme.onPrimary else scheme.onSurface
    val shape = if (selected) RoundedCornerShape(36.dp) else groupShape(position, outer = 32.dp, inner = 8.dp)
    Column(
        modifier =
            Modifier
                .weight(1f)
                .heightIn(min = 72.dp)
                .clip(shape)
                .background(container)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Text(text = name, color = content, style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
        Text(text = sample, color = content.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
    }
}

/** The colours of a look's mini preview: the page, a card and a line of text. */
private data class Preview(
    val page: Brush,
    val card: Color,
    val line: Color,
)

private fun previewFor(look: Look): Preview =
    when (look) {
        Look.SYSTEM ->
            Preview(
                page =
                    Brush.horizontalGradient(
                        0f to LightColors.background,
                        HALF to LightColors.background,
                        HALF to DarkColors.background,
                        1f to DarkColors.background,
                    ),
                card = LightColors.primaryContainer,
                line = LightColors.outline.copy(alpha = 0.6f),
            )
        Look.PAPER ->
            Preview(SolidColor(LightColors.background), LightColors.primaryContainer, LightColors.outlineVariant)
        Look.DARK ->
            Preview(SolidColor(DarkColors.background), DarkColors.primaryContainer, DarkColors.outlineVariant)
    }

/** The three looks as selectable cards with a mini preview each. */
@Composable
private fun LookChoice(
    selected: Look,
    onLook: (Look) -> Unit,
    modifier: Modifier = Modifier,
) {
    val names =
        mapOf(
            Look.SYSTEM to stringResource(R.string.settings_look_system),
            Look.PAPER to stringResource(R.string.settings_look_paper),
            Look.DARK to stringResource(R.string.settings_look_dark),
        )
    Row(
        modifier = modifier.fillMaxWidth().height(150.dp).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Look.entries.forEach { look ->
            LookCard(
                name = names.getValue(look),
                preview = previewFor(look),
                selected = look == selected,
                onClick = { onLook(look) },
            )
        }
    }
}

@Composable
private fun RowScope.LookCard(
    name: String,
    preview: Preview,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val corner: Dp = if (selected) 28.dp else 18.dp
    val shape = RoundedCornerShape(corner)
    Column(
        modifier =
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(shape)
                .background(if (selected) scheme.secondaryContainer else scheme.surfaceContainer)
                .then(if (selected) Modifier.border(3.dp, scheme.primary, shape) else Modifier)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(preview.page)
                    .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(PREVIEW_CARD_WIDTH)
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(preview.card),
            )
            Box(
                Modifier
                    .fillMaxWidth(PREVIEW_LINE_WIDTH)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(preview.line),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                SymbolIcon(R.drawable.ic_sym_check, tint = scheme.onSurface, size = 16.dp)
            }
            Text(text = name, color = scheme.onSurface, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

/** The split point of the half Paper, half Dark preview of the phone setting. */
private const val HALF = 0.5f

/** The widths of the card and the text line in a look preview, as fractions of the page. */
private const val PREVIEW_CARD_WIDTH = 0.7f
private const val PREVIEW_LINE_WIDTH = 0.45f

private const val NEPALI = "ne"
private const val ENGLISH = "en"
