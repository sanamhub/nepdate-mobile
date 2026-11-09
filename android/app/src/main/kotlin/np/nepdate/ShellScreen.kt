package np.nepdate

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Transition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import np.nepdate.ui.common.rememberPredictiveBackTransition

/** What the app shell shows: the first-run screen, an overlay, or the tabs (overlay NONE). */
internal data class ShellScreen(
    val firstRunDone: Boolean,
    val overlay: Overlay,
) {
    /** How deep the screen sits: the tabs, then Settings, Search and Year, then About. */
    val depth: Int
        get() =
            when {
                !firstRunDone -> -1
                overlay == Overlay.NONE -> 0
                overlay == Overlay.ABOUT -> 2
                else -> 1
            }
}

/** The shell's transition, which the back gesture seeks from an overlay toward where back leads. */
@Composable
internal fun rememberShellTransition(
    firstRunDone: Boolean,
    navigation: AppNavigation,
): Transition<ShellScreen> {
    val open = ShellScreen(firstRunDone, navigation.overlay)
    return rememberPredictiveBackTransition(
        target = open,
        backTarget = open.copy(overlay = navigation.backTarget).takeIf { firstRunDone && it != open },
        onBack = navigation::back,
        label = "shell",
    )
}

/**
 * Material's shared X axis between the tabs and the screens above them: the deeper screen comes in
 * from the end and the shallower one returns from the start, both fading over a short slide. Leaving
 * the first-run screen fades through instead, since the tabs are not "deeper" than it.
 */
@Composable
internal fun shellTransition(): AnimatedContentTransitionScope<ShellScreen>.() -> ContentTransform {
    val slide = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val fade = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val fastFade = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val scale = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    return {
        val from = initialState.depth
        val to = targetState.depth
        when {
            from < 0 ->
                (fadeIn(fade) + scaleIn(scale, initialScale = FADE_THROUGH_SCALE)) togetherWith fadeOut(fastFade)
            to > from ->
                (slideInHorizontally(slide) { it / SLIDE_SHARE } + fadeIn(fade)) togetherWith
                    (slideOutHorizontally(slide) { -it / SLIDE_SHARE } + fadeOut(fastFade))
            else ->
                (slideInHorizontally(slide) { -it / SLIDE_SHARE } + fadeIn(fade)) togetherWith
                    (slideOutHorizontally(slide) { it / SLIDE_SHARE } + fadeOut(fastFade))
        }
    }
}

/** The shared-axis slide covers this fraction of the width (one fifth). */
private const val SLIDE_SHARE = 5

private const val FADE_THROUGH_SCALE = 0.94f
