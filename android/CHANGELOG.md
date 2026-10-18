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

