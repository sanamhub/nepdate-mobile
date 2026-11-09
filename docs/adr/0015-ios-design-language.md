# ADR-0015: iOS design language

- Status: Accepted
- Date: 2026-11-09
- Deciders: Sanam Pakuwal
- Relates to: ADR-0001 (native UI per platform), ADR-0004 (iOS architecture), ADR-0014 (Xcode 27)

## Context

The parity work gives the iOS app every feature the Android app has: search, a year view, first run,
appearance, the extra widgets and the rest. That raises a question the earlier scope never had to
answer, because iOS had fewer screens: when Android is the reference for *what* a screen does, how
much of *how it looks* crosses over?

Two things pull in opposite directions. The product is one brand on two platforms, and a Nepali patro
looks a certain way: the marigold today mark, the sindoor holiday red, the big BS numeral. But iOS
users have system habits, and since iOS 26 the system draws Liquid Glass around bars, sheets and
controls whether an app asks for it or not. Copying Material 3 chrome to iOS produces an app that
looks like an Android app running on an iPhone, which is exactly what ADR-0001 decided against.

There is also a hard deadline effect from ADR-0014: from the iOS 27 SDK,
`UIDesignRequiresCompatibility` is ignored, so there is no opt-out of the new bar and sheet
treatment. The design language has to work *with* the platform's materials, not around them.

## Decision

1. **Brand versus chrome.** The brand is shared: colours, the brand marks, the type roles and the
   words. The chrome is native: tab bars, navigation bars, toolbars, sheets, lists, pickers, menus,
   alerts, and the glass the system puts on them. No Material chrome on iOS (no floating pill bar, no
   connected button group, no Material dialog), and no copying iOS chrome to Android. The shared part
   is design tokens and marks, not components.
2. **Glass rules.** Glass comes from system components only: bars, sheets, toolbars, standard buttons
   and menus. The content layer has no glass, per Apple's guidance for the iOS 27 design language.
   - One custom `glassEffect` is allowed: the floating Today button, if a screen needs one.
   - It is written behind `#available(iOS 26, *)`, which ADR-0004 already permits, and falls back to
     `.regularMaterial` before that.
   - One place, not a helper the whole app reaches for. A second use needs this ADR amended.
3. **No iOS 27-only UI APIs in v1.** Their names are still unsettled in the SDK and there are crash
   reports against the first 27.0 releases. The single exception stays what ADR-0004 already allows:
   `systemExtraLargePortrait`, behind `#available(iOS 27, *)`, because it is a widget family the
   product wants and it has no replacement.
4. **Language** is chosen through the system's per-app Language page, which iOS 18 exposes in Settings
   for any app with more than one localisation. The in-app row that opens that page stays: it is the
   native pattern, and it is what makes the setting findable. Digits keep being formatted by the
   library (ADR-0004), not by `NumberFormatter` in a view.
5. **Marks, not images.** The four brand marks are SwiftUI `Shape`s in `CalendarUI`
   (`SunnyShape`, `SoftBurstShape`, `DiamondShape`, `CookieShape`), so the app and the widget
   extension share one definition and each mark can be snapshot-tested in a fixed rect.
6. **Surfaces stay system.** `systemBackground`, `secondarySystemGroupedBackground` and the grouped
   list colours. The Android cream surface palette is not ported; a Nepali patro has no business
   being cream on iOS.
7. **Type is system type.** Text styles only (`.body`, `.title2`, `.caption2`), `.rounded` design with
   `.monospacedDigit()` for numerals. No fixed heights, no `lineLimit(1)` with `minimumScaleFactor` on
   Devanagari: a conjunct that does not fit wraps instead of shrinking.

## Consequences

- A screen looks native on each platform while the brand stays recognisable: the marigold today mark,
  the holiday red, the Sunny, SoftBurst, Diamond and Cookie marks, and the same words.
- Review has a rule for the grey area: if a widget or a control is a *brand mark*, it is shared; if it
  is *chrome*, it comes from the system.
- The one custom glass surface is a maintenance point: when the SDK renames or changes
  `glassEffect`, one button changes, not the app.
- Two things stay platform-specific on purpose: Android's wallpaper colours and its status-bar date
  and notification settings have no iOS equivalent, and iOS's tab bar has no Android equivalent
  (ADR-0013 is Android-only).
- The iOS app is ready for the Liquid Glass era without a compatibility flag, which is what ADR-0014
  makes necessary anyway.

## Alternatives considered

- **Share the Material 3 components on both platforms** (a multiplatform design system). Cheaper to
  build once, but it fights the platform on both sides and undoes ADR-0001.
- **Port the Android surfaces and chips to iOS** so the two apps look identical. Users of each
  platform would see something foreign, and the Liquid Glass bars would clash with the copied chrome.
- **Allow `UIDesignRequiresCompatibility` while it lasts.** It stops working with the iOS 27 SDK, so it
  would only postpone the same decisions by one release cycle.
- **Use the iOS 27 design APIs now** for the new screens. Their names are unsettled and the crash
  reports are fresh; waiting costs nothing because the standard components already give the same
  appearance.
