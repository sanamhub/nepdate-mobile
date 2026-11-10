package np.nepdate.ui.common

import androidx.compose.ui.text.AnnotatedString
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.ui.converter.ConvertDirection
import np.nepdate.ui.converter.typedDate
import np.nepdate.ui.search.parseJumpDates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateDigitsTest {
    @Test
    fun slashesAppearAsEachPartCompletes() {
        assertEquals("2083", withDateSlashes("2083"))
        assertEquals("2083/0", withDateSlashes("20830"))
        assertEquals("2083/07/2", withDateSlashes("2083072"))
        assertEquals("2083/07/22", withDateSlashes("20830722"))
    }

    @Test
    fun onlyEightDigitsAreKeptAndNepaliDigitsCount() {
        assertEquals("20830722", dateDigits("2083-07-22-99"))
        assertEquals("20830722", dateDigits("२०८३०७२२"))
    }

    @Test
    fun theCursorMapsAcrossTheSlashes() {
        val mapping = DateSlashTransformation.filter(AnnotatedString("20830722")).offsetMapping
        assertEquals(10, mapping.originalToTransformed(8))
        assertEquals(5, mapping.originalToTransformed(4).plus(1))
        assertEquals(8, mapping.transformedToOriginal(10))
        assertEquals(4, mapping.transformedToOriginal(5))
    }

    @Test
    fun typedDigitsBecomeADateInEitherCalendar() {
        assertEquals(NepaliDate.of(2083, 7, 22), typedDate("20830722", ConvertDirection.BS_TO_AD))
        assertEquals(NepaliDate.of(2083, 7, 22), typedDate("20261108", ConvertDirection.AD_TO_BS))
        assertNull(typedDate("20831340", ConvertDirection.BS_TO_AD))
        assertEquals(NepaliDate.of(2083, 7, 22), parseJumpDates("20830722").primary)
    }
}
