# Manual test checklist

Run this before an Android or iOS release. One row is one acceptance criterion that no automated
test covers. Mark each device **Pass**, **Fail** or **Not run**, and attach the evidence to the
release pull request.

| Column | Device |
|---|---|
| iPhone | iPhone 12, iOS 18 |
| Android A15 class | Samsung Galaxy A15 class, Android 14, 4 GB |
| Android low-end | Android Go class, Android 11, 2 GB |

The iPhone column is not run yet; the iOS agent fills it in its M0 work. Automated tests cover month
layout, view models, scheduling and settings; this file covers the rest.

## MR-01 Month view

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC5: swipe left and right, then use the previous and next buttons. The month changes each time. "Today" shows only on another month. | Not run |  |  |
| AC6: tap the header. The month and year picker opens and reaches BS 1901 and 2199. | Not run |  |  |
| AC7: open a 5-row month and a 6-row month. Neighbouring-month days are dimmed and do not open the day sheet. | Not run |  |  |
| AC8: turn on the tithi line. A conjunct is never cut mid-character in a cell. | Not run |  |  |

## MR-02 Day detail

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC2: "Add to calendar" opens the system event editor, pre-filled all day on that single AD date (not spanning two days), and asks for no permission. Check once with the device calendar set to Buddhist or Japanese. | Not run |  |  |
| AC3: "Copy date" puts the BS long date in the app language on the clipboard. | Not run |  |  |
| AC5: open a holiday with a scope, for example Indra Jatra or Jitiya. The scope text is shown exactly as the data names it. | Not run |  |  |
| AC6: open BS 2084-01-01. No tithi row appears; the holiday flag and events still do. | Not run |  |  |

## MR-03 Upcoming

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC1: the list starts with today and has up to 20 entries in date order, then file order, each with "in N days", "Today" or "Tomorrow". | Not run |  |  |
| AC2: entries are grouped by BS month, and tapping one opens its day detail. | Not run |  |  |
| AC3: near the end of coverage the list ends with "Holiday data ends on <BS date>". Today that is BS 2084-08-20. | Not run |  |  |

## MR-04 Converter

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC1: BS input accepts Nepali and English digits and month names; AD input uses the platform date picker. | Not run |  |  |
| AC4: enter an impossible day, for example Asoj 31, 2083. One line says what is wrong and the last valid result stays. | Not run |  |  |

## MR-05 Widgets

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC1: set the clock one minute past midnight. Every widget shows the new date within the 10-minute inexact alarm window. | Not run |  |  |
| AC1: change the time zone, then the clock. Widgets catch up within 1 minute on Android. | Not run |  |  |
| AC2: a holiday carries colour and the H1 shape in full-colour and dynamic-colour modes. | Not run |  |  |
| AC3: tap each widget. It opens the app on the right day or screen. | Not run |  |  |

## MR-06 Nepal date abroad

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC1: with the device outside Asia/Kathmandu, first launch suggests "Nepal time" once. Accept it; the suggestion does not return. Repeat with dismiss. | Not run |  |  |
| AC2: choose "Nepal time". The month header shows the current Nepal time when the zones differ, and "today" follows Nepal. | Not run |  |  |

## MR-07 Settings

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC2: About shows the app version, library version, holiday coverage, data source, the known-data-questions link, "Report a wrong date", licences, privacy and source. Each link opens the system browser, and the report link opens the wrong-date form. | Not run |  |  |
| AC3: change a setting with a widget on the home screen. The widget updates within 1 second. | Not run |  |  |

## MR-08 Privacy

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC1: the system app info lists no internet permission. The app works with mobile data and Wi-Fi off. | Not run |  |  |
| AC2: Play Data safety says "No data collected" and "No data shared"; the App Store label says "Data Not Collected". | Not run |  |  |
| AC3: a full pass never shows a runtime permission dialog. | Not run |  |  |

## MR-09 Performance and size

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC1: cold start to a drawn grid, median of 10. Limit 400 ms on the A15 class and 1 s on the low-end device. | Not run |  |  |
| AC2: record a 10-swipe trace on the A15 class. No dropped frames. | Not run |  |  |
| AC3: the universal APK is 8 MB or less; the store download is 6 MB or less. | Not run |  |  |
| AC4: the widget process stays under the API 37 RemoteViews cap and uses no bitmaps. | Not run |  |  |
| AC5: `dumpsys alarm` shows one inexact alarm a day, and no other background work runs. | Not run |  |  |

## MR-10 Accessibility

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC1: with TalkBack, a day cell says the BS date, weekday, AD date, public holiday and event name. | Not run |  |  |
| AC2: set font scale 2.0 on a 360 dp wide screen. The month becomes a list, and no text is clipped. | Not run |  |  |
| AC3: recheck the contrast pairs in [the spike results](../product/spike-results.md) on the device, in light and dark. | Not run |  |  |
| AC4: TalkBack and Switch Access reach every control; touch targets are at least 48 dp. | Not run |  |  |

## MR-11 Localisation

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC1: switch to Nepali. Every string is Nepali, with no English fallback, and a native speaker has reviewed the copy. | Not run |  |  |
| AC2: Devanagari uses the system font; no bundled font is installed. | Not run |  |  |

## MR-12 Holidays in the system calendar

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| AC2: "Subscribe to Nepal holidays" opens the share sheet and a calendar app accepts the `holidays.ics` link. The app itself makes no request. | Not run |  |  |

## A2 widget Tier 1 (Android)

The A2-04 checklist. These rows need a device or emulator; issue
[#11](https://github.com/sanamhub/nepdate-mobile/issues/11) tracks the missing hardware.

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| W-01, W-02 and W-03 appear in the picker with their English and Nepali names and descriptions. | Not run | Blocked, issue #11 | Blocked, issue #11 |
| Each widget fills its four edges, uses the system corner radius, and resizes through its breakpoints. | Not run | Blocked, issue #11 | Blocked, issue #11 |
| Generated previews appear on Android 15+; the static `previewLayout` appears on older launchers. | Not run | Blocked, issue #11 | Blocked, issue #11 |
| Dynamic colour follows the wallpaper; the holiday and today marks keep their fixed meaning. | Not run | Blocked, issue #11 | Blocked, issue #11 |
| The configuration activity changes the background to solid, 70 % and transparent, and updates the placed widget. | Not run | Blocked, issue #11 | Blocked, issue #11 |
| W-01 and W-02 are offered on the lock screen; opening either asks for unlock first. | Not run | Blocked, issue #11 | Blocked, issue #11 |
| `adb shell dumpsys alarm` shows one inexact `np.nepdate` alarm, not an exact alarm. | Not run | Blocked, issue #11 | Blocked, issue #11 |
| Without `WAKE_LOCK`, `ACCESS_NETWORK_STATE` and `FOREGROUND_SERVICE` (ADR-0011), placing a widget, changing Digits and passing midnight all update it, and logcat shows no `SecurityException`. | Not run | Blocked, issue #11 | Blocked, issue #11 |
| A widget placed before the app is ever opened gets the midnight alarm (`dumpsys alarm` after placing it). | Not run | Blocked, issue #11 | Blocked, issue #11 |

## W-01 Today

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| At 2×2, the BS numeral, month, weekday and AD date are visible. At 4×1, the content becomes one line in the "असोज २० मङ्गलवार, 6 Oct" style. | Not run |  |  |
| On a holiday, the first holiday name replaces the AD line and the numeral has the H1 mark. | Not run |  |  |
| Tap opens the app on today. | Not run |  |  |
| With "Date follows: Nepal time" and different zones, a small "Nepal" label shows under the numeral. | Not run |  |  |

## W-02 Today and upcoming

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| The left half matches W-01; the right half lists the next three holidays or events with "in N days". | Not run |  |  |
| Tapping an entry opens its day detail; tapping the left half opens today. | Not run |  |  |
| With fewer than three entries left, the right side ends with "Holiday data ends on <BS date>". | Not run |  |  |

## W-03 Month

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| Previous, next and "Today" change the month inside the widget. The next midnight returns it to the current month. | Not run |  |  |
| Tapping a day opens that day's detail. | Not run |  |  |
| A 6-week month renders without clipping at the smallest allowed size (4×4 on Android). | Not run |  |  |
| On iOS `systemExtraLargePortrait`, the month's holidays and events appear under the grid. | Not run |  |  |

## W-04 Lock screen

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| W-01 and W-02 declare keyguard eligibility and are legible on the lock screen. | Not run |  |  |
| An activity opened from the lock screen requires unlock; no `showWhenLocked`. | Not run |  |  |
| The holiday is legible in the vibrant and tinted rendering modes, with no colour-only information. | Not run |  |  |

## W-05 Control (iOS only)

| Check | iPhone | Android A15 class | Android low-end |
|---|---|---|---|
| "Open converter" opens the Converter tab; "Open today" opens Month on today. | Not run | n/a | n/a |
| Both controls appear in the gallery and can be placed in Control Center, the Lock Screen and the Action button. | Not run | n/a | n/a |
