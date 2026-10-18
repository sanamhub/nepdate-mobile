package np.nepdate.ui.settings

import android.content.Intent

/** The links About shows. The app opens them in the system browser; it never fetches them itself. */
internal const val KNOWN_DATA_URL =
    "https://github.com/sanamhub/nepdate/blob/main/data/README.md#known-data-questions-to-raise-upstream"
internal const val REPORT_DATE_URL =
    "https://github.com/sanamhub/nepdate-mobile/issues/new?template=wrong-date.yml"
internal const val LICENCES_URL =
    "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/legal/README.md"
internal const val PRIVACY_URL =
    "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/privacy-policy.md"
internal const val SOURCE_URL = "https://github.com/sanamhub/nepdate-mobile"

/** Empty until the hub publishes `holidays.ics` (hub task H4; ADR-0003). */
internal const val HOLIDAYS_ICS_URL = ""

/** A share intent that hands the subscription URL to a calendar app; the app itself never fetches it. */
internal fun holidayShareIntent(url: String): Intent =
    Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
