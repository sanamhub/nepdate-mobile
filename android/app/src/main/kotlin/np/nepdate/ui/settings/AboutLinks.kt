package np.nepdate.ui.settings

import android.content.Intent
import java.net.URI

/** The links About shows. The app opens them in the system browser; it never fetches them itself. */
internal const val KNOWN_DATA_URL =
    "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/known-data-questions.md"
internal const val REPORT_DATE_URL =
    "https://github.com/sanamhub/nepdate-mobile/issues/new?template=wrong-date.yml"
internal const val LICENCES_URL =
    "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/legal/README.md"
internal const val PRIVACY_URL =
    "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/privacy-policy.md"
internal const val SOURCE_URL = "https://github.com/sanamhub/nepdate-mobile"

/** The NepDate site that powers the app's data, and its web patro that runs in any browser. */
internal const val NEPDATE_SITE_URL = "https://nepdate.com"
internal const val WEB_PATRO_URL = "https://patro.nepdate.com"

/** The hub's public holiday feed (ADR-0003). Calendar apps take the https URL as text. */
internal const val HOLIDAYS_ICS_URL = "https://patro.nepdate.com/holidays.ics"

/** Only repositories whose pages are public today; a dead data-questions link may not ship. */
internal val ALLOWED_ABOUT_REPOSITORIES = setOf("sanamhub/nepdate-mobile")

/** The project's own sites, which About may link to at any path. */
internal val ALLOWED_ABOUT_SITES = setOf("nepdate.com", "patro.nepdate.com")

private const val HTTPS_PORT = 443

/**
 * Returns why [url] may not appear in About, or null when it is a public GitHub repository page or
 * one of the project's own sites.
 */
internal fun aboutUrlProblem(url: String): String? {
    val uri = runCatching { URI(url) }.getOrNull()
    if (uri != null && uri.isProjectSite()) return null
    val segments =
        uri
            ?.path
            .orEmpty()
            .trim('/')
            .split('/')
            .filter { it.isNotEmpty() }
    val repository = segments.take(2).joinToString("/")
    return when {
        uri == null -> "not a valid URL: $url"
        uri.scheme != "https" -> "not https: $url"
        uri.host != "github.com" -> "not github.com: $url"
        uri.port != -1 && uri.port != HTTPS_PORT -> "unexpected port: $url"
        segments.any { it == "." || it == ".." } -> "path traversal: $url"
        repository !in ALLOWED_ABOUT_REPOSITORIES ->
            "repository is not on the public allow-list: $repository"
        else -> null
    }
}

/** True for an https page on one of [ALLOWED_ABOUT_SITES] at the default port. */
private fun URI.isProjectSite(): Boolean = scheme == "https" && port == -1 && host in ALLOWED_ABOUT_SITES

/** A share intent that hands the subscription URL to a calendar app; the app itself never fetches it. */
internal fun holidayShareIntent(url: String): Intent =
    Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
