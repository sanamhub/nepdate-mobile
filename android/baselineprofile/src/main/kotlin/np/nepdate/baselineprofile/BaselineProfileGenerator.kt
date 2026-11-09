package np.nepdate.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the Baseline Profile and the startup profile on a device. Both are checked in and
 * merged into every variant (ADR-0005). The `android-baseline-profile` workflow runs this on a
 * managed device and uploads the files; the pull request that changes them checks them in.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    /**
     * A fresh install opens on the first-run welcome, which is not the screen the profile is for.
     * Open it once, then stop the app so every measured start begins from the same place.
     */
    @Before
    fun dismissFirstRun() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("am start -n $PACKAGE_NAME/.MainActivity")
        device.openPastFirstRun()
        device.executeShellCommand("am force-stop $PACKAGE_NAME")
    }

    /** Cold start to the first drawn month grid. Also written to the startup profile. */
    @Test
    fun startup() {
        rule.collect(
            packageName = PACKAGE_NAME,
            includeInStartupProfile = true,
        ) {
            startActivityAndWait()
            device.waitForIdle()
        }
    }

    /** The month swipes the user does most. */
    @Test
    fun monthSwipes() {
        rule.collect(packageName = PACKAGE_NAME) {
            startActivityAndWait()
            device.waitForIdle()
            val swipeY = monthCellCenterY()
            repeat(PROFILE_SWIPES) {
                swipeToNextMonth(swipeY)
            }
        }
    }
}
