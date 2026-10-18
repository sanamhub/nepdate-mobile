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
    /** 0123456789. */
    ENGLISH,

    /** ०१२३४५६७८९. */
    NEPALI,
}

/**
 * The persisted preferences from MR-06 and MR-07. The app language is not stored here: Android's
 * per-app locale owns it (ADR-0005).
 */
data class Settings(
    val dateFollows: DateFollows = DateFollows.DEVICE,
    val digits: Digits = Digits.NEPALI,
    val showTithi: Boolean = false,
    val dateFollowsSuggestionHandled: Boolean = false,
)
