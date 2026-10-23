<p align="center">
  <img src="https://raw.githubusercontent.com/sanamhub/nepdate-mobile/main/docs/assets/nepdate-mobile-128.png" width="128" alt="NepDate app logo">
</p>

<h1 align="center">nepdate-mobile</h1>

<p align="center">
  <a href="https://github.com/sanamhub/nepdate-mobile/actions/workflows/android.yml"><img src="https://github.com/sanamhub/nepdate-mobile/actions/workflows/android.yml/badge.svg" alt="Android CI"></a>
  <a href="https://github.com/sanamhub/nepdate-mobile/actions/workflows/ios.yml"><img src="https://github.com/sanamhub/nepdate-mobile/actions/workflows/ios.yml/badge.svg" alt="iOS CI"></a>
  <a href="https://github.com/sanamhub/nepdate-mobile/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue" alt="License: MIT"></a>
</p>

A Nepali calendar for Android and iPhone that does one job: show the Bikram Sambat date, the
month, and its holidays and events, on the home screen and in the app. No ads, no account, no
news, no horoscope, no network.

| | Android (`android/`) | iOS (`ios/`) |
|---|---|---|
| UI | Jetpack Compose, Material 3 | SwiftUI |
| Widgets | Glance: home, lock screen (Android 16 QPR1+) | WidgetKit: home, lock screen, StandBy, Control Center |
| Minimum OS | Android 8.0 (API 26) | iOS 18 |
| Permissions | none, not even internet | none |

## Status

Neither app is released yet.

- **Android:** feature complete for a first closed test. It goes to a Google Play closed test with
  at least 12 testers for 14 days, the rule for new personal developer accounts
  ([ADR-0009](https://github.com/sanamhub/nepdate-mobile/blob/main/docs/adr/0009-distribution-and-releases.md)),
  once the developer account exists. F-Droid, IzzyOnDroid and GitHub Releases follow.
- **iOS:** built and tested in CI on every change, with snapshot and UI tests, but not
  distributed. There is no TestFlight or App Store release for now.

Screens rendered by the Android screenshot tests in CI (Roborazzi), not taken on a device:

<p align="center">
  <img src="https://raw.githubusercontent.com/sanamhub/nepdate-mobile/main/android/app/src/test/screenshots/a1-month-light.png" width="240" alt="Month screen, Asoj 2083, with Saturdays and public holidays marked">
  <img src="https://raw.githubusercontent.com/sanamhub/nepdate-mobile/main/android/app/src/test/screenshots/a1-day-detail-light.png" width="240" alt="Day detail for Asoj 9, 2083: tithi, public holiday and three events">
  <img src="https://raw.githubusercontent.com/sanamhub/nepdate-mobile/main/android/app/src/test/screenshots/a1-month-ne.png" width="240" alt="Month screen in Nepali">
</p>

## Where the calendar comes from

The apps hold no calendar logic of their own. They use the NepDate libraries, each as two units:
the core (conversion, formatting) and the patro (tithi, holidays, events).

| App | Library | How it is pulled in today |
|---|---|---|
| iOS | [nepdate-swift](https://github.com/sanamhub/nepdate-swift), released as [0.1.0-beta.1](https://github.com/sanamhub/nepdate-swift/releases/tag/0.1.0-beta.1) | SwiftPM, still pinned to an earlier commit in [`ios/project.yml`](https://github.com/sanamhub/nepdate-mobile/blob/main/ios/project.yml); moving to the tag is a separate change |
| Android | [nepdate-kotlin](https://github.com/sanamhub/nepdate-kotlin) | a Gradle included build of a local checkout, although nepdate-kotlin is now on Maven Central as `io.github.sanamhub:nepdate:0.1.0-beta.1`; moving to it is a separate change ([ADR-0003](https://github.com/sanamhub/nepdate-mobile/blob/main/docs/adr/0003-calendar-libraries-and-data.md)) |

The data and its rules live in the hub, [sanamhub/nepdate](https://github.com/sanamhub/nepdate),
which also lists every package in [all languages](https://github.com/sanamhub/nepdate#packages).

## More

| Topic | Where |
|---|---|
| What the app does, requirement by requirement | [docs/product/requirements.md](https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/requirements.md) |
| Screens, widgets and the design system | [design-system.md](https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/design-system.md), [widgets.md](https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/widgets.md) |
| Why native, and every other decision | [docs/adr](https://github.com/sanamhub/nepdate-mobile/blob/main/docs/adr/README.md) |
| Privacy policy and Play data safety | [privacy-policy.md](https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/privacy-policy.md), [play-data-safety.md](https://github.com/sanamhub/nepdate-mobile/blob/main/docs/legal/play-data-safety.md) |
| How it compares with Hamro Patro and others | [docs/competitors.md](https://github.com/sanamhub/nepdate-mobile/blob/main/docs/competitors.md) |
| Building, testing and pull requests | [CONTRIBUTING.md](https://github.com/sanamhub/nepdate-mobile/blob/main/CONTRIBUTING.md) |
| Reporting a vulnerability | [SECURITY.md](https://github.com/sanamhub/nepdate-mobile/blob/main/SECURITY.md) |
| A wrong date, holiday or tithi | the hub's [data error form](https://github.com/sanamhub/nepdate/issues/new/choose) |

```
android/        Gradle project: one :app module with Glance widgets
ios/            XcodeGen project.yml, app, widget extension, local SwiftPM packages
docs/product/   requirements (MR-xx), design system, widget catalogue, privacy policy
docs/adr/       decisions for both apps
docs/legal/     store declarations and open legal questions
docs/plan/      phases and tasks with acceptance criteria
docs/runbooks/  release procedures per store
```

## License

MIT ([LICENSE](https://github.com/sanamhub/nepdate-mobile/blob/main/LICENSE)). Calendar data from
NepDate by Raju Prasai and contributors, MIT. Holiday data is informational; this app is not
affiliated with the Government of Nepal.
