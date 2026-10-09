# A1: Android app screens

Read first: ADR-0003, ADR-0005, `docs/product/requirements.md`, `docs/product/design-system.md`.
Precondition: `nepdate-kotlin` through K4, both `io.github.sanamhub:nepdate` and
`io.github.sanamhub:nepdate-patro` (released, or as an included build, ADR-0003 §6).

## Tasks

### A1-01 Settings store (`data/SettingsRepository`)
Preferences DataStore with keys for MR-06 and MR-07 (language is handled by `LocaleManager`, not
stored). Exposes `Flow<Settings>`. A `TodayProvider` returns today's BS date for the chosen zone from an
injected `Clock`.
AC: unit tests: with "Nepal time" and a clock at 2024-07-30 18:30 UTC, today is BS 2081-04-16; with
"This device" in `America/New_York` and the same clock, today is BS 2081-04-15; a fresh install
reads "This device" (MR-06 AC1).

### A1-02 Month layout (`ui.month.MonthLayout`)
Pure function: (BS year, month, settings, today) → 35 or 42 cells with BS day, AD day, Saturday,
holiday, event, today, in-month flags, using `nepdate` and `nepdate-patro`.
AC: MR-01 AC1, AC3, AC7 as unit tests for:
- Asoj 2083: starts Thursday 17 Sep 2026, 35 cells, holidays on the 3rd, 9th, 18th, 25th and 31st;
- Asar 2083: 32 days, starts Monday 15 Jun 2026, 35 cells;
- Jestha 2083: starts Friday 15 May 2026, 42 cells;
- Baisakh 2084: day 1 has the holiday flag and no tithi (MR-02 AC6).

### A1-03 Month screen (MR-01)
`HorizontalPager` of months, header with month picker, today button, grid per design-system §4,
spoken labels (MR-10 AC1), list layout at large font scales (MR-10 AC2). Settings opens from the top
app bar (design-system §4).
AC:
- Roborazzi screenshots of Asoj 2083 with today = Asoj 20: light, dark, Nepali, font scale 2.0
  (MR-01 AC1–AC4, AC7, AC8 with tithi on and off; MR-10 AC2 per its test line).
- Compose UI tests: swipe and next/previous change the month and the "Today" button appears only
  off the current month (MR-01 AC5); the picker offers BS 1901 to 2199 and jumps to the chosen
  month (MR-01 AC6); a dimmed cell has no click action (MR-01 AC7); the content description of Asoj
  25 2083 equals the MR-10 AC1 string.
- Every clickable element is at least 48 dp (a Compose UI test with `assertTouchHeightIsAtLeast`);
  TalkBack and Switch Access reach every control (manual checklist, MR-10 AC4).

### A1-04 Day detail (MR-02)
Modal bottom sheet; "Add to calendar" via `Intent.ACTION_INSERT` with `Events.CONTENT_URI`, all-day,
title = holiday or event name; "Copy date".
AC: MR-02 AC1–AC6 as Compose UI tests on BS 2083-06-09 (holiday with scope), 2084-01-01 (no tithi)
and 2090-01-01 (outside coverage); an instrumented test checks the insert intent extras
(`EXTRA_EVENT_ALL_DAY = true`, `TITLE`, `EXTRA_EVENT_BEGIN_TIME` at local midnight of the AD date).

### A1-05 Upcoming (MR-03) and Converter (MR-04)
Converter errors use the library's parse error kinds mapped to the copy in design-system §6.
AC: unit tests on the view models:
- Upcoming with today = BS 2083-06-20: 20 entries, the first two are today's events (Indira
  Ekadashi Brata, Ekadashi Shraddha) labelled "Today", grouped by BS month; with today = BS
  2084-08-10 the list has 3 entries and ends with "Holiday data ends on" and BS 2084-08-20
  (MR-03 AC1, AC3). A Compose UI test taps an entry and sees its day
  detail (MR-03 AC2).
- Converter: "२०८३ असोज २०" and "2083-06-20" both give 2026-10-06, Tuesday; 2026-10-06 gives BS
  2083-06-20; "2083-06-32" shows "Asoj has 31 days in 2083" and keeps the previous result;
  2081-01-01 to 2082-01-01 is 1 year, 0 months, 0 days (MR-04 AC1–AC4).

### A1-06 Settings and About (MR-06, MR-07, MR-11, MR-12)
Includes the first-launch "Nepal time" suggestion, the per-app language picker (Android 13+), the
"Subscribe to Nepal holidays" share (hidden while the H4 URL constant is empty) and "Copy diagnostic
info" (ADR-0007 §5). The library version shown in About comes from `BuildConfig.NEPDATE_VERSION`,
set in Gradle from the `nepdate` entry of `libs.versions.toml`.
AC:
- MR-06 AC1: with the device zone `Asia/Kathmandu` no suggestion shows; with `Europe/London` it shows
  once, and not again after "Not now" (Compose UI test).
- MR-06 AC2: with "Nepal time" and the device in `Europe/London`, the month header shows Nepal time;
  with "This device" it doesn't (screenshot).
- MR-07 AC1: each setting changes the month screen (one screenshot per setting); the language row is
  absent below API 33.
- MR-07 AC2: About shows the version, `BuildConfig.NEPDATE_VERSION`, coverage first and last date,
  and the links listed in the requirement (screenshot; a unit test checks the two URLs).
- MR-11 AC1: Lint `MissingTranslation` is an error, so a string without `values-ne` fails
  `./gradlew ci`.
- MR-12 AC2: with the URL constant empty the row is absent; with it set, tapping sends
  `Intent.ACTION_SEND` with the URL as `EXTRA_TEXT` (instrumented test).
- "Copy diagnostic info" puts app version, OS version and settings on the clipboard, and nothing else
  (unit test on the text builder).

### A1-07 Start-up and size
Baseline Profile for start-up and month swipe; the app calls `reportFullyDrawn()` when the first month
grid is drawn. Measure with Macrobenchmark (`StartupTimingMetric`, cold, 10 iterations, median of
`timeToFullDisplayMs`; `FrameTimingMetric` over 10 swipes) on the reference devices, and the
universal release APK size. Record the numbers in `docs/benchmarks.md`. If a target is missed, report
it with a trace; don't add complexity speculatively.
AC: `docs/benchmarks.md` has MR-09 AC1, AC2 and AC3 (APK size; Play download size after A3-02) per
device with the date and the commit; each meets its target or has an open issue.
