package np.nepdate.ui.common

import android.provider.Settings
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Whether the user turned animations off (Developer options or Accessibility "Remove animations"
 * set the animator duration scale to 0). Compose already snaps animations to their end then; this
 * lets a screen also skip motion it starts itself, such as a page scroll it could jump instead.
 */
@Composable
internal fun rememberReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/**
 * The room the floating navigation bar takes at the bottom of a tab, including the gesture bar.
 * Tabs add it to the bottom of their scrolling content so the last row can scroll clear of the bar
 * while the content still draws behind it. Outside the tabs it is zero.
 */
internal val LocalBottomBarPadding = compositionLocalOf { PaddingValues(0.dp) }
