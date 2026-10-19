# I1: iOS app screens

Read first: ADR-0003, ADR-0004, ADR-0010, `docs/product/requirements.md`,
`docs/product/design-system.md`.
Precondition: `nepdate-swift` through S4, products `NepDate` and `NepDatePatro` (released, or as a
local package, ADR-0003 §6).

There is no Mac, simulator or SwiftUI preview on the maintainer's PC (ADR-0010). Every check below
is a test in `ios/scripts/ci.sh`, a snapshot PNG from the `ios.yml` artefact, or a manual-checklist
line run on the TestFlight build.

## Tasks

### I1-01 Settings store and today (`CalendarKit`)
App Group `UserDefaults` wrapper for MR-06 and MR-07; `TodayProvider` from an injected clock and the
chosen zone.
AC: Swift Testing, same cases as A1-01: with "Nepal time" and 2024-07-30 18:30 UTC, today is BS
2081-04-16; with "This device" in `America/New_York`, BS 2081-04-15; a fresh store reads "This
device".

### I1-02 Month layout (`CalendarKit.MonthLayout`)
Same contract and test cases as A1-02, parameterised `@Test(arguments:)`.

### I1-03 Month screen (MR-01)
`TabView` (Month, Upcoming, Converter), month grid in `CalendarUI` (shared with widgets), paging with
`ScrollView` + `scrollTargetBehavior(.paging)`, month picker sheet, today button, VoiceOver labels,
list layout at accessibility text sizes.
AC:
- Snapshots of Asoj 2083 with today = Asoj 20: light, dark, Nepali, `.xxxLarge`, `.accessibility2`
  (MR-01 AC1–AC4, AC7, AC8 with tithi on and off; MR-10 AC2 per its test line).
- Swift Testing on the month view model: next/previous change the month and `showsTodayButton` is
  true only off the current month (MR-01 AC5); the picker range is BS 1901 to 2199 (MR-01 AC6);
  dimmed cells are not selectable (MR-01 AC7); the accessibility label of Asoj 25 2083 equals the
  MR-10 AC1 string.
- A UI test runs `performAccessibilityAudit()` on the month screen with no issues (MR-10 AC4, touch
  targets ≥ 44 pt); VoiceOver and Switch Control reach every control (manual checklist).

### I1-04 Day detail (MR-02)
Sheet with `presentationDetents`; "Add to calendar" with `EKEventEditViewController` (no permission
prompt, iOS 17+); "Copy date".
AC: MR-02 AC1–AC6 as snapshots and view-model tests on BS 2083-06-09, 2084-01-01 and 2090-01-01 (as
A1-04); a unit test checks the `EKEvent` passed to the editor (all-day, title, start date); the
manual checklist confirms no permission prompt appears.

### I1-05 Upcoming, Converter, Settings and About
MR-03, MR-04, MR-06, MR-07, MR-11, MR-12 (`webcal://` link opened with `openURL`, hidden while the H4
URL constant is empty), and "Copy diagnostic info" (ADR-0007 §5). The library version shown in About
is the Info.plist key `NepDateLibraryVersion`, set in `project.yml`; `ci.sh` fails if it differs from
the `exact:` pin of `nepdate-swift` in `ios/Packages/CalendarKit/Package.swift`.

Acceptance criteria, copied before code (I1-05-00), each mirroring an A1-05 or A1-06 test:

- I1-05-01 Upcoming: with today BS 2083-06-20 the view model returns 20 entries, the first two are
  "Indira Ekadashi Brata" and "Ekadashi Shraddha" on 2083-06-20 with 0 days, and the list is grouped
  by BS month in date and file order (A1-05 `firstTwentyEntriesStartWithTodaysEvents`,
  `entriesAreGroupedByBsMonthInDateOrder`).
- I1-05-01 Upcoming: the 2083-06-25 entry is "Ghatasthapana Brata", is a holiday and is 5 days away
  (A1-05 `publicHolidayEntriesCarryTheMark`).
- I1-05-01 Upcoming: with today BS 2084-08-10 the list has 3 entries, ends with "Holiday data ends on
  Mangsir 20, 2084", and their holiday flags are false, true, false (A1-05
  `nearTheEndTheListStopsAtCoverageWithThreeEntries`).
- I1-05-01 Converter: "२०८३ असोज २०" and "2083-06-20" both give 2026-10-06, Tuesday, 0 days from
  today; 2026-10-06 gives BS 2083-06-20 (A1-05 `nepaliBsInputConvertsToAd`,
  `numericBsInputConvertsToAd`, `adInputConvertsToBs`).
- I1-05-01 Converter: "2083-06-32" shows "Asoj has 31 days in 2083" and keeps the previous result;
  "2083-13-01" reports an invalid month (A1-05 `invalidDayKeepsThePreviousResult`,
  `invalidMonthIsReported`, A1-05 screen test `invalidDayShowsTheMessageAndKeepsThePreviousResult`).
- I1-05-01 Converter: 2081-01-01 compared with 2082-01-01 is 1 year, 0 months, 0 days; comparing
  without a first date is an error (A1-05 `secondDateGivesYearsMonthsAndDays`,
  `comparingWithoutAFirstDateIsAnError`).
- I1-05-01 Digits: counts in Upcoming and Converter use the resolved Digits, including "5 days",
  "1 year", the invalid-day message and "in N days"; the Devanagari day-detail line still keeps
  Devanagari digits (A1-05 `PluralDigitsTest`).
- I1-05-01 screens: Upcoming and Converter each have light, dark, Nepali and `.accessibility2`
  reference snapshots; tapping an Upcoming entry opens that day's detail (A1-05
  `UpcomingScreenshotTest`, `ConverterScreenshotTest`, `UpcomingScreenTest.tappingAnEntryOpensItsDayDetail`).
- I1-05-02 Settings: Language, Digits, Date follows and Show tithi write the `SettingsStore` and
  survive a reload; Digits has Follow language, Nepali and English values; there is no theme switch
  (A1-06 `choicesWriteTheSettings`, Store tests).
- I1-05-02 Settings: the Language row opens the system per-app language setting through
  `UIApplication.openSettingsURLString` (A1-06 `LanguageSection`, adapted for iOS).
- I1-05-02 Settings: "Date follows" offers This device and Nepal time; the Nepal time suggestion is
  shown once per the stored flag (MR-06 AC1; A1-06 `NepalTimeSuggestionTest`).
- I1-05-03 About: app version, `NepDateLibraryVersion`, holiday coverage Baisakh 1 2077 to Mangsir
  20 2084, data source, official sources and the five links are shown; `ci.sh` fails when
  `NepDateLibraryVersion` differs from the nepdate-swift exact pin (A1-06
  `aboutShowsVersionsCoverageAndLinks`, `BuildConfigTest`, new iOS gate).
- I1-05-03 About: the link set is exactly the five Android `AboutLinks` URLs, each on
  `sanamhub/nepdate-mobile` or its issue form; the known-data link is
  `docs/product/known-data-questions.md`, not the unpublished hub (A1-06 `AboutLinksTest`).
- I1-05-03 About: "Subscribe to Nepal holidays" is hidden while `HOLIDAYS_ICS_URL` is empty and
  calls `openURL` with the `webcal://` URL when an injected constant is set (A1-06
  `HolidayShareTest`, `SettingsScreenTest`).
- I1-05-03 About: "Copy diagnostic info" copies exactly App, iOS, Date follows, Digits and Show
  tithi, with no personal data, and shows a visible "Copied" confirmation (A1-06 `DiagnosticTest`,
  `copyDiagnosticInfoUsesTheBuilder`).
- I1-05-04 Localisation: `ci.sh` fails if any key in `Localizable.xcstrings` lacks a translated
  `ne` value; a negative control empties one key (A1-06 MR-11 AC1, new iOS gate).
- I1-05-05 Copy feedback: tapping Copy date on the day sheet shows a visible, non-blocking "Date
  copied" line and posts one VoiceOver announcement; a snapshot and UI test cover it (carried from
  the I1-04 review).
- Accessibility: Upcoming, Converter, Settings and About each run the seven audit categories with
  an exact table starting empty, and one pass at `.accessibility2`; any tolerance needs its reason
  in the test and `ios/README.md` (MR-10 AC4).

The language row opens the system per-app language setting; the other cases are the same as A1-05
and A1-06, as Swift Testing on the view models plus the snapshots above.

### I1-06 Start-up and size
- CI: an `XCTApplicationLaunchMetric` UI test on the simulator, logged per run as a regression signal.
- Device (MR-09 AC1): on the reference iPhone, record the screen at 60 fps for 10 cold starts of the
  TestFlight build (app removed from the app switcher first) and count frames from the tap to the
  drawn grid; the median must be ≤ 24 frames (400 ms).
- Size (MR-09 AC3): the download size for the reference iPhone from App Store Connect, TestFlight,
  build, "App File Sizes".
AC: `docs/benchmarks.md` has the three numbers with the date and the build number; each meets its
target or has an open issue. Use Instruments on a rented Mac only if a target is missed (ADR-0010).
