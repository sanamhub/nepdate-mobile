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
 * Publishes generated previews on Android 15+ (A2-04). Older launchers use the static
 * `previewLayout` in each `appwidget-provider` XML.
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

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun publishGenerated() {
        val manager = GlanceAppWidgetManager(context)
        val homeAndKeyguard =
            intSetOf(
                AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN or
                    AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD,
            )
        manager.setWidgetPreviews<TodayGlanceWidgetReceiver>(homeAndKeyguard)
        manager.setWidgetPreviews<UpcomingGlanceWidgetReceiver>(homeAndKeyguard)
        manager.setWidgetPreviews<MonthGlanceWidgetReceiver>(
            intSetOf(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN),
        )
    }

    private companion object {
        const val TAG = "NepdateWidgets"
    }
}
