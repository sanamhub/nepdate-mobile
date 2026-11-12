package np.nepdate.tile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import np.nepdate.MainActivity
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.notify.dayIconRes
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.common.weekdayName
import np.nepdate.widget.appContainer

/** The two lines a Quick Settings tile can show. */
internal data class TodayTileText(
    val label: String,
    val subtitle: String,
    val description: String,
)

/**
 * The Quick Settings tile: today's Bikram Sambat date, read from the same settings as the app and
 * the widgets. Tapping it opens the app on today. The tile is not a switch, so it is always drawn
 * as inactive; Android does not let an app add or remove a tile by itself.
 */
class TodayTileService : TileService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onStartListening() {
        super.onStartListening()
        refresh()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        refresh()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun refresh() {
        scope.launch {
            val container = appContainer(applicationContext)
            val settings = container.settingsRepository.settings.first()
            val lang = languageFor(resources.configuration.locales[0])
            val digits = settings.digits.resolve(lang)
            val today = container.todayProvider.today(settings.dateFollows)
            val months = resources.getStringArray(R.array.bs_month_long).toList()
            val text = todayTileText(today, digits, months, lang)
            val dayIcon = dayIconRes(applicationContext, today.dayOfMonth, nepaliDigits = digits == Digits.NEPALI)
            qsTile?.apply {
                state = Tile.STATE_INACTIVE
                // The date is the tile's label, so it reads at a glance like the status bar day.
                label = text.label
                contentDescription = text.description
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) subtitle = text.subtitle
                icon = Icon.createWithResource(this@TodayTileService, dayIcon)
                updateTile()
            }
        }
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        val intent =
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pending =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}

/**
 * The tile's text: the label `Asoj 20` (`असोज २०`), the subtitle `Tuesday, 2083`, and the full
 * date for TalkBack. A tile label is cut after a dozen characters, so the year moves to the
 * subtitle. Kept pure so it can be tested without the Android tile host.
 */
internal fun todayTileText(
    today: NepaliDate,
    digits: Digits,
    monthNames: List<String>,
    lang: Lang,
): TodayTileText {
    val month = monthNames.getOrElse(today.monthValue - 1) { "" }
    val day = today.dayOfMonth.toString().inDigits(digits)
    val year = today.year.toString().inDigits(digits)
    val weekday = weekdayName(today, lang)
    return TodayTileText(
        label = "$month $day",
        subtitle = "$weekday, $year",
        description = "$weekday, $month $day, $year",
    )
}
