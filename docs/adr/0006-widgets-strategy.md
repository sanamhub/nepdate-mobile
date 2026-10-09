# ADR-0006: Widgets: data flow, refresh and limits

- Status: Accepted
- Date: 2026-10-06
- Deciders: Sanam Pakuwal

## Context

The widget catalogue is `docs/product/widgets.md`. Competitor reviews (`docs/competitors.md`) show
the most common failures: widgets stuck on "loading", blank, or showing yesterday's date.

## Decision

**Data flow (both platforms)**
- A widget renders only from data computed for it: the library is called in the timeline provider
  (iOS) or `provideGlance` (Android), and the result is a small immutable "widget model" (strings,
  numbers, flags). No disk reads at render time except the settings store; no network ever.
- "Today" is always computed from the current clock in the zone chosen in Settings (MR-06), never
  stored. A stale refresh can therefore never show a wrong date once the widget re-renders.

**iOS refresh**
- Timeline: entries at each local midnight for the next 7 days, policy `.atEnd`. That costs one reload
  a week in normal use, far below the 40–70 per day budget.
- `WidgetCenter.shared.reloadAllTimelines()` when settings change, when the app opens on a new day,
  and on `NSSystemTimeZoneDidChange` / `significantTimeChange` while the app runs.
- Month widget navigation (W-03) uses App Intents that write the shown month to the App Group and
  reload that widget kind only.
- Memory budget: the extension stays ≤ 15 MB peak (MR-09 AC4): system fonts, no images except SF
  Symbols, event lookups only for the visible range.

**Android refresh**
- `updatePeriodMillis = 0`. One **inexact** alarm (`AlarmManager.setWindow`, 10-minute window) at the
  next local midnight, re-armed each time it fires. No exact alarms, no WorkManager.
- A manifest receiver for `ACTION_TIME_SET`, `ACTION_TIMEZONE_CHANGED`, `ACTION_LOCALE_CHANGED` and
  `BOOT_COMPLETED` (all exempt from the implicit-broadcast limits) calls `updateAll` and re-arms.
- Opening the app also calls `updateAll`.
- Glance state holds the month shown by W-03; `actionRunCallback` changes it.
- No bitmaps in RemoteViews (API 37 memory cap); colours and drawables by resource ID.

**Rendering rules**
- Holiday mark H1 in every rendering mode (`widgetRenderingMode` on iOS; dynamic colour and the
  opacity setting on Android).
- Previews: iOS placeholder and snapshot use a fixed sample date with a holiday; Android generated
  previews (Android 15+) plus a static `previewLayout` fallback.

**Later**
- Apple Watch complications reuse the W-04 accessory views in a watchOS target (Swift runs on every
  watch architecture). Wear OS waits for `glance-wear` to be stable. Both need an ADR.

## Consequences

- On Android, a heavily battery-restricted device may draw the new date some minutes after
  midnight; the date is still right whenever the widget renders.
- No widget depends on the app having been opened recently.
