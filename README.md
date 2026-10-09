# nepdate-mobile

A Nepali calendar for iPhone and Android that does one job: show the Bikram Sambat date,
the month, and its holidays and events, on the home screen and in the app. No ads, no
account, no news, no horoscope, no network.

Part of the NepDate family. The calendar data and its rules live in the hub
[`sanamhub/nepdate`](https://github.com/sanamhub/nepdate); the apps use the Swift and Kotlin
libraries [`nepdate-swift`](https://github.com/sanamhub/nepdate-swift) and
[`nepdate-kotlin`](https://github.com/sanamhub/nepdate-kotlin), each as two units: the core and the
patro package (tithi, holidays, events).

| | iOS (`ios/`) | Android (`android/`) |
|---|---|---|
| UI | SwiftUI | Jetpack Compose, Material 3 |
| Widgets | WidgetKit: home, lock screen, StandBy, Control Center | Glance: home, lock screen (Android 16 QPR1+) |
| Minimum OS | iOS 18 | Android 8.0 (API 26) |
| Distribution | App Store | Google Play, F-Droid / IzzyOnDroid, GitHub Releases |
| Permissions | none | none (not even internet) |

**Status:** planning complete, implementation not started (next: phase M0). The libraries it
needs are planned in their own repositories.

## Where to start

| You are | Read |
|---|---|
| an AI agent implementing a phase | [AGENTS.md](AGENTS.md) → [docs/plan/00-ROADMAP.md](docs/plan/00-ROADMAP.md) |
| deciding what the app does | [docs/product/requirements.md](docs/product/requirements.md) |
| designing screens or widgets | [docs/product/design-system.md](docs/product/design-system.md), [docs/product/widgets.md](docs/product/widgets.md) |
| comparing with Hamro Patro and others | [docs/competitors.md](docs/competitors.md) |
| wondering "why native, why this way?" | [docs/adr/README.md](docs/adr/README.md) |

## Layout

```
ios/            XcodeGen project.yml, app, widget extension, local SwiftPM packages (from M0-03)
android/        Gradle project: single :app module with Glance widgets          (from M0-02)
docs/product/   requirements (MR-xx), design system, widget catalogue, privacy policy
docs/adr/       decisions for both apps
docs/plan/      phases and tasks with acceptance criteria
docs/runbooks/  release procedures per store
```

## License

MIT ([LICENSE](LICENSE)). Calendar data from NepDate by Raju Prasai and contributors, MIT.
Holiday data is informational; this app is not affiliated with the Government of Nepal.
