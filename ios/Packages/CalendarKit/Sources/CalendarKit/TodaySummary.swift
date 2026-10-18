import Foundation
import NepDate
import NepDatePatro

/// The values the Today widget draws, computed once per timeline entry.
public struct TodaySummary: Sendable, Hashable {
  /// The day number in the resolved Digits setting.
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
  ///   - language: The language of the names.
  ///   - digits: The resolved Digits setting.
  public init?(year: Int, month: Int, day: Int, language: AppLanguage, digits: Digits) {
    guard let date = try? NepaliDate(year: year, month: month, day: day) else { return nil }
    self.init(date: date, language: language, digits: digits)
  }

  /// Creates today's summary from the stored settings: today in the zone "Date follows" picks,
  /// names in the resolved language and digits from the resolved Digits setting.
  /// - Parameters:
  ///   - settings: The stored settings.
  ///   - provider: The clock and the device zone.
  /// - Returns: Today's summary.
  public static func make(settings: Settings, provider: TodayProvider = .init()) -> TodaySummary {
    let today = provider.today(dateFollows: settings.dateFollows)
    let language = settings.language.resolved
    let digits = settings.digits.resolved(for: language)
    // The provider returns a library date clamped to its range, so the fallback never runs.
    guard let date = try? NepaliDate(year: today.year, month: today.month, day: today.day) else {
      return TodaySummary(date: .min, language: language, digits: digits)
    }
    return TodaySummary(date: date, language: language, digits: digits)
  }

  private init(date: NepaliDate, language: AppLanguage, digits: Digits) {
    self.day = digits.format(date.day)
    self.month = MonthNames.bs(date.month, language)
    self.weekday = date.weekday.name(language.lang)
    self.isHoliday = date.calendarInfo.isPublicHoliday
  }
}
