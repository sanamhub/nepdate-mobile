package np.nepdate.ui.common

import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The back gesture seeks the transition, finishes it on release and undoes it on cancel. */
@RunWith(AndroidJUnit4::class)
class PredictiveBackTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var screen by mutableStateOf(DETAIL)

    private fun showScreens() {
        composeRule.setContent {
            val transition =
                rememberPredictiveBackTransition(
                    target = screen,
                    backTarget = if (screen == DETAIL) HOME else null,
                    onBack = { screen = HOME },
                    label = "test",
                )
            transition.AnimatedContent { shown -> Text(shown) }
        }
        composeRule.mainClock.autoAdvance = false
    }

    @Test
    fun dragShowsTheScreenBehindAndReleaseGoesBack() {
        showScreens()
        val dispatcher = composeRule.activity.onBackPressedDispatcher

        composeRule.runOnUiThread {
            dispatcher.dispatchOnBackStarted(backEvent(0f))
            dispatcher.dispatchOnBackProgressed(backEvent(HALF))
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onNodeWithText(HOME).assertExists()
        composeRule.onNodeWithText(DETAIL).assertExists()

        composeRule.runOnUiThread { dispatcher.onBackPressed() }
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        assertEquals(HOME, screen)
        composeRule.onNodeWithText(DETAIL).assertDoesNotExist()
    }

    @Test
    fun cancellingTheDragStaysOnTheScreen() {
        showScreens()
        val dispatcher = composeRule.activity.onBackPressedDispatcher

        composeRule.runOnUiThread {
            dispatcher.dispatchOnBackStarted(backEvent(0f))
            dispatcher.dispatchOnBackProgressed(backEvent(HALF))
            dispatcher.dispatchOnBackCancelled()
        }
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        assertEquals(DETAIL, screen)
        composeRule.onNodeWithText(DETAIL).assertExists()
        composeRule.onNodeWithText(HOME).assertDoesNotExist()
    }

    private fun backEvent(progress: Float) = BackEventCompat(0f, 0f, progress, BackEventCompat.EDGE_LEFT)

    private companion object {
        const val HOME = "home"
        const val DETAIL = "detail"
        const val HALF = 0.5f
    }
}
