# ADR-0011: WorkManager only as Glance's dependency

- Status: Accepted
- Date: 2026-10-18
- Deciders: Sanam Pakuwal
- Amends: ADR-0005 (dependency list) and ADR-0006 (Android refresh)

## Context

ADR-0005 lists WorkManager among the libraries the Android app does not use, and ADR-0006 says the
widgets refresh with one inexact alarm and "no WorkManager". Glance 1.2.0, the widget toolkit
ADR-0006 chose, depends on `androidx.work:work-runtime` 2.7.1: every Glance session runs as a
`SessionWorker`. Glance cannot be used without it.

The WorkManager manifest asks for `WAKE_LOCK`, `ACCESS_NETWORK_STATE`, `FOREGROUND_SERVICE` and
`RECEIVE_BOOT_COMPLETED`. ADR-0007 allows only `RECEIVE_BOOT_COMPLETED`.

What the A2 review checked in the 2.7.1 artifacts:

- `PowerManager` wake locks are taken only in `Processor.startForeground` and in the
  `systemalarm` package (`SystemAlarmService`, `SystemAlarmDispatcher`, `DelayMetCommandHandler`).
- `SystemAlarmService` is enabled by `enable_system_alarm_service_default`, which is `true` below
  API 23 and `false` from API 23 (`res/values-v23`). With `minSdk = 26` the app always schedules
  through `SystemJobService`, where the system holds the wake lock for the job.
- Glance 1.2.0 never calls `setForeground` or builds a `ForegroundInfo`, so the foreground path is
  not reached.

## Decision

1. WorkManager stays in the app only as Glance's transitive dependency. App code never imports
   `androidx.work`. `./gradlew :app:checkNoWorkManagerUse` (part of `ci`) fails if any Kotlin file
   under `app/src` does.
2. The three extra permissions are removed in the app manifest with `tools:node="remove"`.
   `checkPermissions` reads the merged release manifest, so a dependency that adds them back fails
   CI.
3. The midnight refresh stays as ADR-0006 describes it: one inexact `setWindow` alarm, no periodic
   or one-off work requests of our own.
4. `minSdk` stays at 26 or higher while this ADR holds. Lowering it below 23 would enable the alarm
   path that needs `WAKE_LOCK` and needs a new ADR.

## Consequences

- The release APK carries WorkManager's classes and its disabled-by-default receivers.
- Widget updates on a real device without the three permissions are not yet verified. The check is
  on the A2 Tier 1 list in `docs/runbooks/manual-test.md` and blocked by issue #11 until hardware is
  available.
- A Glance upgrade that starts using foreground work or a new permission fails `checkPermissions`
  and needs a new ADR before it can merge.
