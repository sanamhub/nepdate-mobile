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
- [ ] Negative control: a throwaway draft PR with a non-deterministic build change makes the check
      fail once, then the PR is closed and its branch deleted.
- [x] AC locally: `D094892C14CE1F777213E8E89CE74F699AB5048606F6F22D7EC402F69FB46DBC` from two
      `clean :app:assembleRelease` builds, both 2,392,322 bytes.

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

- `./gradlew ci`: `BUILD SUCCESSFUL in 54s`, 192 tests, 0 failures, 1 skipped (the release-only
  freshness test when `RELEASE_DATE` is absent).
- `python tools/check_android_release_workflow.py`: positive check and five negative fixtures pass.
- `actionlint 1.7.12`: clean on `android.yml` and `android-release.yml`.
- Store screenshots generated twice with identical SHA-256 values.

## Pending before the checkpoint report

- GitHub Android CI run URL.
- The reproducibility negative-control draft PR.
- The two CI APK checksums.

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
