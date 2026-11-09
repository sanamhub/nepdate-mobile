package np.nepdate.widget

import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.PreviewSizeMode
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.state.updateAppWidgetState
import io.github.sanamhub.nepdate.NepaliDate

/** The fixed sample for generated previews: Ghatasthapana, Asoj 25, 2083. */
internal val PREVIEW_DATE: NepaliDate = NepaliDate.of(2083, 6, 25)

/** The sample for the festival widget previews: Asoj 20, 2083, five days before Ghatasthapana. */
internal val PREVIEW_FESTIVAL_DATE: NepaliDate = NepaliDate.of(2083, 6, 20)

/** W-01 Today. Exact sizing, so the numeral and the type grow with the widget. */
class TodayGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(160.dp, 160.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) = provideNepdateContent(context) { environment, preferences ->
        val model = remember(environment) { environment.todayModel(environment.today) }
        TodayWidgetContent(
            model,
            preferences.opacity(),
            showAd = preferences[TODAY_SHOW_AD_KEY] ?: false,
            minimal = preferences.minimal(),
        )
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val model = previewEnvironment(context).todayModel(PREVIEW_DATE)
        providePreviewContent { TodayWidgetContent(model, WidgetOpacity.SOLID) }
    }
}

/** W-02 Today and upcoming. */
class UpcomingGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(300.dp, 150.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) = provideNepdateContent(context) { environment, preferences ->
        val model = remember(environment) { environment.upcomingModel(environment.today) }
        UpcomingWidgetContent(model, preferences.opacity(), minimal = preferences.minimal())
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val model = previewEnvironment(context).upcomingModel(PREVIEW_FESTIVAL_DATE)
        providePreviewContent { UpcomingWidgetContent(model, WidgetOpacity.SOLID) }
    }
}

/** W-03 Month. */
class MonthGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(MONTH_WIDGET_SIZES)
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(300.dp, 300.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) = provideNepdateContent(context) { environment, preferences ->
        val shown = shownMonth(preferences, environment.today)
        val model =
            remember(environment, shown) { environment.monthModel(shown.first, shown.second, environment.today) }
        MonthWidgetContent(model, preferences.opacity())
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val model = previewEnvironment(context).monthModel(PREVIEW_DATE.year, PREVIEW_DATE.monthValue, PREVIEW_DATE)
        providePreviewContent { MonthWidgetContent(model, WidgetOpacity.SOLID) }
    }

    /** Clears the shown month so a midnight refresh draws today's month (W-03 AC1). */
    suspend fun clearShownMonth(context: Context) {
        GlanceAppWidgetManager(context).getGlanceIds(MonthGlanceWidget::class.java).forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { preferences ->
                preferences.remove(SHOWN_YEAR_KEY)
                preferences.remove(SHOWN_MONTH_KEY)
            }
        }
    }
}

/** Next festival: days left to the next festival, with arrows to browse the ones after it. */
class NextFestivalGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(160.dp, 160.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) = provideNepdateContent(context) { environment, preferences ->
        val index = preferences[FESTIVAL_INDEX_KEY] ?: 0
        val model = remember(environment, index) { environment.nextFestival(context, environment.today, index) }
        NextFestivalWidgetContent(model, preferences.opacity(), minimal = preferences.minimal())
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val model = previewEnvironment(context).nextFestival(context, PREVIEW_FESTIVAL_DATE, 0)
        providePreviewContent { NextFestivalWidgetContent(model, WidgetOpacity.SOLID) }
    }

    /** Returns every placed copy to the nearest festival; called by the midnight refresh. */
    suspend fun clearBrowsing(context: Context) {
        GlanceAppWidgetManager(context).getGlanceIds(NextFestivalGlanceWidget::class.java).forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { preferences -> preferences.remove(FESTIVAL_INDEX_KEY) }
        }
    }
}

/** Festivals list: a scrolling list of upcoming festivals and holidays with a holidays only filter. */
class FestivalsGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(FESTIVALS_WIDGET_SIZES)
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(300.dp, 220.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) = provideNepdateContent(context) { environment, preferences ->
        val holidaysOnly = preferences[HOLIDAYS_ONLY_KEY] ?: false
        val model =
            remember(environment, holidaysOnly) {
                environment.festivalsList(context, environment.today, holidaysOnly)
            }
        FestivalsWidgetContent(model, preferences.opacity())
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val model = previewEnvironment(context).festivalsList(context, PREVIEW_FESTIVAL_DATE, false)
        providePreviewContent { FestivalsWidgetContent(model, WidgetOpacity.SOLID, scrolling = false) }
    }
}
