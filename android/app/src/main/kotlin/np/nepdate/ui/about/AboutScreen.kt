package np.nepdate.ui.about

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.MaterialShape
import np.nepdate.ui.common.ShapeBadge
import np.nepdate.ui.settings.BackButton
import np.nepdate.ui.settings.NEPDATE_SITE_URL

/**
 * About and privacy (MR-11, MR-12): the app icon and version, what powers it, the three promises,
 * the holiday data coverage and the links. [digits] is the Digits setting; the app language resolves
 * it.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val scroll = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .nestedScroll(scroll.nestedScrollConnection)
                .testTag("about_screen"),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onBack = onBack, tag = "about_back") },
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
            AppIdentity(appVersion)
            AboutDetails(appVersion, libraryVersion, osVersion, digits)
        }
    }
}

/** The icon on a soft four-sided cookie, the name, the version and what powers the app. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppIdentity(appVersion: String) {
    val iconLabel = stringResource(R.string.about_icon)
    val iconShape = remember { MaterialShape(MaterialShapes.Cookie4Sided) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // The adaptive launcher icon cannot be painted directly, so its foreground sits on the
        // icon's own sindoor background.
        ShapeBadge(
            shape = iconShape,
            color = colorResource(R.color.ic_launcher_background),
            size = 104.dp,
            modifier = Modifier.semantics { contentDescription = iconLabel },
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
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.about_version, appVersion),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        PoweredBy()
    }
}

/** "Powered by nepdate.com", where the site name is a link that opens the browser. */
@Composable
private fun PoweredBy() {
    val context = LocalContext.current
    val site = stringResource(R.string.about_site_name)
    val template = stringResource(R.string.about_powered_by, site)
    val linkColor = MaterialTheme.colorScheme.primary
    val text =
        buildAnnotatedString {
            val start = template.indexOf(site)
            append(template.substring(0, start.coerceAtLeast(0)))
            val link =
                LinkAnnotation.Clickable(
                    tag = NEPDATE_SITE_URL,
                    styles =
                        TextLinkStyles(
                            style =
                                SpanStyle(
                                    color = linkColor,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline,
                                ),
                        ),
                ) { openUrl(context, NEPDATE_SITE_URL) }
            withLink(link) { append(site) }
            if (start >= 0) append(template.substring(start + site.length))
        }
    Text(
        text = text,
        modifier = Modifier.heightIn(min = 48.dp).padding(top = 12.dp).testTag("about_powered_by"),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
    )
}

/** Opens [url] in the browser with `ACTION_VIEW`; a phone without a browser simply does nothing. */
internal fun openUrl(
    context: Context,
    url: String,
) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (_: ActivityNotFoundException) {
        // No browser installed; there is nothing useful to show instead.
    }
}
