import CalendarKit
import Foundation
import Testing

/// W-03: the month widget's stored month, its timeline and its day links.
private enum MonthWidgetTestData {
  static let zone = TimeZone(identifier: "Asia/Kathmandu")!

  static func suiteName() -> String {
    "np.nepdate.tests.monthwidget.\(UUID().uuidString)"
  }

  static func remove(_ suiteName: String) {
    UserDefaults(suiteName: suiteName)?.removePersistentDomain(forName: suiteName)
  }

  static func navigation(_ suiteName: String, today: MonthDate) throws -> MonthNavigation {
    let now = try WidgetTestData.noon(today)
    return MonthNavigation(suiteName: suiteName, now: { now }, deviceZone: zone)
  }
}

@Suite("Month widget intents")
struct MonthWidgetIntentTests {
  @Test("previous and next move the stored month by one, and Today clears it")
  func previousNextToday() throws {
    let suite = MonthWidgetTestData.suiteName()
    defer { MonthWidgetTestData.remove(suite) }
    let today = MonthDate(year: 2083, month: 6, day: 20)
    let navigation = try MonthWidgetTestData.navigation(suite, today: today)

    #expect(navigation.shownMonth(on: today) == nil)

    navigation.perform(.previous)
    #expect(navigation.shownMonth(on: today) == MonthDate(year: 2083, month: 5, day: 1))

    navigation.perform(.previous)
    #expect(navigation.shownMonth(on: today) == MonthDate(year: 2083, month: 4, day: 1))

    navigation.perform(.next)
    #expect(navigation.shownMonth(on: today) == MonthDate(year: 2083, month: 5, day: 1))

    navigation.perform(.today)
    #expect(navigation.shownMonth(on: today) == nil)
  }

  @Test("the month steps across a year boundary")
  func yearBoundary() throws {
    let suite = MonthWidgetTestData.suiteName()
    defer { MonthWidgetTestData.remove(suite) }
    let today = MonthDate(year: 2083, month: 1, day: 10)
    let navigation = try MonthWidgetTestData.navigation(suite, today: today)

    navigation.perform(.previous)
    #expect(navigation.shownMonth(on: today) == MonthDate(year: 2082, month: 12, day: 1))

    navigation.perform(.next)
    #expect(navigation.shownMonth(on: today) == MonthDate(year: 2083, month: 1, day: 1))
  }

  @Test("a press that would leave BS 1901 to 2199 changes nothing")
  func edgesChangeNothing() throws {
    let suite = MonthWidgetTestData.suiteName()
    defer { MonthWidgetTestData.remove(suite) }
    let first = MonthDate(year: 1901, month: 1, day: 15)
    let navigation = try MonthWidgetTestData.navigation(suite, today: first)

    navigation.perform(.previous)
    #expect(navigation.shownMonth(on: first) == nil)

    navigation.perform(.next)
    #expect(navigation.shownMonth(on: first) == MonthDate(year: 1901, month: 2, day: 1))
  }

  @Test("a stored month from an earlier day is not shown")
  func storedMonthExpires() throws {
    let suite = MonthWidgetTestData.suiteName()
    defer { MonthWidgetTestData.remove(suite) }
    let today = MonthDate(year: 2083, month: 6, day: 20)
    let navigation = try MonthWidgetTestData.navigation(suite, today: today)
    navigation.perform(.next)

    let tomorrow = MonthDate(year: 2083, month: 6, day: 21)
    #expect(navigation.shownMonth(on: today) == MonthDate(year: 2083, month: 7, day: 1))
    #expect(navigation.shownMonth(on: tomorrow) == nil)
  }
}

@Suite("Month widget timeline")
struct MonthWidgetTimelineTests {
  private static let settings = Settings(language: .english, digits: .english, dateFollows: .nepal)

  @Test("the first entry after midnight ignores the stored month and shows the current one")
  func midnightResetsNavigation() throws {
    let suite = MonthWidgetTestData.suiteName()
    defer { MonthWidgetTestData.remove(suite) }
    let today = MonthDate(year: 2083, month: 6, day: 20)
    let navigation = try MonthWidgetTestData.navigation(suite, today: today)
    navigation.perform(.next)

    let entries = MonthWidgetTimeline.entries(
      now: try WidgetTestData.noon(today),
      settings: Self.settings,
      deviceZone: MonthWidgetTestData.zone,
      navigation: navigation
    )

    #expect(entries.count == 8)
    #expect(entries[0].title == "Kartik 2083")
    #expect(entries[0].isCurrentMonth == false)
    #expect(entries[1].title == "Asoj 2083")
    #expect(entries[1].isCurrentMonth)
    #expect(entries.dropFirst().allSatisfy { $0.title == "Asoj 2083" })
  }

  @Test("Jestha 2083 is a six-week month and fills 42 cells")
  func sixWeekMonth() throws {
    let entry = try #require(
      MonthWidgetTimeline.entry(
        at: try WidgetTestData.noon(MonthDate(year: 2083, month: 2, day: 10)),
        shown: nil,
        settings: Self.settings,
        deviceZone: MonthWidgetTestData.zone
      )
    )

    #expect(entry.title == "Jestha 2083")
    #expect(entry.cells.count == 42)
    #expect(entry.rowCount == 6)
    #expect(entry.weekdays.count == 7)
    #expect(entry.cells.filter(\.isInMonth).count >= 29)
    #expect(entry.cells.filter(\.isToday).count == 1)
  }

  @Test("the Nepali entry uses Nepali names and digits")
  func nepaliEntry() throws {
    let entry = try #require(
      MonthWidgetTimeline.entry(
        at: try WidgetTestData.noon(MonthDate(year: 2083, month: 6, day: 25)),
        shown: nil,
        settings: Settings(language: .nepali, digits: .nepali, dateFollows: .nepal),
        deviceZone: MonthWidgetTestData.zone
      )
    )

    #expect(entry.title == "असोज २०८३")
    let cell = try #require(entry.cells.first { $0.date?.day == 25 && $0.isInMonth })
    #expect(cell.bs == "२५")
  }

  @Test("a day link is nepdate://day/2083-06-25 and parses back to that day")
  func dayLinks() throws {
    let entry = try #require(
      MonthWidgetTimeline.entry(
        at: try WidgetTestData.noon(MonthDate(year: 2083, month: 6, day: 20)),
        shown: nil,
        settings: Self.settings,
        deviceZone: MonthWidgetTestData.zone
      )
    )
    let holiday = MonthDate(year: 2083, month: 6, day: 25)
    let cell = try #require(entry.cells.first { $0.date == holiday && $0.isInMonth })

    #expect(cell.url?.absoluteString == "nepdate://day/2083-06-25")
    #expect(cell.url.flatMap { AppRoute(url: $0) } == .day(holiday))
    #expect(cell.isHoliday)
    #expect(cell.hasEvents)
    #expect(entry.cells.filter { !$0.isInMonth }.allSatisfy { $0.url == nil })
  }
}
