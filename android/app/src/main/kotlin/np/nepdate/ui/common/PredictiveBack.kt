package np.nepdate.ui.common

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * A transition to [target] that the system back gesture can seek: while the user drags, the screen
 * follows the finger toward [backTarget], so they see where back leads before letting go. Letting go
 * calls [onBack], which moves [target] there and finishes the move; cancelling animates back. This
 * is the documented predictive back pattern for a custom in-app transition, a
 * [SeekableTransitionState] driven by [PredictiveBackHandler]. With no [backTarget] the gesture is
 * left to the system, which animates leaving the app.
 */
@Composable
internal fun <T> rememberPredictiveBackTransition(
    target: T,
    backTarget: T?,
    onBack: () -> Unit,
    label: String,
): Transition<T> {
    val state = remember { SeekableTransitionState(target) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(target) { state.animateTo(target) }
    PredictiveBackHandler(enabled = backTarget != null) { progress ->
        try {
            progress.collect { event -> if (backTarget != null) state.seekTo(event.progress, backTarget) }
            onBack()
        } catch (cancelled: CancellationException) {
            scope.launch { state.animateTo(target) }
            throw cancelled
        }
    }
    return rememberTransition(state, label = label)
}
