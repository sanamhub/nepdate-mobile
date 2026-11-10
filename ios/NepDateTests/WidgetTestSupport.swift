import CalendarKit
import Foundation
import NepDate
import Testing

/// Fixed widget entries for the snapshot and screenshot tests.
enum WidgetTestData {
  static let zone = TimeZone(identifier: "Asia/Kathmandu")!

  /// Noon of a Bikram Sambat date in Kathmandu.
  static func noon(_ date: MonthDate) throws -> Date {
    let nepal = try #require(
      try? NepaliDate(year: date.year, month: date.month, day: date.day)
    )
    return nepal.date(in: zone).addingTimeInterval(12 * 3_600)
  }

  /// The settings a test widget draws with.
  static func settings(language: AppLanguage = .english) -> Settings {
    Settings(
      language: language == .nepali ? .nepali : .english,
      digits: language == .nepali ? .nepali : .english,
      dateFollows: .nepal
    )
  }

  static func entry(
    _ date: MonthDate,
    language: AppLanguage = .english
  ) throws -> WidgetEntry {
    let nepal = try #require(
      try? NepaliDate(year: date.year, month: date.month, day: date.day)
    )
    let now = nepal.date(in: zone).addingTimeInterval(12 * 3_600)
    let settings = Settings(
      language: language == .nepali ? .nepali : .english,
      digits: language == .nepali ? .nepali : .english,
      dateFollows: .nepal
    )
    return try #require(
      WidgetTimeline.entries(now: now, settings: settings, deviceZone: zone).first
    )
  }
}

extension WidgetTestData {
  /// The month widget's entry for Jestha 2083, a six-week month, with today on the 10th.
  static func monthEntry(language: AppLanguage = .english) throws -> MonthWidgetEntry {
    let settings = Settings(
      language: language == .nepali ? .nepali : .english,
      digits: language == .nepali ? .nepali : .english,
      dateFollows: .nepal
    )
    return try #require(
      MonthWidgetTimeline.entry(
        at: try noon(MonthDate(year: 2083, month: 2, day: 10)),
        shown: nil,
        settings: settings,
        deviceZone: zone
      )
    )
  }
}
