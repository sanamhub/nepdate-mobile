# ADR-0003: Calendar logic and data come from the NepDate libraries

- Status: Accepted
- Date: 2026-10-06
- Deciders: Sanam Pakuwal

## Decision

1. Each app depends on both units of its library (hub ADR-0004): the core and the patro package.
   **iOS** depends on `nepdate-swift` (SwiftPM, products `NepDate` and `NepDatePatro`). **Android**
   depends on `nepdate-kotlin` (Maven Central, artifacts `io.github.sanamhub:nepdate` and
   `io.github.sanamhub:nepdate-patro`, package `io.github.sanamhub.nepdate.patro`). Both units
   share one version. Exact versions are pinned: `exact:` in the `Package.swift` of the local
   packages under `ios/Packages/` (the generated Xcode project and its `Package.resolved` are not
   committed, ADR-0010), and `gradle/libs.versions.toml` on Android.
2. The apps never implement conversion, formatting, parsing, fiscal year or event lookup. A
   missing capability is added to the library first (and to the hub spec, if it is behaviour).
3. **Data is bundled, never downloaded.** The libraries compile the hub's data into code, so the app
   works offline and needs no network permission (ADR-0007). New holiday data reaches users as a
   library patch release, then an app patch release, through normal store auto-updates.
4. **Freshness gate:** each app's release preflight fails when the bundled public-holiday data ends
   less than 183 days after the release date, the same rule as the hub's `tools/data_status.py`
   (hub task H1-02). Tithi or events ending within 183 days is a warning printed by the preflight
   and copied into the release PR, not a failure: tithi ends on BS 2083-12-30 (AD 2027-04-13) with
   today's data, and blocking every release from 2026-10-12 would help nobody. After the data ends,
   the app shows "no data" (MR-02), never a wrong value. The hub's weekly sync warns months ahead.
5. **System calendar subscription (MR-12 AC2):** the hub publishes `holidays.ics` (one VEVENT per
   public holiday, all coverage years) to GitHub Pages from its data, as a hub task (H4).
   The app only opens or shares that URL; the calendar app fetches it.
6. While a library has no release yet, the app may use it from a local path (SwiftPM local package,
   Gradle composite build `includeBuild`), never a copied source file.
7. **Known data gaps, raised with the hub (not solved in the apps):**
   - Holidays that apply to some people only (Kathmandu Valley, women employees, a community) are
     encoded today only in the event name. Proposed hub schema 2: a `scope` field per holiday.
   - The government marks some holidays "to be determined" and announces ad-hoc ones (elections,
     mourning). With bundled data these appear only after the next app update.

## Consequences

- A data fix takes three steps (hub → library → app). The weekly syncs make each step a reviewed PR.
- App binaries contain event data for BS 2077-01-01 to 2084-08-20 today, with tithi only from
  2078-01-02 to 2083-12-30 (hub `spec/PATRO.md`). It is about 100 KB compiled (hub ADR-0004; layout
  in hub PATRO §3).

## Alternatives considered

- Download yearly data from GitHub at runtime: instant updates (including ad-hoc holidays), but
  needs the network permission, signature checking and an offline fallback. Rejected for v1; the
  first candidate to revisit if ad-hoc holidays matter more to users than "no network".
