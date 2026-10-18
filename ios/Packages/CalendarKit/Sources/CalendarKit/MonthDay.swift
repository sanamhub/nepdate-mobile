import Foundation
import NepDate
import NepDatePatro

/// One day in a month grid.
public struct MonthDay: Sendable, Hashable, Identifiable {
  /// The Bikram Sambat year.
  public let year: Int

  /// The Bikram Sambat month, 1 to 12.
  public let month: Int

  /// The Bikram Sambat day of the month.
  public let day: Int

  /// The Gregorian day of the month, shown small in the corner of the cell.
  public let adDay: Int

  /// Whether the date is a public holiday.
  public let isHoliday: Bool

  /// Whether the date is today.
  public let isToday: Bool

  /// A stable identity for SwiftUI, for example `2083-6-18`.
  public var id: String {
    "\(year)-\(month)-\(day)"
  }

  init(date: NepaliDate, today: (year: Int, month: Int, day: Int)?) {
    self.year = date.year
    self.month = date.month
    self.day = date.day
    self.adDay = date.gregorian.day
    self.isHoliday = date.calendarInfo.isPublicHoliday
    if let today {
      self.isToday = today.year == date.year && today.month == date.month && today.day == date.day
    } else {
      self.isToday = false
    }
  }

  /// The day number in the given language's digits.
  /// - Parameter language: The language to show.
  /// - Returns: Devanagari digits for Nepali, ASCII digits for English.
  public func number(_ language: AppLanguage) -> String {
    language == .nepali ? nepaliDigits(day) : String(day)
  }

  /// The Gregorian day number in the given language's digits.
  /// - Parameter language: The language to show.
  /// - Returns: Devanagari digits for Nepali, ASCII digits for English.
  public func adNumber(_ language: AppLanguage) -> String {
    language == .nepali ? nepaliDigits(adDay) : String(adDay)
  }
}
