package np.nepdate.ui.month

import org.junit.Assert.assertEquals
import org.junit.Test

class TithiBreaksTest {
    @Test
    fun longLatinNamesBreakAtTheirJoins() {
        assertEquals("Eka${SHY}dashi", withTithiBreaks("Ekadashi"))
        assertEquals("Chatur${SHY}dashi", withTithiBreaks("Chaturdashi"))
        assertEquals("Chatur${SHY}thi", withTithiBreaks("Chaturthi"))
        assertEquals("Shash${SHY}thi", withTithiBreaks("Shashthi"))
        assertEquals("Da${SHY}shami", withTithiBreaks("Dashami"))
    }

    @Test
    fun shortAndDevanagariNamesStayAsTheyAre() {
        assertEquals("Navami", withTithiBreaks("Navami"))
        assertEquals("एकादशी", withTithiBreaks("एकादशी"))
    }

    private companion object {
        val SHY = Char(0xAD).toString()
    }
}
