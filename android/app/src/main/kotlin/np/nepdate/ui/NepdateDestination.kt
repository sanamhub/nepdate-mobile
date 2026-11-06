package np.nepdate.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import np.nepdate.R

/** The three top-level destinations in the short navigation bar. Settings, Search and Year are top-bar destinations. */
internal enum class NepdateDestination(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int,
) {
    MONTH(R.string.nav_month, R.drawable.ic_sym_calendar_month, R.drawable.ic_sym_calendar_month_filled),
    UPCOMING(R.string.nav_upcoming, R.drawable.ic_sym_temple_hindu, R.drawable.ic_sym_temple_hindu_filled),
    CONVERTER(R.string.nav_converter, R.drawable.ic_sym_swap_horiz, R.drawable.ic_sym_swap_horiz),
}
