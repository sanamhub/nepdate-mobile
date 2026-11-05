package np.nepdate.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
            repeat(PROFILE_SWIPES) {
                device.swipe(
                    (device.displayWidth * SWIPE_START_X_FRACTION).toInt(),
                    (device.displayHeight * SWIPE_Y_FRACTION).toInt(),
                    (device.displayWidth * SWIPE_END_X_FRACTION).toInt(),
                    (device.displayHeight * SWIPE_Y_FRACTION).toInt(),
                    SWIPE_STEPS,
                )
                device.waitForIdle()
            }
        }
    }
}
