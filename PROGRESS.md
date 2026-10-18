# A2 progress

Branch `feat/A2-android-widgets`, from `main` at `15137e1`. Acceptance criteria copied from
`docs/plan/A2-android-widgets.md` before implementation.

Automated criteria are complete. Device-only rows are blocked by
[issue #11](https://github.com/sanamhub/nepdate-mobile/issues/11) and are not ticked.

## A2-01 Widget model and midnight scheduler (`schedule/`)

- [x] `WidgetModel` builders for W-01–W-03 are pure functions and unit-tested.
- [x] `MidnightScheduler` computes the next local midnight in the chosen zone, arms
      `AlarmManager.setWindow(RTC, t, 10 min)`, and on fire calls `GlanceAppWidgetManager.updateAll`
      and re-arms.
- [x] `SystemChangeReceiver` handles `TIME_SET`, `TIMEZONE_CHANGED`, `LOCALE_CHANGED`,
      `BOOT_COMPLETED`.
- [x] A settings change calls `updateAll` (MR-07 AC3); the observer test completes within 1 s.
- [x] Unit tests for next midnight in Asia/Kathmandu (+05:45), America/New_York across a DST change,
      and Australia/Sydney.
- [x] Unit tests that each `WidgetModel` for BS 2083-06-20 and 2083-06-25 has the expected strings
      and holiday flag.
- [ ] Manual: `adb shell dumpsys alarm` lists exactly one alarm for `np.nepdate`, and it is not exact
      (MR-09 AC5).
- [ ] Manual: move the device clock across midnight, widgets update within the alarm window; change
      the time zone, widgets update within 1 minute (MR-05 AC1); change a setting, widgets update
      within 1 second (MR-07 AC3).

## A2-02 W-01 Today and W-02 Today and upcoming

- [x] `SizeMode.Responsive` with 2×2, 4×1 and 4×2 breakpoints.
- [x] `GlanceTheme` with dynamic colour.
- [x] Holiday mark H1.
- [x] Tap actions per `widgets.md`.
- [x] W-01 AC1–AC3 and W-02 AC1–AC2 as Roborazzi (Glance) screenshots at every breakpoint, light,
      dark, Nepali, each opacity, on a holiday (2083-06-25) and a normal day (MR-05 AC2).
- [x] A unit test that the tap action of each area carries the right deep-link extra (MR-05 AC3).

## A2-03 W-03 Month

- [x] Grid in Glance `Column`/`Row` (no bitmaps).
- [x] Previous, next and today via `actionRunCallback` and Glance state.
- [x] Day tap opens `MainActivity` with a deep-link extra.
- [x] Resets to the current month at midnight.
- [x] W-03 AC1, AC2 and AC4 at 4×3, 4×4 and 5×5: screenshots of Jestha 2083 (6 weeks) without
      clipping.
- [x] A unit test that the callbacks move the shown month by one and "Today" restores it.
- [x] A unit test that the midnight update clears the shown month.

## A2-04 Lock screen eligibility, configuration and previews

- [x] `widgetCategory="home_screen|keyguard"` on W-01 and W-02.
- [x] Configuration activity with the opacity setting.
- [x] `providePreview` generated previews (Android 15+) with a holiday sample.
- [x] Static `previewLayout` fallback.
- [x] Descriptive names and descriptions in English and Nepali.
- [ ] Tier 1 checklist in `docs/runbooks/manual-test.md` ticked. Blocked by issue #11.
- [ ] W-04 on the lock screen of an Android 16 QPR1+ device opens the app only after unlock.
      Blocked by issue #11.

## A2-05 Widget size and memory check

- [x] No bitmaps; log the RemoteViews parcel size in a debug build.
- [x] A CI check fails if `Bitmap` or `ImageProvider(` with a bitmap appears under `widget/`.
- [x] The logged parcel size of W-03 at 5×5 is recorded in `docs/benchmarks.md` (MR-09 AC4).
