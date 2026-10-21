# I2: iOS widgets (WidgetKit)

Status: planned on 2026-10-20, not started. The previous iOS phase, I1, is complete except the
device-only I1-06 start-up and download-size numbers, which have no recorded result in
`PROGRESS.md`.

Read first: ADR-0004, ADR-0006, ADR-0008, ADR-0010, `docs/product/widgets.md`,
`docs/product/requirements.md` (MR-05, MR-07 AC3, MR-09 AC4-AC5, MR-10), and
`docs/runbooks/manual-test.md`.

Widgets are reviewed as committed reference PNGs and the `ios-screenshots` artefact. WidgetKit
developer mode is unavailable without a Mac, and the simulator cannot show a real Home Screen
widget, so the unit and snapshot tests below cover layout and data; the device rows in the manual
checklist cover placement, refresh and rendering modes.

## Constraints

- Deployment target stays iOS 18.0.
- `systemExtraLargePortrait` is iOS 27.0. The `macos-26` runner has Xcode 26.6, so the extra-large
  W-03 family cannot compile yet. It is deferred to a later phase and is not part of I2.
- The app has no URL scheme or route parser yet. Both are added here because every widget tap and
  every control needs one.
- No new dependency, permission, background mode or network call. I2-01 adds the
  `UIBackgroundModes` and `BGTaskScheduler` check to `ci.sh`; it does not exist yet.
- There is no `ios-beta.yml`, no `DESIGN_SPIKE` memory screen and no rented Mac. The peak-memory
  number is a manual device check; until it is run, `docs/benchmarks.md` records it as not measured.

## Tasks

### I2-01 Shared widget model and timeline (CalendarKit)

Add a small, `Sendable` widget model in `CalendarKit`; the extension target stays a thin WidgetKit
adapter. The model holds only strings and flags computed from `SettingsStore`, `TodayProvider`,
`MonthLayout` and `NepDatePatro`. No view reads the library or disk at render time.

The widgets use `StaticConfiguration` and `TimelineProvider`. `AppIntentTimelineProvider` is for a
configurable widget, and no widget in this phase has a configuration intent; the App Intents here
are the month buttons and the two controls.

The timeline contains the entry for the build instant and then one entry at each of the next seven
local midnights, with `.atEnd`. Midnights are computed with a Gregorian `Calendar` in the chosen
zone by adding one day to the start of the day, never by adding 86,400 seconds.

- AC: a timeline built at 2026-10-20 23:58 in Asia/Kathmandu has its next entry at 2026-10-21
  00:00, then one per day for seven days; its total is eight entries.
  Test: `WidgetTimelineTests.kathmanduMidnights`.
- AC: the same test over the New York spring-forward and fall-back dates and the Sydney DST dates
  keeps each entry at local midnight; the elapsed gaps around the change are 23 and 25 hours, not a
  fixed 86,400 seconds.
  Test: `WidgetTimelineTests.newYorkDST`, `WidgetTimelineTests.sydneyDST`.
- AC: settings `Date follows: Nepal time` uses Asia/Kathmandu at 2024-07-30 18:30 UTC (BS
  2081-04-16), and `This device` in New York uses BS 2081-04-15.
  Test: `WidgetTimelineTests.nepalAndDeviceZones`.
- AC: each entry carries the resolved language and digits, the holiday flag, the next holiday and
  the three upcoming entries at the time of that entry.
  Test: `WidgetTimelineTests.entryContent`.
- AC: no Info.plist contains `UIBackgroundModes` and no source contains `BGTaskScheduler`; the new
  `ci.sh` check fails when either appears, and a negative control puts one back once.
  Test: `scripts/check-background-work.sh`, called from `ci.sh`; the control is recorded on a
  throwaway branch.

### I2-02 W-01 Today and W-02 Today and upcoming

Draw both widgets in `CalendarUI`, with the small/medium sizes and the content from the catalogue.
`W-01` shows the day numeral, month, weekday, AD line or holiday name, the holiday mark, and the
"Nepal" label when the zones differ. `W-02` puts `W-01` on the left and the next three entries on
the right, ending with the coverage footer when fewer than three remain.

Every widget view gets a `widgetURL`/`Link`: the left half to `nepdate://today`, each upcoming row
to `nepdate://day/<yyyy-MM-dd>` in ASCII digits. `AppRoute` owns the URL format and is tested once.

- AC: a holiday shows the first holiday name in place of the AD line and an H1 mark; a normal day
  shows the AD line and no mark.
  Test: `TodayWidgetModelTests.holidayAndNormalDay`.
- AC: the "Nepal" label appears only with `Date follows: Nepal time` and a device zone with a
  different offset.
  Test: `TodayWidgetModelTests.nepalLabel`.
- AC: `W-02` lists exactly the next three holidays or events in date order and then file order, with
  "in N days", and uses the same coverage footer as `UpcomingViewModel` near the end of data.
  Test: `UpcomingWidgetModelTests.threeEntries`, `UpcomingWidgetModelTests.coverageFooter`.
- AC: the tap URLs are `nepdate://today`, `nepdate://day/2083-06-25` and
  `nepdate://day/2084-08-20`; parsing each one back gives the same route.
  Test: `WidgetLinkTests.todayAndDayURLs`, `AppRouteTests.roundTrip`.
- AC: reference images cover `systemSmall` and `systemMedium`, English and Nepali, full colour,
  accented and vibrant, on holiday 2083-06-25; full colour additionally covers normal day
  2083-06-20 in both languages.
  Test: `WidgetSnapshotTests.todayAndUpcoming`; the recording run supplies the PNGs.

### I2-03 W-03 Month with App Intents

Draw the compact month grid from `MonthLayout` at `systemLarge`: BS numerals, AD numerals where
they fit, holiday marks, event dots and the today ring. Previous, next and Today are
`Button(intent:)`; each writes the shown month to the App Group, reloads only the month widget kind,
and records no state that survives the next midnight. Each in-month day is a `Link` to its day URL.

- AC: Jestha 2083, a six-week month, renders without clipping in `systemLarge`.
  Test: `WidgetSnapshotTests.monthLarge`; a layout test also asserts 42 cells and the expected row
  count.
- AC: the previous intent moves the stored month by one, next by one, and Today clears it; a test
  reads the store after each call.
  Test: `MonthWidgetIntentTests.previousNextToday`.
- AC: the first timeline entry after midnight ignores the stored month and shows the current one.
  Test: `MonthWidgetTimelineTests.midnightResetsNavigation`.
- AC: a day link is `nepdate://day/2083-06-25` and opens that day's detail through the same router
  used by W-02.
  Test: `WidgetLinkTests.monthDayURL`.

`systemExtraLargePortrait` and its holiday/event list are deferred until the runner has Xcode 27;
the code is not written against an SDK that cannot compile it. This is recorded in `PROGRESS.md`.

### I2-04 W-04 Lock screen and W-05 Controls

Add `accessoryInline`, `accessoryCircular` and `accessoryRectangular` views with the catalogue's
content, using `widgetAccentable()` on the numeral and holiday mark. Add two
`ControlWidgetButton`s, "Open converter" and "Open today", using SF Symbols only.

Controls write their target route to the App Group and set `openAppWhenRun`; the app consumes the
pending route on activation. They do not open a second in-app picker.

- AC: the inline view reads "असोज २०, मङ्गलवार" in Nepali and "Asoj 20, Tuesday" in English; the
  circular view has the day over the short month and a holiday underline; the rectangular view has
  the date and today's or next holiday with "in N days".
  Test: `LockScreenWidgetModelTests.content` and `WidgetSnapshotTests.lockScreen`.
- AC: the three accessory families render in full colour, accented and vibrant, in both languages,
  on holiday 2083-06-25.
  Test: `WidgetSnapshotTests.lockScreen`; a reference for each matrix cell.
- AC: "Open converter" and "Open today" write `nepdate://converter` and `nepdate://today`, and the
  app router selects Converter and Month-today respectively.
  Test: `WidgetControlTests.routes`.
- AC: both controls appear in the controls gallery and can be placed in Control Center, on the Lock
  Screen and on the Action button.
  Manual: `docs/runbooks/manual-test.md`, W-05 AC2.

### I2-05 Refresh, routing, accessibility and memory

Add the `nepdate` URL scheme to the app's Info.plist and a single `AppRoute` parser/router. The app
handles `onOpenURL` for `today`, `day/<date>` and `converter`; an unknown or malformed URL opens the
Month tab without a sheet. The day route selects Month, selects the date and presents its detail.

Reload widgets with `WidgetCenter.shared.reloadAllTimelines()` after a settings change, when the
app becomes active on a new day, and on `NSSystemTimeZoneDidChange` and
`UIApplication.significantTimeChangeNotification`. Inject the reload call into the settings
callback so a test can count it.

- AC: `nepdate://day/2083-06-25`, `nepdate://today`, `nepdate://converter` parse to the expected
  routes; `nepdate://day/2083-06-32`, `nepdate://day/83-6-25` and `https://example.com` are rejected.
  Test: `AppRouteTests.validAndInvalid`.
- AC: a UI test launches with `NEPDATE_UI_TEST_URL=nepdate://day/2083-06-25` and sees the day sheet
  for Asoj 25; the Converter route lands on Converter.
  Test: `WidgetRoutingUITests.dayAndConverter`.
- AC: a settings change calls the injected reload once; an app activation on a new day calls it
  once; an activation on the same day does not.
  Test: `WidgetRefreshTests.settingsChange`, `WidgetRefreshTests.newDay`.
- AC: every widget has a spoken label for its numeral, holiday and count; the month's buttons are
  labelled Previous, Next and Today.
  Test: `WidgetAccessibilityTests.labels`; VoiceOver on the device remains manual.
- AC: a 7-day W-03 timeline builds under an `XCTMemoryMetric` test and the number is printed each
  run as a regression signal. The device peak with W-01, W-02, W-03 and all W-04 families placed
  and W-03 paged ten times is a manual check; until it is run, `docs/benchmarks.md` says
  "not measured".
  Test: `WidgetTimelinePerformanceTests.memory`; manual row in the checklist.
- AC: the release build logs the widget executable size and the size is compared with the I1
  baseline; a change is explained in the PR.
  Test: `ci.sh` size lines, reviewed in the PR.

## Snapshot matrix and counts

The matrix is sized by distinct pixels, not by an assumed number of modes. Each widget family and
language keeps its full-colour holiday reference and, for W-01 and W-02, a full-colour normal-day
reference. An accented or vibrant rendering is kept only when its PNG differs from the full-colour
twin; otherwise it is collapsed.

`WidgetSnapshotTests.collapsedModesMatch` renders each collapsed mode and compares its PNG data with
the kept twin byte for byte, so a mode is not dropped by assumption. `SNAPSHOT_REFERENCE_COUNT` and
`SNAPSHOT_COUNT` are updated from the recording run, and the final reference count is reported in
the task's PR and at the checkpoint.

At the I2-02 checkpoint the recording run added 8 references, 35 in total. The accented and vibrant
renders of W-01 and W-02 were byte-identical to full colour, so both modes are collapsed. The
screenshot artefact count is 12. W-03 and W-04 add their references in their own tasks.

W-05 controls have no image reference; their routes are unit-tested and the gallery placement is
manual.

## Checkpoint

Stop after I2-05 and the first fully green CI run, before any device-only work. The review needs:

1. The commits, the draft PR link, and the CI link with these lines verbatim:
   `ci: 6/6 gates passed`, the test counts, the final reference and screenshot counts, and both size
   lines.
2. The new snapshot contact sheet or artefact, the final reference count, the collapsed modes, and
   the byte-equality evidence for each.
3. The AC table with one test name per row, and the manual rows that remain open.
4. Confirmation that `systemExtraLargePortrait` is still deferred, that the memory number is
   recorded as not measured, and that no tag, release, signing key, secret or repository setting
   was created.

Do not merge the draft PR. The reviewer decides whether the deferred extra-large family needs its
own phase before I3.

## Out of scope

- Apple Watch complications, Live Activities, configurable widgets, Android widgets and any new
  App Intents beyond the three month actions and two controls.
- App Store submission, TestFlight setup, signing, tags and releases; those stay in I3.
- A second in-app language picker or a new settings screen.
