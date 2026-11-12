import CalendarKit
import Foundation
import Testing

/// W-04: what the three Lock Screen families draw.
@Suite("Lock Screen widget model")
struct LockScreenWidgetModelTests {
  private static let holiday = MonthDate(year: 2083, month: 6, day: 25)
  private static let normal = MonthDate(year: 2083, month: 6, day: 20)

  @Test("the inline, circular and rectangular content on a normal day")
  func content() throws {
    let english = try WidgetTestData.entry(Self.normal).lockScreen
    #expect(english.inline == "Asoj 20, Tuesday")
    #expect(english.circularDay == "20")
    #expect(english.circularMonth == "Aso")
    #expect(english.isHoliday == false)
    #expect(english.rectangularDate == "Asoj 20 · Tuesday")
    let next = try #require(english.rectangularHoliday)
    #expect(next.daysFromToday > 0)

    let nepali = try WidgetTestData.entry(Self.normal, language: .nepali).lockScreen
    #expect(nepali.inline == "असोज २०, मङ्गलवार")
    #expect(nepali.circularDay == "२०")
  }

  @Test("W-09: the rectangular Lock Screen view has the tithi in both languages")
  func rectangularTithi() throws {
    let english = try WidgetTestData.entry(Self.normal).day.tithi
    let nepali = try WidgetTestData.entry(Self.normal, language: .nepali).day.tithi

    #expect(english != nil)
    #expect(nepali != nil)
    #expect(english != nepali)
  }

  @Test("a holiday underlines the circular view and shows today in the rectangular one")
  func holidayContent() throws {
    let content = try WidgetTestData.entry(Self.holiday).lockScreen
    #expect(content.isHoliday)
    #expect(content.inline == "Asoj 25, Sunday")
    let today = try #require(content.rectangularHoliday)
    #expect(today.daysFromToday == 0)
    #expect(today.name == "Ghatasthapana Brata")
  }
}

/// W-05: the controls hand their route to the app through the App Group.
@Suite("Widget controls")
struct WidgetControlTests {
  private static func suiteName() -> String {
    "np.nepdate.tests.controls.\(UUID().uuidString)"
  }

  @Test("Open converter and Open today write their routes, and the app reads each once")
  func routes() {
    let suite = Self.suiteName()
    defer { UserDefaults(suiteName: suite)?.removePersistentDomain(forName: suite) }
    let store = PendingRouteStore(suiteName: suite)

    #expect(store.consume() == nil)

    store.write(.converter)
    #expect(store.consume() == .converter)
    #expect(store.consume() == nil)

    store.write(.today)
    #expect(store.consume() == .today)
  }

  @Test("the router selects the Converter and the Month")
  func tabs() {
    #expect(AppRoute.converter.tab == .converter)
    #expect(AppRoute.today.tab == .patro)
    #expect(AppRoute.day(MonthDate(year: 2083, month: 6, day: 25)).tab == .patro)
  }

  @Test("a stored value that is not a route is dropped")
  func notARoute() {
    let suite = Self.suiteName()
    defer { UserDefaults(suiteName: suite)?.removePersistentDomain(forName: suite) }
    UserDefaults(suiteName: suite)?.set("https://example.com", forKey: "pending_route")

    #expect(PendingRouteStore(suiteName: suite).consume() == nil)
  }
}

private final class ReloadCounter: @unchecked Sendable {
  nonisolated(unsafe) var count = 0
}

/// When the app asks WidgetKit to reload.
@Suite("Widget refresh")
struct WidgetRefreshTests {
  private static func suiteName() -> String {
    "np.nepdate.tests.refresh.\(UUID().uuidString)"
  }

  @Test("a settings change calls the injected reload once")
  func settingsChange() {
    let suite = Self.suiteName()
    defer { UserDefaults(suiteName: suite)?.removePersistentDomain(forName: suite) }
    let counter = ReloadCounter()
    let refresher = WidgetRefresher(suiteName: suite) { counter.count += 1 }

    refresher.settingsChanged()

    #expect(counter.count == 1)
  }

  @Test("an activation on a new day reloads once and one on the same day does not")
  func newDay() {
    let suite = Self.suiteName()
    defer { UserDefaults(suiteName: suite)?.removePersistentDomain(forName: suite) }
    let counter = ReloadCounter()
    let refresher = WidgetRefresher(suiteName: suite) { counter.count += 1 }
    let monday = MonthDate(year: 2083, month: 6, day: 20)
    let tuesday = MonthDate(year: 2083, month: 6, day: 21)

    refresher.becameActive(on: monday)
    #expect(counter.count == 1)

    refresher.becameActive(on: monday)
    #expect(counter.count == 1)

    refresher.becameActive(on: tuesday)
    #expect(counter.count == 2)
  }

  @Test("a time or zone change reloads")
  func timeChange() {
    let suite = Self.suiteName()
    defer { UserDefaults(suiteName: suite)?.removePersistentDomain(forName: suite) }
    let counter = ReloadCounter()
    let refresher = WidgetRefresher(suiteName: suite) { counter.count += 1 }

    refresher.timeChanged()

    #expect(counter.count == 1)
  }
}

/// MR-10: every widget has a spoken label for its numeral, holiday and count.
@Suite("Widget accessibility")
struct WidgetAccessibilityTests {
  private static let words = SpokenWords(
    publicHoliday: "public holiday",
    today: "today",
    tomorrow: "tomorrow",
    inDays: { "in \($0) days" }
  )

  @Test("the numeral, holiday and count are spoken")
  func labels() throws {
    let holiday = try WidgetTestData.entry(MonthDate(year: 2083, month: 6, day: 25))
    #expect(
      WidgetSpeech.today(holiday, words: Self.words)
        == "Asoj 25, Sunday, 11 October 2026, public holiday, Ghatasthapana Brata"
    )
    let first = try #require(holiday.upcoming.first)
    #expect(WidgetSpeech.event(first, words: Self.words) == "Ghatasthapana Brata, today")

    let normal = try WidgetTestData.entry(MonthDate(year: 2083, month: 6, day: 20))
    #expect(
      WidgetSpeech.today(normal, words: Self.words) == "Asoj 20, Tuesday, 6 October 2026"
    )
    let next = try #require(normal.nextHoliday)
    #expect(next.daysFromToday >= 2)
    #expect(WidgetSpeech.relative(next, words: Self.words) == "in \(next.count) days")
  }

  @Test("a month cell speaks its date, weekday, holiday and event")
  func monthCell() throws {
    let entry = try #require(
      MonthWidgetTimeline.entry(
        at: try WidgetTestData.noon(MonthDate(year: 2083, month: 6, day: 20)),
        shown: nil,
        settings: Settings(language: .english, digits: .english, dateFollows: .nepal),
        deviceZone: WidgetTestData.zone
      )
    )
    let cell = try #require(
      entry.cells.first { $0.date == MonthDate(year: 2083, month: 6, day: 25) && $0.isInMonth }
    )

    #expect(
      cell.spoken(publicHoliday: "public holiday")
        == "Asoj 25, 2083, Sunday, 11 October 2026, public holiday, Ghatasthapana Brata"
    )
  }
}
