package np.nepdate.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.MacrobenchmarkScope

/** The package used by the baseline profile and Macrobenchmark tests. */
internal const val PACKAGE_NAME = "np.nepdate"

/** Swipe geometry shared by the profile generator and the swipe benchmark. */
internal const val SWIPE_START_X_FRACTION = 0.85f
internal const val SWIPE_END_X_FRACTION = 0.15f
internal const val SWIPE_Y_FRACTION = 0.55f
internal const val SWIPE_STEPS = 20
internal const val PROFILE_SWIPES = 3

/** Nothing is compiled ahead of time: what a first run after an install looks like. */
internal fun withoutProfile(): CompilationMode = CompilationMode.None()

/** The checked-in Baseline Profile is installed and compiled, and the run fails if it is missing. */
internal fun withProfile(): CompilationMode =
    CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require, warmupIterations = 2)

/**
 * `StartupTimingMetric` reads the first frame drawn after `reportFullyDrawn()`, and the app is idle
 * once it has drawn the month grid, so no such frame would exist. A short vertical drag on the grid
 * asks for one. It runs after the launch, so the first-display and fully-drawn times are not moved.
 */
internal fun MacrobenchmarkScope.drawFrameAfterFullyDrawn() {
    device.waitForIdle()
    val x = device.displayWidth / 2
    val y = (device.displayHeight * SWIPE_Y_FRACTION).toInt()
    device.swipe(x, y, x, y + FRAME_NUDGE_PX, FRAME_NUDGE_STEPS)
    device.waitForIdle()
}

private const val FRAME_NUDGE_PX = 40
private const val FRAME_NUDGE_STEPS = 10
