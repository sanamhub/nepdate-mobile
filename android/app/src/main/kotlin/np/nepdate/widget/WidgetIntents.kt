package np.nepdate.widget

import android.content.Context
import android.content.Intent
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.MainActivity

/** The extra W-02 and W-03 put on their launch intents (W-02 AC1, W-03 AC2). */
internal const val EXTRA_BS_DATE = "np.nepdate.extra.BS_DATE"

/** An explicit intent into the app, optionally opening one BS day's detail. */
internal fun widgetIntent(
    context: Context,
    date: NepaliDate? = null,
): Intent =
    Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (date != null) putExtra(EXTRA_BS_DATE, date.toIsoString())
    }

/**
 * The BS date carried by [intent], or null for the default today view. MainActivity is exported, so
 * the extra is untrusted: only the strict ISO form [widgetIntent] writes is accepted.
 */
internal fun dateFromIntent(intent: Intent?): NepaliDate? {
    val iso = intent?.getStringExtra(EXTRA_BS_DATE) ?: return null
    return NepaliDate.parseOrNull(iso)
}
