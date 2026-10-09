# ADR-0010: Build, sign and ship the iOS app without a Mac

- Status: Accepted
- Date: 2026-10-06
- Deciders: Sanam Pakuwal

## Context

The maintainer has a Windows PC and an iPhone, and no Mac. Building an iOS app needs Xcode, which runs
only on macOS. The repository is public, so GitHub-hosted macOS runners cost nothing for it. There is
no iOS Simulator, no SwiftUI preview, no Instruments and no Console on Windows.

## Decision

1. **The library needs no Mac.** `nepdate-swift` is pure Swift and builds and tests on Windows with
   the swift.org toolchain, and on Linux. All calendar logic is developed and tested there.
2. **The Xcode project is generated, not committed.** `ios/project.yml` (XcodeGen) describes the app
   target, the widget extension, the App Group, entitlements and the local packages; CI runs
   `xcodegen generate` first. A YAML file can be written and reviewed on Windows; a `.pbxproj` can't.
   `*.xcodeproj` is in `.gitignore`. All app code sits in the local SwiftPM packages (`CalendarKit`,
   `CalendarUI`) except the thin `@main` app and widget bundle files.
3. **Every iOS build runs on a GitHub-hosted macOS runner** (`macos-26` image, latest stable Xcode):
   - `ios.yml` on every PR: build, Swift Testing, snapshot tests on the simulator. The snapshot PNGs
     are uploaded as a workflow artefact, so screens and widgets can be reviewed from Windows.
     `performAccessibilityAudit()` in a UI test stands in for Accessibility Inspector.
   - `ios-beta.yml` (manual dispatch from `main`, environment `testflight`): archive, sign and upload
     to TestFlight for internal testing. Internal builds need no Apple review; they reach the iPhone
     through the TestFlight app in minutes.
   - `ios-release.yml` (tag `ios-v*`, environment `production` with a required reviewer): the same,
     then submission is done by hand in App Store Connect on the web.
4. **Signing is cloud-managed.** `xcodebuild -allowProvisioningUpdates` with
   `-authenticationKeyPath`, `-authenticationKeyID` and `-authenticationKeyIssuerID` lets Xcode
   create and use a cloud-managed distribution certificate and profiles; no `.p12` and no keychain
   import. Apple requires an **Admin** App Store Connect API key for cloud signing, so the key is
   treated as the most sensitive secret in the project (see 6).
5. **Testing on the device:** the reference iPhone runs TestFlight builds. Widget behaviour at midnight,
   lock-screen widgets and controls are checked there by the manual checklist. Crash reports and
   TestFlight feedback are read in App Store Connect on the web.
6. **Public-repository safety:**
   - The API key (`.p8`, key ID, issuer ID) lives only in the `testflight` and `production`
     environment secrets; both environments accept deployments from `main` and tags only.
   - PR workflows use `pull_request`, never `pull_request_target`, and get no secrets; fork PRs
     can't reach the environments.
   - The key is written to `$RUNNER_TEMP` and deleted in an `always()` step; runners are ephemeral.
   - Actions pinned by SHA; signing jobs have `permissions: contents: read` and nothing else.
   - If the key leaks: revoke it in App Store Connect (Users and Access, Integrations) at once,
     create a new one, update both environments.
7. **Escape hatch:** for work that truly needs Xcode's tools (a stubborn widget layout, Instruments
   profiling), rent a cloud Mac for a day. Apple's licence makes most providers bill at least 24
   hours. This is expected rarely, perhaps once per major release, and is not part of any routine.

## Consequences

- iOS feedback is slower: a change is seen as snapshot images about 10–15 minutes after a push, and
  on the phone after a TestFlight upload. Android stays the first platform (roadmap) and the design
  spike is judged on Android and on the iPhone through TestFlight.
- No SwiftUI previews. Views are kept small and covered by snapshot tests, which also act as the
  preview.
- Development-certificate builds (running a debug build on the iPhone by cable) are not possible;
  every on-device build is a TestFlight build in the release configuration. Debug-only screens from
  the design spike (M0-05) compile only when `ios-beta.yml` sets the `DESIGN_SPIKE` compilation
  condition; `ios-release.yml` never sets it.
- Xcode Cloud is not used: setting it up needs Xcode.

## Alternatives considered

- **React Native, Flutter or Expo with a cloud build service:** also need a Mac (or a cloud Mac) for
  the widget extension, which is native SwiftUI anyway. Doesn't remove the problem (ADR-0001).
- **Codemagic:** hosted macOS builds with a free monthly allowance; a fine fallback if GitHub's macOS
  runners become unavailable for public repositories. Not chosen because GitHub Actions already runs
  everything else and keeps secrets in one place.
- **Buy a Mac mini:** the simplest developer experience. Left to the maintainer; nothing here
  depends on it, and adopting one later changes nothing in CI.
