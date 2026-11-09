package np.nepdate.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Light haptic feedback that honours the Haptics setting. Every call is a no-op when the setting is
 * off; the system's own touch-feedback setting still applies on top.
 */
@Stable
internal class Haptics(
    private val feedback: HapticFeedback,
    private val enabled: Boolean,
) {
    /** One detent of a wheel or a stepper. */
    fun tick() = perform(HapticFeedbackType.SegmentFrequentTick)

    /** A switch or toggle button changed to [on]. */
    fun toggle(on: Boolean) = perform(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)

    /** An action completed, such as a copy. */
    fun confirm() = perform(HapticFeedbackType.Confirm)

    private fun perform(type: HapticFeedbackType) {
        if (enabled) feedback.performHapticFeedback(type)
    }
}

/** The [Haptics] for this composition; pass `settings.haptics`. */
@Composable
internal fun rememberHaptics(enabled: Boolean): Haptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback, enabled) { Haptics(feedback, enabled) }
}
