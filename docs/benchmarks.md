# Benchmarks

MR-09 targets: cold start to a drawn month grid ≤ 400 ms on the iPhone 12 and the Galaxy A15 class,
≤ 1 s on the Android Go class; month swipe holds 60 fps on the A15 class; universal APK ≤ 8 MB and
store download ≤ 6 MB.

## Android

### 2026-11-11 on `cfaf1bd`

Measured on the Gradle Managed Device (Pixel 6, API 34, x86_64, software GPU) by
[run 43033742105](https://github.com/sanamhub/nepdate-mobile/actions/runs/43033742105). These are
emulator numbers, not the owner's phone.

| Test | Metric | Min | Median | Max |
|---|---|---:|---:|---:|
| `coldWithProfile` | `timeToFullDisplayMs` | 1637.7 | 1821.7 | 2061.1 |
| `coldWithoutProfile` | `timeToFullDisplayMs` | 1779.4 | 2043.1 | 2154.7 |
| `warmWithProfile` | `timeToFullDisplayMs` | 693.6 | 824.2 | 1157.6 |
| `warmWithoutProfile` | `timeToFullDisplayMs` | 756.9 | 840.8 | 1049.0 |

| Test | Metric | Min | Median | Max |
|---|---|---:|---:|---:|
| `monthSwipesWithProfile` | `frameCount` over 10 swipes | 232 | 239 | 244 |
| | `frameDurationCpuMs` P50 | - | 48.5 | - |
| | `frameDurationCpuMs` P90 | - | 86.6 | - |
| | `frameDurationCpuMs` P99 | - | 107.5 | - |
| | `frameOverrunMs` P50 | - | 50.3 | - |
| `monthSwipesWithoutProfile` | `frameCount` over 10 swipes | 231 | 234 | 245 |
| | `frameDurationCpuMs` P50 | - | 46.3 | - |
| | `frameDurationCpuMs` P90 | - | 85.0 | - |
| | `frameDurationCpuMs` P99 | - | 106.8 | - |
| | `frameOverrunMs` P50 | - | 45.3 | - |

Against the 2026-11-10 run on `chore/ios-quality`, cold start with the profile is 1821.7 ms instead
of 3251.4 ms and the swipe frame P50 is 48.5 ms instead of 86.0 ms. There is no start-up regression
against the M2-02 numbers; the earlier run was the slower one.

### Earlier runs and size

Measured on 2026-10-18 on `32a16d5` after the rebase onto `main` (the rebase changed no Android
code).

| Device | Cold start, median of 10 `timeToFullDisplayMs` | Month swipe, 10 swipes | Result |
|---|---:|---:|---|
| Samsung Galaxy A15 class (Android 14, 4 GB) | Not measured | Not measured | Blocked, issue [#11](https://github.com/sanamhub/nepdate-mobile/issues/11) |
| Android Go class (Android 11, 2 GB) | Not measured | Not applicable | Blocked, issue [#11](https://github.com/sanamhub/nepdate-mobile/issues/11) |

| Size | Value | Target | Result |
|---|---:|---:|---|
| Universal release APK at A1, `app-release-unsigned.apk` | 1,659,769 bytes (1.66 MB) | ≤ 8 MB | Pass |
| Universal release APK at A2, `app-release-unsigned.apk` | 2,392,371 bytes (2.28 MB) | ≤ 8 MB | Pass |
| Tester APK at the 1.0.0-beta.1 pull request (run [42748845548](https://github.com/sanamhub/nepdate-mobile/actions/runs/42748845548)) | 2,913,845 bytes (2.78 MB) | ≤ 8 MB | Pass |
| Tester APK on 2026-11-11 (run [43034701342](https://github.com/sanamhub/nepdate-mobile/actions/runs/43034701342)) | 2,933,937 bytes (2.80 MB) | ≤ 8 MB | Pass, +0.69 % against the beta pull request and under the 5 % review line |
| Play download size | Pending the release workflow | ≤ 6 MB | Pending |
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

## iOS

These are simulator numbers from the `iPhone 17` on iOS 26.5. They are not device numbers and are
not a gate; the shared runner cannot judge an absolute launch time or frame rate.

| Measurement | Value | Result |
|---|---|---|
| Widget peak memory on a device with W-01, W-02, W-03 and every W-04 family placed and W-03 paged ten times | Not measured | Needs a device; there is no Apple account yet, so no TestFlight build |
| `WidgetTimelinePerformanceTests.testMemory`, peak resident size of the test process around building 8 month entries (CI run [42011732710](https://github.com/sanamhub/nepdate-mobile/actions/runs/42011732710)) | 347,078,656 bytes before, 369,410,048 bytes after | A regression signal only: the process also holds the app and the test frameworks |
| Release `NepDate.app` bundle (run [43027874744](https://github.com/sanamhub/nepdate-mobile/actions/runs/43027874744)) | 6,740 KB | Contains the widget extension |
| Release `NepDateWidgets.appex` bundle (same run) | 1,824 KB | The widget extension |
| Release `NepDate` executable (same run) | 4,688,720 bytes | App executable |
| Release `NepDateWidgets` executable (same run) | 1,771,008 bytes | Widget executable |
| `PerformanceUITests.testAppLaunch` (`XCTApplicationLaunchMetric`, run [43034213474](https://github.com/sanamhub/nepdate-mobile/actions/runs/43034213474)) | 5.029 s average of five launches, relative standard deviation 20.175 % | Report only |
| `PerformanceUITests.testMonthSwipe` (`XCTOSSignpostMetric.scrollDecelerationMetric`, same run) | 0.401 s average of five swipes, relative standard deviation 21.391 % | Report only |
| Earlier `PerformanceUITests.testAppLaunch` | 3.937 s average of five launches, relative standard deviation 20.3 % (run [42853920542](https://github.com/sanamhub/nepdate-mobile/actions/runs/42853920542)) | Report only |

The app and widget bundle sizes grew with the parity screens and widgets: the earlier executable
numbers recorded in `ios/README.md` were 2,515,160 and 1,219,656 bytes. The size gate still prints
them and gates none of them.

The launch number is above the earlier 3.937 s run, but the two runs are on different commits and
the metric's own relative standard deviation is about 20 %, so this is a simulator observation, not
a device regression. No launch-path code changed in this measurement. The Android cold start, which
is measured the same way on the same emulator, improved.
