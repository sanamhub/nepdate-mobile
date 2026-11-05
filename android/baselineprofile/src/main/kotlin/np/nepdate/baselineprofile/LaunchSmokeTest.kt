package np.nepdate.baselineprofile

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The shrunk release build must start and draw its first screen. Debug builds and unit tests do
 * not run R8, so a rule the shrinker needs can go missing without any of them failing. On a crash
 * the failure message carries the crash log, which is the only place to read it on CI.
 */
@RunWith(AndroidJUnit4::class)
class LaunchSmokeTest {
    @Test
    fun releaseBuildStartsAndShowsTheApp() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("logcat -c")
        device.executeShellCommand("am force-stop $PACKAGE_NAME")
        device.executeShellCommand("am start -n $PACKAGE_NAME/.MainActivity")

        val shown = device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), LAUNCH_TIMEOUT_MS)
        val crash = device.executeShellCommand("logcat -d -b crash")
        val runtime = device.executeShellCommand("logcat -d -s AndroidRuntime:E")

        assertTrue(
            "The app did not start. shown=$shown\n--- crash buffer ---\n$crash\n--- AndroidRuntime ---\n$runtime",
            shown && crash.isBlank(),
        )
    }

    private companion object {
        const val LAUNCH_TIMEOUT_MS = 15_000L
    }
}
