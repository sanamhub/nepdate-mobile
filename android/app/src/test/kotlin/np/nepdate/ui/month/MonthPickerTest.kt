package np.nepdate.ui.month

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.Lang
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
class MonthPickerTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun opens() {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                MonthYearPicker(
                    initialYear = 2083,
                    initialMonth = 6,
                    lang = Lang.ENGLISH,
                    onDismiss = {},
                    onConfirm = { _, _ -> },
                )
            }
        }
        composeRule.onNodeWithText("Choose month and year").assertExists()
        composeRule.onNodeWithTag("month_picker_year").assertExists()
    }
}
