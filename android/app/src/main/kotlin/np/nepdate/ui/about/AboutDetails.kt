package np.nepdate.ui.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
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
import np.nepdate.ui.settings.HOLIDAYS_ICS_URL
import np.nepdate.ui.settings.KNOWN_DATA_URL
import np.nepdate.ui.settings.LICENCES_URL
import np.nepdate.ui.settings.PRIVACY_URL
import np.nepdate.ui.settings.REPORT_DATE_URL
import np.nepdate.ui.settings.SOURCE_URL
import np.nepdate.ui.settings.WEB_PATRO_URL
import np.nepdate.ui.settings.holidayShareIntent

/**
 * The promises, the data coverage, the web patro, the links and the build details. The Settings
 * screen shows the same block while the About screen is not wired in.
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

/** Three equal-height badges; long labels wrap inside them in balanced lines. */
@Composable
private fun Promises() {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(container)
                .padding(horizontal = 10.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SymbolIcon(icon, tint = content, size = 28.dp)
        Text(
            text = stringResource(label),
            color = content,
            style = MaterialTheme.typography.labelLarge.copy(lineBreak = LineBreak.Heading),
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
        listOf(
            stringResource(R.string.settings_official_sources) + ": " +
                stringResource(R.string.settings_official_sources_value),
            stringResource(R.string.settings_data_source) + ": " + stringResource(R.string.settings_data_source_value),
            stringResource(R.string.about_data_updates),
        ).forEach { line ->
            Text(
                text = line,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** One About link: what it says, an optional second line, its symbol and the intent it fires. */
private data class AboutLink(
    @param:DrawableRes val icon: Int,
    @param:StringRes val label: Int,
    @param:StringRes val supporting: Int?,
    val intent: () -> Intent,
)

private fun browse(url: String): () -> Intent = { Intent(Intent.ACTION_VIEW, url.toUri()) }

private fun aboutLinks(): List<AboutLink> =
    buildList {
        add(
            AboutLink(
                icon = R.drawable.ic_sym_language,
                label = R.string.about_web_patro,
                supporting = R.string.about_web_patro_supporting,
                intent = browse(WEB_PATRO_URL),
            ),
        )
        add(AboutLink(R.drawable.ic_sym_flag, R.string.settings_report_date, null, browse(REPORT_DATE_URL)))
        add(AboutLink(R.drawable.ic_sym_help, R.string.settings_known_data, null, browse(KNOWN_DATA_URL)))
        add(AboutLink(R.drawable.ic_sym_policy, R.string.settings_privacy, null, browse(PRIVACY_URL)))
        add(AboutLink(R.drawable.ic_sym_code, R.string.settings_source_code, null, browse(SOURCE_URL)))
        add(AboutLink(R.drawable.ic_sym_license, R.string.settings_licences, null, browse(LICENCES_URL)))
        if (HOLIDAYS_ICS_URL.isNotEmpty()) {
            add(
                AboutLink(R.drawable.ic_sym_share, R.string.settings_subscribe, null) {
                    Intent.createChooser(holidayShareIntent(HOLIDAYS_ICS_URL), null)
                },
            )
        }
    }

/** The links as one segmented list; each row rounds out while pressed and opens the browser. */
@Composable
private fun Links() {
    val links = aboutLinks()
    val context = LocalContext.current
    val colors =
        ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            leadingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
        links.forEachIndexed { index, link ->
            SegmentedListItem(
                onClick = { context.startActivitySafely(link.intent()) },
                shapes = ListItemDefaults.segmentedShapes(index, links.size),
                colors = colors,
                leadingContent = { SymbolIcon(link.icon, size = 22.dp) },
                trailingContent = { SymbolIcon(R.drawable.ic_sym_open_in_new, size = 20.dp) },
                supportingContent = link.supporting?.let { { Text(stringResource(it)) } },
            ) {
                Text(text = stringResource(link.label), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

private fun Context.startActivitySafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No app can open it; nothing to do.
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
