package np.nepdate.ui.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import np.nepdate.R
import np.nepdate.data.DateFollows
import np.nepdate.data.Digits
import np.nepdate.data.Look
import np.nepdate.data.Settings
import np.nepdate.data.StartTab

/** The rows of one settings group, each drawn with the shapes of its place in the group. */
private typealias Rows = MutableList<@Composable (ListItemShapes) -> Unit>

@Composable
internal fun LookGroup(
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
    val rows: Rows = mutableListOf()
    rows += { shapes ->
        ChoiceRow(
            icon = R.drawable.ic_sym_palette_filled,
            tone = Tone.PRIMARY,
            title = stringResource(R.string.settings_look),
            choices = looks,
            selected = settings.look,
            onSelect = onLook,
            shapes = shapes,
        )
    }
    // Wallpaper colours need the Android 12 dynamic colour APIs.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rows += { shapes ->
            SwitchRow(
                icon = R.drawable.ic_sym_wallpaper_filled,
                tone = Tone.PRIMARY,
                title = stringResource(R.string.settings_wallpaper_colors),
                supporting = stringResource(R.string.settings_wallpaper_colors_supporting),
                checked = settings.wallpaperColors,
                onCheckedChange = onWallpaperColors,
                shapes = shapes,
            )
        }
    }
    SettingsGroup(stringResource(R.string.settings_group_look), rows)
}

@Composable
internal fun LanguageGroup(
    settings: Settings,
    onDigits: (Digits) -> Unit,
) {
    val digits =
        listOf(
            Choice(Digits.FOLLOW_LANGUAGE, stringResource(R.string.settings_digits_follow)),
            Choice(Digits.NEPALI, stringResource(R.string.settings_digits_nepali)),
            Choice(Digits.ENGLISH, stringResource(R.string.settings_digits_english)),
        )
    val rows: Rows = mutableListOf()
    // The per-app language exists from Android 13; older phones follow the system language.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rows += { shapes -> LanguageRow(shapes) }
    }
    rows += { shapes ->
        ChoiceRow(
            icon = R.drawable.ic_sym_pin,
            tone = Tone.SECONDARY,
            title = stringResource(R.string.settings_digits),
            choices = digits,
            selected = settings.digits,
            onSelect = onDigits,
            shapes = shapes,
        )
    }
    SettingsGroup(stringResource(R.string.settings_group_language), rows)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun LanguageRow(shapes: ListItemShapes) {
    val context = LocalContext.current
    // Keyed on the configuration: the language can also change from the system's app settings,
    // and the activity now stays alive through that change.
    val configuration = LocalConfiguration.current
    var selected by remember(configuration) { mutableStateOf(AppLanguage.current(context).orEmpty()) }
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
        shapes = shapes,
    )
}

@Composable
internal fun PatroGroup(
    settings: Settings,
    onDateFollows: (DateFollows) -> Unit,
    onShowTithi: (Boolean) -> Unit,
    onShowAdDates: ((Boolean) -> Unit)?,
) {
    val follows =
        listOf(
            Choice(DateFollows.DEVICE, stringResource(R.string.settings_date_device)),
            Choice(DateFollows.NEPAL, stringResource(R.string.settings_date_nepal)),
        )
    val rows: Rows = mutableListOf()
    rows += { shapes ->
        SwitchRow(
            icon = R.drawable.ic_sym_bedtime_filled,
            tone = Tone.TERTIARY,
            title = stringResource(R.string.settings_show_tithi),
            supporting = stringResource(R.string.settings_tithi_supporting),
            checked = settings.showTithi,
            onCheckedChange = onShowTithi,
            shapes = shapes,
        )
    }
    if (onShowAdDates != null) {
        rows += { shapes ->
            SwitchRow(
                icon = R.drawable.ic_sym_date_range,
                tone = Tone.TERTIARY,
                title = stringResource(R.string.settings_show_ad),
                supporting = stringResource(R.string.settings_show_ad_supporting),
                checked = settings.showAdDates,
                onCheckedChange = onShowAdDates,
                shapes = shapes,
            )
        }
    }
    rows += { shapes ->
        ChoiceRow(
            icon = R.drawable.ic_sym_schedule_filled,
            tone = Tone.TERTIARY,
            title = stringResource(R.string.settings_date_follows),
            choices = follows,
            selected = settings.dateFollows,
            onSelect = onDateFollows,
            shapes = shapes,
        )
    }
    SettingsGroup(stringResource(R.string.settings_group_patro), rows)
}

/** The tab the app opens on and haptic feedback. Left out entirely while neither is wired. */
@Composable
internal fun AppGroup(
    settings: Settings,
    onStartTab: ((StartTab) -> Unit)?,
    onHaptics: ((Boolean) -> Unit)?,
) {
    val rows: Rows = mutableListOf()
    if (onStartTab != null) {
        val tabs =
            listOf(
                Choice(StartTab.PATRO, stringResource(R.string.nav_month)),
                Choice(StartTab.FESTIVALS, stringResource(R.string.nav_upcoming)),
                Choice(StartTab.CONVERTER, stringResource(R.string.nav_converter)),
            )
        rows += { shapes ->
            ChoiceRow(
                icon = R.drawable.ic_sym_tab,
                tone = Tone.SECONDARY,
                title = stringResource(R.string.settings_start_tab),
                choices = tabs,
                selected = settings.startTab,
                onSelect = onStartTab,
                shapes = shapes,
            )
        }
    }
    if (onHaptics != null) {
        rows += { shapes ->
            SwitchRow(
                icon = R.drawable.ic_sym_vibration,
                tone = Tone.SECONDARY,
                title = stringResource(R.string.settings_haptics),
                supporting = stringResource(R.string.settings_haptics_supporting),
                checked = settings.haptics,
                onCheckedChange = onHaptics,
                shapes = shapes,
            )
        }
    }
    if (rows.isNotEmpty()) SettingsGroup(stringResource(R.string.settings_group_app), rows)
}
