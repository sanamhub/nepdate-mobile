# ADR-0009: Distribution and releases

- Status: Accepted
- Date: 2026-10-06
- Deciders: Sanam Pakuwal

## Decision

**Versions:** semver per app, tags `ios-vX.Y.Z` and `android-vX.Y.Z`, changelogs in each folder.
Release notes come from the changelog section. Bundle / application ID `np.nepdate` (human confirms
availability before M0).

**iOS**
- App Store only. Free, no in-app purchases. DSA status: non-trader.
- Build and upload on a GitHub-hosted macOS runner with `xcodebuild archive` + `-exportArchive`
  (`method = app-store-connect`, `destination = upload`), signed by cloud-managed certificates
  through an App Store Connect API key (`.p8`, Admin role, which Apple requires for cloud signing)
  kept in the `testflight` and `production` environment secrets. No `.p12`, no fastlane, no Mac
  (ADR-0010).
- TestFlight internal testing first, then App Store review after the `production` approval.

**Android**
- Channels: Google Play, IzzyOnDroid and F-Droid, GitHub Releases (also used by Obtainium).
- **One signing key for every channel:** the upload key is enrolled as the Play app signing key
  ("use my own key"), so the same signed APK can be installed from any channel and updated from
  another. The key lives only in the `production` environment secrets and an offline backup.
- Reproducible builds (ADR-0005) so F-Droid publishes the developer-signed APK and IzzyOnDroid can
  verify it.
- Play: new personal developer accounts must run a closed test with 12 testers for 14 days before
  production; plan it in A3. Gradle Play Publisher with a service-account JSON in secrets.
- **Android developer verification:** register the package name and signing key in the Android
  Developer Console before global enforcement in 2027, so sideloaded installs keep working.

**Release gates (both):** CI green, data freshness gate (ADR-0003 §4), manual test checklist done on
the reference devices (MR-09), `production` environment approval by a second person (org §12).
Before the first public release only: both **open** rows in `docs/legal/README.md` (Panchanga
Nirnayak Samiti approval, provenance of the event data) are closed with a record; while the
provenance row is open, no build that contains the event data is published.
Rollback: halt staged rollout (Play) or remove from sale / expedite a fix (App Store); GitHub release
marked pre-release.

## Consequences

- Users can move between Play, F-Droid and GitHub builds without uninstalling.
- No store-specific code paths: every Android channel ships the same APK.
