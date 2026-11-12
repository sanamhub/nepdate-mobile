package np.nepdate.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import np.nepdate.MainActivity
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.widget.TodayWidgetModel
import np.nepdate.widget.todayModel
import np.nepdate.widget.widgetEnvironment

/** Shows or clears the date notification after each refresh (midnight, boot, settings, app open). */
internal fun interface TodayNotifier {
    suspend fun update(settings: Settings)

    companion object {
        /** For tests and code paths that draw no notification. */
        val NONE = TodayNotifier { }
    }
}

/**
 * The opt-in date notification, with two independent switches:
 * - "Day in status bar" draws today's BS day as the status bar icon. Android shows a status bar
 *   icon only for a notification, so with this switch alone the notification is a single line.
 * - "Date in notifications" keeps a silent card with the full date, tithi and today's holiday.
 *   With this switch alone the card goes on a minimum importance channel, which Android keeps out
 *   of the status bar.
 * Nothing is posted until a switch is on and the notification permission is granted.
 */
internal class DateNotification(
    private val context: Context,
) : TodayNotifier {
    override suspend fun update(settings: Settings) {
        val manager = NotificationManagerCompat.from(context)
        val wanted = settings.statusBarDate || settings.notificationDate
        if (!wanted || !canPost()) {
            manager.cancel(NOTIFICATION_ID)
            return
        }
        val environment = widgetEnvironment(context)
        val model = environment.todayModel(environment.today)
        val channel = if (settings.statusBarDate) ICON_CHANNEL else CARD_CHANNEL
        ensureChannel(channel)
        val icon = dayIcon(environment.today.dayOfMonth, nepaliDigits = environment.digits == Digits.NEPALI)
        notify(manager, build(model, channel, icon, full = settings.notificationDate))
    }

    @SuppressLint("MissingPermission") // update() checks canPost() first.
    private fun notify(
        manager: NotificationManagerCompat,
        notification: Notification,
    ) = manager.notify(NOTIFICATION_ID, notification)

    private fun canPost(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun build(
        model: TodayWidgetModel,
        channel: String,
        @DrawableRes icon: Int,
        full: Boolean,
    ): Notification {
        val open =
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val builder =
            NotificationCompat
                .Builder(context, channel)
                .setSmallIcon(icon)
                .setContentTitle("${model.weekday}, ${model.bsLongLine}")
                .setContentIntent(open)
                .setOngoing(true)
                .setSilent(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setPriority(
                    if (channel == ICON_CHANNEL) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_MIN,
                ).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        if (full) {
            val holiday = model.holidayName ?: model.eventName
            val line = listOfNotNull(holiday, model.tithi.ifEmpty { null }).joinToString(" · ")
            builder
                .setContentText(line)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(card(model, line, R.layout.notification_date))
                .setCustomBigContentView(card(model, line, R.layout.notification_date_big))
        }
        return builder.build()
    }

    /**
     * The styled card inside the system's decorated frame (the standard custom notification API):
     * the day on the Sunny shape, the BS date in the accent colour, today's holiday or event with
     * the tithi, and in the expanded card the AD date and the next public holiday.
     */
    private fun card(
        model: TodayWidgetModel,
        line: String,
        layout: Int,
    ): RemoteViews =
        RemoteViews(context.packageName, layout).apply {
            setTextViewText(R.id.notify_day, model.bsDay)
            setTextViewText(R.id.notify_title, "${model.bsLongLine}, ${model.weekday}")
            setTextViewText(R.id.notify_line, line)
            if (layout == R.layout.notification_date_big) {
                setTextViewText(R.id.notify_title, model.bsLongLine)
                setTextViewText(R.id.notify_weekday, model.weekday)
                setTextViewText(R.id.notify_ad, model.adLine)
                val next = model.nextHoliday
                setTextViewText(R.id.notify_next, next?.let { "${it.name} · ${it.relative}" }.orEmpty())
            }
        }

    private fun ensureChannel(id: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val icon = id == ICON_CHANNEL
        val channel =
            NotificationChannel(
                id,
                context.getString(if (icon) R.string.notify_channel_icon else R.string.notify_channel_card),
                // Low shows the icon without sound or pop-up; minimum keeps it out of the status bar.
                if (icon) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_MIN,
            ).apply {
                setShowBadge(false)
                description = context.getString(R.string.notify_channel_description)
            }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    @DrawableRes
    private fun dayIcon(
        day: Int,
        nepaliDigits: Boolean,
    ): Int = dayIconRes(context, day, nepaliDigits)

    internal companion object {
        const val ICON_CHANNEL = "status_bar_date"
        const val CARD_CHANNEL = "date_card"
        const val NOTIFICATION_ID = 2083
    }
}

/**
 * Today's BS day as an icon: one of the 64 generated vector drawables (days 1 to 32, Latin and
 * Devanagari digits). Android 16 replaces a bitmap small icon with the app icon, so the day has to
 * be a resource; the status bar and the Quick Settings tile keep only its alpha and tint it like
 * every other icon.
 */
@SuppressLint("DiscouragedApi") // 64 generated names; res/raw/keep_day_icons.xml keeps them.
@DrawableRes
internal fun dayIconRes(
    context: Context,
    day: Int,
    nepaliDigits: Boolean,
): Int =
    context.resources
        .getIdentifier("ic_day_${if (nepaliDigits) "ne" else "en"}_$day", "drawable", context.packageName)
        .takeIf { it != 0 } ?: R.drawable.ic_sym_event_available
