# Product requirements (v1.0)

Who it is for: people in Nepal and Nepalis abroad who check the Bikram Sambat date, holidays and
tithi every day, and who are tired of calendar apps that also sell news, horoscopes, ads and
battery drain. The job: answer "what is the date, is it a holiday, what's coming" in one glance,
on the home screen.

Each requirement has an ID (`MR-xx`) and testable acceptance criteria (`AC`). Commits, PRs and
tests cite them (`test_mr01_ac3_saturday_column_red`). Anything not listed is out of scope.

Terms: **BS** Bikram Sambat date, **AD** Gregorian date, **event data** tithi, public holidays and
events from the library's patro package (`NepDatePatro`, `nepdate-patro`; hub ADR-0004),
**coverage** the range the patro package reports (BS 2077-01-01 to 2084-08-20 today; tithi only
2078-01-02 to 2083-12-30), **Nepal date** the date in `Asia/Kathmandu`.

## MR-01 Month view (home screen of the app)

A full-width 7-column month grid, Sunday first (the Nepali week), for one BS month.
- AC1: each cell shows the BS day large and the AD day small in the top-right corner; digits follow
  the Digits setting (MR-07).
- AC2: the header shows the BS month and year in the app language, and the AD month span
  (e.g. "Sep – Oct 2026").
- AC3: the Saturday column and public holidays use the holiday colour **and** a mark (rule H1);
  days with events show an event dot.
- AC4: today has the today ring; the selected day has the selection fill.
- AC5: horizontal swipe and previous/next buttons change the month; a "Today" button appears only
  when another month is shown.
- AC6: tapping the header opens a month and year picker covering BS 1901–2199.
- AC7: a 5- or 6-row grid; cells from neighbouring months are dimmed and not tappable.
- AC8: optional tithi line in each cell (Settings, off by default); never truncated mid-character
  (Devanagari conjuncts stay whole).

## MR-02 Day detail

Tapping a day opens a sheet (iOS) or bottom sheet (Android).
- AC1: shows the BS date in long form (both scripts when the app language is English), the AD date,
  the weekday, the tithi, "Public holiday" when it is one, and every event in file order. The
  Devanagari line shown with English keeps Devanagari digits whatever the Digits setting is, since
  it exists to show the Nepali script.
- AC2: "Add to calendar" opens the system event editor pre-filled with the holiday or event, all
  day. It needs no permission (iOS `EKEventEditViewController`, Android `Intent.ACTION_INSERT`).
- AC3: "Copy date" copies the BS date as text in the app language.
- AC4: outside coverage the sheet says "No holiday or tithi data for this date yet" instead
  of empty fields (test: BS 2090-01-01).
- AC5: a holiday that applies only to some people keeps its scope visible exactly as the data names
  it (for example "Indra Jatra (Kathmandu Valley Holiday)", "Jitiya Festival (Women Employees
  Only)"). The app never shortens or drops the scope. Structured scope tags need a hub data-schema
  change (proposed in ADR-0003).
- AC6: inside coverage, a day without tithi (BS 2084-01-01 today) shows no tithi row and no tithi
  line in its month cell; its holiday flag and events still show.

## MR-03 Upcoming

A list of the next public holidays and events from today.
- AC1: the next 20 entries starting with today, one entry per event in date order and then file
  order, each with BS date, AD date, name and "in N days" (or "Today", "Tomorrow"); entries on a
  public holiday carry the holiday mark.
- AC2: grouped by BS month; tapping an entry opens its day detail.
- AC3: when fewer than 20 entries remain before the end of coverage, the list ends with "Holiday
  data ends on <BS date>", where the date is the last day of coverage (BS 2084-08-20 today).

## MR-04 Converter

- AC1: BS → AD and AD → BS. BS input accepts the library's lenient parser (Nepali or English digits
  and month names); AD input uses the platform date picker.
- AC2: shows the converted date, weekday, and "N days from today".
- AC3: a second date gives the difference in years, months and days (library `diff(to:)` in Swift,
  `until` in Kotlin).
- AC4: invalid input shows what is wrong in one line ("Asoj has 31 days in 2083") and keeps the
  last valid result.

## MR-05 Widgets

The widget catalogue, sizes and behaviour per platform are in `docs/product/widgets.md`; each
widget there has its own ACs (W-xx). Common ACs:
- AC1: every widget shows the correct date within 5 minutes after local midnight (iOS) or within
  the 10-minute inexact alarm window (Android, ADR-0006). After the time zone or the clock changes:
  within 1 minute on Android; on iOS within 1 minute while the app is running, otherwise when the
  app is next opened or at the next timeline entry (ADR-0006, iOS refresh).
- AC2: holidays carry the mark (rule H1) in full-colour, tinted and clear / dynamic-colour modes.
- AC3: tapping a widget opens the app on the right day or screen.

## MR-06 Nepal date abroad

- AC1: Setting "Date follows": "This device" (the default) or "Nepal time" (`Asia/Kathmandu`).
  On first launch, if the device time zone is not `Asia/Kathmandu`, the app suggests "Nepal time"
  once; the suggestion never shows again after it is accepted or dismissed.
- AC2: app and widgets use the chosen zone for "today"; the month view header shows the current
  Nepal time when the setting is "Nepal time" and the zones differ.

## MR-07 Settings

- AC1: Language: System, English, नेपाली. Digits: Follow language (default), Nepali (०१२), English
  (012). Follow language gives Nepali digits when the app language resolves to नेपाली and English
  digits otherwise; with Language set to System it follows the resolved system locale. The setting
  applies to every BS and AD number and every count ("in 5 days", "1 year"), except the MR-02 AC1
  Devanagari line. Date follows (MR-06). Show tithi in month grid. Theme follows the system (no in-app theme switch).
- AC2: "About": app version, library version, holiday data coverage (first and last date), data
  source (C# NepDate via the hub, MIT), the official sources it follows (Ministry of Home Affairs
  holiday notice, Nepal Panchanga Nirnayak Samiti), a link to the "Known data questions" list
  (`docs/product/known-data-questions.md`, a copy of the hub section) and to "Report a wrong date"
  (the issue form from M0-01), licences, privacy statement, source code. Links open in the system
  browser; the app itself makes no request.
- AC3: changing a setting updates open widgets within 1 second (iOS `reloadAllTimelines`, Android
  `updateAll`).

## MR-08 Privacy

- AC1: no network access: Android has no `INTERNET` permission; iOS makes no network calls (CI checks
  both, ADR-0007).
- AC2: no analytics, crash SDK, ads, account or tracking. Store labels: "Data not collected" /
  "No data collected or shared".
- AC3: no runtime permission is requested anywhere in v1.

## MR-09 Performance and size

Reference devices: iPhone 12 (iOS 18), Samsung Galaxy A15 class (Android 14, 4 GB), and an Android
Go-class device (Android 11, 2 GB) for the low end.
- AC1: cold start to a drawn month grid: ≤ 400 ms on the iPhone 12 and the A15 class, ≤ 1 s on the
  low-end device (median of 10).
- AC2: month swipe holds 60 fps (no dropped frames in a 10-swipe trace) on the A15 class.
- AC3: download size ≤ 6 MB on Android (universal APK ≤ 8 MB) and ≤ 6 MB on iOS (App Store thinned).
- AC4: iOS widget extension peak memory ≤ 15 MB (half the observed 30 MB kill limit); Android widget
  RemoteViews stay under the API 37 memory cap with no bitmaps.
- AC5: no background work except the midnight refresh (Android: one inexact alarm a day; iOS:
  timeline entries, no background tasks).

## MR-10 Accessibility

- AC1: every day cell has a spoken label: "Asoj 25, 2083, Sunday, 11 October 2026, public holiday,
  Ghatasthapana Brata" (in the app language).
- AC2: Dynamic Type / font scale up to 200 %: the month view switches to a list layout above the
  largest size that fits 7 columns. Test: at 100 % the grid shows; at 200 % (Android font scale
  2.0 on a 360 dp wide screen, iOS `.accessibility2` on a 390 pt wide screen) the list shows; no
  text is clipped in either.
- AC3: text contrast ≥ 4.5:1 in light and dark; the holiday colour pairs pass on both backgrounds.
- AC4: all controls reachable with VoiceOver / TalkBack and Switch Access; touch targets ≥ 44 pt / 48 dp.

## MR-11 Localisation

- AC1: every string in English and Nepali (CI fails on a missing Nepali string); Nepali copy
  written by a native speaker, not machine translation.
- AC2: Devanagari renders with the system font (no bundled font unless the M0 spike fails).

## MR-12 Holidays in the system calendar

- AC1: per item, MR-02 AC2.
- AC2: "Subscribe to Nepal holidays" opens the hub's published `holidays.ics` as a `webcal://` link
  (iOS) or shares it to a calendar app (Android). The calendar app fetches it; this app never does.
  Depends on the hub publishing the file (hub task, see ADR-0003).

## Out of scope for v1 (each needs a new ADR)

Horoscope, news, forex, gold price, radio, notes, accounts and sync, ads, in-app purchases, themes
beyond system light/dark, tablets beyond what adaptive layouts give for free.

v1.x candidates, most requested in competitor reviews (`docs/competitors.md`), each needs an ADR:
1. Tithi-based yearly reminders stored on the device (lunar birthdays, shraddha); needs
   notifications.
2. Android ongoing notification with today's BS date (needs `POST_NOTIFICATIONS`).
3. Apple Watch complications and a Wear OS complication (ADR-0006).
4. Faster data updates for holidays announced mid-year ("to be determined" dates, ad-hoc holidays),
   which today wait for an app update (ADR-0003).
