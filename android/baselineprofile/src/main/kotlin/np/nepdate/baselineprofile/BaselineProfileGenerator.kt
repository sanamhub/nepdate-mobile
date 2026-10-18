package np.nepdate.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the Baseline Profile on a device. The profile is checked in and merged into every
 * variant (ADR-0005); CI never runs this test.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
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
