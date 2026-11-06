# ADR-0012: material3 1.5 pre-release for Material 3 Expressive

- Status: Proposed
- Date: 2026-11-06
- Deciders: Sanam Pakuwal

## Context

The Android redesign follows Material 3 Expressive. The Compose BOM in use (2026.09.00) pins
`androidx.compose.material3` 1.4.0, which has none of the Expressive components:
`MaterialExpressiveTheme`, `MotionScheme.expressive()`, `ShortNavigationBar`, `ButtonGroup`,
toggle and split buttons, the floating toolbar, `LoadingIndicator` and the `MaterialShapes`
library. Rebuilding them by hand costs code and drifts from the platform look and motion.

Expressive ships in material3 1.5.0. On 2026-11-06 the newest build that meets the 14-day age rule
is 1.5.0-beta02 (published 2026-10-21). It needs Compose foundation and ui 1.12.0, and the BOM
already provides 1.12.1, so no other library moves.

## Decision

1. `androidx.compose.material3:material3` is pinned to 1.5.0-beta02 above the BOM in
   `gradle/libs.versions.toml`. Nothing else leaves the BOM.
2. Expressive APIs are used where the platform component fits. Each opt-in to
   `ExperimentalMaterial3ExpressiveApi` stays at the call site.
3. The pin moves to 1.5.0 stable once it is 14 days old, in its own pull request.

## Consequences

- The app gets the official Expressive components and spring-based motion with no new library.
- A beta can change API before stable. The upgrade to stable may need small code changes, and the
  screenshot tests catch visual drift.
- If 1.5.0 stable slips or regresses, reverting means restoring the hand-built pieces from git
  history.
