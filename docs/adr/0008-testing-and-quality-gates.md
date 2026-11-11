# ADR-0008: Testing and quality gates

- Status: Accepted
- Date: 2026-10-06
- Deciders: Sanam Pakuwal

## Decision

Calendar correctness is tested in the libraries (shared vectors, every day). The apps test what they
add: layout of the month, view models, settings, scheduling, widgets, accessibility and size.

**Android** (`./gradlew ci`, Linux runner):
1. `ktlintCheck`, `detekt`, Android Lint with `warningsAsErrors`.
2. Unit tests (JUnit, Turbine): month layout (35 or 42 cells, leading and trailing days), view models,
   midnight scheduling (next-midnight computation across time zones and DST in a fake clock),
   settings migration.
3. Roborazzi screenshots: every screen and every widget size in light, dark and Nepali, plus font
   scale 200 %. Golden images reviewed in the PR.
4. `checkNoInternet` on the merged release manifest; `checkPermissions` against the allow-list.
5. Release build size check: universal APK ≤ 8 MB (MR-09 AC3).
6. Instrumented tests on Gradle Managed Devices (API 26 `aosp` image, since ATD images start at
   API 30, and API 37 `aosp-atd`) for the widget update receiver and Compose UI flows, nightly
   (`android-nightly.yml`) rather than per PR.

**iOS** (`ios/scripts/ci.sh`, macOS runner, latest stable Xcode):
1. `swift format lint --strict` (ships with the toolchain, ADR-0004) with findings failing the build.
2. Swift Testing for `CalendarKit` (month layout, timeline entries at midnight across zones, settings),
   parameterised over dates.
3. `swift-snapshot-testing`: every screen and widget family in full-colour, accented and vibrant
   modes, light and dark, English and Nepali, Dynamic Type `.xxxLarge` and `.accessibility2` (about
   200 % of body text, MR-10 AC2).
4. Network check: no `URLSession`/`Network` symbols in the release build.
5. Size of the archived `.app` logged on every run as a regression signal, and an XCTest memory
   metric for the widget timeline provider. The MR-09 AC3 and AC4 numbers are measured on the
   TestFlight build (I1-06, I2-05), because the simulator and the archive don't give thinned size
   or extension memory.

**Both:** coverage ≥ 80 % for view models and scheduling code (org target): Android packages
`ui.*` view models and `schedule` (AGP JaCoCo report), iOS package `CalendarKit` (`xcodebuild
-enableCodeCoverage YES`, read with `xcrun xccov`). Accessibility audit
(iOS: `performAccessibilityAudit()` in CI plus VoiceOver on the device; Android: Accessibility
Scanner) in each release's manual checklist
(`docs/runbooks/manual-test.md`, written in M0).

CI: actions pinned by SHA, `permissions: contents: read`, `paths:` filters per platform (ADR-0002).
