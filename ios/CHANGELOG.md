# Changelog

All notable changes to the iOS app. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- The iOS project skeleton: the app target, the Today widget extension, the App Group
  `group.np.nepdate`, the local packages `CalendarKit` and `CalendarUI`, `PrivacyInfo.xcprivacy` in
  both targets, a String Catalog with English and Nepali, and the five gates of `ios/scripts/ci.sh`.
- The settings store in the App Group (Language, Digits, Date follows, Show tithi), the injected
  today provider and the 5- or 6-row month layout with tithi, events and dimmed neighbouring days.
- The CalendarKit coverage gate at 80 %.
- The Today widget reads the shared settings for its day, language and digits.
- The month screen: `TabView` with Month, Upcoming and Converter, month paging, the BS 1901 to 2199
  picker, previous and next buttons, the Today button, day selection, VoiceOver labels and the list
  layout at accessibility text sizes (MR-01, MR-10).
- Six reference snapshots of Asoj 2083 in light, dark, Nepali, `.xxxLarge` and `.accessibility2`,
  compared with `swift-snapshot-testing` 1.19.6.
- XCUITests that check the pager opens on today, the picker reaches both ends of BS 1901 to 2199,
  the pager keeps the swiped month across its window recentre, and the accessibility audit has only
  the tolerated findings, counted exactly per category on the grid and the list.
- The day detail sheet (MR-02): the BS and AD dates, the Devanagari line in English, weekday,
  tithi, public holiday and every event with its scope; Add to calendar through
  `EKEventEditViewController`; Copy date; and the no-data message outside coverage.
- Four day sheet reference snapshots (light, dark, Nepali, `.accessibility2`), and Swift Testing on
  the day view model, the all-day EKEvent and the pasteboard.
- A day sheet XCUITest that taps Asoj 25, checks the spoken fields, taps Copy date and audits the
  sheet with no tolerated findings.
- Upcoming (MR-03), Converter (MR-04), Settings (MR-06, MR-07) and About (MR-12), with the
  `DayDetail` day sheet, the system per-app language row, the Nepal time suggestion, the five
  allow-listed links, the injected webcal row and Copy diagnostic info.
- Sixteen I1-05 reference snapshots: Upcoming, Converter, Settings and About in light, dark, Nepali
  and `.accessibility2`, plus `daydetail.copied.png` for the Copy date confirmation.
- `ci.sh` checks `NepDateLibraryVersion` against the nepdate-swift pin and every catalog key for a
  translated `ne` value.
- The widget timeline: one entry for the build instant and one at each of the next seven local
  midnights, with the day, the next holiday and the next three events.
- W-01 Today and W-02 Today and upcoming, with the holiday mark, the Nepal label, the coverage
  footer and `nepdate://` links. Eight widget reference snapshots, 35 in total.
- The app opens `nepdate://today` on today's month and `nepdate://day/<date>` on that day's detail;
  a malformed or unknown link opens the app where it was.

### Changed

- The month screen, its view model, its month keys, Upcoming, Converter, Settings and About are in
  the `CalendarScreens` product. The widget executable is 615,656 bytes in I1-05, 272 bytes above
  I1-04 from linker layout; no widget source changed.
- Copy date now shows "Date copied" and announces it once to VoiceOver.
- Light secondary text is 0.33 white (7.55:1 on the background).
