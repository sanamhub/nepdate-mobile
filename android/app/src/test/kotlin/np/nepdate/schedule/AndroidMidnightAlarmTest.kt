package np.nepdate.schedule

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class AndroidMidnightAlarmTest {
    @Test
    fun armUsesOneInexactTenMinuteWindow() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val manager = context.getSystemService(AlarmManager::class.java)

        AndroidMidnightAlarm(context).arm(1_234_567L, MidnightScheduler.WINDOW_MILLIS)

        val alarms = Shadows.shadowOf(manager).scheduledAlarms
        assertEquals(1, alarms.size)
        val alarm = alarms.single()
        assertEquals(AlarmManager.RTC, alarm.type)
        assertEquals(1_234_567L, alarm.triggerAtTime)
        assertEquals(10 * 60 * 1000L, alarm.windowLengthMs)
    }
}
