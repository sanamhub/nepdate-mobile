package np.nepdate.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** MR-09 AC2: ten swipes after a warm start, with frame timing. */
@RunWith(AndroidJUnit4::class)
class MonthSwipeBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun monthSwipes() {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.DEFAULT,
            startupMode = StartupMode.WARM,
            iterations = ITERATIONS,
            setupBlock = {
                startActivityAndWait()
                device.waitForIdle()
            },
        ) {
            repeat(SWIPES) {
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

    private companion object {
        const val ITERATIONS = 10
        const val SWIPES = 10
    }
}
