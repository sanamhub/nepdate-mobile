# ios

The iPhone and iPad app and the Today widget extension. The Xcode project is generated from
`project.yml` by XcodeGen and never committed (ADR-0010).

| Path | What it is |
|---|---|
| `project.yml` | XcodeGen spec: targets, packages, App Group, Info.plist and entitlements paths |
| `NepDateApp/` | The thin `@main` SwiftUI app and its privacy manifest |
| `NepDateWidgets/` | The widget extension: `@main` bundle, the placeholder Today widget, privacy manifest |
| `Packages/CalendarKit` | Local SwiftPM package: dates, month layout, widget summaries, settings (I1) |
| `Packages/CalendarUI` | Local SwiftPM package: product `CalendarUI` (views and colours shared by app and widget) and product `CalendarScreens` (the app-only month, day, Upcoming, Converter, Settings and About screens and their view models) |
| `NepDateTests/` | Swift Testing tests and the committed snapshot references; renders the screenshot artefact |
| `NepDateUITests/` | XCUITests: opening on today, the picker at both range ends, the pager recentre, the month, day and I1-05 accessibility audits, Copy date and diagnostic copy |
| `Resources/` | `Localizable.xcstrings` (English and Nepali) |
| `Config/` | Info.plists and the App Group entitlements for both targets |
| `scripts/ci.sh` | The gates of ADR-0008 |

## Generating and testing

```sh
cd ios
./scripts/ci.sh
open NepDate.xcodeproj        # after ci.sh, on a Mac
```

`ci.sh` downloads XcodeGen 2.46.0, checks its SHA-256, generates the project and runs six gates:
xcodegen, format, build, test, coverage, network. It ends with `ci: 6/6 gates passed`. The test gate
runs the Swift Testing suites and the XCUITest, renders the month screen and the widget to
`build/snapshots`, and compares the twenty-seven reference snapshots. `ios.yml` uploads the screenshots and
the references as artefacts.

The coverage gate reads the test result bundle with `xcrun xccov` and fails below 80 % line
coverage of `CalendarKit` (ADR-0008). On Xcode 26.6 `xccov` reports the local package target with no
source lines (run 39103877148), so when that happens the gate reads the run's profile with
`llvm-cov` against the `CalendarKit` framework instead, filtered to `Packages/CalendarKit/Sources`
so code the framework carries from nepdate-swift does not count. The gate prints how many lines
`llvm-cov` counted and fails when that is zero, so a filter or path that matches nothing cannot
pass.

`swift-snapshot-testing` 1.19.6 is pinned in `project.yml` and linked only to `NepDateTests`. The
six month references are in `NepDateTests/__Snapshots__/MonthSnapshotTests`: light with Asoj 25
selected, dark, Nepali with the tithi line, tithi in English, `.xxxLarge`, and `.accessibility2`.
The four day references are in `NepDateTests/__Snapshots__/DayDetailSnapshotTests`: Asoj 9 2083 in
light, dark, Nepali and `.accessibility2`. They are 390 by 844 points, rendered on the iPhone 17
simulator by Xcode 26.6. The test gate sets `SNAPSHOT_TESTING_RECORD=never`, so a missing image
fails instead of being written. `ci.sh` exports it as `TEST_RUNNER_SNAPSHOT_TESTING_RECORD`, because
`xcodebuild` passes a variable into the test process on the simulator only with that prefix;
without it the record run of 39144415913 compared instead of recording.

To re-record after an intended visual change, run the iOS workflow by hand on the branch with
`record_snapshots` checked, download the `ios-snapshot-references` artefact, copy the PNGs into
the matching folder under `NepDateTests/__Snapshots__`, review them, and commit. The recording run
fails at the test gate by design, because `swift-snapshot-testing` reports every recorded image as
an issue so that a human reviews it. Use its artefact, then run the workflow normally. A throwaway
control PR moved the day sheet's vertical spacing by two points; all four day references failed
(run 39191170937), and the PR was closed and its branch deleted.

1.19.6 is the newest tag (checked 2026-10-18). Its package has one `SnapshotTesting` product, and
that product always compiles its SwiftUI file, so no product leaves SwiftUI out. The tests call the
UIKit `.image` strategy on a `UIHostingController`, which is the renderer the SwiftUI overload uses
too. Run 39111474141 linked the package and recorded an image. It showed no `SwiftUICore` or
`UIUtilities` link warnings. The failure in run 39102808714 was undefined `NepDatePatro` symbols
from the test bundle, fixed in I1-01 by linking the library products directly.

The XCUITests in `NepDateUITests` run inside gate 4, so the gate count is unchanged. Debug builds
read `NEPDATE_UI_TEST_TODAY` (for example `2083-06-20`) to pin today, so every UI test sees Asoj
2083 and the audit counts are exact; a release build always uses the clock. The tests check that the
pager opens on today, that the picker reaches Chaitra 2199 and Baisakh 1901 with the next and
previous buttons disabled at those ends, and that 21 swipes cross the window recentre and land on
Asar 2085. The day sheet test taps Asoj 25, checks every spoken field and event, taps Copy date and
reads the action's accessibility value, then audits the sheet.

The pager holds 49 months around the shown one, because a 3588-page accessibility tree made the
audit time out. When a swipe comes within five pages of an edge the window is rebuilt around the
month. Rebuilding keeps the scroll offset, not the page, so the screen now ignores paging updates
until it has scrolled back to the month; before that fix the swipe test landed on Asoj 2093.

The audit runs one category per call (contrast, Dynamic Type, element detection, hit region,
element description, clipped text, traits), on the grid at the default size and on the list at
the accessibility L size. It inspects the text drawn inside a day cell or list row even though
that text is hidden and the cell's element is the full spoken date. The test counts findings on
those hidden text nodes per category and kind and compares them with an exact table; any other
finding, or a different count, fails. The table and the evidence for each entry are in
`MonthAccessibilityTests.swift`:

| Layout | Finding | Count | Why it is tolerated |
|---|---|---|---|
| Grid | Dynamic Type on BS and AD numerals | 63 | The grid keeps numerals at a size that fits seven columns; the list replaces it at every accessibility size and has no numeral finding |
| Grid | Contrast on AD numerals 13 and 27 | 2 | The pair measures 7.55:1; changing the colour from 5.74:1 to 7.55:1 left both findings at the same frames |
| List | Dynamic Type and clipped text on date, weekday and event lines | 10 and 10 | The lines have no line limit and wrap (`month.accessibility2.png`) |
| List | Contrast on the weekday line of Asoj 4 | 1 | The line is behind the translucent tab bar; the same pair passes on every row above |

The day sheet audit has no tolerated findings. It opens from Asoj 25 and runs the same seven
categories; the green run prints `AUDIT tolerated []`. The contrast fix was to tint both actions
with `BrandColor.today`; the default bordered button's blue failed on "Copy date" in run
39184279428.

The UI test cannot read the system pasteboard directly. In this runner a `UIPasteboard.general`
read waits on the system paste prompt (runs 39184279428 and 39186125450), so the app repeats the
copied text as the Copy date button's accessibility value and the UI test reads that. The
`DayClipboardTests` unit test writes and reads the real pasteboard in the app process, where the
same write and read pass. The manual checklist still covers the clipboard and the permission
prompt.

Colour contrast, measured with WCAG relative luminance (`BrandColor`): secondary text is 0.33 white
in light (7.55:1 on the background, 6.36:1 on `holidayFill`, 6.25:1 on `selection`) and 0.78 white in
dark (12.41:1, 10.48:1, 8.87:1); `holiday` is 5.88:1, 4.95:1 and 4.87:1 in light and 7.64:1, 6.45:1
and 5.46:1 in dark. The design system names the system tertiary label for dimmed days and AD
numerals, but that colour is below 4.5:1, so the app uses secondary text for them. Dimmed
neighbouring days are not selectable (`MonthViewModelTests.dimmedCells`). VoiceOver and Switch
Control on a device, and the list at the largest text size, are still manual checklist items
(MR-10 AC4); there is no device in this repository's CI.

The month screen and the day sheet are in the `CalendarScreens` product, which only the app links;
the month view model and the month keys moved there in I1-04 as well. Their types are app-only, and
keeping them out of `CalendarKit` stops the widget extension carrying them. The widget executable
was 640,528 bytes after the I1-03 split, grew to 649,072 bytes when `DayDetailViewModel` joined
`CalendarKit`, and is 615,384 bytes with the month view model and keys moved out (Release build
sizes from `ci.sh`, runs 39143033588, 39188791325 and 39190060914).

The day sheet opens from a grid cell or a list row with medium and large detents. It shows the BS
long date, the Devanagari line in English, the weekday and AD date, the tithi, Public holiday and
every event in file order. `DayDetailViewModel` in `CalendarKit` holds those strings;
`DayDetailContent` in `CalendarScreens` draws them. "Copy date" calls `DayClipboard`. "Add to
calendar" builds an all-day `EKEvent` in `DayEvent` and presents `EKEventEditViewController`; the
app never reads a calendar or requests access. The event date uses the Gregorian calendar in the
current time zone, whatever calendar the user has set, and ends on the day it starts. The unit
tests check all-day, title, local midnight, an end on the same day and no alarm without a
permission request, and Gregorian midnight in three zones with the Buddhist-calendar case. VoiceOver, Switch Control and the absence of a
permission prompt are still manual checklist items, because CI has no device.

Digits have their own setting, as on Android (MR-07 AC1): Follow language (the fresh-install
default), Nepali and English. `DigitsSetting.resolved(for:)` turns it into the digits to draw at
render time, so changing the language changes the digits without writing the setting.

The App Group is `group.np.nepdate`; the bundle identifier is `np.nepdate` for the app and
`np.nepdate.widgets` for the extension.

## I1-05 screens

Upcoming, Converter, Settings and About live in `CalendarScreens`, with the Upcoming and Converter
view models. Settings opens from the gear in the Month, Upcoming and Converter headers. About is a
row inside the Settings `NavigationStack`. The Language row opens
`UIApplication.openSettingsURLString`; the app does not implement a second in-app language picker.

Upcoming uses the same 20 entries, BS-month groups, holiday marks and coverage footer as A1-05.
Converter uses the library's lenient BS parser, a platform AD `DatePicker`, the comparison breakdown
and the invalid-day message. Settings persists Language, Digits, Date follows and Show tithi through
`SettingsStore`; the first-launch Nepal time suggestion is shown once when the device zone differs.
About shows the app version, `NepDateLibraryVersion`, coverage, data and official sources, the five
allow-listed links, the injected webcal row and the diagnostic copy.

`ci.sh` checks that `NEPDATE_LIBRARY_VERSION` in `project.yml` equals the nepdate-swift revision in
both package manifests. It also fails when any key in `Localizable.xcstrings` lacks a translated
`ne` value. Negative controls for both checks are recorded on the throwaway control PR.

The Upcoming and Converter audits tolerate nothing. Settings and About tolerate only the entries in
`ScreenUITests.swift` and this README:

| Screen | Finding | Count | Why it is tolerated |
|---|---|---|---|
| Settings | Dynamic Type on the toolbar title and Close | 2 | Both use system text styles; the audit reports partial support |
| About | Dynamic Type on the toolbar title and two-line info rows | 5, 4 at `.accessibility2` | The `.accessibility2` reference wraps the rows; the audit reports partial support |
| About | Contrast on the Privacy link | 1 | The bottom link sits behind the home indicator; the same tint passes on the links above |

The day sheet now shows "Date copied" for two seconds and posts one VoiceOver announcement when
Copy date is tapped. `daydetail.copied.png` records the confirmation.

## The nepdate-swift pin

`Packages/CalendarKit/Package.swift` depends on `nepdate-swift` by revision
`708cd3626ecb22abbb90a3f1d1adc05e778db6d5`, the `main` commit of 2026-10-18. The library has no
release tag yet, and this repository does not create tags, so a revision pin is the only exact pin
available (ADR-0003 §1).

The revision appears once more in `project.yml`. Xcode links package products per target, and the
test bundle imports `NepDate` and `NepDatePatro` directly to compare the month layout with the
library, so it needs both products at the project level. `ci.sh` fails when the two revisions differ.

When `nepdate-swift` 0.1.0 is released, replace both pins with `.exact("0.1.0")` and `exactVersion:
0.1.0`, update the Dependabot entry for the two local packages, and check the app still passes
`./scripts/ci.sh`.

## Xcode version

ADR-0004 and ADR-0008 ask for the latest stable Xcode, which ADR-0004 records as 27.x. On
2026-10-18 the `macos-26` image has Xcode 26.0 to 26.6 in `/Applications` and no Xcode 27 (run
39100819816 lists them), so `ios.yml` selects `/Applications/Xcode_26.6.app` by path through
`DEVELOPER_DIR` and fails if that path is missing. The workflow prints the list on every run. Move
the path to Xcode 27.x once the image has it, and before the first App Store upload (I3), since
uploads need the current SDK.

## Network check

`scripts/check-network.sh` greps the sources, then checks the release executables with `otool -L`
for networking frameworks and `nm -u` for networking classes. Each half has failed on purpose once:
a source `import Network` (run 39098192026) and, with the source grep switched off, a
`URLSession.shared` call caught in the binary (run 39100833738).

## Executable sizes

The build gate prints the sizes and gates none of them. In I1-05 the app executable is 2,045,984
bytes and the widget executable is 615,656 bytes, 272 bytes above the 615,384-byte I1-04 baseline.
The widget source did not change; the difference is linker layout from the app-only screens. Both
link `nepdate-swift` (including the `NepDatePatro` tables) statically through `CalendarKit`, and the
extension also carries its WidgetKit timeline code. This is the expected cost of static SwiftPM
products and was not measured with a link map. If the tables become a real share of the download,
I2 can ship `CalendarKit` as one dynamic framework in the app bundle that both executables load.
