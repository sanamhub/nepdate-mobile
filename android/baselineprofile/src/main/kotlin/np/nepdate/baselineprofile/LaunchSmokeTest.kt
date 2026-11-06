package np.nepdate.baselineprofile

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Starts the shrunk release build and walks the paths that load classes by name. Debug builds and
 * unit tests do not run R8, so a missing keep rule shows up only here. Every test fails on any
 * entry in the crash buffer and puts that log in its message.
 */
@RunWith(AndroidJUnit4::class)
class LaunchSmokeTest {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun startClean() {
        device.executeShellCommand("logcat -c")
        device.executeShellCommand("am force-stop $PACKAGE_NAME")
    }

    @Test
    fun releaseBuildStartsAndShowsTheApp() {
        launch()
        assertNoCrash("start")
    }

    @Test
    fun everyTabAndTheSettingsScreenOpen() {
        launch()
        for (tab in listOf("Festivals", "Converter", "Patro")) {
            tap(tab, "the $tab tab")
        }
        tap("Settings", "the settings button")
        assertTrue(
            "The settings screen did not open",
            device.wait(Until.hasObject(By.text("Settings").pkg(PACKAGE_NAME)), UI_TIMEOUT_MS),
        )
        device.pressBack()
        assertNoCrash("tabs and settings")
    }

    /**
     * Sends the update broadcast to each widget provider, which makes the system create the receiver
     * and Glance start up. A launcher picks the preview from the same classes. The generated
     * previews are published only on Android 15 and later, so this device does not run that call.
     */
    @Test
    fun widgetProvidersStart() {
        for (receiver in WIDGET_RECEIVERS) {
            device.executeShellCommand(
                "am broadcast -a android.appwidget.action.APPWIDGET_UPDATE -n $PACKAGE_NAME/$receiver",
            )
        }
        device.waitForIdle(SETTLE_MS)
        assertNoCrash("widget providers")
    }

    private fun launch() {
        device.executeShellCommand("am start -n $PACKAGE_NAME/.MainActivity")
        val shown = device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), LAUNCH_TIMEOUT_MS)
        assertTrue("The app did not show a window", shown)
    }

    /** A Compose item may expose its label as a description or as text, so try both. */
    private fun tap(
        label: String,
        what: String,
    ) {
        val target =
            device.wait(Until.findObject(By.desc(label).pkg(PACKAGE_NAME)), UI_TIMEOUT_MS)
                ?: device.wait(Until.findObject(By.text(label).pkg(PACKAGE_NAME)), UI_TIMEOUT_MS)
        assertTrue("Could not find $what", target != null)
        target.click()
        device.waitForIdle()
    }

    private fun assertNoCrash(step: String) {
        val crash = device.executeShellCommand("logcat -d -b crash")
        val runtime = device.executeShellCommand("logcat -d -s AndroidRuntime:E")
        val running = device.executeShellCommand("pidof $PACKAGE_NAME").isNotBlank()
        val message = "The app crashed at: $step. running=$running"
        assertTrue(
            "$message\n--- crash buffer ---\n$crash\n--- AndroidRuntime ---\n$runtime",
            crash.isBlank() && runtime.lines().none { it.contains("FATAL EXCEPTION") },
        )
    }

    private companion object {
        const val LAUNCH_TIMEOUT_MS = 15_000L
        const val UI_TIMEOUT_MS = 5_000L
        const val SETTLE_MS = 3_000L
        val WIDGET_RECEIVERS =
            listOf(
                ".widget.TodayGlanceWidgetReceiver",
                ".widget.UpcomingGlanceWidgetReceiver",
                ".widget.MonthGlanceWidgetReceiver",
            )
    }
}
