# A3 progress

Branch `feat/A3-android-release-ready`, from `main` at `c9935fd`. Acceptance criteria copied from
`docs/plan/A3-android-release.md` before implementation. A3-04 and A3-05 are human-gated and are not
started.

## A3-00 About links

- [x] A unit test lists every URL in `AboutLinks.kt` by reflection and fails when the host or
      repository is outside the public allow-list (`sanamhub/nepdate-mobile` today).
- [x] The data-questions link opens a copy of the hub's known-questions section inside this
      repository, with a header naming the hub commit it was copied from.
- [x] `docs/product/requirements.md` MR-07 AC2 points at the copy, not at the unpublished hub.
- [x] Negative control: putting the hub URL back fails the link test (`AboutLinksTest`, two failures
      with the old URL, then reverted).

## A3-01 Reproducible release build

- [x] CI uses a fixed JDK 17.
- [x] Release `vcsInfo { include = false }` and no build timestamps.
- [x] PNG crunching is off in the release build type.
- [x] A Baseline Profile is checked in at `android/app/src/main/baseline-prof.txt`. It contains the
      merged AndroidX rules. App-specific rules from `:baselineprofile` still need a device and are
      blocked by issue #11.
- [x] A CI job builds the unsigned release APK twice on clean runners and compares SHA-256; the job
      fails on a difference.
- [x] Negative control: a throwaway draft PR with a non-deterministic build change makes the check
      fail once, then the PR is closed and its branch deleted. Run
      [39150451614](https://github.com/sanamhub/nepdate-mobile/actions/runs/39150451614) failed with
      APK A `7935ec87bce30823ce492a10bab0ec3c399089af18a6587ec92e20d1404b259c` and APK B
      `fdf2b58ba8883d95d7e030f77335accf3a3fe3dcf1b90ae32779883b6498ddec`. PR #19 is closed and
      the branch is deleted.
- [x] AC: GitHub CI run
      [39150421202](https://github.com/sanamhub/nepdate-mobile/actions/runs/39150421202) built the
      APK twice and both were
      `d094892c14ce1f777213e8e89ce74f699ab5048606f6f22d7ec402f69fb46dbc`, 2,392,322 bytes. Local
      `clean :app:assembleRelease` builds gave the same checksum.

## A3-02 Release workflow (`android-release.yml`)

- [x] The workflow triggers on `android-v*` tags and `workflow_dispatch`; `dry_run` is a boolean
      defaulting to `true`. It does not run in this phase.
- [x] Preflight checks that the tag equals `versionName`, that `android/CHANGELOG.md` has the
      version's section, and that the bundled public holidays reach 183 days after the UTC release
      date. Tithi and events below the threshold warn and do not fail.
- [x] A fixed-date JVM test fails at AD 2027-06-04 and passes at AD 2027-06-03, matching the Go,
      Python and Rust gates. Local reports: 183 days with a tithi warning, then 182 days and a
      failure.
- [x] CI, then signing, Play upload and the draft GitHub Release with the APK, `SHA256SUMS` and a
      build-provenance attestation.
- [x] Signing and Play upload run only in GitHub environment `production`; no Play promotion.
- [x] Every action is pinned to a full commit SHA. Top-level `permissions` is `contents: read`;
      only the attest job gets `id-token` and `attestations`, and only the release job gets
      `contents: write`.
- [x] A static workflow test parses the YAML (no regex) and checks triggers, pins, permissions,
      environment, `dry_run` default and the production gates, with one negative fixture per rule.
- [x] `actionlint` 1.7.12 passes on both workflows.

## A3-03 Store listings

- [x] `fastlane/metadata/android/en-US` and `ne-NP` have title, short description, full description,
      and a changelog for `versionCode` 1.
- [x] Screenshots come from the Roborazzi set. Phone frames are produced only by
      `tools/frame_store_screenshots.py`; the red test label is cropped. Six English and four Nepali
      images, all 1080x1920.
- [x] Data safety answers are a checked-in markdown draft: no data collected, no data shared.
- [x] Content-rating answers are a checked-in markdown draft.
- [x] The listing states the privacy policy URL and matches `docs/product`; it claims nothing the
      app does not do and mentions no network, analytics or permission beyond the app.
- [x] The listing is English and Nepali.

## Local checks

- `./gradlew ci`: `BUILD SUCCESSFUL in 3m 31s`, 192 tests, 0 failures, 1 skipped (the release-only
  freshness test when `RELEASE_DATE` is absent).
- GitHub Android CI: run
  [39150421202](https://github.com/sanamhub/nepdate-mobile/actions/runs/39150421202) green. `Gradle
  ci` took 3m 48s, the two APK jobs took 3m 16s and 2m 32s, and the checksum compare took 6s.
- `python tools/check_android_release_workflow.py`: positive check and five negative fixtures pass.
- `actionlint 1.7.12`: clean on `android.yml` and `android-release.yml`.
- Store screenshots generated twice with identical SHA-256 values.

## Out of scope

- A3-04 (closed test, production rollout, IzzyOnDroid, F-Droid) is human-gated and not started.
- A3-05 (Play account, signing key, service account, environment secrets) is human-gated and not
  started. No tag, release, signing key, secret or repository setting is created in this phase.

## A2 review (merged in #15)

The A2 review is kept here for the release work:

- Settings changes inside a running Glance session now write a `refresh` key so the session redraws;
  tested with a negative control.
- The midnight alarm is armed from `NepdateApplication.onCreate`, not only when the app opens.
- `WidgetConfigActivity` accepts only widget ids bound to this app's providers and declares the
  `APPWIDGET_CONFIGURE` filter.
- The exported `MainActivity` accepts only strict ISO date extras from widgets.
- WorkManager is accepted only as Glance's dependency (ADR-0011), with a CI check.
- Checked and kept: one fixed inexact `setWindow` alarm, the system-change receiver, all widget
  numbers through the resolved Digits, and no widget bitmaps.
- Device-only checks remain blocked by issue #11.
Left for later: generated previews are republished on every app open and the system rate-limits
them, so a language change shows in the picker preview only after a later open.

## I1-03 review (iOS month screen, PR #14)

Reviewed at `4e25a1c`; fixes on the branch, CI run 39147652594 green before the merge of `main`.

- Pager recentre: swapping the 49-month window kept the scroll offset, not the page, so 21 swipes
  from Asoj 2083 landed on Asoj 2093. The screen now ignores paging updates until it scrolls back
  to the month; a UI test swipes across the recentre and lands on Asar 2085.
- Snapshot record mode never reached the simulator test process (`xcodebuild` forwards only
  `TEST_RUNNER_` variables), so `record_snapshots` compared instead of recording and the gate's
  `never` mode did nothing. `ci.sh` exports `TEST_RUNNER_SNAPSHOT_TESTING_RECORD`; run 39146539348
  recorded with it, and `month.dark.png` came back byte-identical.
- Accessibility audit: the blanket filter on digit labels is gone. UI tests pin today to
  2083-06-20 (Debug-only `NEPDATE_UI_TEST_TODAY`) and compare the findings on hidden text nodes
  with an exact table per category and kind, on the grid and on the list at the accessibility L
  size: grid 63 Dynamic Type and 2 contrast on numerals, list 10 Dynamic Type, 10 clipped and
  1 contrast on text, each with its evidence in the test and `ios/README.md`. Element detection
  and trait audits were added. The two grid contrast findings stayed at the same frames when
  the colour went from 5.74:1 to 7.55:1.
- Light secondary text 0.40 to 0.33 white: 7.55:1 on the background, at least 6.25:1 on the
  fills. References re-recorded.
- Widget size: the month screen moved to a `CalendarScreens` product linked only by the app; the
  widget executable went from 1,172,184 to 640,528 bytes.
- Picker: a UI test picks Chaitra 2199 and Baisakh 1901 and checks the page and the disabled
  next and previous buttons.

Checked and kept: run 39102808714 failed on undefined `NepDatePatro` symbols, and the
`SwiftUICore` and `UIUtilities` lines were warnings, as the agent reported; Digits resolve every
number on the grid, list, header and picker; the MR-10 AC1 label test matches the requirement
string exactly; dimmed days are not selectable.

Left for later: VoiceOver, Switch Control and the largest text size on a device (manual
checklist); the audit's list Dynamic Type and clipping predictions are tolerated on the snapshot
evidence, not a device.
