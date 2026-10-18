package np.nepdate.ui.settings

import android.app.LocaleManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.LocaleList
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.sanamhub.nepdate.patro.Patro
import np.nepdate.R
import np.nepdate.data.DateFollows
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.theme.LocalNepdateColors

/** Settings and About (MR-06, MR-07, MR-11, MR-12). */
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
        SettingsHeader(onClose)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                LanguageSection()
            }
            SettingsChoices(settings, onDateFollows, onDigits, onShowTithi)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            AboutSection(settings, appVersion, libraryVersion, osVersion)
        }
    }
}

@Composable
private fun SettingsChoices(
    settings: Settings,
    onDateFollows: (DateFollows) -> Unit,
    onDigits: (Digits) -> Unit,
    onShowTithi: (Boolean) -> Unit,
) {
    SettingsSection(stringResource(R.string.settings_digits))
    ChoiceRow(
        label = stringResource(R.string.settings_digits_nepali),
        selected = settings.digits == Digits.NEPALI,
        onClick = { onDigits(Digits.NEPALI) },
    )
    ChoiceRow(
        label = stringResource(R.string.settings_digits_english),
        selected = settings.digits == Digits.ENGLISH,
        onClick = { onDigits(Digits.ENGLISH) },
    )
    SettingsSection(stringResource(R.string.settings_date_follows))
    ChoiceRow(
        label = stringResource(R.string.settings_date_device),
        selected = settings.dateFollows == DateFollows.DEVICE,
        onClick = { onDateFollows(DateFollows.DEVICE) },
    )
    ChoiceRow(
        label = stringResource(R.string.settings_date_nepal),
        selected = settings.dateFollows == DateFollows.NEPAL,
        onClick = { onDateFollows(DateFollows.NEPAL) },
    )
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(
                    value = settings.showTithi,
                    onValueChange = onShowTithi,
                    role = Role.Switch,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.settings_show_tithi),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(checked = settings.showTithi, onCheckedChange = null)
    }
}

@Composable
private fun AboutSection(
    settings: Settings,
    appVersion: String,
    libraryVersion: String,
    osVersion: String,
) {
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    SettingsSection(stringResource(R.string.settings_about))
    SettingsInfo(
        label = stringResource(R.string.settings_app_version),
        value = appVersion,
    )
    SettingsInfo(
        label = stringResource(R.string.settings_library_version),
        value = libraryVersion,
    )
    SettingsInfo(
        label = stringResource(R.string.settings_coverage),
        value =
            stringResource(
                R.string.settings_coverage_value,
                bsLong(Patro.coverage.start, bsMonthNames[0], settings.digits),
                bsLong(
                    Patro.coverage.endInclusive,
                    bsMonthNames[Patro.coverage.endInclusive.monthValue - 1],
                    settings.digits,
                ),
            ),
    )
    SettingsInfo(
        label = stringResource(R.string.settings_data_source),
        value = stringResource(R.string.settings_data_source_value),
    )
    SettingsInfo(
        label = stringResource(R.string.settings_official_sources),
        value = stringResource(R.string.settings_official_sources_value),
    )
    SettingsLink(stringResource(R.string.settings_known_data), KNOWN_DATA_URL)
    SettingsLink(stringResource(R.string.settings_report_date), REPORT_DATE_URL)
    SettingsLink(stringResource(R.string.settings_licences), LICENCES_URL)
    SettingsLink(stringResource(R.string.settings_privacy), PRIVACY_URL)
    SettingsLink(stringResource(R.string.settings_source_code), SOURCE_URL)
    if (HOLIDAYS_ICS_URL.isNotEmpty()) {
        ShareSubscription(HOLIDAYS_ICS_URL)
    }
    CopyDiagnostic(appVersion, osVersion, settings)
}

@Composable
private fun CopyDiagnostic(
    appVersion: String,
    osVersion: String,
    settings: Settings,
) {
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
            nepaliDigits = stringResource(R.string.diagnostic_digits_nepali),
            englishDigits = stringResource(R.string.diagnostic_digits_english),
        )
    var copied by remember { mutableStateOf(false) }
    TextButton(
        onClick = {
            val text = diagnosticText(appVersion, osVersion, settings, labels)
            val clipboard = context.getSystemService(ClipboardManager::class.java)
            clipboard.setPrimaryClip(ClipData.newPlainText("Nepdate", text))
            copied = true
        },
        modifier = Modifier.heightIn(min = 48.dp),
    ) {
        Text(stringResource(R.string.settings_copy_diagnostic))
    }
    if (copied) {
        Text(
            text = stringResource(R.string.settings_copied),
            color = LocalNepdateColors.current.dimmed,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SettingsHeader(onClose: () -> Unit) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
                .heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("settings_close"),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.day_close),
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 12.dp),
        color = LocalNepdateColors.current.dimmed,
        style = MaterialTheme.typography.titleSmall,
    )
}

@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SettingsInfo(
    label: String,
    value: String,
) {
    Column {
        Text(text = label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, color = LocalNepdateColors.current.dimmed, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SettingsLink(
    label: String,
    url: String,
) {
    val context = LocalContext.current
    TextButton(
        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) },
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun ShareSubscription(url: String) {
    val context = LocalContext.current
    TextButton(
        onClick = {
            context.startActivity(Intent.createChooser(holidayShareIntent(url), null))
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
    ) {
        Text(stringResource(R.string.settings_subscribe))
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun LanguageSection() {
    val context = LocalContext.current
    val manager = context.getSystemService(LocaleManager::class.java) ?: return
    var selected by remember {
        mutableStateOf(
            manager.applicationLocales
                .takeUnless { it.isEmpty }
                ?.get(0)
                ?.language,
        )
    }
    SettingsSection(stringResource(R.string.settings_language))
    ChoiceRow(
        label = stringResource(R.string.settings_language_system),
        selected = selected == null,
        onClick = {
            manager.applicationLocales = LocaleList.getEmptyLocaleList()
            selected = null
        },
    )
    ChoiceRow(
        label = stringResource(R.string.settings_language_english),
        selected = selected == "en",
        onClick = {
            manager.applicationLocales = LocaleList.forLanguageTags("en")
            selected = "en"
        },
    )
    ChoiceRow(
        label = stringResource(R.string.settings_language_nepali),
        selected = selected == "ne",
        onClick = {
            manager.applicationLocales = LocaleList.forLanguageTags("ne")
            selected = "ne"
        },
    )
}
