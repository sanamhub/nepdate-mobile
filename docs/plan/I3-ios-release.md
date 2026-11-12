# I3: iOS release 1.0

## Status on 2026-11-05

The listing copy and screenshots are drafted. The workflow, TestFlight and submission have not
started. The maintainer has no Mac and no Apple Developer account (ADR-0010), so the plan splits
into what can be done now and what waits for the account.

| Task | Status | What is possible now |
|---|---|---|
| I3-01 Release workflow | Not started, on purpose | Nothing runnable. `ios-release.yml` and `ios-beta.yml` need the App Store Connect key and the `production` environment, so a workflow written now could not be dispatched or tested. It is written after the account exists, together with a static check like the Android one |
| I3-02 App Store listing | Drafted, waiting for the account | English name, subtitle, description, keywords, release notes and privacy URL are in `fastlane/metadata/ios/en-US`. App Privacy (Data Not Collected), age rating, App Review notes and export compliance are checked in beside them. Nepali is not an App Store Connect metadata language, so the listing stays English; the in-app language is still Nepali. Six screens in English and Nepali are rendered at 1320 by 2868 by `StoreScreenshotTests` and uploaded as `ios-store-screenshots`. The App Store Connect record is still the owner's |
| I3-03 TestFlight, then 1.0 | Blocked | Needs the account, a build, and both open rows in `docs/legal/README.md` closed with a record (ADR-0009) |
| I3-04 Human tasks | Blocked | The key check names `ios-beta.yml`, which does not exist yet; it can run only after I3-01 |

I3-04 below says the one-time setup was done in an earlier phase. This repository has no
`ios-beta.yml` and no record of an Apple account, so treat that setup as not done.

Also waiting for a device: the widget peak-memory number and the start-up and download-size numbers
of the first iOS phase. `systemExtraLargePortrait` stays deferred until the CI runner has Xcode 27.

### What the owner must buy or create

1. Apple Developer Program, individual, USD 99 a year, with an Apple ID that has two-factor
   authentication and a card that allows international payments.
2. In Certificates, Identifiers and Profiles: the bundle IDs `np.nepdate` and `np.nepdate.widgets`
   and the App Group `group.np.nepdate`.
3. In App Store Connect: the app record, the current agreements accepted, the DSA status set to
   non-trader, and an App Store Connect API key with the Admin role (cloud signing needs Admin).
4. In GitHub: the environments `testflight` and `production` with the secrets `ASC_KEY_P8`,
   `ASC_KEY_ID` and `ASC_ISSUER_ID`, and a second person as the `production` reviewer.
5. Both open legal rows closed with a record.
6. Optional, only if a problem needs Xcode's own tools (Instruments, a stubborn widget layout): a
   cloud Mac for one day. Providers bill at least 24 hours (ADR-0010).

The steps in order, with the Android ones, are in
[the 1.0 runbook](../runbooks/release-1.0.md).

Read first: ADR-0007, ADR-0009, `docs/runbooks/release-ios.md`.

## Tasks

### I3-01 Release workflow (`ios-release.yml`)
On tag `ios-v*`: preflight (tag = `MARKETING_VERSION`, changelog section, data freshness gate per
ADR-0003 §4: a Swift test fails when the library's last public holiday is less than 183 days after
the tag date, and warns for tithi), CI,
then in environment `production`: write the App Store Connect API key to `$RUNNER_TEMP`,
`xcodegen generate`, `xcodebuild archive` and `xcodebuild -exportArchive` with `destination = upload`,
both with `-allowProvisioningUpdates` and the `-authenticationKey*` flags (cloud signing,
ADR-0010), then delete the key in an `always()` step. The build lands in TestFlight; submission for
review is manual.
AC: the workflow has no `pull_request_target` trigger; the key file is absent after the job, also
when a step fails; preflight fails for a tag that differs from `MARKETING_VERSION`.

### I3-02 App Store listing
English listing (Nepali is not an App Store Connect metadata language), screenshots for the required
iPhone sizes from the snapshot set, privacy label "Data Not Collected", age rating questionnaire
(including the social-media questions: no), privacy policy URL, DSA non-trader, export compliance
`ITSAppUsesNonExemptEncryption = NO`. Review notes explain the widgets and that data is offline.

### I3-03 TestFlight, then 1.0
**Gate:** both **open** rows in `docs/legal/README.md` (Panchanga Nirnayak Samiti approval,
provenance of the event data) are closed with a record before App Store submission (ADR-0009). If the
provenance answer says the data was scraped, stop and ask the maintainer.
Internal testers, then external TestFlight (beta review), then App Store submission with phased
release.
AC: the version is live on the App Store; `docs/legal/README.md` has no open row.

### I3-04 Human tasks
The one-time setup has **not** been done: there is no Apple Developer account, no App Store Connect
key and no `ios-beta.yml` (`docs/runbooks/release-ios.md`, "One-time setup"). Before the first
release: create the account and the key, then check it works by running `ios-beta.yml`, accept the
current agreements in App Store Connect, set the DSA status to non-trader, and approve the
`production` deployment.
