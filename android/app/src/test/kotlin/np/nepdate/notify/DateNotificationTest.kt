package np.nepdate.notify

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import np.nepdate.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "en-rUS")
class DateNotificationTest {
    private val context: Application = ApplicationProvider.getApplicationContext()
    private val manager = context.getSystemService(NotificationManager::class.java)

    private fun posted(settings: Settings): Notification? {
        runBlocking { DateNotification(context).update(settings) }
        return shadowOf(manager).allNotifications.singleOrNull()
    }

    @Test
    fun bothSwitchesPostTheFullDateWithTheDayIconInTheStatusBar() {
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val notification = posted(Settings(statusBarDate = true, notificationDate = true))

        assertNotNull(notification)
        notification!!
        assertTrue(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
        val icon = context.resources.getResourceEntryName(notification.smallIcon.resId)
        assertTrue("the status bar icon is a day drawable: $icon", icon.matches(Regex("""ic_day_en_\d+""")))
        assertEquals(DateNotification.ICON_CHANNEL, notification.channelId)
        val title = notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString()
        assertTrue("title names the weekday and the BS date: $title", title.contains(","))
        assertNotNull(notification.extras.getCharSequence(Notification.EXTRA_TEXT))
        assertEquals(
            NotificationManager.IMPORTANCE_LOW,
            manager.getNotificationChannel(DateNotification.ICON_CHANNEL).importance,
        )
    }

    @Test
    fun theStatusBarSwitchAlonePostsOneLine() {
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val notification = posted(Settings(statusBarDate = true))!!

        assertEquals(DateNotification.ICON_CHANNEL, notification.channelId)
        assertNull(notification.extras.getCharSequence(Notification.EXTRA_TEXT))
    }

    @Test
    fun theNotificationSwitchAloneStaysOutOfTheStatusBar() {
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val notification = posted(Settings(notificationDate = true))!!

        assertEquals(DateNotification.CARD_CHANNEL, notification.channelId)
        assertEquals(
            NotificationManager.IMPORTANCE_MIN,
            manager.getNotificationChannel(DateNotification.CARD_CHANNEL).importance,
        )
    }

    @Test
    fun withoutPermissionOrWithBothSwitchesOffNothingIsShown() {
        shadowOf(context).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertNull(posted(Settings(statusBarDate = true, notificationDate = true)))

        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        posted(Settings(statusBarDate = true))
        assertNull(posted(Settings()))
    }
}
