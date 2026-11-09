# ADR-0014: iOS CI toolchain and the Xcode 27 move

- Status: Accepted
- Date: 2026-11-09
- Deciders: Sanam Pakuwal
- Supersedes: ADR-0010 §3, the runner line only

## Context

ADR-0010 §3 put every iOS build on the `macos-26` image with its latest stable Xcode, and ADR-0004
asks for "the latest stable Xcode (27.x)". Those two agree only while Xcode 27 is unavailable:
`macos-26` carries Xcode up to 26.6 and no Xcode 27 at all, so the app has been building with the
iOS 26 SDK while the toolchain ADR names 27.

Apple's news of 2026-09-09 sets the deadline: from **April 2027** App Store Connect accepts only
uploads built with the iOS 27 SDK. The same release notes say submissions need iPhone Duo
screenshots from April 2027 (2026-10-05).

Both images were measured on 2026-11-09 with a one-off workflow that only listed Xcode, runtimes and
devices (run 42659249230):

| | `macos-26` | `xcode-27` |
|---|---|---|
| Xcodes | 26.0 to 26.6 | 27.0, 27.1 (default), 27.2 |
| Simulator runtimes | iOS 26.2, 26.4, 26.5 | iOS 27.0, 27.1, 27.2 |
| `iPhone 17` exists on | iOS 26.5 | iOS 27.0 and 27.2 |
| iOS 18 runtime | no | no |

Two details matter. XcodeGen 2.46.0 has no `xcode27_0` project format and has a known Xcode 27.2
conversion bug that is fixed but not released, so 27.1 is the only usable Xcode on that image. And
the iOS 27.1 runtime on the `xcode-27` image carries exactly one simulator, **iPhone Duo**; the
`iPhone 17` device exists only on the 27.0 and 27.2 runtimes.

## Decision

1. **CI stays on `macos-26` with `/Applications/Xcode_26.6.app` for now.** The move to `xcode-27`
   was attempted first, on branch `ci/ios-xcode-27`, and the gates do not pass on iOS 27 (run
   42659249230, gate 4):
   - Snapshot rendering fails for every reference: writing the PNG fails inside the simulator with
     `zlib error` and `No IDATs written into file`, so nine render tests fail with `RenderFailure`.
   - The accessibility audits report fewer findings than the tests tolerate, so the tolerated-finding
     lists no longer match: the month grid loses its 63 `dynamicType numeral` findings and the list
     loses its `textClipped` and `dynamicType` findings.
   - The month pager's swipe test lands on the wrong month after the window recentre.
   - The Settings and About UI test fails five assertions, and both Settings audits gain a contrast
     failure.
   These are iOS 27 behaviour changes, not our regressions: the same commit passes on iOS 26.5. Fixing
   them is an app migration with its own PR and its own device testing, and the tests would then only
   cover iOS 27 while the app still ships to iOS 18. It does not belong in a toolchain change.
2. **The simulator destination is pinned** by name and OS: `iPhone 17` on `OS=26.5`, the runtime the
   previous `OS=latest` resolved to on this image. The job prints `xcrun simctl list devices
   available`, so a renamed device or a withdrawn runtime is visible in the log before the gates run.
3. **XcodeGen stays at 2.46.0** with its SHA-256 check. When the toolchain does move, it moves to
   Xcode 27.1, and to 27.2 only with the XcodeGen release that fixes the conversion bug.
4. **iOS 18 has no simulator runtime on either image.** iOS 18 is therefore covered by compile-time
   `#available` checks, by the deployment target (iOS 18.0) and by the TestFlight build on the
   reference iPhone, not by a second simulator job. CI does not download runtimes: the images do not
   document it as supported.
5. **Revisit before the first App Store upload**, and in any case before April 2027, which is when
   the iOS 27 SDK becomes mandatory for uploads. The blocking work is the list in point 1; the
   destination to use is `iPhone 17` on `OS=27.0` or `27.2`, or `iPhone Duo` on 27.1 if the resizable
   layout work lands first.

## Consequences

- ADR-0004's "latest stable Xcode 27.x" stays untrue until the migration above happens; this ADR
  records that as a known gap with a date rather than an oversight.
- The app keeps testing on the runtime the snapshot references were recorded on, so the parity work
  does not fight a moving baseline.
- The one-off measurement workflow is not kept: the device and runtime lists are recorded here, and
  the CI job prints the device list on every run.
- A **Preview** image can change or disappear without notice; that risk is deferred together with the
  move.

## Alternatives considered

- **Move to `xcode-27` and fix the tests now.** It is the honest long-term direction, but it merges
  four unrelated behaviour changes into a toolchain PR, needs on-device checks for the pager, and
  would leave the test suite unable to run on the iOS 18 target it still supports.
- **Pin `OS=27.1` as originally intended.** The 27.1 runtime on that image has only the iPhone Duo
  device, and the snapshot references are iPhone 17 sized, so every reference would need re-recording
  on a different screen for a toolchain that does not pass the gates anyway.
- **`xcode-27-xlarge`.** Larger runners are billed per minute even for public repositories.
- **Stay on `macos-26` and never revisit.** That is what point 5 forbids: April 2027 makes the move
  mandatory.
