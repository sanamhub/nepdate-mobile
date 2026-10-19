import CalendarKit
import CalendarScreens
import EventKit
import Foundation
import Testing

/// I1-04: the event handed to `EKEventEditViewController` (MR-02 AC2). The store is only a
/// container here; the test never requests calendar access and the event belongs to no calendar.
@MainActor
@Suite("Day event")
struct DayEventTests {
  private static let gregorian: Calendar = {
    var calendar = Calendar(identifier: .gregorian)
    calendar.timeZone = .current
    return calendar
  }()

  @Test("MR-02 AC2: Asoj 9 is an all-day holiday event at AD midnight with no alarm")
  func asoj9() throws {
    let model = try #require(
      DayDetailViewModel(
        date: MonthDate(year: 2083, month: 6, day: 9),
        language: .english,
        digits: .english
      )
    )
    let calendar = Self.gregorian
    let event = try #require(
      DayEvent.event(
        for: model,
        publicHoliday: "Public holiday",
        store: EKEventStore()
      )
    )

    #expect(event.isAllDay)
    #expect(event.title == "Indra Jatra (Kathmandu Valley Holiday)")
    #expect((event.alarms ?? []).isEmpty)
    #expect(event.calendar == nil)

    let start = try #require(event.startDate)
    let parts = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: start)
    #expect(parts.year == 2026)
    #expect(parts.month == 9)
    #expect(parts.day == 25)
    #expect(parts.hour == 0)
    #expect(parts.minute == 0)
    // EventKit ends a one-day all-day event at 23:59:59 of the same day.
    let end = try #require(event.endDate)
    #expect(calendar.isDate(end, inSameDayAs: start))
  }

  @Test("MR-02 AC2: the start is Gregorian midnight in any time zone, whatever the user calendar")
  func gregorianInEveryZone() throws {
    for identifier in ["Asia/Kathmandu", "Pacific/Kiritimati", "America/Los_Angeles"] {
      let zone = try #require(TimeZone(identifier: identifier))
      let start = try #require(DayEvent.startOfDay(year: 2026, month: 9, day: 25, timeZone: zone))
      var calendar = Calendar(identifier: .gregorian)
      calendar.timeZone = zone
      let parts = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: start)
      #expect(parts.year == 2026, "\(identifier)")
      #expect(parts.month == 9, "\(identifier)")
      #expect(parts.day == 25, "\(identifier)")
      #expect(parts.hour == 0, "\(identifier)")
      #expect(parts.minute == 0, "\(identifier)")

      // A Buddhist calendar reads the same instant as BE 2569; built with that calendar, 2026
      // would have been BE 2026, five centuries early.
      var buddhist = Calendar(identifier: .buddhist)
      buddhist.timeZone = zone
      #expect(buddhist.component(.year, from: start) == 2569, "\(identifier)")
    }
  }

  @Test("MR-02 AC6: Baisakh 1 2084 uses its event and starts on 14 April 2027")
  func baisakh1() throws {
    let model = try #require(
      DayDetailViewModel(
        date: MonthDate(year: 2084, month: 1, day: 1),
        language: .english,
        digits: .english
      )
    )
    let calendar = Self.gregorian
    let event = try #require(
      DayEvent.event(
        for: model,
        publicHoliday: "Public holiday",
        store: EKEventStore()
      )
    )

    #expect(event.isAllDay)
    // The hub data names 2084's new year "New Year 2083" (hub known-questions.json,
    // new-year-name); this fails, as it should, once the corrected data is synced.
    #expect(event.title == "New Year 2083")
    #expect((event.alarms ?? []).isEmpty)
    let start = try #require(event.startDate)
    let parts = calendar.dateComponents([.year, .month, .day], from: start)
    #expect(parts.year == 2027)
    #expect(parts.month == 4)
    #expect(parts.day == 14)
  }

  @Test("MR-02 AC4: a day outside coverage uses its BS date as the title")
  func outsideCoverage() throws {
    let model = try #require(
      DayDetailViewModel(
        date: MonthDate(year: 2090, month: 1, day: 1),
        language: .english,
        digits: .english
      )
    )
    let event = try #require(
      DayEvent.event(
        for: model,
        publicHoliday: "Public holiday",
        store: EKEventStore()
      )
    )

    #expect(event.title == "Baisakh 1, 2090")
    #expect(event.isAllDay)
    #expect((event.alarms ?? []).isEmpty)
  }
}
