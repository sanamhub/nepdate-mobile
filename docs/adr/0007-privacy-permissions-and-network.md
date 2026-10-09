# ADR-0007: Privacy, permissions and no network

- Status: Accepted
- Date: 2026-10-06
- Deciders: Sanam Pakuwal

## Context

The best-known Nepali calendar app ships 25 tracker SDKs and 40 permissions on Android (Exodus
report, 2025-10); the iOS listings of the big two declare data "used to track you". The cleanest
competitor that users praise has 0 trackers. No Nepali calendar is on F-Droid.

## Decision

1. **No network.**
   - Android: the `INTERNET` permission is absent from the merged release manifest. CI fails
     (`./gradlew checkNoInternet`) if any dependency adds it.
   - iOS: no `URLSession`, `Network` framework or web views. CI greps the sources and checks the
     linked frameworks of the release build.
2. **No permissions requested in v1.** Calendar integration uses the system editor
   (`EKEventEditViewController`, `Intent.ACTION_INSERT`), which needs none. No notifications, no
   exact alarms, no location (the time zone choice is manual, MR-06).
   Android normal (install-time) permissions allowed: `RECEIVE_BOOT_COMPLETED` only.
3. **No SDKs that collect anything:** no analytics, crash reporting, ads, A/B testing, Firebase or
   Google Play Services.
4. **Store declarations:** App Store privacy label "Data Not Collected"; Play Data safety "No data
   collected", "No data shared"; privacy policy `docs/product/privacy-policy.md` published at a
   stable URL.
5. Bug reports come through GitHub issues the user writes, with a "Copy diagnostic info" button in
   About that copies app version, OS version and settings as text (no automatic sending).

## Consequences

- The app qualifies for F-Droid without anti-features.
- Crash insight is limited to what users report and what Apple/Google provide to the developer
  consoles for users who opted in at OS level.
- Any future network feature (data updates, ADR-0003) is a visible change: a new permission and a
  new ADR.
