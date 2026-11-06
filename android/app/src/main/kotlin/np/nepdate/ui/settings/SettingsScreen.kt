package np.nepdate.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import np.nepdate.R
import np.nepdate.data.DateFollows
import np.nepdate.data.Digits
import np.nepdate.data.Look
import np.nepdate.data.Settings
import np.nepdate.ui.about.AboutDetails
import np.nepdate.ui.common.SymbolIcon

/**
 * Settings (MR-06, MR-07, MR-12): segmented groups for the look, language and digits, the patro and
 * About. Multi-choice rows open a dialog; toggles switch in place. About opens its own screen.
 */
@Composable
internal fun SettingsScreen(
    settings: Settings,
    appVersion: String,
    libraryVersion: String,
    osVersion: String,
    onClose: () -> Unit,
    onDateFollows: (DateFollows) -> Unit,
    onDigits: (Digits) -> Unit,
    onShowTithi: (Boolean) -> Unit,
    onLook: (Look) -> Unit = {},
    onWallpaperColors: (Boolean) -> Unit = {},
    onOpenAbout: (() -> Unit)? = null,
) {
    BackHandler { onClose() }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
                .testTag("settings_screen"),
    ) {
        BackHeader(onBack = onClose, backTag = "settings_close", title = stringResource(R.string.settings_title))
        LookGroup(settings, onLook, onWallpaperColors)
        LanguageGroup(settings, onDigits)
        PatroGroup(settings, onDateFollows, onShowTithi)
        AboutGroup(settings, appVersion, libraryVersion, osVersion, onOpenAbout)
    }
}

@Composable
private fun LookGroup(
    settings: Settings,
    onLook: (Look) -> Unit,
    onWallpaperColors: (Boolean) -> Unit,
) {
    val looks =
        listOf(
            Choice(Look.SYSTEM, stringResource(R.string.settings_look_system)),
            Choice(Look.PAPER, stringResource(R.string.settings_look_paper)),
            Choice(Look.DARK, stringResource(R.string.settings_look_dark)),
        )
    val rows =
        buildList<@Composable (Shape) -> Unit> {
            add { shape ->
                ChoiceRow(
                    icon = R.drawable.ic_sym_palette_filled,
                    tone = Tone.PRIMARY,
                    title = stringResource(R.string.settings_look),
                    choices = looks,
                    selected = settings.look,
                    onSelect = onLook,
                    shape = shape,
                )
            }
            // Wallpaper colours need the Android 12 dynamic colour APIs.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add { shape ->
                    SwitchRow(
                        icon = R.drawable.ic_sym_wallpaper_filled,
                        tone = Tone.PRIMARY,
                        title = stringResource(R.string.settings_wallpaper_colors),
                        supporting = stringResource(R.string.settings_wallpaper_colors_supporting),
                        checked = settings.wallpaperColors,
                        onCheckedChange = onWallpaperColors,
                        shape = shape,
                    )
                }
            }
        }
    SettingsGroup(stringResource(R.string.settings_group_look), rows)
}

@Composable
private fun LanguageGroup(
    settings: Settings,
    onDigits: (Digits) -> Unit,
) {
    val digits =
        listOf(
            Choice(Digits.FOLLOW_LANGUAGE, stringResource(R.string.settings_digits_follow)),
            Choice(Digits.NEPALI, stringResource(R.string.settings_digits_nepali)),
            Choice(Digits.ENGLISH, stringResource(R.string.settings_digits_english)),
        )
    val rows =
        buildList<@Composable (Shape) -> Unit> {
            // The per-app language exists from Android 13; older phones follow the system language.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add { shape -> LanguageRow(shape) }
            }
            add { shape ->
                ChoiceRow(
                    icon = R.drawable.ic_sym_pin,
                    tone = Tone.SECONDARY,
                    title = stringResource(R.string.settings_digits),
                    choices = digits,
                    selected = settings.digits,
                    onSelect = onDigits,
                    shape = shape,
                )
            }
        }
    SettingsGroup(stringResource(R.string.settings_group_language), rows)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun LanguageRow(shape: Shape) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(AppLanguage.current(context).orEmpty()) }
    ChoiceRow(
        icon = R.drawable.ic_sym_translate,
        tone = Tone.SECONDARY,
        title = stringResource(R.string.settings_language),
        choices =
            listOf(
                Choice("", stringResource(R.string.settings_language_system)),
                Choice("en", stringResource(R.string.settings_language_english)),
                Choice("ne", stringResource(R.string.settings_language_nepali)),
            ),
        selected = selected,
        onSelect = { tag ->
            AppLanguage.set(context, tag.ifEmpty { null })
            selected = tag
        },
        shape = shape,
    )
}

@Composable
private fun PatroGroup(
    settings: Settings,
    onDateFollows: (DateFollows) -> Unit,
    onShowTithi: (Boolean) -> Unit,
) {
    val follows =
        listOf(
            Choice(DateFollows.DEVICE, stringResource(R.string.settings_date_device)),
            Choice(DateFollows.NEPAL, stringResource(R.string.settings_date_nepal)),
        )
    SettingsGroup(
        stringResource(R.string.settings_group_patro),
        listOf(
            { shape ->
                SwitchRow(
                    icon = R.drawable.ic_sym_bedtime_filled,
                    tone = Tone.TERTIARY,
                    title = stringResource(R.string.settings_show_tithi),
                    supporting = stringResource(R.string.settings_tithi_supporting),
                    checked = settings.showTithi,
                    onCheckedChange = onShowTithi,
                    shape = shape,
                )
            },
            { shape ->
                ChoiceRow(
                    icon = R.drawable.ic_sym_schedule_filled,
                    tone = Tone.TERTIARY,
                    title = stringResource(R.string.settings_date_follows),
                    choices = follows,
                    selected = settings.dateFollows,
                    onSelect = onDateFollows,
                    shape = shape,
                )
            },
        ),
    )
}

@Composable
private fun AboutGroup(
    settings: Settings,
    appVersion: String,
    libraryVersion: String,
    osVersion: String,
    onOpenAbout: (() -> Unit)?,
) {
    var copied by remember { mutableStateOf(false) }
    val copy = rememberCopyDiagnostic(appVersion, osVersion, settings) { copied = true }
    val rows =
        buildList<@Composable (Shape) -> Unit> {
            if (onOpenAbout != null) {
                add { shape ->
                    SettingsRow(
                        icon = R.drawable.ic_sym_info_filled,
                        tone = Tone.SECONDARY,
                        title = stringResource(R.string.settings_about_row),
                        supporting = stringResource(R.string.settings_about_row_supporting),
                        shape = shape,
                        modifier = Modifier.clickable(role = Role.Button, onClick = onOpenAbout),
                    ) {
                        SymbolIcon(R.drawable.ic_sym_chevron_right, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            add { shape ->
                SettingsRow(
                    icon = R.drawable.ic_sym_content_copy,
                    tone = Tone.SECONDARY,
                    title = stringResource(R.string.settings_copy_diagnostic),
                    supporting = stringResource(R.string.settings_copy_diagnostic_supporting),
                    shape = shape,
                    modifier = Modifier.clickable(role = Role.Button, onClick = copy),
                )
            }
        }
    SettingsGroup(stringResource(R.string.settings_about), rows)
    if (copied) {
        Text(
            text = stringResource(R.string.settings_copied),
            modifier =
                Modifier
                    .padding(horizontal = 32.dp, vertical = 4.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
    // Without the About screen wired in, the about details stay reachable here.
    if (onOpenAbout == null) {
        AboutDetails(appVersion, libraryVersion, osVersion, settings.digits)
    }
}

/** The click action that copies the diagnostic text (ADR-0007 §5) to the clipboard. */
@Composable
private fun rememberCopyDiagnostic(
    appVersion: String,
    osVersion: String,
    settings: Settings,
    onCopied: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val labels =
        DiagnosticLabels(
            app = stringResource(R.string.diagnostic_app),
            android = stringResource(R.string.diagnostic_android),
            dateFollows = stringResource(R.string.diagnostic_date_follows),
            digits = stringResource(R.string.diagnostic_digits),
            tithi = stringResource(R.string.diagnostic_tithi),
            on = stringResource(R.string.diagnostic_on),
            off = stringResource(R.string.diagnostic_off),
            device = stringResource(R.string.diagnostic_device),
            nepal = stringResource(R.string.diagnostic_nepal),
            followLanguage = stringResource(R.string.settings_digits_follow),
            nepaliDigits = stringResource(R.string.diagnostic_digits_nepali),
            englishDigits = stringResource(R.string.diagnostic_digits_english),
        )
    return {
        val text = diagnosticText(appVersion, osVersion, settings, labels)
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("NepDate", text))
        onCopied()
    }
}
