# Benchmarks

MR-09 targets: cold start to a drawn month grid ≤ 400 ms on the iPhone 12 and the Galaxy A15 class,
≤ 1 s on the Android Go class; month swipe holds 60 fps on the A15 class; universal APK ≤ 8 MB and
store download ≤ 6 MB.

## Android

Measured on 2026-10-18 at A1-07 (`32a16d5` after the rebase onto `main`; the rebase changed no
Android code).

| Device | Cold start, median of 10 `timeToFullDisplayMs` | Month swipe, 10 swipes | Result |
|---|---:|---:|---|
| Samsung Galaxy A15 class (Android 14, 4 GB) | Not measured | Not measured | Blocked, issue [#11](https://github.com/sanamhub/nepdate-mobile/issues/11) |
| Android Go class (Android 11, 2 GB) | Not measured | Not applicable | Blocked, issue [#11](https://github.com/sanamhub/nepdate-mobile/issues/11) |

| Size | Value | Target | Result |
|---|---:|---:|---|
| Universal release APK at A1, `app-release-unsigned.apk` | 1,659,769 bytes (1.66 MB) | ≤ 8 MB | Pass |
| Universal release APK at A2, `app-release-unsigned.apk` | 2,392,371 bytes (2.28 MB) | ≤ 8 MB | Pass |
| Play download size | Pending A3-02 | ≤ 6 MB | Pending |
| W-03 RemoteViews parcel at 5×5, debug | 86,268 bytes (84.2 KiB) | API 37 cap, no bitmaps | Pass, measured 2026-10-18 by `WidgetParcelSizeTest` |

The widget parcel size is a build-time RemoteViews measurement, not a device memory measurement. The
API 37 memory cap on a device remains blocked by issue
[#11](https://github.com/sanamhub/nepdate-mobile/issues/11), like the other hardware checks.

The APK was built with `.\gradlew.bat :app:assembleRelease` after R8 full mode and resource
shrinking. The benchmark tests are in `:baselineprofile`:

- `StartupBenchmark.coldStartup` uses `StartupMode.COLD`, `StartupTimingMetric` and 10 iterations.
- `MonthSwipeBenchmark.monthSwipes` uses `FrameTimingMetric`, a warm start and 10 swipes.

`BaselineProfileGenerator` swipes the month three times after the cold start. The generated profile
is not checked in: this environment has no Android reference device or emulator system image, so
`generateBaselineProfile` could not run. The open issue above also tracks generating and committing
that profile after the device measurements.
