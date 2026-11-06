package np.nepdate.ui.about

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.sanamhub.nepdate.patro.Patro
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.common.segmentShape
import np.nepdate.ui.settings.BackHeader
import np.nepdate.ui.settings.HOLIDAYS_ICS_URL
import np.nepdate.ui.settings.KNOWN_DATA_URL
import np.nepdate.ui.settings.LICENCES_URL
import np.nepdate.ui.settings.PRIVACY_URL
import np.nepdate.ui.settings.REPORT_DATE_URL
import np.nepdate.ui.settings.SOURCE_URL
import np.nepdate.ui.settings.holidayShareIntent

/**
 * About and privacy (MR-11, MR-12): the app icon and version, the three promises, the holiday data
 * coverage and the links. [digits] is the Digits setting; the app language resolves it.
 */
@Composable
internal fun AboutScreen(
    appVersion: String,
    libraryVersion: String,
    osVersion: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    digits: Digits = Digits.FOLLOW_LANGUAGE,
) {
    BackHandler { onBack() }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
                .testTag("about_screen"),
    ) {
        BackHeader(onBack = onBack, backTag = "about_back")
        AppIdentity(appVersion)
        AboutDetails(appVersion, libraryVersion, osVersion, digits)
    }
}

@Composable
private fun AppIdentity(appVersion: String) {
    val iconLabel = stringResource(R.string.about_icon)
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // The adaptive launcher icon cannot be painted directly, so its foreground sits on the
        // icon's own sindoor background.
        Box(
            modifier =
                Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(colorResource(R.color.ic_launcher_background))
                    .semantics { contentDescription = iconLabel },
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = stringResource(R.string.about_name),
            modifier = Modifier.padding(top = 6.dp).semantics { heading() },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = stringResource(R.string.about_version, appVersion),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * The promises, the data coverage, the links and the build details. The Settings screen shows the
 * same block while the About screen is not wired in.
 */
@Composable
internal fun AboutDetails(
    appVersion: String,
    libraryVersion: String,
    osVersion: String,
    digits: Digits = Digits.FOLLOW_LANGUAGE,
) {
    val resolved = digits.resolve(languageFor(LocalConfiguration.current.locales[0]))
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Promises()
        CoverageCard(resolved)
        Links()
        BuildDetails(appVersion, libraryVersion, osVersion)
    }
}

@Composable
private fun Promises() {
    val scheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Promise(
            R.drawable.ic_sym_wifi_off_filled,
            R.string.about_offline,
            scheme.primaryContainer,
            scheme.onPrimaryContainer,
        )
        Promise(
            R.drawable.ic_sym_block_filled,
            R.string.about_no_ads,
            scheme.secondaryContainer,
            scheme.onSecondaryContainer,
        )
        Promise(
            R.drawable.ic_sym_shield_lock_filled,
            R.string.about_no_data,
            scheme.tertiaryContainer,
            scheme.onTertiaryContainer,
        )
    }
}

@Composable
private fun RowScope.Promise(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    container: Color,
    content: Color,
) {
    Column(
        modifier =
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(container)
                .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SymbolIcon(icon, tint = content, size = 28.dp)
        Text(
            text = stringResource(label),
            color = content,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CoverageCard(digits: Digits) {
    val months = stringArrayResource(R.array.bs_month_long).toList()
    val start = Patro.coverage.start
    val end = Patro.coverage.endInclusive
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_coverage),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text =
                stringResource(
                    R.string.about_coverage_range,
                    bsLong(start, months[start.monthValue - 1], digits),
                    bsLong(end, months[end.monthValue - 1], digits),
                ),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text =
                stringResource(R.string.settings_official_sources) + ": " +
                    stringResource(R.string.settings_official_sources_value),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text =
                stringResource(R.string.settings_data_source) + ": " +
                    stringResource(R.string.settings_data_source_value),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.about_data_updates),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** One About link: what it says, its symbol and the intent it fires. */
private data class AboutLink(
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
    val intent: () -> Intent,
)

private fun browse(url: String): () -> Intent = { Intent(Intent.ACTION_VIEW, url.toUri()) }

@Composable
private fun Links() {
    val links =
        buildList {
            add(AboutLink(R.drawable.ic_sym_flag, R.string.settings_report_date, browse(REPORT_DATE_URL)))
            add(AboutLink(R.drawable.ic_sym_help, R.string.settings_known_data, browse(KNOWN_DATA_URL)))
            add(AboutLink(R.drawable.ic_sym_policy, R.string.settings_privacy, browse(PRIVACY_URL)))
            add(AboutLink(R.drawable.ic_sym_code, R.string.settings_source_code, browse(SOURCE_URL)))
            add(AboutLink(R.drawable.ic_sym_license, R.string.settings_licences, browse(LICENCES_URL)))
            if (HOLIDAYS_ICS_URL.isNotEmpty()) {
                add(
                    AboutLink(R.drawable.ic_sym_share, R.string.settings_subscribe) {
                        Intent.createChooser(holidayShareIntent(HOLIDAYS_ICS_URL), null)
                    },
                )
            }
        }
    val context = LocalContext.current
    val opensBrowser = stringResource(R.string.about_opens_browser)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        links.forEachIndexed { index, link ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clip(segmentShape(index, links.size, outer = 20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clickable(role = Role.Button, onClickLabel = opensBrowser) {
                            context.startActivity(link.intent())
                        }.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SymbolIcon(link.icon, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 22.dp)
                Text(
                    text = stringResource(link.label),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                )
                SymbolIcon(
                    R.drawable.ic_sym_open_in_new,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    size = 20.dp,
                )
            }
        }
    }
}

@Composable
private fun BuildDetails(
    appVersion: String,
    libraryVersion: String,
    osVersion: String,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BuildDetail(stringResource(R.string.settings_app_version), appVersion)
        BuildDetail(stringResource(R.string.settings_library_version), libraryVersion)
        BuildDetail(stringResource(R.string.about_android_version), osVersion)
    }
}

@Composable
private fun BuildDetail(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(text = value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
    }
}
