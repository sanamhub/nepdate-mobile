import CalendarKit
import Testing

/// W-06: the countdown's window is twelve days, matching Android, not twelve events. A busy day
/// contributes all of its events and does not shorten the window.
struct FestivalWindowTests {
  private static let zone = WidgetTestData.zone
  private static let settings = WidgetTestData.settings()

  @Test("the window stops after twelve days")
  func twelveDays() throws {
    let now = try WidgetTestData.noon(MonthDate(year: 2083, month: 6, day: 8))
    let window = WidgetTimeline.festivalWindow(
      now: now,
      settings: Self.settings,
      deviceZone: Self.zone
    )

    #expect(!window.isEmpty)
    #expect(window.allSatisfy { $0.daysFromToday >= 0 && $0.daysFromToday < 12 })
  }

  @Test("every event on a busy day is kept")
  func busyDay() throws {
    let now = try WidgetTestData.noon(MonthDate(year: 2083, month: 6, day: 8))
    let window = WidgetTimeline.festivalWindow(
      now: now,
      settings: Self.settings,
      deviceZone: Self.zone
    )
    let asoj9 = MonthDate(year: 2083, month: 6, day: 9)

    #expect(window.filter { $0.date == asoj9 }.count > 1)
  }

  @Test("a window of no days is empty")
  func empty() throws {
    let now = try WidgetTestData.noon(MonthDate(year: 2083, month: 6, day: 20))
    let window = WidgetTimeline.festivalWindow(
      now: now,
      settings: Self.settings,
      deviceZone: Self.zone,
      days: 0
    )

    #expect(window.isEmpty)
  }
}
