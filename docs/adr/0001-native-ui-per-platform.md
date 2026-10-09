# ADR-0001: Native UI on each platform

- Status: Accepted
- Date: 2026-10-06
- Deciders: Sanam Pakuwal

## Context

The product is a calendar whose main surface is the home screen: widgets, lock screen, StandBy,
Control Center. It must feel native, start fast, stay small, and be maintained by one person.
Research on 2026-10-06 (sources in the commit that added this ADR) found:

- **Widgets are native code in every option.** iOS widgets run in a WidgetKit extension and are
  archived SwiftUI views; Android widgets are RemoteViews (Glance). Flutter's `home_widget` says it
  "does not allow writing Widgets with Flutter itself"; .NET MAUI has no widget API; Expo's
  `expo-widgets` covers iOS only; Compose Multiplatform and Skip still need native widget code.
- **iOS widget extensions are killed at about 30 MB** (forum-observed). A Kotlin/Native framework
  in the extension has hit this (KT-66589); a JS runtime inside it is unmeasured.
- **Size and memory:** one 2025 benchmark measured an iOS binary of 0.46 MB native, 18.3 MB Flutter,
  20.2 MB React Native, and memory overhead of 6–10 MB native, 14–25 MB Flutter, 33–45 MB React
  Native. Start-up times did not separate the options.
- **New platform features arrive natively first:** Liquid Glass, tinted and clear widget modes,
  iOS 27's `systemExtraLargePortrait`, Glance generated previews, Android 16 lock-screen widgets.
- **Devanagari shaping and accessibility** are most reliable with the OS text stack (CoreText,
  HarfBuzz/Minikin) and native VoiceOver / TalkBack.
- The calendar logic is a small table-driven library with a shared spec and test vectors, so a
  Swift and a Kotlin port cost little and are proved equal by the vectors (hub ADR-0001).

## Decision

1. **iOS:** Swift, SwiftUI, WidgetKit (ADR-0004).
2. **Android:** Kotlin, Jetpack Compose with Material 3, Glance (ADR-0005).
3. **Calendar logic:** the `nepdate-swift` and `nepdate-kotlin` libraries (ADR-0003). The apps contain
   UI, settings and scheduling only.

## Alternatives considered

| Option | Why not (for this product) |
|---|---|
| Flutter 3.47 | one UI codebase, but widgets, lock screen and watch stay native; adds ~18 MB on iOS; Cupertino has no Liquid Glass yet |
| React Native 0.87 / Expo SDK 57 | iOS widgets via `expo-widgets`, Android needs a different library waking a headless JS task: two widget stacks; largest memory overhead |
| .NET MAUI (.NET 10/11) | no widget API at all; three languages to maintain |
| Kotlin Multiplatform logic + native UI | equal to this decision for the UI; but the Kotlin framework inside the iOS widget extension costs memory, and a pure Swift port is cheap |
| Compose Multiplatform UI | iOS look needs a SwiftUI shell for Liquid Glass; Skia text instead of CoreText; widgets still native |
| Rust core (`nepdate` crate) via UniFFI | proven (Firefox, Bitwarden, Element X), but adds Cargo, UniFFI (pre-1.0), xcframework and NDK builds to the release path, and Rust's `arm64_32` watchOS target is Tier 3 |
| Tauri 2 (web UI in the system WebView, Rust core) | strong on desktop, young on mobile; no official widget support (community `tauri-plugin-widgets`, one maintainer, widgets generated from a JSON layout); web scrolling, gestures, Dynamic Type and VoiceOver feel less native; widgets would still need Swift and Kotlin, so three stacks instead of one per platform |
| Skip (SwiftUI → Compose) | attractive if Swift-first; no documented widget support; Android widgets still native |

**Second best:** native UIs on the Rust core via UniFFI. It wins if the core grows beyond table
lookups (astronomical tithi, a festival rule engine, sync), where one implementation beats several
ports. That would be a new ADR.

## Consequences

- Two UI codebases. The app UI is small (four screens); the widgets would be native anyway.
- Each platform's new widget and design features can ship on day one.
- Android contributors need Android Studio or the Gradle command line. iOS contributors need no Mac:
  every iOS build runs on a GitHub-hosted macOS runner (ADR-0010).
