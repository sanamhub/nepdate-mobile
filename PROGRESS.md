# Progress

Current phase: 1.0 readiness. The Android tester build and the iOS parity screens are in place.
M5 adds the holiday subscription, closes the parity test gaps and fixes the App Store screenshots.
The public releases wait on the owner, the Apple account and the two open legal rows.

## Android

- Month, day detail, Festivals, Search, Year, Converter, Settings and About are built and tested.
- Today, Today and upcoming, Month, Next festival, Festivals, Date chip, Date and tithi, This week
  and Date text widgets, the opt-in status bar and notification card, and the Quick Settings tile.
- The tester APK is the release build with R8 and the Baseline Profile. `1.0.0-beta.1` is on
  GitHub only while Play access is pending.
- Left: the Play closed test, production rollout, IzzyOnDroid and F-Droid (owner), and the device
  numbers and widget placement checks in `docs/runbooks/manual-test.md`.

## iOS

- The four tabs, first run, month grid, year view, day sheet, Festivals, Search, Converter,
  Settings and About are built.
- Today, Today and upcoming, Month, Next festival, Festivals, This week and the Lock Screen
  families, the countdown gauge, two controls and the App Shortcuts are built.
- The English listing, privacy, age-rating, review and export answers are drafted in
  `fastlane/metadata/ios`. Nepali is not an App Store Connect metadata language.
- Left: the App Store Connect record, the key, TestFlight and submission (Apple account);
  `systemExtraLargePortrait` waits for the Xcode 27 migration (ADR-0014); the device numbers are
  not measured.

## Shared

- Both apps use `nepdate 1.0.0-rc.1`. A follow-up change moves them to 1.0.0 when it is tagged.
- The bundled Android holiday data passes the 183-day gate until 2027-06-03. Tithi ends
  2027-04-13 and only warns.
- The two open rows in `docs/legal/README.md` gate any public release.

## Parity

The full table is in `docs/plan/parity-audit.md`. Every requirement and widget has code on both
platforms, and every audited row now has automated evidence except the Android one-line date widget
(W-11) and the Android gate for a missing Nepali string (MR-11). The extra-large month widget, all
placement and rendering-mode passes, and the device numbers are in the owner or device list below.

## What is left, and who it waits on

| Item | Waits on |
|---|---|
| Play closed test and production rollout | Owner, Play account and signing key |
| App Store record, TestFlight and submission | Apple Developer account and App Store Connect key |
| Both open legal rows | Owner and the record they need |
| `systemExtraLargePortrait` (W-03) | Xcode 27 migration and a device review |
| Cold start, frame trace, download size, widget peak memory | Device or reference hardware |
| VoiceOver, TalkBack, Switch Access, haptics, notification permission dialog | Device pass |
| Library 1.0.0 | The library release |

## Earlier phases

- M0: repository, CI and the design spike. Done.
- A1 and A2: Android app and widgets. Done, merged in #2 and #15.
- A3-01 to A3-03: reproducible build, release workflow and Play listing. Done, merged in #18.
- I1: iOS app. Done.
- I2: iOS widgets. Done except the extra-large month family.
- Redesign and M2: Material 3 Expressive, tester APK, benchmarks, icon, splash and the iOS design
  pass. Done, merged in #45, #49, #50, #52 and #58.
- M4: pager race, parity audit and store listings. Done, merged in #74, #75, #77, #78, #79
  and #80.
- M5: holiday subscription, the parity test gaps and the App Store screenshot fixes. Done, in
  review.
