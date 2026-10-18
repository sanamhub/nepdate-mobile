package np.nepdate.widget

import android.content.Context
import io.github.sanamhub.nepdate.KATHMANDU
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.flow.first
import np.nepdate.NepdateApplication
import np.nepdate.data.DateFollows
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.di.AppContainer
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import java.time.ZoneId

/** Everything one widget render needs, read once before `provideContent`. */
internal data class WidgetEnvironment(
    val settings: Settings,
    val today: NepaliDate,
    val lang: Lang,
    val digits: Digits,
    val copy: WidgetCopy,
    val showNepal: Boolean,
)

/**
 * Reads the same settings as the app and resolves Digits against the current app locale. Widgets
 * render from this snapshot; they never read the clock or disk from inside a composable.
 */
internal suspend fun widgetEnvironment(context: Context): WidgetEnvironment {
    val container = appContainer(context)
    val settings = container.settingsRepository.settings.first()
    val lang = languageFor(context.resources.configuration.locales[0])
    return WidgetEnvironment(
        settings = settings,
        today = container.todayProvider.today(settings.dateFollows),
        lang = lang,
        digits = settings.digits.resolve(lang),
        copy = widgetCopy(context),
        showNepal = settings.dateFollows == DateFollows.NEPAL && ZoneId.systemDefault() != KATHMANDU,
    )
}

/** The app's container, or one for a test context that did not start [NepdateApplication]. */
internal fun appContainer(context: Context): AppContainer =
    (context.applicationContext as? NepdateApplication)?.container
        ?: AppContainer(context.applicationContext)
