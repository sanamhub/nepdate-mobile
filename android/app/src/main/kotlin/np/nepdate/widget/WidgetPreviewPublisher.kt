package np.nepdate.widget

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.collection.intSetOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.setWidgetPreviews

/**
 * Publishes generated previews on Android 15+. Android 12 to 14 launchers use the static
 * `previewLayout` in each `appwidget-provider` XML, and older ones its vector `previewImage`.
 */
internal class WidgetPreviewPublisher(
    private val context: Context,
) {
    @Suppress("TooGenericExceptionCaught") // Preview publishing must never take down app start.
    suspend fun publish() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        try {
            publishGenerated()
        } catch (error: Exception) {
            Log.w(TAG, "Could not publish generated widget previews", error)
        }
    }

    /**
     * One call per category, home screen first. With a single `HOME_SCREEN or KEYGUARD` call the
     * three keyguard-capable widgets (Today, Today and upcoming, Next festival) showed no preview in
     * the OnePlus picker while the home-only Month and Festivals list did, so no launcher has to match
     * a combined flag any more. The calls are rate limited; a refused one keeps the last preview.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun publishGenerated() {
        val manager = GlanceAppWidgetManager(context)
        val home = intSetOf(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN)
        val homeThenKeyguard =
            intSetOf(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN, AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD)
        manager.setWidgetPreviews<TodayGlanceWidgetReceiver>(homeThenKeyguard)
        manager.setWidgetPreviews<UpcomingGlanceWidgetReceiver>(homeThenKeyguard)
        manager.setWidgetPreviews<MonthGlanceWidgetReceiver>(home)
        manager.setWidgetPreviews<NextFestivalGlanceWidgetReceiver>(homeThenKeyguard)
        manager.setWidgetPreviews<FestivalsGlanceWidgetReceiver>(home)
        manager.setWidgetPreviews<DateChipGlanceWidgetReceiver>(homeThenKeyguard)
        manager.setWidgetPreviews<DateStripGlanceWidgetReceiver>(home)
        manager.setWidgetPreviews<WeekGlanceWidgetReceiver>(home)
        manager.setWidgetPreviews<TextDateGlanceWidgetReceiver>(homeThenKeyguard)
    }

    private companion object {
        const val TAG = "NepdateWidgets"
    }
}
