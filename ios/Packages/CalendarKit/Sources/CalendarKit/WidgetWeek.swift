import Foundation
import NepDate
import NepDatePatro

/// One day of the this-week widget's row (W-10).
public struct WidgetWeekDay: Sendable, Hashable {
  /// The date.
  public let date: MonthDate

  /// The BS day number in the resolved digits.
  public let day: String

  /// The short weekday heading.
  public let weekday: String

  /// Whether the day is today.
  public let isToday: Bool

  /// Whether the day is a public holiday.
  public let isHoliday: Bool

  /// Whether the day has an event.
  public let hasEvents: Bool
}

/// The this-week widget's values (W-10): the seven days of the BS week and the next event.
public struct WidgetWeek: Sendable, Hashable {
  /// The seven days, Sunday first.
  public let days: [WidgetWeekDay]

  /// The next day in the week with a holiday or event, or the next one after the week.
  public let nextEvent: WidgetEvent?
}

/// Builds the this-week widget's values.
public enum WidgetWeekBuilder {
  /// The week around today and its next event.
  /// - Parameters:
  ///   - now: The instant the entry stands for.
  ///   - settings: The stored settings.
  ///   - deviceZone: The device's zone, injected for tests.
  /// - Returns: The week, or `nil` outside the library's range.
  public static func week(
    now: Date,
    settings: Settings,
    deviceZone: TimeZone = .current
  ) -> WidgetWeek? {
    let today = TodayProvider(now: { now }, deviceZone: deviceZone)
      .today(dateFollows: settings.dateFollows)
    guard let start = try? NepaliDate(year: today.year, month: today.month, day: today.day)
    else { return nil }
    let language = settings.language.resolved
    let digits = settings.digits.resolved(for: language)
    let sunday = try? start.adding(days: -start.weekday.rawValue)
    guard let sunday else { return nil }

    var days: [WidgetWeekDay] = []
    days.reserveCapacity(7)
    for offset in 0..<7 {
      guard let date = try? sunday.adding(days: offset) else { return nil }
      let info = date.calendarInfo
      days.append(
        WidgetWeekDay(
          date: MonthDate(year: date.year, month: date.month, day: date.day),
          day: digits.format(date.day),
          weekday: date.weekday.shortName(language.lang),
          isToday: date == start,
          isHoliday: info.isPublicHoliday,
          hasEvents: !info.events.isEmpty
        )
      )
    }

    var nextEvent: WidgetEvent?
    for offset in 1...13 {
      guard let date = try? start.adding(days: offset) else { break }
      let info = date.calendarInfo
      if info.isPublicHoliday || !info.events.isEmpty {
        let name = info.events.first.map { digits.applying(to: $0.name(language.lang)) }
        nextEvent = WidgetEvent(
          date: MonthDate(year: date.year, month: date.month, day: date.day),
          name: name,
          isHoliday: info.isPublicHoliday,
          daysFromToday: start.days(until: date),
          count: digits.format(start.days(until: date)),
          id: "\(date.year)-\(date.month)-\(date.day)-week"
        )
        break
      }
    }

    return WidgetWeek(days: days, nextEvent: nextEvent)
  }
}

/// The festival countdown's shown index, kept in the App Group for the day it was chosen (W-06).
public struct FestivalWindowNavigation: Sendable {
  /// The defaults suite the navigation opens.
  public let suiteName: String

  /// Creates the navigation.
  /// - Parameter suiteName: The suite to open; defaults to ``AppGroup/identifier``.
  public init(suiteName: String = AppGroup.identifier) {
    self.suiteName = suiteName
  }

  /// The valid shown index for a day, or 0 when none was stored or it is from an earlier day.
  /// - Parameters:
  ///   - today: The day the entry stands for.
  ///   - count: The number of days in the window.
  /// - Returns: The index, in `0..<count`.
  public func index(on today: MonthDate, count: Int) -> Int {
    guard count > 0 else { return 0 }
    guard
      let defaults = AppGroup.defaults(suiteName: suiteName),
      defaults.string(forKey: Key.chosenOn) == today.description,
      let stored = defaults.object(forKey: Key.index) as? Int
    else { return 0 }
    return min(max(0, stored), count - 1)
  }

  /// Moves the shown index by one step, stopping at the ends of the window.
  /// - Parameters:
  ///   - step: `1` for next, `-1` for previous.
  ///   - today: The day the entry stands for.
  ///   - count: The number of days in the window.
  public func step(_ step: Int, today: MonthDate, count: Int) {
    guard count > 0 else { return }
    let current = index(on: today, count: count)
    let updated = min(max(0, current + step), count - 1)
    guard let defaults = AppGroup.defaults(suiteName: suiteName) else { return }
    defaults.set(updated, forKey: Key.index)
    defaults.set(today.description, forKey: Key.chosenOn)
  }

  private enum Key {
    static let index = "festival_window_index"
    static let chosenOn = "festival_window_chosen_on"
  }
}
