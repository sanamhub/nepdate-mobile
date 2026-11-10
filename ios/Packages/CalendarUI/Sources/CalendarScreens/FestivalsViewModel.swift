import CalendarKit
import Foundation
import NepDate
import NepDatePatro

/// One BS month of the festivals list (MR-03 AC2).
public struct FestivalGroup: Sendable, Hashable, Identifiable {
  /// The BS year.
  public let year: Int

  /// The BS month.
  public let month: Int

  /// The month title, for example `Asoj 2083`.
  public let title: String

  /// The number of public holidays in the month.
  public let holidayCount: Int

  /// The days of the month with a holiday or an event, in date order.
  public let days: [FestivalDay]

  /// The identity of the month.
  public var id: Int {
    year * 100 + month
  }
}

/// The festivals list (MR-03, MR-13): the days from today to the end of coverage, grouped by BS
/// month, with a name filter and a public-holidays-only filter, and the next public holiday.
public struct FestivalsViewModel: Sendable {
  /// The first day the list covers.
  public let today: MonthDate

  /// The language of the names.
  public let language: AppLanguage

  /// The resolved Digits setting.
  public let digits: Digits

  /// The months, in date order.
  public let groups: [FestivalGroup]

  /// The next public holiday on or after today, when the data has one.
  public let nextHoliday: HolidayCountdown?

  /// The last day the holiday data covers, in the app language.
  public let dataEndsOn: String

  /// Creates the list.
  /// - Parameters:
  ///   - today: The first day to include.
  ///   - language: The language of the names.
  ///   - digits: The resolved Digits setting.
  ///   - query: A name filter; empty shows everything.
  ///   - holidaysOnly: Whether to hide days that are not public holidays.
  public init(
    today: MonthDate,
    language: AppLanguage,
    digits: Digits,
    query: String = "",
    holidaysOnly: Bool = false
  ) {
    self.today = today
    self.language = language
    self.digits = digits
    let days = FestivalDirectory.from(today).filter { day in
      guard !holidaysOnly || day.isHoliday else { return false }
      guard !query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return true }
      return day.matches(query)
    }
    self.groups = Self.group(days, language: language, digits: digits)
    self.nextHoliday = Self.nextHoliday(after: today)
    let end = Patro.coverage.upperBound
    self.dataEndsOn =
      "\(MonthNames.bs(end.month, language)) \(digits.format(end.day)), "
      + "\(digits.format(end.year))"
  }

  /// The total number of days in the filtered list.
  public var count: Int {
    groups.reduce(0) { $0 + $1.days.count }
  }

  /// Whether the filtered list has no day.
  public var isEmpty: Bool {
    groups.isEmpty
  }

  /// The holiday count of one month, in the list's digits.
  /// - Parameter count: The number of holidays.
  /// - Returns: The count and the localised words.
  public func holidayCountText(_ count: Int) -> String {
    "\(digits.format(count)) \(Self.holidayWord(count, language: language))"
  }

  static func group(
    _ days: [FestivalDay],
    language: AppLanguage,
    digits: Digits
  ) -> [FestivalGroup] {
    var groups: [FestivalGroup] = []
    for day in days {
      if let index = groups.lastIndex(where: {
        $0.year == day.date.year && $0.month == day.date.month
      }) {
        var group = groups[index]
        group = FestivalGroup(
          year: group.year,
          month: group.month,
          title: group.title,
          holidayCount: group.holidayCount + (day.isHoliday ? 1 : 0),
          days: group.days + [day]
        )
        groups[index] = group
      } else {
        groups.append(
          FestivalGroup(
            year: day.date.year,
            month: day.date.month,
            title:
              "\(MonthNames.bs(day.date.month, language)) \(digits.format(day.date.year))",
            holidayCount: day.isHoliday ? 1 : 0,
            days: [day]
          )
        )
      }
    }
    return groups
  }

  private static func nextHoliday(after today: MonthDate) -> HolidayCountdown? {
    PatroLookahead.holiday(on: today) ?? PatroLookahead.nextHoliday(after: today, withinDays: 400)
  }

  private static func holidayWord(_ count: Int, language: AppLanguage) -> String {
    count == 1
      ? AppText.string("public holiday", language)
      : AppText.string("public holidays", language)
  }
}
