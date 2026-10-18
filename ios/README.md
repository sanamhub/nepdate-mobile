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
| `NepDateTests/` | Swift Testing tests, hosted by the app; renders the screenshot artefact |
| `Resources/` | `Localizable.xcstrings` (English and Nepali) |
| `Config/` | Info.plists and the App Group entitlements for both targets |
| `scripts/ci.sh` | The gates of ADR-0008 |

## Generating and testing

```sh
cd ios
./scripts/ci.sh
open NepDate.xcodeproj        # after ci.sh, on a Mac
```

`ci.sh` downloads XcodeGen 2.46.0, checks its SHA-256, generates the project and runs five gates:
xcodegen, format, build, test, network. It ends with `ci: 5/5 gates passed`. The test gate renders
the month screen and the widget to `build/snapshots`; `ios.yml` uploads them as the
`ios-screenshots` artefact.

The App Group is `group.np.nepdate`; the bundle identifier is `np.nepdate` for the app and
`np.nepdate.widgets` for the extension.

## The nepdate-swift pin

`Packages/CalendarKit/Package.swift` depends on `nepdate-swift` by revision
`708cd3626ecb22abbb90a3f1d1adc05e778db6d5`, the `main` commit of 2026-10-18. The library has no
release tag yet, and this repository does not create tags, so a revision pin is the only exact pin
available (ADR-0003 §1).

When `nepdate-swift` 0.1.0 is released, replace the revision with `.exact("0.1.0")`, update the
Dependabot entry for the two local packages, and check the app still passes `./scripts/ci.sh`.

## Xcode version

ADR-0004 and ADR-0008 ask for the latest stable Xcode, which ADR-0004 records as 27.x. On
2026-10-18 the `macos-26` image has Xcode 26.6 as its default and Xcode 27 only as a preview, so
`ios.yml` selects `/Applications/Xcode_26.6.app` by path through `DEVELOPER_DIR` and fails if that
path is missing. The workflow lists the Xcode versions on the image in its log. Move the path to
Xcode 27.x once the image ships a non-preview 27, and before the first App Store upload (I3), since
uploads need the current SDK.

## Executable sizes

The build gate prints the sizes and gates none of them. The widget executable is a little larger
than the app executable because both link `nepdate-swift` (including the `NepDatePatro` tables)
statically through `CalendarKit`, and the extension also carries its WidgetKit timeline code, while
the app is still a single screen. This is the expected cost of static SwiftPM products and was not
measured with a link map. If the tables become a real share of the download, I2 can ship
`CalendarKit` as one dynamic framework in the app bundle that both executables load.
