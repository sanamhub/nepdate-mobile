package np.nepdate.widget

import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.PreviewSizeMode
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Settings
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve

/** The fixed sample for generated previews: Ghatasthapana, Asoj 25, 2083. */
internal val PREVIEW_DATE: NepaliDate = NepaliDate.of(2083, 6, 25)

/** W-01 Today. */
class TodayGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(TODAY_WIDGET_SIZES)
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(250.dp, 110.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val initial = widgetEnvironment(context)
        provideContent {
            val environment = rememberWidgetEnvironment(initial) { widgetEnvironment(context) }
            val model =
                remember(environment) {
                    todayWidgetModel(
                        date = environment.today,
                        lang = environment.lang,
                        digits = environment.digits,
                        copy = environment.copy,
                        showNepal = environment.showNepal,
                    )
                }
            val opacity = WidgetOpacity.fromKey(currentState<Preferences>()[WIDGET_OPACITY_KEY])
            GlanceTheme {
                TodayWidgetContent(model, opacity)
            }
        }
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val environment = previewEnvironment(context)
        val model =
            todayWidgetModel(
                date = PREVIEW_DATE,
                lang = environment.lang,
                digits = environment.digits,
                copy = environment.copy,
                showNepal = false,
            )
        provideContent {
            GlanceTheme {
                TodayWidgetContent(model, WidgetOpacity.SOLID)
            }
        }
    }
}

/** W-02 Today and upcoming. */
class UpcomingGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(TODAY_WIDGET_SIZES)
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(250.dp, 110.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val initial = widgetEnvironment(context)
        provideContent {
            val environment = rememberWidgetEnvironment(initial) { widgetEnvironment(context) }
            val model =
                remember(environment) {
                    upcomingWidgetModel(
                        date = environment.today,
                        lang = environment.lang,
                        digits = environment.digits,
                        copy = environment.copy,
                        showNepal = environment.showNepal,
                    )
                }
            val opacity = WidgetOpacity.fromKey(currentState<Preferences>()[WIDGET_OPACITY_KEY])
            GlanceTheme {
                UpcomingWidgetContent(model, opacity)
            }
        }
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val environment = previewEnvironment(context)
        val model =
            upcomingWidgetModel(
                date = PREVIEW_DATE,
                lang = environment.lang,
                digits = environment.digits,
                copy = environment.copy,
                showNepal = false,
            )
        provideContent {
            GlanceTheme {
                UpcomingWidgetContent(model, WidgetOpacity.SOLID)
            }
        }
    }
}

/** W-03 Month. */
class MonthGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(MONTH_WIDGET_SIZES)
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(250.dp, 250.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val initial = widgetEnvironment(context)
        provideContent {
            val environment = rememberWidgetEnvironment(initial) { widgetEnvironment(context) }
            val preferences = currentState<Preferences>()
            val shown = shownMonth(preferences, environment.today)
            val model =
                monthWidgetModel(
                    year = shown.first,
                    monthValue = shown.second,
                    today = environment.today,
                    lang = environment.lang,
                    digits = environment.digits,
                    copy = environment.copy,
                )
            GlanceTheme {
                MonthWidgetContent(
                    model = model,
                    opacity = WidgetOpacity.fromKey(preferences[WIDGET_OPACITY_KEY]),
                )
            }
        }
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val environment = previewEnvironment(context)
        val model =
            monthWidgetModel(
                year = PREVIEW_DATE.year,
                monthValue = PREVIEW_DATE.monthValue,
                today = PREVIEW_DATE,
                lang = environment.lang,
                digits = environment.digits,
                copy = environment.copy,
            )
        provideContent {
            GlanceTheme {
                MonthWidgetContent(model, WidgetOpacity.SOLID)
            }
        }
    }

    /** Clears the shown month so a midnight refresh draws today's month (W-03 AC1). */
    suspend fun clearShownMonth(context: Context) {
        val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
        manager.getGlanceIds(MonthGlanceWidget::class.java).forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { preferences ->
                preferences.remove(SHOWN_YEAR_KEY)
                preferences.remove(SHOWN_MONTH_KEY)
            }
        }
    }
}

class TodayGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayGlanceWidget()
}

class UpcomingGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = UpcomingGlanceWidget()
}

class MonthGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MonthGlanceWidget()
}

/** The fixed strings and sample date used by `providePreview`, no DataStore read. */
private fun previewEnvironment(context: Context): WidgetEnvironment {
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
