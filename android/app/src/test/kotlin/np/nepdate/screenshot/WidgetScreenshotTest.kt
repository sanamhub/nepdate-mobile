package np.nepdate.screenshot

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.compose
import androidx.glance.appwidget.composeForPreview
import androidx.glance.appwidget.provideContent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.ui.common.resolve
import np.nepdate.widget.MonthWidgetContent
import np.nepdate.widget.TodayWidgetContent
import np.nepdate.widget.UpcomingWidgetContent
import np.nepdate.widget.WidgetOpacity
import np.nepdate.widget.englishWidgetCopy
import np.nepdate.widget.monthWidgetModel
import np.nepdate.widget.nepaliWidgetCopy
import np.nepdate.widget.todayWidgetModel
import np.nepdate.widget.upcomingWidgetModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Locale

/**
 * Renders the Glance RemoteViews through a real Android view tree, the same way the launcher
 * inflates them. The matrix covers W-01 and W-02 at their three sizes, W-03 at its three sizes,
 * both dates and all three opacities in light, dark and Nepali (A2-02, A2-03).
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class WidgetScreenshotTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun todayMatrix() {
        renderTodayMatrix()
    }

    @Test
    fun upcomingMatrix() {
        renderUpcomingMatrix()
    }

    @Test
    fun monthMatrix() {
        renderMonthMatrix()
    }

    @Test
    fun generatedPreviews() {
        listOf(Mode.LIGHT to "light", Mode.DARK to "dark", Mode.NEPALI to "ne").forEach { (mode, modeName) ->
            val renderContext = mode.context(context)
            listOf(
                Triple(np.nepdate.widget.TodayGlanceWidget(), WIDE, "w01"),
                Triple(np.nepdate.widget.UpcomingGlanceWidget(), WIDE, "w02"),
                Triple(np.nepdate.widget.MonthGlanceWidget(), MONTH_44, "w03"),
            ).forEach { (widget, size, name) ->
                val remoteViews =
                    kotlinx.coroutines.runBlocking {
                        widget.composeForPreview(
                            context = renderContext,
                            widgetCategory = android.appwidget.AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                        )
                    }
                val view = inflate(remoteViews, renderContext, size)
                view.drawToBitmap().captureRoboImage("a2-preview-$name-$modeName.png")
            }
        }
    }

    @Test
    fun monthHolidayMarks() {
        listOf(Mode.LIGHT to "light", Mode.DARK to "dark", Mode.NEPALI to "ne").forEach { (mode, modeName) ->
            val renderContext = mode.context(context)
            val lang = if (mode == Mode.NEPALI) Lang.NEPALI else Lang.ENGLISH
            val digits = if (mode == Mode.NEPALI) Digits.NEPALI else Digits.ENGLISH
            val copy = if (mode == Mode.NEPALI) nepaliWidgetCopy else englishWidgetCopy
            val model =
                monthWidgetModel(
                    year = 2083,
                    monthValue = 6,
                    today = HOLIDAY,
                    lang = lang,
                    digits = digits,
                    copy = copy,
                )
            val view =
                render(
                    context = renderContext,
                    size = MONTH_44,
                ) {
                    MonthWidgetContent(model, WidgetOpacity.SOLID)
                }
            view.drawToBitmap().captureRoboImage("a2-w03-4x4-$modeName-holiday.png")
        }
    }

    @Test
    fun monthTextIsNotClippedAtAnySize() {
        val model =
            monthWidgetModel(
                year = 2083,
                monthValue = 2,
                today = NepaliDate.of(2083, 2, 15),
                lang = Lang.ENGLISH,
                digits = Digits.ENGLISH,
                copy = englishWidgetCopy,
            )
        listOf(MONTH_43 to "4x3", MONTH_44 to "4x4", MONTH_55 to "5x5").forEach { (size, name) ->
            val view =
                render(size) {
                    GlanceTheme { MonthWidgetContent(model, WidgetOpacity.SOLID) }
                }
            visitTextViews(view) { text ->
                for (line in 0 until text.lineCount) {
                    assertEquals("$name clips at line $line", 0, text.layout.getEllipsisCount(line))
                }
            }
        }
    }

    @Suppress("NestedBlockDepth") // The screenshot matrix is deliberately four-dimensional.
    private fun renderTodayMatrix() {
        listOf(Mode.LIGHT to "light", Mode.DARK to "dark", Mode.NEPALI to "ne").forEach { (mode, modeName) ->
            val renderContext = mode.context(context)
            listOf(SMALL to "2x2", ONE_LINE to "4x1", WIDE to "4x2").forEach { (size, sizeName) ->
                WidgetOpacity.entries.forEach { opacity ->
                    listOf(NORMAL to "normal", HOLIDAY to "holiday").forEach { (date, dayName) ->
                        val lang = if (mode == Mode.NEPALI) Lang.NEPALI else Lang.ENGLISH
                        val digits =
                            Digits.FOLLOW_LANGUAGE.resolve(
                                if (mode ==
                                    Mode.NEPALI
                                ) {
                                    Lang.NEPALI
                                } else {
                                    Lang.ENGLISH
                                },
                            )
                        val copy = if (mode == Mode.NEPALI) nepaliWidgetCopy else englishWidgetCopy
                        val model =
                            todayWidgetModel(
                                date = date,
                                lang = lang,
                                digits = digits,
                                copy = copy,
                                showNepal = mode == Mode.NEPALI,
                            )
                        val view =
                            render(
                                context = renderContext,
                                size = size,
                            ) {
                                TodayWidgetContent(model, opacity)
                            }
                        view.drawToBitmap().captureRoboImage(
                            "a2-w01-$sizeName-$modeName-${opacity.key}-$dayName.png",
                        )
                    }
                }
            }
        }
    }

    @Suppress("NestedBlockDepth") // The screenshot matrix is deliberately four-dimensional.
    private fun renderUpcomingMatrix() {
        listOf(Mode.LIGHT to "light", Mode.DARK to "dark", Mode.NEPALI to "ne").forEach { (mode, modeName) ->
            val renderContext = mode.context(context)
            listOf(SMALL to "2x2", ONE_LINE to "4x1", WIDE to "4x2").forEach { (size, sizeName) ->
                WidgetOpacity.entries.forEach { opacity ->
                    listOf(NORMAL to "normal", HOLIDAY to "holiday").forEach { (date, dayName) ->
                        val lang = if (mode == Mode.NEPALI) Lang.NEPALI else Lang.ENGLISH
                        val digits =
                            Digits.FOLLOW_LANGUAGE.resolve(
                                if (mode ==
                                    Mode.NEPALI
                                ) {
                                    Lang.NEPALI
                                } else {
                                    Lang.ENGLISH
                                },
                            )
                        val copy = if (mode == Mode.NEPALI) nepaliWidgetCopy else englishWidgetCopy
                        val model =
                            upcomingWidgetModel(
                                date = date,
                                lang = lang,
                                digits = digits,
                                copy = copy,
                                showNepal = mode == Mode.NEPALI,
                            )
                        val view =
                            render(
                                context = renderContext,
                                size = size,
                            ) {
                                UpcomingWidgetContent(model, opacity)
                            }
                        view.drawToBitmap().captureRoboImage(
                            "a2-w02-$sizeName-$modeName-${opacity.key}-$dayName.png",
                        )
                    }
                }
            }
        }
    }

    private fun renderMonthMatrix() {
        listOf(Mode.LIGHT to "light", Mode.DARK to "dark", Mode.NEPALI to "ne").forEach { (mode, modeName) ->
            val renderContext = mode.context(context)
            listOf(MONTH_43 to "4x3", MONTH_44 to "4x4", MONTH_55 to "5x5").forEach { (size, sizeName) ->
                WidgetOpacity.entries.forEach { opacity ->
                    val lang = if (mode == Mode.NEPALI) Lang.NEPALI else Lang.ENGLISH
                    val digits = Digits.FOLLOW_LANGUAGE.resolve(if (mode == Mode.NEPALI) Lang.NEPALI else Lang.ENGLISH)
                    val copy = if (mode == Mode.NEPALI) nepaliWidgetCopy else englishWidgetCopy
                    val model =
                        monthWidgetModel(
                            year = 2083,
                            monthValue = 2,
                            today = NepaliDate.of(2083, 2, 15),
                            lang = lang,
                            digits = digits,
                            copy = copy,
                        )
                    val view =
                        render(
                            context = renderContext,
                            size = size,
                        ) {
                            MonthWidgetContent(model, opacity)
                        }
                    view.drawToBitmap().captureRoboImage(
                        "a2-w03-$sizeName-$modeName-${opacity.key}.png",
                    )
                }
            }
        }
    }

    private fun render(
        size: DpSize,
        context: Context = this.context,
        content: @Composable () -> Unit,
    ): View {
        val remoteViews =
            kotlinx.coroutines.runBlocking {
                HostWidget(content).compose(context, size = size)
            }
        return inflate(remoteViews, context, size)
    }

    private fun inflate(
        remoteViews: android.widget.RemoteViews,
        context: Context,
        size: DpSize,
    ): View {
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

    private fun visitTextViews(
        view: View,
        action: (TextView) -> Unit,
    ) {
        if (view is TextView) action(view)
        if (view is android.view.ViewGroup) {
            for (index in 0 until view.childCount) visitTextViews(view.getChildAt(index), action)
        }
    }

    private class HostWidget(
        private val content: @Composable () -> Unit,
    ) : GlanceAppWidget() {
        override suspend fun provideGlance(
            context: Context,
            id: GlanceId,
        ) {
            provideContent { content() }
        }
    }

    private enum class Mode {
        LIGHT,
        DARK,
        NEPALI,
        ;

        fun context(base: Context): Context {
            val configuration = Configuration(base.resources.configuration)
            configuration.uiMode =
                (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (this == DARK) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            if (this == NEPALI) configuration.setLocale(Locale.forLanguageTag("ne-NP"))
            return base.createConfigurationContext(configuration)
        }
    }
}

private val SMALL = DpSize(110.dp, 110.dp)
private val ONE_LINE = DpSize(250.dp, 70.dp)
private val WIDE = DpSize(250.dp, 110.dp)
private val MONTH_43 = DpSize(250.dp, 180.dp)
private val MONTH_44 = DpSize(250.dp, 250.dp)
private val MONTH_55 = DpSize(320.dp, 320.dp)

private val NORMAL = NepaliDate.of(2083, 6, 20)
private val HOLIDAY = NepaliDate.of(2083, 6, 25)
