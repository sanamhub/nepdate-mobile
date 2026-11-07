package np.nepdate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The Material 3 type scale with emphasized weights for headlines and numerals (design system §3).
 * The system font draws Devanagari; the digits are tabular so the month columns line up.
 */
val NepdateTypography =
    Typography().run {
        copy(
            displayLarge = displayLarge.copy(fontWeight = FontWeight.ExtraBold, fontFeatureSettings = "tnum"),
            displayMedium = displayMedium.copy(fontWeight = FontWeight.ExtraBold, fontFeatureSettings = "tnum"),
            displaySmall = displaySmall.copy(fontWeight = FontWeight.ExtraBold),
            headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Bold),
            headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
            headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
            titleLarge =
                titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                ),
            titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
            labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
            labelMedium = labelMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }

/** The 56 sp numeral on a Today widget (design system §3). */
val HeroTextStyle =
    TextStyle(
        fontSize = 56.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFeatureSettings = "tnum",
    )
