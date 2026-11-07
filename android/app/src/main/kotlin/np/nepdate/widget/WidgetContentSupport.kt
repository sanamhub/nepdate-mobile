package np.nepdate.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Settings
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve

/**
 * The session every NepDate widget runs: the environment read before `provideContent` and reloaded
 * on each refresh token, the widget's own Glance state, and the widget theme around [content].
 */
internal suspend fun GlanceAppWidget.provideNepdateContent(
    context: Context,
    content: @Composable (WidgetEnvironment, Preferences) -> Unit,
): Nothing {
    val initial = widgetEnvironment(context)
    provideContent {
        val environment = rememberWidgetEnvironment(initial) { widgetEnvironment(context) }
        val preferences = currentState<Preferences>()
        NepdateGlanceTheme(environment.settings.wallpaperColors) { content(environment, preferences) }
    }
}

/** A generated preview: the brand scheme, never the wallpaper, around [content]. */
internal suspend fun GlanceAppWidget.providePreviewContent(content: @Composable () -> Unit): Nothing =
    provideContent { NepdateGlanceTheme(wallpaperColors = false) { content() } }

/** The background opacity chosen for this placed widget. */
internal fun Preferences.opacity(): WidgetOpacity = WidgetOpacity.fromKey(this[WIDGET_OPACITY_KEY])

internal fun WidgetEnvironment.todayModel(date: NepaliDate): TodayWidgetModel =
    todayWidgetModel(date = date, lang = lang, digits = digits, copy = copy, showNepal = showNepal)

internal fun WidgetEnvironment.upcomingModel(date: NepaliDate): UpcomingWidgetModel =
    upcomingWidgetModel(date = date, lang = lang, digits = digits, copy = copy, showNepal = showNepal)

internal fun WidgetEnvironment.monthModel(
    year: Int,
    month: Int,
    today: NepaliDate,
): MonthWidgetModel = monthWidgetModel(year, month, today, lang, digits, copy)

internal fun WidgetEnvironment.nextFestival(
    context: Context,
    today: NepaliDate,
    index: Int,
): NextFestivalModel = nextFestivalModel(today, index, lang, digits, copy, widgetControlCopy(context))

internal fun WidgetEnvironment.festivalsList(
    context: Context,
    today: NepaliDate,
    holidaysOnly: Boolean,
): FestivalsListModel = festivalsListModel(today, holidaysOnly, digits, lang, copy, widgetControlCopy(context))

internal fun WidgetEnvironment.weekModel(today: NepaliDate): WeekWidgetModel =
    weekWidgetModel(today, lang, digits, copy)

/** The fixed strings and sample date used by `providePreview`, no DataStore read. */
internal fun previewEnvironment(context: Context): WidgetEnvironment {
    val lang = languageFor(context.resources.configuration.locales[0])
    val settings = Settings()
    return WidgetEnvironment(
        settings = settings,
        today = PREVIEW_DATE,
        lang = lang,
        digits = settings.digits.resolve(lang),
        copy = widgetCopy(context),
        showNepal = false,
    )
}
