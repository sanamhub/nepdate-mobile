import Foundation
import NepDate
import NepDatePatro

/// The values the Today widget draws, computed once per timeline entry.
public struct TodaySummary: Sendable, Hashable {
  /// The day number in the display language's digits.
  public let day: String

  /// The month name.
  public let month: String

  /// The weekday name.
  public let weekday: String

  /// Whether the date is a public holiday.
  public let isHoliday: Bool

  /// Creates a summary for a Bikram Sambat date.
  ///
  /// The initialiser fails when the fields are out of range or do not form a date.
  /// - Parameters:
  ///   - year: The Bikram Sambat year, 1901 to 2199.
  ///   - month: The month, 1 to 12.
  ///   - day: The day, 1 to the month's length.
  ///   - language: The language of names and digits.
  public init?(year: Int, month: Int, day: Int, language: AppLanguage) {
    guard let date = try? NepaliDate(year: year, month: month, day: day) else { return nil }
    self.init(date: date, language: language)
  }

  /// Creates the summary for today in `timeZone`.
  /// - Parameters:
  ///   - timeZone: The zone whose calendar date to show.
  ///   - language: The language of names and digits.
  /// - Returns: Today's summary.
  public static func make(
    timeZone: TimeZone = CalendarDates.nepal,
    language: AppLanguage
  ) -> TodaySummary {
    TodaySummary(date: NepaliDate.today(in: timeZone), language: language)
  }

  private init(date: NepaliDate, language: AppLanguage) {
    self.day = language == .nepali ? nepaliDigits(date.day) : String(date.day)
    self.month = date.bsMonth.name(language.lang)
    self.weekday = date.weekday.name(language.lang)
    self.isHoliday = date.calendarInfo.isPublicHoliday
  }
}
