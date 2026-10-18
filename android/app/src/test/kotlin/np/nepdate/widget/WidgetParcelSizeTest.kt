package np.nepdate.widget

import android.content.Context
import android.os.Parcel
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.compose
import androidx.glance.appwidget.provideContent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** A2-05: logs the 5x5 W-03 RemoteViews parcel size; the number is recorded in docs/benchmarks.md. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class WidgetParcelSizeTest {
    @Test
    fun w03At5x5ParcelSize() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val model =
            monthWidgetModel(
                year = 2083,
                monthValue = 2,
                today = NepaliDate.of(2083, 2, 15),
                lang = Lang.ENGLISH,
                digits = Digits.ENGLISH,
                copy = englishWidgetCopy,
            )
        val remoteViews =
            kotlinx.coroutines.runBlocking {
                HostWidget {
                    GlanceTheme { MonthWidgetContent(model, WidgetOpacity.SOLID) }
                }.compose(context, size = DpSize(320.dp, 320.dp))
            }
        val parcel = Parcel.obtain()
        try {
            remoteViews.writeToParcel(parcel, 0)
            println("W03_5X5_PARCEL_BYTES=${parcel.dataSize()}")
        } finally {
            parcel.recycle()
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
}
