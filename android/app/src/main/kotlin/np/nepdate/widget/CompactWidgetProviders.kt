package np.nepdate.widget

import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.PreviewSizeMode
import androidx.glance.appwidget.SizeMode

/** Date chip: the month and today's numeral in one cell, the weekday beside it from two cells. */
class DateChipGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(80.dp, 80.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) = provideNepdateContent(context) { environment, preferences ->
        val model = remember(environment) { environment.todayModel(environment.today) }
        DateChipContent(model, preferences.opacity())
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val model = previewEnvironment(context).todayModel(PREVIEW_DATE)
        providePreviewContent { DateChipContent(model, WidgetOpacity.SOLID) }
    }
}

/** Date and tithi: a 4x1 strip with the weekday, the BS date, the tithi and the AD date. */
class DateStripGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(300.dp, 64.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) = provideNepdateContent(context) { environment, preferences ->
        val model = remember(environment) { environment.todayModel(environment.today) }
        DateStripContent(model, preferences.opacity(), minimal = preferences.minimal())
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val model = previewEnvironment(context).todayModel(PREVIEW_DATE)
        providePreviewContent { DateStripContent(model, WidgetOpacity.SOLID) }
    }
}

/** Week: the seven days around today with their holiday and event marks. */
class WeekGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(WEEK_WIDGET_SIZES)
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(300.dp, 130.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) = provideNepdateContent(context) { environment, preferences ->
        val model = remember(environment) { environment.weekModel(environment.today) }
        WeekWidgetContent(model, preferences.opacity(), minimal = preferences.minimal())
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val model = previewEnvironment(context).weekModel(PREVIEW_DATE)
        providePreviewContent { WeekWidgetContent(model, WidgetOpacity.SOLID) }
    }
}

/** Date text: today's date as plain type with no card, on the wallpaper by default. */
class TextDateGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val previewSizeMode: PreviewSizeMode = SizeMode.Responsive(setOf(DpSize(250.dp, 80.dp)))

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) = provideNepdateContent(context) { environment, preferences ->
        val model = remember(environment) { environment.todayModel(environment.today) }
        TextDateContent(model, preferences.opacity(TEXT_DATE_OPACITY), minimal = preferences.minimal())
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val model = previewEnvironment(context).todayModel(PREVIEW_DATE)
        providePreviewContent { TextDateContent(model, WidgetOpacity.SOLID) }
    }
}

/** The Date text widget sits on the wallpaper until its owner picks a background. */
internal val TEXT_DATE_OPACITY = WidgetOpacity.TRANSPARENT
