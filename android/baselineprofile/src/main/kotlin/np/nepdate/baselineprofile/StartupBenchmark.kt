package np.nepdate.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MR-09 AC1: cold, warm and hot start with `StartupTimingMetric`, with no profile and with the
 * Baseline Profile. The first display and `reportFullyDrawn()` are both reported.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldWithoutProfile() = start(StartupMode.COLD, withoutProfile())

    @Test
    fun coldWithProfile() = start(StartupMode.COLD, withProfile())

    @Test
    fun warmWithoutProfile() = start(StartupMode.WARM, withoutProfile())

    @Test
    fun warmWithProfile() = start(StartupMode.WARM, withProfile())

    @Test
    fun hotWithoutProfile() = start(StartupMode.HOT, withoutProfile())

    @Test
    fun hotWithProfile() = start(StartupMode.HOT, withProfile())

    private fun start(
        mode: StartupMode,
        compilation: CompilationMode,
    ) {
        benchmarkRule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilation,
            startupMode = mode,
            iterations = ITERATIONS,
        ) {
            pressHome()
            startActivityAndWait()
            drawFrameAfterFullyDrawn()
        }
    }

    private companion object {
        const val ITERATIONS = 10
    }
}
