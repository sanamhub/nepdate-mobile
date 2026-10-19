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

### Changed

- The month screen, its view model and its month keys are in the `CalendarScreens` product, so the
  widget extension no longer carries them. The widget executable is 615,384 bytes in I1-04
  (640,528 after the I1-03 split, run 39190060914).
- Light secondary text is 0.33 white (7.55:1 on the background).
