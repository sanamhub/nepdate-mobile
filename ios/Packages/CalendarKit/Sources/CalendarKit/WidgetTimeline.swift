import Foundation
import NepDate
import NepDatePatro

/// The values one Today or upcoming widget entry draws.
public struct WidgetDay: Sendable, Hashable {
  /// The Bikram Sambat date.
  public let date: MonthDate

  /// The day number in the resolved Digits setting.
  public let day: String

  /// The BS month name.
  public let month: String

  /// The weekday name.
  public let weekday: String

  /// The AD date, for example `6 October 2026`.
  public let adDate: String

  /// The first event on a public holiday, or `nil` when the day has none.
  public let holidayName: String?

  /// Whether the date is a public holiday.
  public let isHoliday: Bool

  /// Whether the small widget shows the "Nepal" label.
  public let showsNepalLabel: Bool
}

/// One holiday or event in the widget's upcoming list.
public struct WidgetEvent: Sendable, Hashable {
  /// The date of the event.
  public let date: MonthDate

  /// The event name, or `nil` for a public holiday with no named event.
  public let name: String?

  /// Whether the day is a public holiday.
  public let isHoliday: Bool

  /// The whole days from the entry's date; 0 is today.
  public let daysFromToday: Int

  /// The day count in the resolved Digits setting.
  public let count: String

  /// A stable identity for the list.
  public let id: String
}

/// One timeline entry: the date at which WidgetKit asks for it and its content.
public struct WidgetEntry: Sendable, Hashable {
  /// The instant of this entry.
  public let date: Date

  /// The day the entry shows.
  public let day: WidgetDay

  /// The next public holiday after today, for the small widget's footer.
  public let nextHoliday: WidgetEvent?

  /// Up to three entries from today in date order and then file order.
  public let upcoming: [WidgetEvent]

  /// The last covered BS date, when fewer than three entries remain.
  public let dataEndsOn: String?
}

/// Builds the widget timeline: one entry for the build instant and one at each of the next seven
/// local midnights (ADR-0006).
public enum WidgetTimeline {
  /// The entries for a timeline.
  /// - Parameters:
  ///   - now: The instant at which the timeline is built.
  ///   - settings: The stored settings.
  ///   - deviceZone: The device's zone, injected for tests.
  ///   - dayCount: The number of midnights to add; seven by default.
  /// - Returns: The entries, oldest first.
  public static func entries(
    now: Date,
    settings: Settings,
    deviceZone: TimeZone = .current,
    dayCount: Int = 7
  ) -> [WidgetEntry] {
    guard dayCount > 0 else { return [] }
    let zone = settings.dateFollows == .nepal ? CalendarDates.nepal : deviceZone
    var calendar = Calendar(identifier: .gregorian)
    calendar.timeZone = zone
    let start = calendar.startOfDay(for: now)

    var instants = [now]
    for offset in 1...dayCount {
      guard let midnight = calendar.date(byAdding: .day, value: offset, to: start) else {
        continue
      }
      instants.append(midnight)
    }

    return instants.compactMap { instant in
      entry(at: instant, settings: settings, deviceZone: deviceZone)
    }
  }

  private static func entry(
    at instant: Date,
    settings: Settings,
    deviceZone: TimeZone
  ) -> WidgetEntry? {
    let today = TodayProvider(now: { instant }, deviceZone: deviceZone)
      .today(dateFollows: settings.dateFollows)
    guard
      let date = try? NepaliDate(year: today.year, month: today.month, day: today.day)
    else { return nil }

    let language = settings.language.resolved
    let digits = settings.digits.resolved(for: language)
    let info = date.calendarInfo
    let holidayName =
      info.isPublicHoliday
      ? info.events.first.map { digits.applying(to: $0.name(language.lang)) }
      : nil
    let day = WidgetDay(
      date: today,
      day: digits.format(date.day),
      month: MonthNames.bs(date.month, language),
      weekday: date.weekday.name(language.lang),
      adDate: adDate(date.gregorian, language: language, digits: digits),
      holidayName: holidayName,
      isHoliday: info.isPublicHoliday,
      showsNepalLabel: settings.dateFollows == .nepal
        && deviceZone.secondsFromGMT(for: instant)
          != CalendarDates.nepal.secondsFromGMT(for: instant)
    )
    let upcoming = upcomingEvents(
      after: date,
      limit: 3,
      language: language,
      digits: digits
    )
    return WidgetEntry(
      date: instant,
      day: day,
      nextHoliday: nextHoliday(after: date, language: language, digits: digits),
      upcoming: upcoming,
      dataEndsOn: upcoming.count < 3
        ? bsDate(Patro.coverage.upperBound, language: language, digits: digits)
        : nil
    )
  }

  private static func nextHoliday(
    after today: NepaliDate,
    language: AppLanguage,
    digits: Digits
  ) -> WidgetEvent? {
    guard let tomorrow = try? today.adding(days: 1) else { return nil }
    var date = max(tomorrow, Patro.coverage.lowerBound)
    while date <= Patro.coverage.upperBound {
      let info = date.calendarInfo
      if info.isPublicHoliday {
        let name = info.events.first.map { digits.applying(to: $0.name(language.lang)) }
        return event(
          date: date,
          today: today,
          name: name,
          isHoliday: true,
          suffix: "holiday",
          digits: digits
        )
      }
      guard let next = try? date.adding(days: 1) else { break }
      date = next
    }
    return nil
  }

  private static func upcomingEvents(
    after today: NepaliDate,
    limit: Int,
    language: AppLanguage,
    digits: Digits
  ) -> [WidgetEvent] {
    var date = max(today, Patro.coverage.lowerBound)
    var events: [WidgetEvent] = []
    while date <= Patro.coverage.upperBound && events.count < limit {
      let info = date.calendarInfo
      for (index, named) in info.events.enumerated() {
        guard events.count < limit else { break }
        let name = digits.applying(to: named.name(language.lang))
        events.append(
          event(
            date: date,
            today: today,
            name: name,
            isHoliday: info.isPublicHoliday,
            suffix: "\(index)",
            digits: digits
          )
        )
      }
      guard let next = try? date.adding(days: 1) else { break }
      date = next
    }
    return events
  }

  private static func event(
    date: NepaliDate,
    today: NepaliDate,
    name: String?,
    isHoliday: Bool,
    suffix: String,
    digits: Digits
  ) -> WidgetEvent {
    WidgetEvent(
      date: MonthDate(year: date.year, month: date.month, day: date.day),
      name: name,
      isHoliday: isHoliday,
      daysFromToday: today.days(until: date),
      count: digits.format(today.days(until: date)),
      id: "\(date.year)-\(date.month)-\(date.day)-\(suffix)"
    )
  }

  private static func adDate(
    _ date: (year: Int, month: Int, day: Int),
    language: AppLanguage,
    digits: Digits
  ) -> String {
    "\(digits.format(date.day)) \(MonthNames.ad(date.month, language)) \(digits.format(date.year))"
  }

  private static func bsDate(
    _ date: NepaliDate,
    language: AppLanguage,
    digits: Digits
  ) -> String {
    "\(MonthNames.bs(date.month, language)) \(digits.format(date.day)), \(digits.format(date.year))"
  }
}
