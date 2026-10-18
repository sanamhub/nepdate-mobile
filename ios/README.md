# ios

The iPhone and iPad app and the Today widget extension. The Xcode project is generated from
`project.yml` by XcodeGen and never committed (ADR-0010).

| Path | What it is |
|---|---|
| `project.yml` | XcodeGen spec: targets, packages, App Group, Info.plist and entitlements paths |
| `NepDateApp/` | The thin `@main` SwiftUI app and its privacy manifest |
| `NepDateWidgets/` | The widget extension: `@main` bundle, the placeholder Today widget, privacy manifest |
| `Packages/CalendarKit` | Local SwiftPM package: dates, month layout, widget summaries, settings (I1) |
| `Packages/CalendarUI` | Local SwiftPM package: the month screen and widget views shared by app and widget |
| `NepDateTests/` | Swift Testing tests and the committed snapshot references; renders the screenshot artefact |
| `NepDateUITests/` | The XCUITest that opens the app and runs `performAccessibilityAudit()` |
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
`build/snapshots`, and compares the six reference snapshots. `ios.yml` uploads the screenshots and
the references as artefacts.

The coverage gate reads the test result bundle with `xcrun xccov` and fails below 80 % line
coverage of `CalendarKit` (ADR-0008). On Xcode 26.6 `xccov` reports the local package target with no
source lines (run 39103877148), so when that happens the gate reads the run's profile with
`llvm-cov` against the `CalendarKit` framework instead, filtered to `Packages/CalendarKit/Sources`
so code the framework carries from nepdate-swift does not count. The gate prints how many lines
`llvm-cov` counted and fails when that is zero, so a filter or path that matches nothing cannot
pass.

`swift-snapshot-testing` 1.19.6 is pinned in `project.yml` and linked only to `NepDateTests`. The
six reference images are in `NepDateTests/__Snapshots__/MonthSnapshotTests`: light with Asoj 25
selected, dark, Nepali with the tithi line, tithi in English, `.xxxLarge`, and `.accessibility2`.
They are 390 by 844 points, rendered on the iPhone 17 simulator by Xcode 26.6. The test gate sets
`SNAPSHOT_TESTING_RECORD=never`, so a missing image fails instead of being written.

To re-record after an intended visual change, run the iOS workflow by hand on the branch with
`record_snapshots` checked, download the `ios-snapshot-references` artefact, copy the PNGs into
`NepDateTests/__Snapshots__/MonthSnapshotTests`, review them, and commit. The recording run fails
at the test gate by design, because `swift-snapshot-testing` reports every recorded image as an
issue so that a human reviews it. Use its artefact, then run the workflow normally.

1.19.6 is the newest tag (checked 2026-10-18). Its package has one `SnapshotTesting` product, and
that product always compiles its SwiftUI file, so no product leaves SwiftUI out. The tests call the
UIKit `.image` strategy on a `UIHostingController`, which is the renderer the SwiftUI overload uses
too. Run 39111474141 linked the package and recorded an image. It showed no `SwiftUICore` or
`UIUtilities` link warnings. The failure in run 39102808714 was undefined `NepDatePatro` symbols
from the test bundle, fixed in I1-01 by linking the library products directly.

The XCUITest in `NepDateUITests` launches the app, checks that the pager opened on today's month by
finding the first day cell's spoken label, and calls `performAccessibilityAudit()`. It runs inside
gate 4, so the gate count is unchanged. VoiceOver and Switch Control on a device are still a manual
checklist item (MR-10 AC4); there is no device in this repository's CI.

Digits have their own setting, as on Android (MR-07 AC1): Follow language (the fresh-install
default), Nepali and English. `DigitsSetting.resolved(for:)` turns it into the digits to draw at
render time, so changing the language changes the digits without writing the setting.

The App Group is `group.np.nepdate`; the bundle identifier is `np.nepdate` for the app and
`np.nepdate.widgets` for the extension.

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

The build gate prints the sizes and gates none of them. The widget executable is a little larger
than the app executable because both link `nepdate-swift` (including the `NepDatePatro` tables)
statically through `CalendarKit`, and the extension also carries its WidgetKit timeline code, while
the app is still a single screen. This is the expected cost of static SwiftPM products and was not
measured with a link map. If the tables become a real share of the download, I2 can ship
`CalendarKit` as one dynamic framework in the app bundle that both executables load.
