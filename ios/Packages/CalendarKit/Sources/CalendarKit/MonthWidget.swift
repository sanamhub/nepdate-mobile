import Foundation
import NepDate

/// One of the month widget's three buttons.
public enum MonthWidgetAction: Sendable, Hashable {
  /// Shows the month before the one on screen.
  case previous

  /// Shows the month after the one on screen.
  case next

  /// Shows the current month again.
  case today
}

/// The month the month widget shows, kept in the App Group (W-03).
///
/// The store records the shown month together with the day it was chosen on. The widget honours it
/// only while that day is still today, so the first entry after midnight shows the current month
/// again and no state outlives the day.
public struct MonthNavigation: Sendable {
  /// The defaults suite the navigation opens.
  public let suiteName: String

  private let now: @Sendable () -> Date
  private let deviceZone: TimeZone

  /// Creates the navigation.
  /// - Parameters:
  ///   - suiteName: The suite to open; defaults to ``AppGroup/identifier``.
  ///   - now: The clock; defaults to the current instant.
  ///   - deviceZone: The device's zone, injected so a test can put the device elsewhere.
  public init(
    suiteName: String = AppGroup.identifier,
    now: @escaping @Sendable () -> Date = { Date() },
    deviceZone: TimeZone = .current
  ) {
    self.suiteName = suiteName
    self.now = now
    self.deviceZone = deviceZone
  }

  /// The stored month, when it was chosen on `today`.
  /// - Parameter today: The date the widget entry stands for.
  /// - Returns: The first day of the stored month, or `nil` when none is stored or it is from an
  ///   earlier day.
  public func shownMonth(on today: MonthDate) -> MonthDate? {
    guard
      let defaults = AppGroup.defaults(suiteName: suiteName),
      let id = defaults.object(forKey: Key.month) as? Int,
      defaults.string(forKey: Key.chosenOn) == today.description
    else { return nil }
    return Self.firstDay(ofMonth: id)
  }

  /// Applies one button press.
  ///
  /// A press that would leave BS 1901 to 2199 changes nothing.
  /// - Parameter action: The button that was pressed.
  public func perform(_ action: MonthWidgetAction) {
    guard let defaults = AppGroup.defaults(suiteName: suiteName) else { return }
    let settings = SettingsStore(suiteName: suiteName).load()
    let today = TodayProvider(now: now, deviceZone: deviceZone)
      .today(dateFollows: settings.dateFollows)

    switch action {
    case .today:
      defaults.removeObject(forKey: Key.month)
      defaults.removeObject(forKey: Key.chosenOn)
    case .previous, .next:
      let shown = shownMonth(on: today) ?? MonthDate(year: today.year, month: today.month, day: 1)
      let step = action == .next ? 1 : -1
      let id = shown.year * 12 + (shown.month - 1) + step
      guard Self.firstDay(ofMonth: id) != nil else { return }
      defaults.set(id, forKey: Key.month)
      defaults.set(today.description, forKey: Key.chosenOn)
    }
  }

  private static func firstDay(ofMonth id: Int) -> MonthDate? {
    let year = id / 12
    let month = id % 12 + 1
    guard (1901...2199).contains(year) else { return nil }
    return MonthDate(year: year, month: month, day: 1)
  }

  private enum Key {
    static let month = "month_widget_month"
    static let chosenOn = "month_widget_chosen_on"
  }
}

/// One day cell of the month widget's grid.
public struct MonthWidgetCell: Sendable, Hashable, Identifiable {
  /// The position in the grid, 0 to 41.
  public let id: Int

  /// The date, or `nil` for a blank cell at the edge of the library's range.
  public let date: MonthDate?

  /// The BS day number in the resolved Digits setting.
  public let bs: String

  /// The AD day number in the resolved Digits setting.
  public let ad: String

  /// Whether the date belongs to the shown month.
  public let isInMonth: Bool

  /// Whether the date is a public holiday.
  public let isHoliday: Bool

  /// Whether the date is today.
  public let isToday: Bool

  /// Whether the day has an event.
  public let hasEvents: Bool

  /// The URL a tap opens: the day's detail, or `nil` outside the shown month.
  public var url: URL? {
    guard isInMonth, let date else { return nil }
    return AppRoute.day(date).url
  }

  private let bsLabel: String
  private let weekday: String
  private let adLabel: String
  private let eventName: String?

  init(
    id: Int,
    cell: MonthCell,
    language: AppLanguage,
    digits: Digits
  ) {
    self.id = id
    self.date = cell.date
    self.bs = cell.date == nil ? "" : cell.bsNumber(digits)
    self.ad = cell.date == nil ? "" : cell.adNumber(digits)
    self.isInMonth = cell.isInMonth
    self.isHoliday = cell.isHoliday
    self.isToday = cell.isToday
    self.hasEvents = cell.hasEvents
    if let date = cell.date {
      self.bsLabel =
        "\(MonthNames.bs(date.month, language)) \(digits.format(date.day)), "
        + "\(digits.format(date.year))"
      self.adLabel =
        "\(digits.format(cell.adDay)) \(MonthNames.ad(cell.adMonth, language)) "
        + "\(digits.format(cell.adYear))"
    } else {
      self.bsLabel = ""
      self.adLabel = ""
    }
    self.weekday = cell.weekdayName(language)
    self.eventName = cell.events.first.map { digits.applying(to: $0.name(language)) }
  }

  /// The spoken label, for example
  /// `Jestha 10, 2083, Friday, 24 May 2026, public holiday, Event`.
  /// - Parameter publicHoliday: The localised words for a public holiday, without a trailing comma.
  /// - Returns: The label, or an empty string for a blank cell.
  public func spoken(publicHoliday: String) -> String {
    guard date != nil else { return "" }
    var parts = [bsLabel, weekday, adLabel]
    if isHoliday { parts.append(publicHoliday) }
    if let eventName { parts.append(eventName) }
    return parts.joined(separator: ", ")
  }
}

/// One timeline entry of the month widget.
public struct MonthWidgetEntry: Sendable, Hashable {
  /// The instant of this entry.
  public let date: Date

  /// The month title, for example `Jestha 2083`.
  public let title: String

  /// The Gregorian span of the month, for example `May – Jun 2026`.
  public let adSpan: String

  /// The seven weekday headings, Sunday first.
  public let weekdays: [String]

  /// The 35 or 42 cells, Sunday first.
  public let cells: [MonthWidgetCell]

  /// Whether the shown month is the current one.
  public let isCurrentMonth: Bool

  /// The number of week rows, 5 or 6.
  public var rowCount: Int {
    cells.count / 7
  }
}

/// Builds the month widget's timeline: one entry for the build instant and one at each of the next
/// seven local midnights (ADR-0006).
public enum MonthWidgetTimeline {
  /// The entries for a timeline.
  ///
  /// The stored month applies only to an entry on the day it was chosen, so the entries after
  /// midnight show the current month.
  /// - Parameters:
  ///   - now: The instant at which the timeline is built.
  ///   - settings: The stored settings.
  ///   - deviceZone: The device's zone, injected for tests.
  ///   - navigation: The stored month choice.
  ///   - dayCount: The number of midnights to add; seven by default.
  /// - Returns: The entries, oldest first.
  public static func entries(
    now: Date,
    settings: Settings,
    deviceZone: TimeZone = .current,
    navigation: MonthNavigation = MonthNavigation(),
    dayCount: Int = 7
  ) -> [MonthWidgetEntry] {
    WidgetTimeline.instants(
      now: now,
      settings: settings,
      deviceZone: deviceZone,
      dayCount: dayCount
    ).compactMap { instant in
      let today = TodayProvider(now: { instant }, deviceZone: deviceZone)
        .today(dateFollows: settings.dateFollows)
      return entry(
        at: instant,
        shown: navigation.shownMonth(on: today),
        settings: settings,
        deviceZone: deviceZone
      )
    }
  }

  /// One entry.
  /// - Parameters:
  ///   - instant: The instant of the entry.
  ///   - shown: A month to show, or `nil` for the current month.
  ///   - settings: The stored settings.
  ///   - deviceZone: The device's zone, injected for tests.
  /// - Returns: The entry, or `nil` when the month is outside BS 1901 to 2199.
  public static func entry(
    at instant: Date,
    shown: MonthDate?,
    settings: Settings,
    deviceZone: TimeZone = .current
  ) -> MonthWidgetEntry? {
    let today = TodayProvider(now: { instant }, deviceZone: deviceZone)
      .today(dateFollows: settings.dateFollows)
    let year = shown?.year ?? today.year
    let month = shown?.month ?? today.month
    guard let layout = MonthLayout(year: year, month: month, showTithi: false, today: today)
    else { return nil }

    let language = settings.language.resolved
    let digits = settings.digits.resolved(for: language)
    let cells = layout.cells.enumerated().map { index, cell in
      MonthWidgetCell(id: index, cell: cell, language: language, digits: digits)
    }
    return MonthWidgetEntry(
      date: instant,
      title: layout.title(language, digits: digits),
      adSpan: layout.adSpan(language, digits: digits),
      weekdays: MonthLayout.weekdayNames(language),
      cells: cells,
      isCurrentMonth: year == today.year && month == today.month
    )
  }
}
