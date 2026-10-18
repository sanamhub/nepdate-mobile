package np.nepdate.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.vector.ImageVector
import np.nepdate.R

/** The three top-level destinations in the design-system bottom bar. */
internal enum class NepdateDestination(
    @StringRes val label: Int,
    val icon: ImageVector,
) {
    MONTH(R.string.nav_month, Icons.Filled.Home),
    UPCOMING(R.string.nav_upcoming, Icons.Filled.Notifications),
    CONVERTER(R.string.nav_converter, Icons.Filled.Refresh),
}
