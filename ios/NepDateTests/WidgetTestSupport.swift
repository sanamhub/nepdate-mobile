import CalendarKit
import Foundation
import NepDate
import Testing

/// Fixed widget entries for the snapshot and screenshot tests.
enum WidgetTestData {
  static let zone = TimeZone(identifier: "Asia/Kathmandu")!

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
