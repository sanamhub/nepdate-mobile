package np.nepdate.ui.settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi

/**
 * The in-app language switch (ADR-0005): Android's per-app locale owns the language, so the app
 * stores nothing itself. Android 13 and later only; older phones follow the system language.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal object AppLanguage {
    /** The chosen language tag (`en` or `ne`), or null when the app follows the system. */
    fun current(context: Context): String? =
        context
            .getSystemService(LocaleManager::class.java)
            ?.applicationLocales
            ?.takeUnless { it.isEmpty }
            ?.get(0)
            ?.language

    /** Sets the app language to [tag], or back to the system language when [tag] is null. */
    fun set(
        context: Context,
        tag: String?,
    ) {
        val manager = context.getSystemService(LocaleManager::class.java) ?: return
        manager.applicationLocales =
            if (tag == null) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(tag)
    }
}
