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
  W-03 family cannot compile yet. It is split out below and is not part of the first checkpoint.
- The app has no URL scheme or route parser yet. Both are added here because every widget tap and
  every control needs one.
- No new dependency, permission, background mode or network call. I2-01 adds the
  `UIBackgroundModes` and `BGTaskScheduler` check to `ci.sh`; it does not exist yet.
- `ios-beta.yml` and the `DESIGN_SPIKE` memory screen referenced by ADR-0010 do not exist in this
  repository. I2-05 either adds that path or records memory on a rented Mac; this is a checkpoint
  decision, not an assumption.

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

`systemExtraLargePortrait` and its holiday/event list are a separate task, I2-03b. They are blocked
until the runner has Xcode 27; the code is not written against an SDK that cannot compile it.

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
  run; the device peak is at most 15 MB with W-01, W-02, W-03 and all W-04 families placed and W-03
  paged ten times.
  Test: `WidgetTimelinePerformanceTests.memory`; device row in the manual checklist and
  `docs/benchmarks.md`.
- AC: the release build logs the widget executable size and the size is compared with the I1
  baseline; a change is explained in the PR.
  Test: `ci.sh` size lines, reviewed in the PR.

### I2-03b systemExtraLargePortrait (blocked)

Add `systemExtraLargePortrait` behind `#available(iOS 27, *)` once the runner image has Xcode 27.
It shows the same month plus the month's holidays and events as a list under the grid.

- AC: Jestha 2083 shows the grid and its holiday/event list without clipping.
  Test: `WidgetSnapshotTests.monthExtraLarge`.
- AC: the family appears only on iOS 27 and later; iOS 18-26 builds use the same `systemLarge`
  fallback.
  Test: `WidgetFamilyTests.availability` (compile-time and a runtime availability check).

## Snapshot matrix and counts

The planned references are:

| View | Families | Languages | Modes | Days | References |
|---|---|---|---|---|---|
| W-01 | small | 2 | 3 | holiday | 6 |
| W-01 | small | 2 | 1 | normal | 2 |
| W-02 | medium | 2 | 3 | holiday | 6 |
| W-02 | medium | 2 | 1 | normal | 2 |
| W-03 | large | 2 | 3 | Jestha 2083 | 6 |
| W-04 | inline, circular, rectangular | 2 | 3 | holiday | 18 |
| W-05 | controls | - | - | - | 0 (manual) |

That is 40 new references, 67 in total. The extra-large family would add 6 after Xcode 27. The
numbers and `SNAPSHOT_REFERENCE_COUNT` are updated only after the recording run, and the PR states
the exact count. `SNAPSHOT_COUNT` for the screenshot artefact moves from 6 to 46.

If a rendering mode produces no pixel difference in the host, the plan is to keep one reference and
record the equality in the test, rather than commit duplicate images. That decision is listed in
the checkpoint.

## Checkpoint

Stop after I2-05 and the first fully green CI run, before any device-only work. The review needs:

1. The commits, the draft PR link, and the CI link with these lines verbatim:
   `ci: 6/6 gates passed`, the test counts, `test: 27 reference snapshots` (or the new count),
   `test: 6 screenshots` (or the new count), and both size lines.
2. The new snapshot contact sheet or artefact, with the matrix above and the reason for every
   changed or added reference.
3. The AC table with one test name per row, and the manual rows that remain open.
4. A decision on the three open points: the iOS 27 extra-large family, the missing beta workflow and
   memory screen, and whether duplicate rendering-mode references are needed.
5. Confirmation that no tag, release, signing key, secret or repository setting was created.

Do not merge the draft PR. The reviewer decides whether to keep I2 as one phase or split I2-03b and
the device work into a follow-up.

## Out of scope

- Apple Watch complications, Live Activities, configurable widgets, Android widgets and any new
  App Intents beyond the three month actions and two controls.
- App Store submission, TestFlight setup, signing, tags and releases; those stay in I3.
- A second in-app language picker or a new settings screen.
