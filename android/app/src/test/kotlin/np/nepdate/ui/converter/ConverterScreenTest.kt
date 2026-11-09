package np.nepdate.ui.converter

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.month.PICKER_FIRST_YEAR
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** A1-05 and MR-04 AC1 to AC4 in the Converter screen. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class ConverterScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = NepaliDate.of(2083, 6, 20)

    @Test
    fun bsWheelShowsTheAdDateAndWeekday() {
        setUp(initialBs = "2083-06-20")

        composeRule.onNodeWithTag("converter_result").assertTextEquals("6 October 2026")
        composeRule.onNodeWithTag("converter_result_detail").assertTextEquals("Tuesday, today")
    }

    @Test
    fun adToBsShowsTheBsDate() {
        setUp(initialBs = "2083-06-20")

        composeRule.onNodeWithTag("converter_ad_to_bs").performClick()

        composeRule.onNodeWithTag("converter_result").assertTextEquals("Asoj 20, 2083")
    }

    @Test
    fun turningTheDayWheelUpdatesTheResultLive() {
        setUp(initialBs = "2083-06-20")

        composeRule
            .onNodeWithTag(
                "converter_wheel_day",
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(24f) }

        composeRule.onNodeWithTag("converter_result").assertTextEquals("11 October 2026")
        composeRule.onNodeWithTag("converter_result_detail").assertTextEquals("Sunday, 5 days from today")
    }

    @Test
    fun twoDatesShowTheDifference() {
        val state = ConverterState().fromBs("2081-01-01", today).compareWithBs("2082-01-01")
        setUp(initialState = state)

        composeRule.onNodeWithTag("converter_comparison").assertTextEquals("1 year, 0 months, 0 days")
    }

    @Test
    fun pickingTheSecondDateInTheDialogUpdatesTheDifference() {
        val state = ConverterState().fromBs("2081-01-01", today).compareWithBs("2082-01-01")
        setUp(initialState = state)

        composeRule.onNodeWithTag("converter_to").performClick()
        composeRule
            .onNode(hasTestTag("converter_wheel_year") and hasAnyAncestor(isDialog()))
            .performSemanticsAction(SemanticsActions.SetProgress) { it((2083 - PICKER_FIRST_YEAR).toFloat()) }
        composeRule.onNodeWithTag("converter_pick_done").performClick()

        composeRule.onNodeWithTag("converter_comparison").assertTextEquals("2 years, 0 months, 0 days")
    }

    private fun setUp(
        initialBs: String = "",
        initialState: ConverterState = ConverterState(),
    ) {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                ConverterScreen(
                    today = today,
                    settings = Settings(digits = Digits.ENGLISH),
                    initialBs = initialBs,
                    initialState = initialState,
                )
            }
        }
    }
}
