package np.nepdate.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.day.DayDetailContent
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A1-04 screenshots; a human checks them, then the device. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class DayDetailScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun light() {
        capture(
            name = "a1-day-detail-light",
            date = NepaliDate.of(2083, 6, 9),
            settings = Settings(digits = Digits.ENGLISH),
        )
    }

    @Test
    fun dark() {
        capture(
            name = "a1-day-detail-dark",
            date = NepaliDate.of(2083, 6, 9),
            settings = Settings(digits = Digits.ENGLISH),
            darkTheme = true,
        )
    }

    @Test
    @Config(qualifiers = "ne-w360dp-h800dp-xhdpi")
    fun nepali() {
        capture(
            name = "a1-day-detail-ne",
            date = NepaliDate.of(2083, 6, 9),
            settings = Settings(digits = Digits.NEPALI),
        )
    }

    @Test
    fun noTithi() {
        capture(
            name = "a1-day-detail-no-tithi",
            date = NepaliDate.of(2084, 1, 1),
            settings = Settings(digits = Digits.ENGLISH),
        )
    }

    @Test
    fun outsideCoverage() {
        capture(
            name = "a1-day-detail-outside-coverage",
            date = NepaliDate.of(2090, 1, 1),
            settings = Settings(digits = Digits.ENGLISH),
        )
    }

    private fun capture(
        name: String,
        date: NepaliDate,
        settings: Settings,
        darkTheme: Boolean = false,
    ) {
        composeRule.setContent {
            NepdateTheme(darkTheme = darkTheme, dynamicColor = false) {
                ScreenshotFrame {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Column(Modifier.fillMaxSize()) {
                            SheetHandle()
                            DayDetailContent(date = date, settings = settings, onClose = {})
                        }
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage("$name.png")
    }
}

@Composable
private fun SheetHandle() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f), CircleShape),
        )
    }
}
