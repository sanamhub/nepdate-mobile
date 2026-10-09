# A3: Android release 1.0

Read first: ADR-0007, ADR-0009, `docs/runbooks/release-android.md`.

## Tasks

### A3-01 Reproducible release build
Per ADR-0005: fixed JDK in CI, `vcsInfo { include = false }`, no timestamps, PNG crunching off,
Baseline Profile checked in. A CI job builds the release APK twice in clean containers and compares
checksums.
AC: two clean builds give byte-identical unsigned APKs.

### A3-02 Release workflow (`android-release.yml`)
On tag `android-v*`: preflight (tag = `versionName`, changelog section, data freshness gate per
ADR-0003 §4: a JVM test takes the last date of the library's public holidays and fails when it is
less than 183 days after the tag date; it prints a warning, not a failure, for tithi), CI,
build and sign AAB + APK with the key from the `production` environment, `SHA256SUMS`,
build-provenance attestation (`actions/attest`), draft GitHub Release with the APK, then upload the
AAB to the Play **internal** track with Gradle Play Publisher. Promotion to closed / production
tracks is manual in Play Console.
AC: the signing and upload jobs run only in environment `production`; preflight fails for a tag that
differs from `versionName` and for a release date that breaks the freshness gate (unit test with a
fixed date); the draft release has the APK, `SHA256SUMS` and an attestation.

### A3-03 Store listings
Play listing (English; Nepali listing text too, since Play supports `ne-NP`), screenshots from the
Roborazzi set on device frames, Data safety "No data collected / shared", content rating, privacy
policy URL (GitHub Pages of `docs/product/privacy-policy.md`). `fastlane/metadata/android/` layout in
the repository so F-Droid and IzzyOnDroid read the same text and screenshots.

### A3-04 Closed test, then 1.0
**Gate:** both **open** rows in `docs/legal/README.md` (Panchanga Nirnayak Samiti approval,
provenance of the event data) are closed with a record before the production rollout, the GitHub
Release, IzzyOnDroid or F-Droid (ADR-0009). If the provenance answer says the data was scraped, stop
and ask the maintainer.
Closed test with at least 12 testers for 14 days (Play rule for new personal accounts); fix what they
report; then production with a staged rollout (20 % → 100 %). Submit to IzzyOnDroid, open the F-Droid
`fdroiddata` merge request with the reproducible-build metadata.
AC: the app is live on Play production, IzzyOnDroid and GitHub Releases at the same `versionCode`;
the `fdroiddata` merge request is open; `docs/legal/README.md` has no open row.

### A3-05 Human tasks
Create the Play Console personal developer account; generate the signing key (offline backup), enrol
it as the Play app signing key, register the package and key for Android developer verification;
create the Play service account for Gradle Play Publisher; store the key and the service-account JSON
in the `production` environment secrets.
