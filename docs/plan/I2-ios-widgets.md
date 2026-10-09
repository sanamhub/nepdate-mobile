# I2: iOS widgets (WidgetKit)

Read first: ADR-0006, ADR-0010, `docs/product/widgets.md`. Watch WWDC26 "WidgetKit foundations" (277)
and WWDC25 "What's new in widgets" (278) before starting.

Widgets are reviewed as snapshot PNGs from the `ios.yml` artefact and on the reference iPhone through
TestFlight (ADR-0010). WidgetKit developer mode is not available: it needs the iPhone paired with
Xcode.

## Tasks

### I2-01 Timeline provider
`AppIntentTimelineProvider` producing entries at each local midnight for 7 days in the chosen zone,
policy `.atEnd`; placeholder and snapshot with a fixed holiday sample. The app calls
`reloadAllTimelines()` on the events listed in ADR-0006 and after every settings change (MR-07 AC3).
AC:
- Swift Testing for entry dates across Asia/Kathmandu, America/New_York (DST) and Australia/Sydney.
- `ci.sh` fails if `UIBackgroundModes` appears in any Info.plist or `BGTaskScheduler` appears in the
  sources (MR-09 AC5).
- Manual checklist: with "Set Automatically" off in Settings, General, Date & Time, move the clock
  to 23:58 and wait; every placed widget shows the new date within 5 minutes after midnight (MR-05
  AC1); change a setting in the app, widgets update within 1 second (MR-07 AC3).

### I2-02 W-01, W-02 and lock-screen W-04
`systemSmall`, `systemMedium`, `accessoryInline`, `accessoryCircular`, `accessoryRectangular`;
`containerBackground(for: .widget)`; `widgetAccentable()` on numeral and holiday mark; checked in
full-colour, accented and vibrant modes.
AC: W-01 AC2–AC3, W-02 AC1–AC2, W-04 AC1 as snapshots per family × mode × language on a holiday
(2083-06-25) and a normal day (MR-05 AC2); a unit test that each tap target's URL opens the right
screen (MR-05 AC3); StandBy and lock-screen placement checked on the reference iPhone (manual
checklist).

### I2-03 W-03 Month with App Intents
`systemLarge` and `systemExtraLargePortrait` (`#available(iOS 27, *)`); `Button(intent:)` for
previous, next, today writing the shown month to the App Group; `Link` per day to
`nepdate://day/<bs-date>` with the date as `YYYY-MM-DD` in ASCII digits (URL scheme declared in
`project.yml`).
AC: W-03 AC1–AC4: snapshots of Jestha 2083 (6 weeks) in `systemLarge` without clipping and in
`systemExtraLargePortrait` with the holiday list; unit tests that the intents move the stored month
by one, "Today" clears it, and the first timeline entry after midnight ignores it; a unit test that
the app routes `nepdate://day/2083-06-25` to that day's detail.

### I2-04 W-05 Controls
`ControlWidgetButton`s "Open converter" and "Open today" (iOS 18+), SF Symbols only.
AC: W-05 AC1 as a unit test on the intents' target routes; W-05 AC2 on the reference iPhone (manual
checklist).

### I2-05 Memory and timing
The simulator can't measure the extension's memory, so it is measured on the device. In builds with
`DESIGN_SPIKE` set (`ios-beta.yml` only, ADR-0010) the extension stores its peak `phys_footprint`
(from `task_info` with `TASK_VM_INFO`) in the App Group after each timeline build, and the debug
screen shows it. CI keeps an `XCTMemoryMetric` test around building the 7-day W-03 timeline as a
regression signal.
AC: on the reference iPhone, with W-01, W-02, W-03 and all W-04 families placed and W-03 paged 10
times, the stored peak is ≤ 15 MB (MR-09 AC4); the number goes in `docs/benchmarks.md`.
