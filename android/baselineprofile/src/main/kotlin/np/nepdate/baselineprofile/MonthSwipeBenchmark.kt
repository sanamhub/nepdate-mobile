package np.nepdate.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MR-09 AC2: ten month swipes after a warm start, with frame timing, with no profile and with the
 * Baseline Profile.
 */
@RunWith(AndroidJUnit4::class)
class MonthSwipeBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /** A fresh install opens on the welcome; the month grid is what the swipes mean to hit. */
    @Before
    fun dismissFirstRun() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).prepareForMeasuredLaunch()
    }

    @Test
    fun monthSwipesWithoutProfile() = swipe(withoutProfile())

    @Test
    fun monthSwipesWithProfile() = swipe(withProfile())

    private fun swipe(compilation: CompilationMode) {
        // The anchor is read in setup, not in the measured block, so finding the cell is not timed.
        var swipeY = 0
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilation,
            startupMode = StartupMode.WARM,
            iterations = ITERATIONS,
            setupBlock = {
                startActivityAndWait()
                device.waitForIdle()
                swipeY = monthCellCenterY()
            },
        ) {
            repeat(SWIPES) {
                swipeToNextMonth(swipeY)
            }
        }
    }

    private companion object {
        const val ITERATIONS = 10
        const val SWIPES = 10
    }
}
