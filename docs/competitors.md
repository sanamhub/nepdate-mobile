# Other Nepali calendar apps

Surveyed 2026-10-06: Play Store listings (US storefront), the iTunes lookup API and customer-review
RSS (Nepal, US, Australia, UK; the latest 150–390 reviews per app), Exodus Privacy reports, GitHub
and F-Droid search. Play shows only 3 reviews per app, so complaint counts come mostly from iOS
reviews. Android APK sizes were not available; iOS sizes are shown. [U] = unverified.

## The big apps

| App | Installs / rating | iOS size | Ads / model | Trackers and permissions (Exodus) | What else it does |
|---|---|---|---|---|---|
| Hamro Patro (`com.hamropatro`) | 10M+ / 3.8★ Android; 4.40★ iOS (NP) | **227.5 MB** | ads + ad-free subscription | **25 trackers, 40 permissions** (v10.5.4, 2025-10): AdMob, AppLovin, Facebook, InMobi, ironSource, Unity Ads…; camera, microphone, contacts, phone state | news, radio, podcasts, forex, rashifal, kundali, paid astrologers, calls, games, payments; classified as social media by the government in Sep 2025 |
| Nepali Patro (`np.com.nepalipatro`) | 10M+ / 4.1★; 4.30★ iOS | 95 MB | ads + ad-free subscription | 8 trackers, 10 permissions (stale report, 2022) | panchang, reminders, kundali, news, 300+ radio stations, forex, weather |
| Smart Patro (ashesh.com.np) | iOS only; 4.56★ | 34.7 MB | ads + paid tier | iOS label: identifiers used to track | home and lock-screen widgets, Apple Watch; praised for working widgets |
| NepCal | 100K+ / 4.5★; 4.19★ iOS | 103 MB | ads | 1 tracker (AdMob), 2 permissions (2021) | users have asked for an iOS widget for years |
| Ramro Patro | 1M+ / 4.6★ | n/a | ads | 3 trackers, 26 permissions incl. call phone, camera (2025-11) | top review: "only one thing is missing that is Widgets" |

## Small "clean" apps

| App | Installs / rating | Trackers | Widgets | Notes |
|---|---|---|---|---|
| Simple Patro | 10K+ / 4.5★ | **0** (Exodus); iOS "Data Not Collected"; 36 MB | yes | "Just what a calendar app should be like"; users ask for widget transparency and light/dark |
| Digital Patro | 10K+ / 4.2★ | ad-free | home + notification-bar date | offline |
| Modern Patro | 50K+ / 4.2★ | ads | widgets + sticky notification | users want "N days ago/later" |
| Saral Patro | 10K+ / 3.6★ | Firebase Analytics | small and medium | reviews report wrong tithi and BS/AD off by one day |
| Mero Patro (iOS) | 4.18★ | n/a | today widget + Watch | review: "Falgun is of 29 but given as 30" |

## Open source

- **F-Droid has no Nepali calendar app** (searched nepali, nepal, patro, bikram, sambat).
- `sa-Samir/hamro_nepali_patro`: Flutter with native Kotlin/Swift widgets, astronomical calculation
  for 1970–2200; 1 commit, no licence, not in stores. Closest in intent; check before building.
- `khumnath/nepdate`: Qt desktop widget, GPL-3.0, official data 2000–2089 then astronomical.
- `dharmapoudel/nepalipatro`: Android Java, GPL-3.0, inactive since 2022.
- Several repositories scrape hamropatro.com; this app never does (legal note below).

## What users ask for (iOS review tags, Play snippets)

1. **No ads.** The top complaint for both big apps ("memberships and ad has literally made this app
   unusable"; "Ads Tsunami").
2. **Widgets that work:** stuck on "loading", blank, or missing sizes in the big apps. Wanted: BS
   date with the AD date, tithi, upcoming events, several sizes, transparency, lock-screen widgets,
   a notification-bar date on Android.
3. **Speed and size:** "slow" or "lag" appears 20 times in Hamro Patro reviews, 17 in Nepali Patro.
4. **Correct dates:** wrong month lengths, holidays shown that aren't, festivals off by a day. Users
   notice and stop trusting the app.
5. Praise goes to "no unnecessary extras", no ads, fast, offline, clean, working widget.
6. Repeated requests: tithi-based yearly reminders (lunar birthdays, shraddha), "days until/ago",
   English UI.

## How this app answers

| Gap | Requirement |
|---|---|
| ads, trackers, permissions | MR-08, ADR-0007: none, no network |
| broken or missing widgets | `widgets.md`: five widget types, lock screen, Tier 1 quality, midnight refresh without opening the app (ADR-0006) |
| size and speed | MR-09: ≤ 6 MB download, ≤ 400 ms cold start |
| trust in dates | data from C# NepDate verified for every day; source, coverage and a public "known data questions" list shown in About (MR-07 AC2) |
| days until | MR-03, MR-04 AC2 |
| BS + AD together | MR-01 AC1, W-01 |
| widget transparency | Android widget opacity setting (`widgets.md`) |
| F-Droid | ADR-0009: reproducible builds |
| tithi reminders, notification-bar date | v1.x candidates (requirements, out of scope) |

## Legal and data-source notes

- **Public holidays:** Ministry of Home Affairs notice in the Nepal Rajpatra (the 2083 list was
  published 2026-03-02). It applies to government offices; some holidays are regional, for women
  employees, or for a community, and some dates are "to be determined" (ADR-0003 §7).
- **Panchang:** Nepal Panchanga Nirnayak (Bikas) Samiti, under the Ministry of Culture, Tourism and
  Civil Aviation. Printed and wall calendars need its prior approval. **Whether a digital app needs
  approval is not stated anywhere found [U]**: human task before the first public release (M0-07).
- Dates and tithis are facts, but a publisher's compiled panchang may be protected as a compilation
  [U, lawyer]. This app takes data only from MIT-licensed NepDate through the hub and never scrapes.
  Open question: NepDate's own solution references an unpublished `HamroPatroScraper` tool, so the
  origin of its event data must be confirmed before release (`docs/legal/README.md`).
- An app without news, messaging or user content should stay outside the 2025 social-media
  registration rules [U].

## Sources

Play listings `play.google.com/store/apps/details?id=` com.hamropatro, np.com.nepalipatro,
com.ramropatro.app, com.nepcal.nepcal_app, com.simplepatro.app, com.yd4011439.patro,
com.saralapps.saralpatro; Exodus `reports.exodus-privacy.eu.org/en/reports/<id>/latest/`; App Store
ids 401074157, 664588996, 1273057004, 570342297, 6738119769, 1006815700 via `itunes.apple.com/lookup`;
https://search.f-droid.org/?q=nepali; https://github.com/sa-Samir/hamro_nepali_patro;
https://github.com/khumnath/nepdate; https://www.pradhanlaw.com/publications/list-of-public-holidays-2083-202627;
https://npns.gov.np/pages/panchanga-of-the-year-2083-5; https://nepalpanchanga.com/calendar_approval_info;
https://ictframe.com/hamro-patro-controversy-nepal-government-social-media-ban/.
