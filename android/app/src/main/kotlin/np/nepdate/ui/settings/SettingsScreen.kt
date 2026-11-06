package np.nepdate.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import np.nepdate.R
import np.nepdate.data.DateFollows
import np.nepdate.data.Digits
import np.nepdate.data.Look
import np.nepdate.data.Settings
import np.nepdate.data.StartTab
import np.nepdate.ui.about.AboutDetails
import np.nepdate.ui.common.SymbolIcon

/**
 * Settings (MR-06, MR-07, MR-12): a large flexible app bar over segmented groups for the look,
 * language and digits, the patro, the app and About. Multi-choice rows open a dialog; toggles switch
 * in place. A row whose callback is null is left out, so a setting never shows before it is wired.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    onShowAdDates: ((Boolean) -> Unit)? = null,
    onHaptics: ((Boolean) -> Unit)? = null,
    onStartTab: ((StartTab) -> Unit)? = null,
) {
    BackHandler { onClose() }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    CompositionLocalProvider(LocalSettingsHaptics provides rememberHaptics(settings.haptics)) {
        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scroll.nestedScrollConnection)
                    .testTag("settings_screen"),
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                LargeFlexibleTopAppBar(
                    title = { Text(stringResource(R.string.settings_title)) },
                    navigationIcon = { BackButton(onBack = onClose, tag = "settings_close") },
                    scrollBehavior = scroll,
                )
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(bottom = 24.dp),
            ) {
                LookGroup(settings, onLook, onWallpaperColors)
                LanguageGroup(settings, onDigits)
                PatroGroup(settings, onDateFollows, onShowTithi, onShowAdDates)
                AppGroup(settings, onStartTab, onHaptics)
                AboutGroup(settings, appVersion, libraryVersion, osVersion, onOpenAbout)
            }
        }
    }
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
    val haptics = LocalSettingsHaptics.current
    val copy =
        rememberCopyDiagnostic(appVersion, osVersion, settings) {
            haptics.confirm()
            copied = true
        }
    val rows =
        buildList<@Composable (ListItemShapes) -> Unit> {
            if (onOpenAbout != null) {
                add { shapes ->
                    SettingsRow(
                        icon = R.drawable.ic_sym_info_filled,
                        tone = Tone.SECONDARY,
                        title = stringResource(R.string.settings_about_row),
                        supporting = stringResource(R.string.settings_about_row_supporting),
                        shapes = shapes,
                        onClick = onOpenAbout,
                    ) {
                        SymbolIcon(R.drawable.ic_sym_chevron_right, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            add { shapes ->
                SettingsRow(
                    icon = R.drawable.ic_sym_content_copy,
                    tone = Tone.SECONDARY,
                    title = stringResource(R.string.settings_copy_diagnostic),
                    supporting = stringResource(R.string.settings_copy_diagnostic_supporting),
                    shapes = shapes,
                    onClick = copy,
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
