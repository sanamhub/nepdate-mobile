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

/** Empty until the hub publishes `holidays.ics` (hub task H4; ADR-0003). */
internal const val HOLIDAYS_ICS_URL = ""

/** Only repositories whose pages are public today; a dead data-questions link may not ship. */
internal val ALLOWED_ABOUT_REPOSITORIES = setOf("sanamhub/nepdate-mobile")

private const val HTTPS_PORT = 443

/** Returns why [url] may not appear in About, or null when it is a public GitHub repository page. */
internal fun aboutUrlProblem(url: String): String? {
    val uri = runCatching { URI(url) }.getOrNull()
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

/** A share intent that hands the subscription URL to a calendar app; the app itself never fetches it. */
internal fun holidayShareIntent(url: String): Intent =
    Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
