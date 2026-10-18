package np.nepdate.ui.month

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.Month
import np.nepdate.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The BS month names live in resources because the product spells three English names differently
 * from the library (Asoj, Asar, Baisakh). Every other name must stay the library's, so the copy
 * cannot drift from it unnoticed.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class MonthNamesTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    @Config(qualifiers = "ne")
    fun nepaliNamesAreTheLibrarys() {
        val names = context.resources.getStringArray(R.array.bs_month_long).toList()

        assertEquals(Month.entries.map { it.displayName(Lang.NEPALI) }, names)
    }

    @Test
    fun englishNamesDifferFromTheLibraryOnlyWhereTheProductSpellsThem() {
        val names = context.resources.getStringArray(R.array.bs_month_long).toList()
        val differences =
            Month.entries
                .filter { names[it.value - 1] != it.displayName(Lang.ENGLISH) }
                .associate { it.displayName(Lang.ENGLISH) to names[it.value - 1] }

        assertEquals(mapOf("Baishakh" to "Baisakh", "Ashad" to "Asar", "Ashoj" to "Asoj"), differences)
    }
}
