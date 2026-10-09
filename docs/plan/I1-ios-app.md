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
AC: the same cases as A1-05 and A1-06, as Swift Testing on the view models plus snapshots, except
that the language row opens the system per-app language setting; `ci.sh` fails if any key in
`Localizable.xcstrings` lacks a translated `ne` value (MR-11 AC1).

### I1-06 Start-up and size
- CI: an `XCTApplicationLaunchMetric` UI test on the simulator, logged per run as a regression signal.
- Device (MR-09 AC1): on the reference iPhone, record the screen at 60 fps for 10 cold starts of the
  TestFlight build (app removed from the app switcher first) and count frames from the tap to the
  drawn grid; the median must be ≤ 24 frames (400 ms).
- Size (MR-09 AC3): the download size for the reference iPhone from App Store Connect, TestFlight,
  build, "App File Sizes".
AC: `docs/benchmarks.md` has the three numbers with the date and the build number; each meets its
target or has an open issue. Use Instruments on a rented Mac only if a target is missed (ADR-0010).
