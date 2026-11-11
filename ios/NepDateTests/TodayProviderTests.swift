import CalendarKit
import Foundation
import Testing

@Suite("Today provider")
struct TodayProviderTests {
  // 2024-07-30T18:30:00Z, the instant the acceptance criteria use.
  private static let clock = Date(timeIntervalSince1970: 1_722_364_200)

  @Test("Nepal time gives the next BS day")
  func nepalTime() throws {
    let newYork = try #require(TimeZone(identifier: "America/New_York"))
    let provider = TodayProvider(now: { Self.clock }, deviceZone: newYork)

    #expect(provider.today(dateFollows: .nepal) == MonthDate(year: 2081, month: 4, day: 16))
  }

  @Test("this device stays on the same AD day")
  func deviceTime() throws {
    let newYork = try #require(TimeZone(identifier: "America/New_York"))
    let provider = TodayProvider(now: { Self.clock }, deviceZone: newYork)

    #expect(provider.today(dateFollows: .device) == MonthDate(year: 2081, month: 4, day: 15))
  }

  @Test("a fresh store follows this device")
  func freshStore() throws {
    let store = SettingsStore(suiteName: "np.nepdate.tests.today.\(UUID().uuidString)")
    defer {
      UserDefaults(suiteName: store.suiteName)?.removePersistentDomain(forName: store.suiteName)
    }
    let newYork = try #require(TimeZone(identifier: "America/New_York"))
    let provider = TodayProvider(now: { Self.clock }, deviceZone: newYork)

    #expect(store.load().dateFollows == .device)
    #expect(
      provider.today(dateFollows: store.load().dateFollows)
        == MonthDate(year: 2081, month: 4, day: 15))
  }

  @Test("a clock outside the library's range clamps to its edge")
  func outOfRange() throws {
    let newYork = try #require(TimeZone(identifier: "America/New_York"))
    let future = TodayProvider(
      now: { Date(timeIntervalSince1970: 7_258_118_400) },
      deviceZone: newYork
    )

    #expect(future.today(dateFollows: .nepal) == MonthDate(year: 2199, month: 12, day: 30))
  }
}
