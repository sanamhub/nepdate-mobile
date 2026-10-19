import CalendarKit
import Foundation
import NepDate
import NepDatePatro

/// One event in the Upcoming list (MR-03 AC1).
public struct UpcomingEntry: Sendable, Hashable, Identifiable {
  /// The event's date.
  public let date: MonthDate

  /// The event name in the app language, with its digits resolved.
  public let name: String

  /// Whether the day is a public holiday.
  public let isHoliday: Bool

  /// The whole days from today; 0 is today.
  public let daysFromToday: Int

  /// A stable identity; the date plus the name keeps duplicate names apart.
  public let id: String

  /// The BS date, for example `Asoj 20, 2083`.
  public let bsDate: String

  /// The AD date, for example `6 October 2026`.
  public let adDate: String

  /// The BS and AD dates on one line.
  public let dateLine: String

  /// "Today", "Tomorrow" or "in N days".
  public let relativeText: String

  init(
    date: MonthDate,
    name: String,
    isHoliday: Bool,
    daysFromToday: Int,
    id: String,
    bsDate: String,
    adDate: String,
    dateLine: String,
    relativeText: String
  ) {
    self.date = date
    self.name = name
    self.isHoliday = isHoliday
    self.daysFromToday = daysFromToday
    self.id = id
    self.bsDate = bsDate
    self.adDate = adDate
    self.dateLine = dateLine
    self.relativeText = relativeText
  }
}

/// The entries of one BS month (MR-03 AC2).
public struct UpcomingGroup: Sendable, Hashable, Identifiable {
  /// The BS year.
  public let year: Int

  /// The BS month, 1 to 12.
  public let month: Int

  /// The group's entries, in date and file order.
  public let entries: [UpcomingEntry]

  /// The identity, the year and month.
  public var id: String { "\(year)-\(month)" }

  /// The month title, for example `Asoj 2083`.
  public let title: String
}

/// The next 20 public holidays and events from today (MR-03).
public struct UpcomingViewModel: Sendable {
  /// The groups, in date order.
  public let groups: [UpcomingGroup]

  /// The last date the patro data covers, when fewer than 20 entries remain.
  public let dataEndsOn: MonthDate?

  /// The text after the last entry, or `nil` when the list reached 20 entries.
  public let dataEndsText: String?

  /// Every entry, in date and file order.
  public var entries: [UpcomingEntry] {
    groups.flatMap(\.entries)
  }

  /// Builds the list.
  /// - Parameters:
  ///   - today: The first day to include.
  ///   - language: The language of the event and date names.
  ///   - digits: The resolved Digits setting.
  ///   - limit: The maximum entries; 20 by default.
  public init?(today: MonthDate, language: AppLanguage, digits: Digits, limit: Int = 20) {
    guard limit > 0 else { return nil }
    guard
      let libraryToday = try? NepaliDate(
        year: today.year,
        month: today.month,
        day: today.day
      )
    else { return nil }

    let coverage = Patro.coverage
    let start = max(libraryToday, coverage.lowerBound)
    var all: [UpcomingEntry] = []
    var date = start
    while date <= coverage.upperBound && all.count < limit {
      let info = date.calendarInfo
      let monthDate = MonthDate(year: date.year, month: date.month, day: date.day)
      let bs =
        "\(MonthNames.bs(date.month, language)) \(digits.format(date.day)), "
        + "\(digits.format(date.year))"
      let gregorian = date.gregorian
      let ad =
        "\(digits.format(gregorian.day)) \(MonthNames.ad(gregorian.month, language)) "
        + "\(digits.format(gregorian.year))"
      let dateLine = AppText.string("\(bs) · \(ad)", language)
      let distance = libraryToday.days(until: date)
      let relative = Self.relativeText(distance, language: language, digits: digits)
      for (index, event) in info.events.enumerated() {
        guard all.count < limit else { break }
        let name = digits.applying(to: event.name(language.lang))
        all.append(
          UpcomingEntry(
            date: monthDate,
            name: name,
            isHoliday: info.isPublicHoliday,
            daysFromToday: distance,
            id: "\(date.year)-\(date.month)-\(date.day)-\(index)-\(name)",
            bsDate: bs,
            adDate: ad,
            dateLine: dateLine,
            relativeText: relative
          )
        )
      }
      guard let next = try? date.adding(days: 1) else { break }
      date = next
    }

    var grouped: [(year: Int, month: Int, entries: [UpcomingEntry])] = []
    for entry in all {
      if let last = grouped.last, last.year == entry.date.year, last.month == entry.date.month {
        grouped[grouped.count - 1].entries.append(entry)
      } else {
        grouped.append((entry.date.year, entry.date.month, [entry]))
      }
    }
    self.groups = grouped.map { group in
      UpcomingGroup(
        year: group.year,
        month: group.month,
        entries: group.entries,
        title: "\(MonthNames.bs(group.month, language)) \(digits.format(group.year))"
      )
    }
    if all.count < limit {
      let end = MonthDate(
        year: coverage.upperBound.year,
        month: coverage.upperBound.month,
        day: coverage.upperBound.day
      )
      self.dataEndsOn = end
      let endText =
        "\(MonthNames.bs(end.month, language)) \(digits.format(end.day)), "
        + "\(digits.format(end.year))"
      self.dataEndsText = AppText.string("Holiday data ends on \(endText)", language)
    } else {
      self.dataEndsOn = nil
      self.dataEndsText = nil
    }
  }

  private static func relativeText(
    _ days: Int,
    language: AppLanguage,
    digits: Digits
  ) -> String {
    switch days {
    case 0:
      return AppText.string("Today", language)
    case 1:
      return AppText.string("Tomorrow", language)
    default:
      let count = digits.format(days)
      return AppText.string("in \(count) days", language)
    }
  }
}
