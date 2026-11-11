import CalendarKit
import Foundation
import Testing

/// W-06: the festival countdown's stored index steps inside the window and starts again on a new
/// day, so the next festival is shown at midnight.
struct FestivalWindowNavigationTests {
  private static func suiteName() -> String {
    "np.nepdate.tests.festival.\(UUID().uuidString)"
  }

  private static func remove(_ suiteName: String) {
    UserDefaults(suiteName: suiteName)?.removePersistentDomain(forName: suiteName)
  }

  @Test("previous and next step one place and stop at both ends")
  func stepsAndStops() {
    let suite = Self.suiteName()
    defer { Self.remove(suite) }
    let today = MonthDate(year: 2083, month: 6, day: 20)
    let navigation = FestivalWindowNavigation(suiteName: suite)

    #expect(navigation.index(on: today, count: 12) == 0)
    navigation.step(1, today: today, count: 12)
    #expect(navigation.index(on: today, count: 12) == 1)
    navigation.step(-1, today: today, count: 12)
    #expect(navigation.index(on: today, count: 12) == 0)
    navigation.step(-1, today: today, count: 12)
    #expect(navigation.index(on: today, count: 12) == 0)

    navigation.step(1, today: today, count: 12)
    navigation.step(1, today: today, count: 12)
    #expect(navigation.index(on: today, count: 12) == 2)

    navigation.step(12, today: today, count: 12)
    #expect(navigation.index(on: today, count: 12) == 11)
    navigation.step(1, today: today, count: 12)
    #expect(navigation.index(on: today, count: 12) == 11)
  }

  @Test("a stored index from an earlier day is not shown")
  func midnightReset() {
    let suite = Self.suiteName()
    defer { Self.remove(suite) }
    let today = MonthDate(year: 2083, month: 6, day: 20)
    let tomorrow = MonthDate(year: 2083, month: 6, day: 21)
    let navigation = FestivalWindowNavigation(suiteName: suite)

    navigation.step(1, today: today, count: 12)
    #expect(navigation.index(on: today, count: 12) == 1)
    #expect(navigation.index(on: tomorrow, count: 12) == 0)
  }

  @Test("a window with no days keeps index zero")
  func emptyWindow() {
    let suite = Self.suiteName()
    defer { Self.remove(suite) }
    let today = MonthDate(year: 2083, month: 6, day: 20)
    let navigation = FestivalWindowNavigation(suiteName: suite)

    navigation.step(1, today: today, count: 0)
    #expect(navigation.index(on: today, count: 0) == 0)
  }
}
