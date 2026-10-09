# Roadmap (mobile apps)

Android first: Nepal is mostly Android, and F-Droid has no Nepali calendar yet. iOS follows the same
requirements. Each phase is one PR series; task IDs `M<n>-<k>` (both), `A<n>-<k>` (Android),
`I<n>-<k>` (iOS).

```
library releases: nepdate-kotlin K4 ─┐                  nepdate-swift S4 ─┐
                                     ▼                                    ▼
M0 repo, CI, design spike ─► A1 Android app ─► A2 Android widgets ─► A3 Android release
                         └─────────────────► I1 iOS app ─► I2 iOS widgets ─► I3 iOS release
```

Preconditions:
- A1 needs `nepdate-kotlin` through phase K4 (conversion, format, parse, and the patro package
  `nepdate-patro` with `java.time` interop); I1 needs `nepdate-swift` through S4 (the same, with
  `NepDatePatro` and Foundation interop). The apps use both units (ADR-0003 §1). Until they are
  released, use them from a local path (ADR-0003 §6).
- The first public release of either app (A3-04, I3-03) needs both **open** rows in
  `docs/legal/README.md` closed (ADR-0009, release gates).
- MR-12 AC2 (holiday subscription) needs the hub's `holidays.ics` (hub task H4); if it is
  not ready, the button is hidden and the requirement moves to the next release.

| Phase | File | Output |
|---|---|---|
| M0 | `M0-repo-ci-and-design-spike.md` | repo files, CI skeletons, Devanagari and colour spike on both platforms, manual-test checklist |
| A1 | `A1-android-app.md` | month, day detail, upcoming, converter, settings (MR-01–04, MR-06–12) |
| A2 | `A2-android-widgets.md` | W-01–W-04 on Glance, midnight refresh, previews, opacity setting (MR-05, MR-07 AC3, MR-09 AC4–AC5) |
| A3 | `A3-android-release.md` | reproducible signed builds, Play closed test, IzzyOnDroid, F-Droid, v1.0 |
| I1 | `I1-ios-app.md` | the same screens in SwiftUI (MR-01–04, MR-06–12) |
| I2 | `I2-ios-widgets.md` | W-01–W-05 on WidgetKit, App Intents, rendering modes (MR-05, MR-07 AC3, MR-09 AC4–AC5) |
| I3 | `I3-ios-release.md` | TestFlight, App Store review, v1.0 |

## Definition of Done (every task)

1. Each cited MR/W acceptance criterion has a test, or a line in `docs/runbooks/manual-test.md`.
2. The platform CI passes (`./gradlew ci`, `ios/scripts/ci.sh`).
3. Screenshots attached for every changed screen or widget: light, dark, Nepali.
4. No new dependency or permission beyond ADR-0004, ADR-0005, ADR-0007 and ADR-0009.
5. The platform `CHANGELOG.md` `[Unreleased]` updated.
6. Prose follows `.claude/skills/writing-style/SKILL.md`; in-app copy follows design-system §6.

## Milestones

| Milestone | Exit |
|---|---|
| M1 Android beta | A1–A2 done, plus A3-01, A3-02 and A3-05; closed test on Play started with 12+ testers |
| M2 Android 1.0 | A3 done; on Play, IzzyOnDroid, GitHub; F-Droid submission open |
| M3 iOS 1.0 | I1–I3 done; on the App Store |
