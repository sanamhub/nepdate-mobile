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
