package np.nepdate.screenshot

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.compose
import androidx.glance.appwidget.provideContent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.widget.DateChipContent
import np.nepdate.widget.DateStripContent
import np.nepdate.widget.FestivalsWidgetContent
import np.nepdate.widget.NextFestivalWidgetContent
import np.nepdate.widget.TextDateContent
import np.nepdate.widget.TodayWidgetContent
import np.nepdate.widget.UpcomingWidgetContent
import np.nepdate.widget.WeekWidgetContent
import np.nepdate.widget.WidgetOpacity
import np.nepdate.widget.englishWidgetCopy
import np.nepdate.widget.festivalsListModel
import np.nepdate.widget.nepaliWidgetCopy
import np.nepdate.widget.nextFestivalModel
import np.nepdate.widget.todayWidgetModel
import np.nepdate.widget.upcomingWidgetModel
import np.nepdate.widget.weekWidgetModel
import np.nepdate.widget.widgetControlCopy
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Locale

/**
 * Every widget at the sizes people place it, on one busy day (Kartik 23, 2083: World Freedom Day
 * and more), in English and Nepali. The gallery is for review: a human checks the PNGs.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h800dp-xhdpi")
class WidgetGalleryScreenshotTest {
    private val base: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun english() {
        gallery(nepali = false)
    }

    @Test
    fun nepali() {
        gallery(nepali = true)
    }

    private fun gallery(nepali: Boolean) {
        val context = localeContext(nepali)
        val lang = if (nepali) Lang.NEPALI else Lang.ENGLISH
        val digits = if (nepali) Digits.NEPALI else Digits.ENGLISH
        val copy = if (nepali) nepaliWidgetCopy else englishWidgetCopy
        val controls = widgetControlCopy(context)
        val today = todayWidgetModel(DAY, lang, digits, copy, showNepal = false)
        val upcoming = upcomingWidgetModel(DAY, lang, digits, copy, showNepal = false)
        val week = weekWidgetModel(DAY, lang, digits, copy)
        val next = nextFestivalModel(DAY, 0, lang, digits, copy, controls)
        val list =
            festivalsListModel(
                DAY,
                holidaysOnly = false,
                digits = digits,
                lang = lang,
                copy = copy,
                controls = controls,
            )
        val suffix = if (nepali) "ne" else "en"
        val shots: List<Triple<String, DpSize, @Composable () -> Unit>> =
            listOf(
                Triple("today-2x2", DpSize(110.dp, 110.dp)) { TodayWidgetContent(today, WidgetOpacity.SOLID) },
                Triple("today-4x2", DpSize(250.dp, 110.dp)) { TodayWidgetContent(today, WidgetOpacity.SOLID) },
                Triple("today-4x3", DpSize(250.dp, 180.dp)) { TodayWidgetContent(today, WidgetOpacity.SOLID) },
                Triple("today-5x5", DpSize(320.dp, 320.dp)) { TodayWidgetContent(today, WidgetOpacity.SOLID) },
                Triple("upcoming-4x2", DpSize(250.dp, 110.dp)) { UpcomingWidgetContent(upcoming, WidgetOpacity.SOLID) },
                Triple("upcoming-4x3", DpSize(250.dp, 180.dp)) { UpcomingWidgetContent(upcoming, WidgetOpacity.SOLID) },
                Triple("upcoming-5x4", DpSize(320.dp, 250.dp)) { UpcomingWidgetContent(upcoming, WidgetOpacity.SOLID) },
                Triple("chip-1x1", DpSize(57.dp, 57.dp)) { DateChipContent(today, WidgetOpacity.SOLID) },
                Triple("chip-2x2", DpSize(110.dp, 110.dp)) { DateChipContent(today, WidgetOpacity.SOLID) },
                Triple("strip-4x1", DpSize(250.dp, 57.dp)) { DateStripContent(today, WidgetOpacity.SOLID) },
                Triple("strip-5x2", DpSize(320.dp, 110.dp)) { DateStripContent(today, WidgetOpacity.SOLID) },
                Triple("text-3x1", DpSize(180.dp, 57.dp)) { TextDateContent(today, WidgetOpacity.SOLID) },
                Triple("text-5x3", DpSize(320.dp, 180.dp)) { TextDateContent(today, WidgetOpacity.SOLID) },
                Triple("week-4x1", DpSize(250.dp, 57.dp)) { WeekWidgetContent(week, WidgetOpacity.SOLID) },
                Triple("week-5x3", DpSize(320.dp, 180.dp)) { WeekWidgetContent(week, WidgetOpacity.SOLID) },
                Triple("week-5x4", DpSize(320.dp, 250.dp)) { WeekWidgetContent(week, WidgetOpacity.SOLID) },
                Triple("week-4x2", DpSize(250.dp, 110.dp)) { WeekWidgetContent(week, WidgetOpacity.SOLID) },
                Triple("next-2x2", DpSize(110.dp, 110.dp)) { NextFestivalWidgetContent(next, WidgetOpacity.SOLID) },
                Triple("next-4x4", DpSize(250.dp, 250.dp)) { NextFestivalWidgetContent(next, WidgetOpacity.SOLID) },
                Triple("next-4x1", DpSize(250.dp, 70.dp)) { NextFestivalWidgetContent(next, WidgetOpacity.SOLID) },
                Triple("next-3x3", DpSize(180.dp, 180.dp)) { NextFestivalWidgetContent(next, WidgetOpacity.SOLID) },
                Triple("festivals-4x2", DpSize(250.dp, 110.dp)) {
                    FestivalsWidgetContent(list, WidgetOpacity.SOLID, scrolling = false)
                },
                Triple("festivals-5x5", DpSize(320.dp, 320.dp)) {
                    FestivalsWidgetContent(list, WidgetOpacity.SOLID, scrolling = false)
                },
            )
        shots.forEach { (name, size, content) ->
            render(context, size, content).drawToBitmap().captureRoboImage("a4-gallery-$name-$suffix.png")
        }
    }

    private fun localeContext(nepali: Boolean): Context {
        val configuration = Configuration(base.resources.configuration)
        configuration.uiMode =
            (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
        configuration.setLocale(if (nepali) Locale.forLanguageTag("ne-NP") else Locale.ENGLISH)
        return base.createConfigurationContext(configuration)
    }

    private fun render(
        context: Context,
        size: DpSize,
        content: @Composable () -> Unit,
    ): View {
        val remoteViews = kotlinx.coroutines.runBlocking { Host(content).compose(context, size = size) }
        val view = remoteViews.apply(context, FrameLayout(context))
        val density = context.resources.displayMetrics.density
        val width = (size.width.value * density).toInt()
        val height = (size.height.value * density).toInt()
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, width, height)
        return view
    }

    private class Host(
        private val content: @Composable () -> Unit,
    ) : GlanceAppWidget() {
        override suspend fun provideGlance(
            context: Context,
            id: GlanceId,
        ) {
            provideContent { content() }
        }
    }

    private companion object {
        /** 9 November 2026. */
        val DAY: NepaliDate = NepaliDate.of(2083, 7, 23)
    }
}
