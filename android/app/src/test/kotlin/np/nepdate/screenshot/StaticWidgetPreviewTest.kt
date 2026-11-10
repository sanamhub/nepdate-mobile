package np.nepdate.screenshot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import np.nepdate.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The static widget previews that Android 12 to 14 launchers inflate, at the size each widget opens
 * at. They are not a pass: the PNGs show the drift between a preview and its widget in review. Every
 * preview is applied as a [RemoteViews], so a view that RemoteViews cannot inflate fails here.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-xhdpi")
class StaticWidgetPreviewTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    /**
     * Glyphs are rasterised by the host, so a text edge lands a pixel or two differently on CI than
     * on the machine that recorded the references. One percent still fails on any layout change.
     */
    private val tolerance =
        RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))

    @Test
    fun previews() {
        val previews =
            listOf(
                Preview("chip-1x1", R.layout.widget_preview_chip, DpSize(57.dp, 57.dp)),
                Preview("next-2x2", R.layout.widget_preview_next_festival, DpSize(110.dp, 110.dp)),
                Preview("today-2x2", R.layout.widget_preview_today, DpSize(110.dp, 110.dp)),
                Preview("strip-4x1", R.layout.widget_preview_strip, DpSize(250.dp, 57.dp)),
                Preview("text-3x1", R.layout.widget_preview_text, DpSize(180.dp, 57.dp)),
                Preview("week-4x2", R.layout.widget_preview_week, DpSize(250.dp, 110.dp)),
                Preview("upcoming-4x2", R.layout.widget_preview_upcoming, DpSize(250.dp, 110.dp)),
                Preview("festivals-4x2", R.layout.widget_preview_festivals, DpSize(250.dp, 110.dp)),
                Preview("month-4x4", R.layout.widget_preview_month, DpSize(250.dp, 250.dp)),
            )
        previews.forEach(::capture)
    }

    private fun capture(preview: Preview) {
        val view = RemoteViews(context.packageName, preview.layout).apply(context, FrameLayout(context))
        val density = context.resources.displayMetrics.density
        val width = (preview.size.width.value * density).toInt()
        val height = (preview.size.height.value * density).toInt()
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        bitmap.captureRoboImage("a5-static-${preview.name}.png", roborazziOptions = tolerance)
    }

    private data class Preview(
        val name: String,
        val layout: Int,
        val size: DpSize,
    )
}
