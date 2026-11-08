package np.nepdate.data

/** Which zone "today" follows (MR-06). */
enum class DateFollows {
    /** The device's current zone. */
    DEVICE,

    /** Asia/Kathmandu, whatever the device zone is. */
    NEPAL,
}

/** The numerals the app shows (MR-07). */
enum class Digits {
    /** Follow the resolved app language: Nepali for `ne`, English for everything else. */
    FOLLOW_LANGUAGE,

    /** 0123456789. */
    ENGLISH,

    /** ०१२३४५६७८९. */
    NEPALI,
}

/** The look the app uses: the phone's setting, or a fixed light or dark theme. */
enum class Look {
    /** Light or dark with the system setting. */
    SYSTEM,

    /** The light theme, shown as "Light". */
    PAPER,

    /** The Night theme. */
    DARK,
}

/** The tab the app opens on when it starts without a date to show. */
enum class StartTab {
    /** The month view. */
    PATRO,

    /** The festivals and holidays list. */
    FESTIVALS,

    /** The date converter. */
    CONVERTER,
}

/** The two date notifications, each with its own switch. */
enum class DateNotice {
    /** Today's BS day as the status bar icon. */
    STATUS_BAR,

    /** A silent card with the full date in the notifications. */
    CARD,
}

/**
 * The persisted preferences from MR-06 and MR-07. The app language is not stored here: Android's
 * per-app locale owns it (ADR-0005).
 *
 * @property showAdDates whether the month grid shows the small AD day next to each BS day
 * @property haptics whether wheels, toggles and steppers give a light vibration
 * @property startTab the tab a cold start opens on
 * @property statusBarDate whether today's BS day shows as an icon in the status bar
 * @property notificationDate whether a silent card with today's full date stays in the notifications
 */
data class Settings(
    val dateFollows: DateFollows = DateFollows.DEVICE,
    val digits: Digits = Digits.FOLLOW_LANGUAGE,
    val showTithi: Boolean = false,
    val dateFollowsSuggestionHandled: Boolean = false,
    val look: Look = Look.SYSTEM,
    val wallpaperColors: Boolean = false,
    val firstRunDone: Boolean = false,
    val showAdDates: Boolean = true,
    val haptics: Boolean = true,
    val startTab: StartTab = StartTab.PATRO,
    val statusBarDate: Boolean = false,
    val notificationDate: Boolean = false,
)
