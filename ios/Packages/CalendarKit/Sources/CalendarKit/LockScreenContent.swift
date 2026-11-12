import Foundation

/// What the three Lock Screen families draw (W-04), taken from a ``WidgetEntry``.
public struct LockScreenContent: Sendable, Hashable {
  /// The inline line, for example `Asoj 20, Tuesday`.
  public let inline: String

  /// The day number for the circular view.
  public let circularDay: String

  /// The short month name under the day number.
  public let circularMonth: String

  /// Whether the day is a public holiday, which underlines the circular view.
  public let isHoliday: Bool

  /// The rectangular view's date line, for example `Asoj 25 · Sunday`.
  public let rectangularDate: String

  /// The rectangular view's tithi line, when the day has one (W-09).
  public let rectangularTithi: String?

  /// Today when it is a holiday, otherwise the next holiday; `nil` when the data has no more.
  public let rectangularHoliday: WidgetEvent?
}

extension WidgetEntry {
  /// The Lock Screen content of this entry.
  public var lockScreen: LockScreenContent {
    let holiday: WidgetEvent?
    if day.isHoliday {
      holiday = WidgetEvent(
        date: day.date,
        name: day.holidayName,
        isHoliday: true,
        daysFromToday: 0,
        count: "",
        id: "today"
      )
    } else {
      holiday = nextHoliday
    }
    return LockScreenContent(
      inline: "\(day.month) \(day.day), \(day.weekday)",
      circularDay: day.day,
      circularMonth: day.shortMonth,
      isHoliday: day.isHoliday,
      rectangularDate: "\(day.month) \(day.day) · \(day.weekday)",
      rectangularTithi: day.tithi,
      rectangularHoliday: holiday
    )
  }
}

/// The localised words a spoken label needs. CalendarKit has no string catalog, so the caller
/// passes them in, as the month screen does.
public struct SpokenWords: Sendable {
  /// The words for a public holiday, without a trailing comma.
  public let publicHoliday: String

  /// The word for today.
  public let today: String

  /// The word for tomorrow.
  public let tomorrow: String

  /// Builds "in N days" from the day count in the resolved digits.
  public let inDays: @Sendable (String) -> String

  /// Creates the words.
  /// - Parameters:
  ///   - publicHoliday: The words for a public holiday.
  ///   - today: The word for today.
  ///   - tomorrow: The word for tomorrow.
  ///   - inDays: Builds "in N days" from the count.
  public init(
    publicHoliday: String,
    today: String,
    tomorrow: String,
    inDays: @escaping @Sendable (String) -> String
  ) {
    self.publicHoliday = publicHoliday
    self.today = today
    self.tomorrow = tomorrow
    self.inDays = inDays
  }
}

/// The spoken labels of the widgets (MR-10): the numeral, the holiday and the day count.
public enum WidgetSpeech {
  /// The label of the day numeral, for example
  /// `Asoj 25, Sunday, 11 October 2026, public holiday, Ghatasthapana Brata`.
  /// - Parameters:
  ///   - entry: The entry the widget draws.
  ///   - words: The localised words.
  /// - Returns: The label.
  public static func today(_ entry: WidgetEntry, words: SpokenWords) -> String {
    var parts = [
      "\(entry.day.month) \(entry.day.day)",
      entry.day.weekday,
      entry.day.adDate,
    ]
    if entry.day.isHoliday {
      parts.append(words.publicHoliday)
      if let name = entry.day.holidayName { parts.append(name) }
    }
    return parts.joined(separator: ", ")
  }

  /// The label of one upcoming row, for example `Ghatasthapana Brata, in 3 days`.
  /// - Parameters:
  ///   - event: The holiday or event.
  ///   - words: The localised words.
  /// - Returns: The name, or the public-holiday words when it has none, then the day count.
  public static func event(_ event: WidgetEvent, words: SpokenWords) -> String {
    "\(event.name ?? words.publicHoliday), \(relative(event, words: words))"
  }

  /// The day count in words.
  /// - Parameters:
  ///   - event: The holiday or event.
  ///   - words: The localised words.
  /// - Returns: Today, tomorrow, or "in N days".
  public static func relative(_ event: WidgetEvent, words: SpokenWords) -> String {
    switch event.daysFromToday {
    case 0:
      return words.today
    case 1:
      return words.tomorrow
    default:
      return words.inDays(event.count)
    }
  }
}
