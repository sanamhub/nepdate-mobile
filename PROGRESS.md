# Progress

Current phase: A3, Android release readiness. Reviews of merged phases follow it.

## A3 Android release readiness

Branch `feat/A3-android-release-ready`, from `main` at `c9935fd`. Acceptance criteria copied from
`docs/plan/A3-android-release.md` before implementation. A3-04 and A3-05 are human-gated and are not
started.

## I2 iOS widgets

Branch `feat/I2-ios-widgets`, from `main` at `302eb0a`. The plan is
`docs/plan/I2-ios-widgets.md`.

| Task | Status |
|---|---|
| I2-01 Timeline | Done: one entry plus seven local midnights, tested across Kathmandu, New York and Sydney DST |
| I2-02 W-01 and W-02 | Done: Today and upcoming views, `AppRoute` builds and parses the URLs and the app opens them, eight references, 35 in total |
| I2-03 W-03 | Not started |
| I2-04 W-04 and W-05 | Not started |
| I2-05 Refresh and memory | Not started |

- `systemExtraLargePortrait` is deferred until the runner has Xcode 27.
- The widget peak-memory number is a manual device check and will be recorded as **not measured**.
  There is no `ios-beta.yml` and no rented Mac.
- The snapshot matrix keeps a reference only for rendering modes whose pixels differ. The accented
  and vibrant renders of W-01 and W-02 are byte-identical to full colour, so both are collapsed; the
  equality test passed. The screenshot artefact has 12 images.

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
- [ ] A Baseline Profile is checked in. Blocked by issue #11: `:baselineprofile` needs a device to
      generate the app's rules. AGP already merges the AndroidX and kotlinx library profiles from
      their AARs, so a checked-in copy of those rules adds nothing and is not used as a stand-in.
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
- [x] Preflight checks that the tag equals the `versionName` read from `build.gradle.kts`, that `android/CHANGELOG.md` has the
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
      environment, `dry_run` default and the production gates, with one negative fixture per rule and six controls that edit the real workflow in memory.
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

## I1-05 iOS screens

Branch `feat/I1-05-ios-screens`, from `main` at `f3b3f3b`. Acceptance criteria copied to
`docs/plan/I1-ios-app.md` before code (I1-05-00).

| Task | Status |
|---|---|
| I1-05-00 Acceptance criteria | Done before code (commit `3d656b2`) |
| I1-05-01 Upcoming and Converter | Done: view models, tests and eight references |
| I1-05-02 Settings | Done: store-backed choices, language row and suggestion |
| I1-05-03 About and version gate | Done: five links, diagnostic copy and `NepDateLibraryVersion` gate |
| I1-05-04 Localisation gate | Done: every key has a `ne` value |
| I1-05-05 Copy confirmation | Done: visible line, announcement and snapshot |

## Local checks

- `./gradlew ci`: `BUILD SUCCESSFUL in 3m 31s`, 192 tests, 0 failures, 1 skipped (the release-only
  freshness test when `RELEASE_DATE` is absent).
- GitHub Android CI: run
  [39150421202](https://github.com/sanamhub/nepdate-mobile/actions/runs/39150421202) green. `Gradle
  ci` took 3m 48s, the two APK jobs took 3m 16s and 2m 32s, and the checksum compare took 6s.
- `python tools/check_android_release_workflow.py`: positive check, five negative fixtures and six
  in-memory controls pass.
- `actionlint 1.7.12`: clean on `android.yml` and `android-release.yml`.
- Store screenshots generated twice with identical SHA-256 values.

## Out of scope

- A3-04 (closed test, production rollout, IzzyOnDroid, F-Droid) is human-gated and not started.
- A3-05 (Play account, signing key, service account, environment secrets) is human-gated and not
  started. No tag, release, signing key, secret or repository setting is created in this phase.

## A3 review (part 1, PR #18)

Reviewed at `ded63ff`; fixes on the branch.

- The tag check compared the tag with itself: preflight took the version from the tag, then checked
  the tag against it. A tag `android-v0.1.0` with a `[0.1.0]` changelog section would have passed
  while `versionName` stayed `0.0.1`. The version now always comes from `build.gradle.kts`.
- The Play upload job ran Gradle without checking out `nepdate-kotlin`, which `settings.gradle.kts`
  includes, so it would have failed to configure on the first real release.
- The Play upload and the draft release used the downloaded artifacts without checking them. Both
  now run `sha256sum --check --strict SHA256SUMS` first, and the release attaches the AAB so every
  line of `SHA256SUMS` can be checked.
- The signing job now removes the decoded keystore in an `always()` step.
- The reproducible-build jobs restored the shared Gradle cache with `org.gradle.caching=true`, so
  both APKs could reuse the same cached task outputs and match without being built. Both jobs now
  run with `cache-disabled: true` and `--no-build-cache`.
- `android/app/src/main/baseline-prof.txt` was a copy of the library profiles AGP merges anyway; it
  and `checkBaselineProfile` are removed and the AC stays open on issue #11.
- The static check also requires: only the release job has `contents: write`, the exact `needs` of
  each job, `nepdate-kotlin` in every job that runs Gradle, and the checksum and keystore steps,
  each with an in-memory negative control.

Checked and kept: the known-data copy matches the hub section at `6965896` apart from line wrapping;
the About test reads every static string in `AboutLinks.kt`; the freshness gate uses the UTC date,
fails holidays below 183 days and only warns for tithi and events; listing lengths are within Play
limits.

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

## I1-04 review (iOS day detail, PR #20)

Reviewed at `a97288d`, with `main` (`1226b35`) merged into the branch.

- Add to calendar used `Calendar.current` to turn the AD year, month and day into a date. With the
  device calendar set to Buddhist or Japanese, 2026 is read as a year of that era, so the event
  landed centuries away. `DayEvent.startOfDay` now always uses a Gregorian calendar; a test checks
  25 September 2026 at midnight in Kathmandu, Kiritimati and Los Angeles and that a Buddhist
  calendar reads the same instant as BE 2569. The event itself uses the current time zone, since
  EventKit keeps an all-day event's dates there (run 39196207345 showed other zones are rewritten).
- The all-day event ended at the next midnight, which the system calendar can show as a two-day
  event. It now ends on the day it starts, and EventKit stores 23:59:59 of that day (seen in run
  39196207345); the test checks the end is on the start day. The manual checklist row (MR-02 AC2) asks for a
  single-day event and one run with a Buddhist or Japanese device calendar, since CI has no device.
- The holiday-only title branch was untested: the test named for it used Asoj 9, which has events.
  The rule is now `DayDetailViewModel.calendarTitle(events:isPublicHoliday:bsDate:publicHoliday:)`,
  and a test covers the holiday, plain-date and first-event branches with synthetic input.
- The Baisakh 1 2084 test expects "New Year 2083", which is the hub data as it stands (hub
  `known-questions.json`, new-year-name); a comment says the test fails, as it should, once the
  corrected data is synced.

Checked and kept: Asoj 9 2083 matches the hub (holiday, Chaturdashi, three events in file order,
scope whole); Baisakh 1 2084 has a holiday and an event and no tithi; BS 2090 shows the no-data
message; the Devanagari line keeps Devanagari digits; Copy date copies `bsDate`, the line on
screen, in the app language and resolved Digits; no `requestAccess`, default calendar or
authorization read anywhere, and the paste read is test-only; on iOS 18 the out-of-process
`EKEventEditViewController` needs no calendar usage string; the widget target links only
`CalendarKit`, and moving `MonthViewModel` and `MonthKey` to `CalendarScreens` duplicates nothing.
W-03 (I2) may move `MonthKey` back into a small shared module; that is acceptable. The record
mode default stays `never` and `SNAPSHOT_REFERENCE_COUNT` is 10.

Left for later: the Copy date button's accessibility value is the copied date, which VoiceOver
reads, but there is no visible confirmation; the sheet audit runs at the default text size only,
with `.accessibility2` covered by its snapshot.

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
