# AGENTS.md: rules for AI coding agents (mobile apps)

You are implementing a planned product. Follow the plan; don't redesign it. The product is
deliberately small: refuse features that aren't in `docs/product/requirements.md`.

## 0. Before writing any code

1. Read `README.md`, this file, every ADR in `docs/adr/`, and the product docs your phase lists.
2. Open `docs/plan/00-ROADMAP.md`, find your phase, read that phase file. Check its preconditions
   (library versions, earlier phases); if one is unmet, stop and say so.
3. Work on one platform per task. A task ID starts with `A` (Android), `I` (iOS) or `M` (both).

## 1. Sources of truth (in priority order)

1. `docs/product/requirements.md` (MR-xx with acceptance criteria) and `docs/product/widgets.md`.
2. `docs/adr/*`: decisions. 3. `docs/product/design-system.md`: tokens and rules.
4. `docs/plan/*`: tasks.

Calendar behaviour (conversion, formatting, parsing, tithi, holidays, events) belongs to the libraries
`nepdate-swift` and `nepdate-kotlin` (core plus patro package, ADR-0003). **Never reimplement calendar logic in the apps.** If a
library lacks something, stop and report it; the fix goes to the library (and, for behaviour, the hub).

## 2. Hard rules

- **No network.** Android: no `INTERNET` permission in any manifest, including merged ones (a CI
  check reads the merged manifest). iOS: no `URLSession`, no networking frameworks.
- No analytics, crash reporting, ads or tracking SDKs. No Google Play Services or Firebase.
- No permission beyond ADR-0007. No exact alarms. No notifications in v1.
- No new third-party dependency or Gradle plugin beyond those named in ADR-0004, ADR-0005 and
  ADR-0009 (Gradle Play Publisher). Need one? Stop and ask.
- Holidays are never shown by colour alone (design-system rule H1).
- Every user-visible string is localised in English and Nepali (String Catalog / `values-ne`).
- Widgets render from data passed in, never from disk or network at render time.
- Secrets (signing keys, store API keys) never go in code, logs or commits.
- iOS: never commit `*.xcodeproj`; change `ios/project.yml` (ADR-0010). Workflows that use the App
  Store Connect key run only in the `testflight` or `production` environment, never on
  `pull_request_target`.

## 3. How to work

- Test first for logic (view models, timeline providers, midnight scheduling).
- Run the platform's CI command before saying you're done (`./gradlew ci` in `android/`,
  `ios/scripts/ci.sh` in `ios/`) and paste its last line in the PR.
- UI changes: attach screenshots for light, dark and Nepali, and for each widget size touched.
- PR description: task IDs, what changed and why, how tested, "What to look at first" (2–3 bullets).

## 4. Writing

All prose follows [`.claude/skills/writing-style/SKILL.md`](.claude/skills/writing-style/SKILL.md),
for every agent. In-app copy also follows `docs/product/design-system.md` §6. **No AI attribution**
in commits or PRs.
