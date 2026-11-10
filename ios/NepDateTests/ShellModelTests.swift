import CalendarKit
import Foundation
import Testing

/// MR-15 AC3 and MR-05 AC1: the first-run flag and the midnight wait.
@Suite("Shell")
struct ShellModelTests {
  @Test("MR-15: the first-run flag starts unset and stays set")
  func firstRunFlag() {
    let suite = "np.nepdate.tests.firstrun.\(UUID().uuidString)"
    defer { UserDefaults(suiteName: suite)?.removePersistentDomain(forName: suite) }
    let store = FirstRunStore(suiteName: suite)

    #expect(store.hasShown == false)
    store.markShown()
    #expect(store.hasShown)
    #expect(FirstRunStore(suiteName: suite).hasShown, "the flag is stored, not held in memory")
  }

  @Test("MR-15: the first-run flag defaults to the App Group")
  func firstRunAppGroup() {
    #expect(FirstRunStore().suiteName == AppGroup.identifier)
  }

  @Test("MR-05: a noon waits twelve hours for the next midnight in Nepal")
  func nepalMidnight() {
    // 2026-10-11T12:00:00+05:45, the fixed sample day.
    let noon = Date(timeIntervalSince1970: 1_791_699_300)
    let wait = MidnightRefresh.secondsUntilNextMidnight(now: noon, zone: CalendarDates.nepal)
    #expect(wait == 12 * 60 * 60)
  }

  @Test("MR-05: the wait follows a daylight-saving change")
  func daylightSavingMidnight() throws {
    let zone = try #require(TimeZone(identifier: "America/New_York"))
    let formatter = ISO8601DateFormatter()
    // 2026-03-08T00:00:00-05:00; the clocks move forward that day, so the next midnight is 23
    // hours away, not 24.
    let start = try #require(formatter.date(from: "2026-03-08T00:00:00-05:00"))
    let wait = MidnightRefresh.secondsUntilNextMidnight(now: start, zone: zone)
    #expect(wait == 23 * 60 * 60)
  }

  @Test("MR-05: the zone follows the Date follows setting")
  func midnightZone() {
    #expect(MidnightRefresh.zone(for: .nepal) == CalendarDates.nepal)
    #expect(MidnightRefresh.zone(for: .device) == TimeZone.current)
  }
}
