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
- An XCUITest that checks the pager opens on today and runs `performAccessibilityAudit()`.
