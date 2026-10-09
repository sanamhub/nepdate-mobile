# I3: iOS release 1.0

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
The one-time setup was done in M0-07 (`docs/runbooks/release-ios.md`, "One-time setup"). Before the
first release: check the Admin API key still works by running `ios-beta.yml`, accept the current agreements in
App Store Connect, set the DSA status to non-trader, and approve the `production` deployment.
