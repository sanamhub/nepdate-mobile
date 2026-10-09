# ADR-0004: iOS architecture and toolchain

- Status: Accepted
- Date: 2026-10-06
- Deciders: Sanam Pakuwal

## Context

As of 2026-10-06: iOS 27 and Xcode 27 shipped on 2026-09-14 (Swift 6.4 compiler); uploads must use
the iOS 26 SDK or later; iOS 26 ran on 79 % of all iPhones in June 2026. iOS 27 runs on the same
devices as iOS 26 (iPhone 11 and later, SE 2nd gen and later).

## Decision

**Platform**
- Deployment target **iOS 18.0**: reaches the iPhone XS/XR and un-updated phones common in Nepal,
  and still has `@Observable`, interactive widgets, Control Center controls and
  `widgetAccentedRenderingMode`. Liquid Glass comes automatically from the iOS 26+ SDK; any
  `glassEffect` use is behind `#available(iOS 26, *)`. `systemExtraLargePortrait` behind
  `#available(iOS 27, *)`.
- Build with the latest stable Xcode (27.x) on the GitHub-hosted macOS runner (ADR-0010). Swift 6
  language mode, strict concurrency.

**Targets and packages** (`ios/`; the Xcode project is generated from `project.yml` by XcodeGen
and not committed, ADR-0010)
```
project.yml           XcodeGen spec for the targets below
  NepDateApp          app target (SwiftUI)
  NepDateWidgets      widget extension (WidgetKit + App Intents controls)
Packages/
  CalendarKit         local SwiftPM package: view models, month layout, settings store,
                      formatting helpers; depends on nepdate-swift (NepDate, NepDatePatro)
  CalendarUI          local SwiftPM package: month grid and day views shared by app and widgets
```
- App and widget extension share settings through an **App Group** (`group.np.nepdate`):
  `UserDefaults(suiteName:)`. Nothing else is persisted; no SwiftData.
- `CalendarKit` is `nonisolated` and `Sendable`; UI targets use default `MainActor` isolation.
- State: `@Observable` models, `@State` ownership, `@Environment` with `@Entry` for injection. A
  `Clock`/"today" provider is injected so date logic is testable. No third-party DI.

**Text and localisation**
- String Catalog (`Localizable.xcstrings`) with `en` and `ne`. Digits are formatted by the library
  (never by `NumberFormatter` locale guessing).
- Nepali is not an App Store Connect metadata language: store text is English; the app itself is
  fully Nepali when the device or per-app language is Nepali.

**Dependencies (complete list):** `nepdate-swift` (products `NepDate` and `NepDatePatro`); test-only
`swift-snapshot-testing` (pointfreeco, 1.19+); build-only XcodeGen (CI tool, pinned version, not
linked). Linting uses `swift format`, which ships with the Swift toolchain. Nothing else.

**Privacy files:** `PrivacyInfo.xcprivacy` in the app and the extension: `NSPrivacyTracking = false`,
no collected data types, `NSPrivacyAccessedAPICategoryUserDefaults` with reasons `CA92.1` and
`1C8F.1`. `ITSAppUsesNonExemptEncryption = NO`.

## Consequences

- No `#available` branches except the two listed above.
- Foundation's `Calendar.Identifier.vikram` (iOS 26) is the Indian lunisolar calendar, not Nepal's
  solar Bikram Sambat. Never use it for dates; all dates come from `nepdate-swift`.
