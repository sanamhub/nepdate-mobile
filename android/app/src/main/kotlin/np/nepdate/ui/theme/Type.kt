package np.nepdate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The type scale from design system §3. The system font draws Devanagari; the digits are tabular
 * so the month columns line up.
 */
val NepdateTypography =
    Typography().run {
        copy(
            titleLarge =
                titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                ),
            headlineMedium = headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }

/** The 56 sp numeral on a Today widget (design system §3). */
val HeroTextStyle =
    TextStyle(
        fontSize = 56.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum",
    )
