package np.nepdate.ui.settings

import np.nepdate.data.DateFollows
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Test

/** A1-06 and ADR-0007 §5: the diagnostic text has the app, OS and settings and nothing else. */
class DiagnosticTest {
    private val labels =
        DiagnosticLabels(
            app = "App",
            android = "Android",
            dateFollows = "Date follows",
            digits = "Digits",
            tithi = "Show tithi",
            on = "on",
            off = "off",
            device = "This device",
            nepal = "Nepal time",
            followLanguage = "Follow language",
            nepaliDigits = "Nepali",
            englishDigits = "English",
        )

    @Test
    fun textHasTheVersionOsAndSettingsOnly() {
        val text =
            diagnosticText(
                appVersion = "0.0.1 (1)",
                osVersion = "14",
                settings =
                    Settings(
                        dateFollows = DateFollows.NEPAL,
                        digits = Digits.ENGLISH,
                        showTithi = true,
                    ),
                labels = labels,
            )

        assertEquals(
            "App: 0.0.1 (1)\n" +
                "Android: 14\n" +
                "Date follows: Nepal time\n" +
                "Digits: English\n" +
                "Show tithi: on",
            text,
        )
    }

    @Test
    fun defaultsAreFollowLanguageAndTithiOff() {
        val text = diagnosticText("0.0.1 (1)", "14", Settings(), labels)

        assertEquals(
            "App: 0.0.1 (1)\n" +
                "Android: 14\n" +
                "Date follows: This device\n" +
                "Digits: Follow language\n" +
                "Show tithi: off",
            text,
        )
    }
}
