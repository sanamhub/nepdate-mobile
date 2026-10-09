# ADR-0002: One repository for the iOS and Android apps

- Status: Accepted
- Date: 2026-10-06
- Deciders: Sanam Pakuwal

## Context

The libraries were split into one repository per language (hub ADR-0001) because each publishes
to its own registry for its own developers. The apps are different: they are **one product** with
one set of requirements, one design system, one privacy policy, one issue tracker, shared store
text and screenshots, and features that should land on both platforms together.

## Decision

1. One repository, `sanamhub/nepdate-mobile`, with two independent projects:
   `ios/` (XcodeGen `project.yml` and local SwiftPM packages, ADR-0010) and `android/` (Gradle). No build or code is shared between them; the shared part
   is `docs/` (requirements, design system, widget catalogue, privacy policy) and store metadata.
2. CI runs per platform with `paths:` filters: `ios/**` → macOS runner, `android/**` → Linux runner.
   `docs/**` changes run neither.
3. Task IDs say which platform: `I*` iOS, `A*` Android, `M*` both. One PR touches one platform,
   except `M*` tasks.
4. Each app has its own version and changelog (`ios/CHANGELOG.md`, `android/CHANGELOG.md`) and tag
   prefix (`ios-vX.Y.Z`, `android-vX.Y.Z`), because store review and rollout differ.

## Consequences

- A requirement change is one PR that both platforms then implement; nothing drifts between two
  issue trackers.
- Contributors open only the folder for their platform.

## Alternatives considered

- `nepdate-ios` and `nepdate-android` repositories: cleaner CI, but requirements, design and store
  text would be duplicated or live in a third repository. Rejected for a single maintainer.
- Putting the apps into the library repositories: mixes app release cycles with library releases.
  Rejected.
