import NepDate
import NepDatePatro

/// A day with a public holiday or an event, as the festivals list and search show it (MR-03,
/// MR-13).
public struct FestivalDay: Sendable, Hashable, Identifiable {
  /// The day.
  public let date: MonthDate

  /// Whether the day is a public holiday.
  public let isHoliday: Bool

  /// The day's events in file order; empty on a holiday with no named event.
  public let names: [NamedText]

  /// A stable identity: the date and the first name, so two holidays on one day stay apart.
  public var id: String {
    "\(date.year)-\(date.month)-\(date.day)-\(names.first?.english ?? "holiday")"
  }

  /// The first name in the given language, or `nil` for a holiday with no named event.
  /// - Parameter language: The language of the name.
  /// - Returns: The first event's name.
  public func name(_ language: AppLanguage) -> String? {
    names.first?.name(language)
  }

  /// The day as the festivals list draws it, when it has no named event.
  /// - Parameters:
  ///   - language: The language of the name.
  ///   - publicHoliday: The localised words for a public holiday.
  /// - Returns: The first name, or the public-holiday words.
  public func title(_ language: AppLanguage, publicHoliday: String) -> String {
    name(language) ?? publicHoliday
  }

  /// Whether a query matches a name in either script (MR-13 AC1).
  /// - Parameter query: The typed text; leading and trailing spaces are ignored.
  /// - Returns: `true` when a name contains the query.
  public func matches(_ query: String) -> Bool {
    let needle = query.trimmingCharacters(in: .whitespacesAndNewlines)
    guard !needle.isEmpty else { return false }
    return names.contains { named in
      named.english.localizedCaseInsensitiveContains(needle) || named.nepali.contains(needle)
    }
  }
}

/// The days in the patro coverage that have a public holiday or an event (MR-03, MR-13).
public enum FestivalDirectory {
  /// Every covered day with a holiday or an event, in date order.
  ///
  /// The patro table is scanned once, when the directory is first read.
  public static let all: [FestivalDay] = build()

  /// The days from a date to the end of coverage.
  /// - Parameter date: The first day to include.
  /// - Returns: The days in date order.
  public static func from(_ date: MonthDate) -> [FestivalDay] {
    all.filter { $0.date >= date }
  }

  /// The next few days with a holiday or an event, starting today.
  /// - Parameters:
  ///   - date: The first day to include.
  ///   - limit: The maximum number of days.
  /// - Returns: The days in date order.
  public static func next(after date: MonthDate, limit: Int) -> [FestivalDay] {
    guard limit > 0 else { return [] }
    return Array(all.lazy.filter { $0.date >= date }.prefix(limit))
  }

  private static func build() -> [FestivalDay] {
    var days: [FestivalDay] = []
    days.reserveCapacity(1_000)
    for date in Patro.coverage {
      let info = date.calendarInfo
      guard info.isPublicHoliday || !info.events.isEmpty else { continue }
      days.append(
        FestivalDay(
          date: MonthDate(year: date.year, month: date.month, day: date.day),
          isHoliday: info.isPublicHoliday,
          names: info.events.map(NamedText.init)
        )
      )
    }
    return days
  }
}
