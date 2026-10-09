# A2: Android widgets (Glance)

Read first: ADR-0006, `docs/product/widgets.md`, Android
[widget quality guidelines](https://developer.android.com/docs/quality-guidelines/widget-quality).

## Tasks

### A2-01 Widget model and midnight scheduler (`schedule/`)
`WidgetModel` builders for W-01–W-03 (pure functions, unit-tested). `MidnightScheduler`: computes the
next local midnight in the chosen zone, arms `AlarmManager.setWindow(RTC, t, 10 min)`, and on fire
calls `GlanceAppWidgetManager` `updateAll` and re-arms. `SystemChangeReceiver` for `TIME_SET`,
`TIMEZONE_CHANGED`, `LOCALE_CHANGED`, `BOOT_COMPLETED`. A settings change calls `updateAll`
(MR-07 AC3).
AC:
- Unit tests for next-midnight in Asia/Kathmandu (+05:45), America/New_York across a DST change,
  and Australia/Sydney.
- Unit tests that each `WidgetModel` for BS 2083-06-20 and 2083-06-25 has the expected strings and
  holiday flag.
- `adb shell dumpsys alarm` lists exactly one alarm for `np.nepdate`, and it is not exact (MR-09
  AC5, manual checklist).
- Manual checklist: move the device clock across midnight, widgets update within the alarm window;
  change the time zone, widgets update within 1 minute (MR-05 AC1); change a setting, widgets update
  within 1 second (MR-07 AC3).

### A2-02 W-01 Today and W-02 Today and upcoming
`SizeMode.Responsive` with 2×2, 4×1, 4×2 breakpoints; `GlanceTheme` with dynamic colour; holiday
mark H1; tap actions per widgets.md.
AC: W-01 AC1–AC3 and W-02 AC1–AC2 as Roborazzi (Glance) screenshots at every breakpoint, light, dark,
Nepali, each opacity, on a holiday (2083-06-25) and a normal day (MR-05 AC2); a unit test that the
tap action of each area carries the right deep-link extra (MR-05 AC3).

### A2-03 W-03 Month
Grid in Glance `Column`/`Row` (no bitmaps); previous/next/today via `actionRunCallback` and Glance
state; day tap opens `MainActivity` with a deep-link extra; resets to the current month at midnight.
AC: W-03 AC1, AC2, AC4 at 4×3, 4×4 and 5×5: screenshots of Jestha 2083 (6 weeks) without clipping;
a unit test that the callbacks move the shown month by one and "Today" restores it; a unit test that
the midnight update clears the shown month.

### A2-04 Lock screen eligibility, configuration and previews
`widgetCategory="home_screen|keyguard"`; configuration activity with the opacity setting;
`providePreview` generated previews (Android 15+) with a holiday sample and a static
`previewLayout` fallback; descriptive names and descriptions in English and Nepali.
AC: Tier 1 checklist in `docs/runbooks/manual-test.md` ticked; W-04 on the lock screen of an
Android 16 QPR1+ device opens the app only after unlock (manual checklist).

### A2-05 Widget size and memory check
No bitmaps; log the RemoteViews parcel size in a debug build.
AC: a CI check fails if `Bitmap` or `ImageProvider(` with a bitmap appears under `widget/`; the logged
parcel size of W-03 at 5×5 is recorded in `docs/benchmarks.md` (MR-09 AC4).
