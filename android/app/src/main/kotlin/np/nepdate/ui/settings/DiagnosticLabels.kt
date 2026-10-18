package np.nepdate.ui.settings

import np.nepdate.data.DateFollows
import np.nepdate.data.Digits
import np.nepdate.data.Settings

/** The localised labels of the diagnostic text. */
internal data class DiagnosticLabels(
    val app: String,
    val android: String,
    val dateFollows: String,
    val digits: String,
    val tithi: String,
    val on: String,
    val off: String,
    val device: String,
    val nepal: String,
    val nepaliDigits: String,
    val englishDigits: String,
)

/**
 * The text behind "Copy diagnostic info" (ADR-0007 §5): app version, OS version and the three
 * settings, and nothing else. It is never sent anywhere by the app.
 */
internal fun diagnosticText(
    appVersion: String,
    osVersion: String,
    settings: Settings,
    labels: DiagnosticLabels,
): String =
    buildString {
        appendLine("${labels.app}: $appVersion")
        appendLine("${labels.android}: $osVersion")
        appendLine(
            "${labels.dateFollows}: " +
                if (settings.dateFollows == DateFollows.NEPAL) labels.nepal else labels.device,
        )
        appendLine(
            "${labels.digits}: " +
                if (settings.digits == Digits.NEPALI) labels.nepaliDigits else labels.englishDigits,
        )
        append("${labels.tithi}: ${if (settings.showTithi) labels.on else labels.off}")
    }
