# M0: Repository, CI skeletons, design spike

Read first: `AGENTS.md`, ADR-0001, ADR-0002, ADR-0008, ADR-0010, `docs/product/design-system.md`.

## Tasks

### M0-01 Repository files
`CHANGELOG.md` in `ios/` and `android/` (empty `[Unreleased]`), `SECURITY.md`, `CONTRIBUTING.md`,
`.github/pull_request_template.md`, issue templates: bug report (device, OS, app version, steps,
screenshot), **"Wrong date or holiday"** (`.github/ISSUE_TEMPLATE/wrong-date.yml`: BS date, what is
wrong, official source link) which also asks whether the problem is in the data (then it moves to
the hub), `config.yml` with blank issues off. Dependabot for `github-actions`, `gradle` (`/android`)
and `swift` (`/ios/Packages/CalendarKit` and `/ios/Packages/CalendarUI`, where the exact pins live,
ADR-0003 §1).
AC: every file above exists; `https://github.com/sanamhub/nepdate-mobile/issues/new?template=wrong-date.yml`
opens the form (MR-07 AC2 links to it).

### M0-02 Android project skeleton (`android/`)
Per ADR-0005: version catalog, one `:app` module with an empty Compose activity, `:baselineprofile`,
ktlint, detekt, Lint `warningsAsErrors`, Roborazzi. Gradle task `ci` runs the gates in ADR-0008 that
already apply, plus `checkNoInternet` and `checkPermissions` (allow-list: `RECEIVE_BOOT_COMPLETED`
only, ADR-0007).
AC: `./gradlew ci` passes on Linux and Windows; adding `<uses-permission
android:name="android.permission.INTERNET"/>` to the manifest makes `./gradlew ci` fail.

### M0-03 iOS project skeleton (`ios/`)
Per ADR-0004 and ADR-0010: `project.yml` for XcodeGen with the app target, a widget extension target
with one placeholder widget, the App Group, the local packages `CalendarKit` and `CalendarUI`,
`PrivacyInfo.xcprivacy` in both targets, and a String Catalog with `en` and `ne`. `ios/scripts/ci.sh`
installs the pinned XcodeGen release (download by version, check its SHA-256), runs
`xcodegen generate`, then builds, runs the tests, runs `swift format lint --strict` and the network
check (ADR-0007).
AC: `ios/scripts/ci.sh` passes on `macos-26` with the latest stable Xcode; the snapshot PNGs are
downloadable as a workflow artefact; `.gitignore` ignores `*.xcodeproj/` and the PR contains no
`.xcodeproj` or `.pbxproj` file.

### M0-04 CI workflows
`android.yml` (`paths: android/**`), `ios.yml` (`paths: ios/**`), both on `pull_request` and pushes to
`main`, pinned by SHA, `permissions: contents: read`, `concurrency`. `android-nightly.yml` (schedule,
daily) runs the instrumented tests on Gradle Managed Devices (ADR-0008, Android 6).
`ios-beta.yml` (manual dispatch from `main`, environment `testflight`, ADR-0010): archive with cloud
signing and upload to TestFlight.
AC: no workflow uses `pull_request_target`; a dispatch of `ios-beta.yml` from `main` puts a build in
TestFlight that installs on the reference iPhone; a dispatch from another branch is refused by the
environment.

### M0-05 Design spike (go/no-go, both platforms)
A debug-only screen and a debug widget that render:
- every line of the Devanagari test strings (copy
  [`nepdate-rust/docs/plan/devanagari-test-strings.md`](https://github.com/sanamhub/nepdate-rust/blob/main/docs/plan/devanagari-test-strings.md))
  at the cell, hero and widget sizes of design-system §3, with the system font;
- the colour tokens of design-system §2 on light and dark backgrounds, with measured contrast;
- one holiday cell in each iOS widget rendering mode and with Android dynamic colour.
The human checks screenshots on a real iPhone (TestFlight build from `ios-beta.yml` with
`DESIGN_SPIKE` set) and on two Android devices (one low-end). Pass: no
broken conjuncts, no clipped vowel signs (Android 16 `elegantTextHeight` change), contrast ≥ 4.5:1.
Fail → bundle a subset of Noto Sans Devanagari (OFL) and re-test.
AC: `docs/product/spike-results.md` records, per platform and device, pass or fail for each test
string line and the measured contrast of each colour pair in light and dark.

### M0-06 Manual test checklist
`docs/runbooks/manual-test.md`: one section per MR and W ID that has manual ACs (VoiceOver/TalkBack,
font scale 200 %, midnight rollover with the clock moved, time-zone change, lock screen, store
labels), with columns for iPhone, Android A15 class, Android low-end.
AC: every MR and W acceptance criterion that no automated test covers has a line in the file.

### M0-07 Human tasks
- Create `sanamhub/nepdate-mobile`; branch protection on `main`; environments `testflight`
  (deployments from `main` only) and `production` (tags `android-v*` and `ios-v*` only, required reviewer).
- Confirm the app/bundle ID `np.nepdate` (or pick another) on App Store Connect and Play Console.
- iOS one-time setup, steps 1 to 6 of `docs/runbooks/release-ios.md` (Apple Developer Program,
  bundle IDs and App Group, app record, Admin API key, environment secrets, TestFlight). M0-04 needs
  them.
- Ask the Nepal Panchanga Nirnayak Samiti whether a free digital calendar app needs its approval;
  record the answer in `docs/legal/README.md`.
- Ask Raju Prasai where NepDate's tithi, holiday and event data came from (hub
  `docs/upstream/proposals.md`, U-04); record the answer in `docs/legal/README.md`.
- Find a native Nepali speaker to write and review the Nepali strings (MR-11 AC1).
