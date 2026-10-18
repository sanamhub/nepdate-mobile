# Changelog

All notable changes to the Android app. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Month view (MR-01): a swipeable 5- or 6-row BS grid with AD days, holiday marks, event
  dots, the today ring, a month and year picker, and a list layout at 200 % font scale.
- Optional tithi line in the month grid (MR-01 AC8).
- Day detail sheet (MR-02): BS and AD dates, weekday, tithi, public-holiday mark, every event
  in file order, "Add to calendar" through the system event editor, and "Copy date".
- Upcoming tab (MR-03): the next 20 public holidays and events, grouped by BS month, with
  "Today", "Tomorrow" or "in N days", the holiday mark, day detail on tap, and the coverage-end
  message near the end of the data.
- Converter tab (MR-04): BS to AD with the lenient parser, AD to BS through the platform date
  picker, weekday and days from today, a second date's year/month/day difference, and inline
  parse errors that keep the last result.
- Bottom navigation with Month, Upcoming and Converter (design-system §4).
- Settings and About (MR-06, MR-07, MR-12): language on Android 13+, digits, the date-follows
  choice, the tithi switch, version and coverage details, the official sources, the known-data,
  wrong-date, licence, privacy and source links, and "Copy diagnostic info". The holiday
  subscription row stays hidden until the hub publishes its `.ics` file.
- The first-launch "Nepal time" suggestion, and the current Nepal time in the month header when
  the zones differ. Settings opens from the top app bar on every tab.
- `reportFullyDrawn()` after the first month grid is drawn, a Baseline Profile generator for
  start-up and month swipes, and Macrobenchmarks for cold start and month swipe frame timing.

