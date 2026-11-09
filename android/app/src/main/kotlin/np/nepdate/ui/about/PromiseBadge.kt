package np.nepdate.ui.about

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import np.nepdate.R

/** One promise the app makes: its symbol and its short label. */
internal data class PromiseBadge(
    @param:DrawableRes val icon: Int,
    @param:StringRes val label: Int,
)

/** What the app promises, shown in About and on the first-run screen. */
internal val APP_PROMISES =
    listOf(
        PromiseBadge(R.drawable.ic_sym_wifi_off_filled, R.string.about_offline),
        PromiseBadge(R.drawable.ic_sym_block_filled, R.string.about_no_ads),
        PromiseBadge(R.drawable.ic_sym_shield_lock_filled, R.string.about_no_data),
        PromiseBadge(R.drawable.ic_sym_code, R.string.about_open_source),
        PromiseBadge(R.drawable.ic_sym_bolt_filled, R.string.about_fast),
        PromiseBadge(R.drawable.ic_sym_translate, R.string.about_bilingual),
    )
