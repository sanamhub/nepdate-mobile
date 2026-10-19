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
  @Test("MR-02 AC2: Asoj 9 is an all-day holiday event at AD midnight with no alarm")
  func asoj9() throws {
    let model = try #require(
      DayDetailViewModel(
        date: MonthDate(year: 2083, month: 6, day: 9),
        language: .english,
        digits: .english
      )
    )
    let calendar = Calendar.current
    let event = try #require(
      DayEvent.event(
        for: model,
        publicHoliday: "Public holiday",
        store: EKEventStore(),
        calendar: calendar
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
    let calendar = Calendar.current
    let event = try #require(
      DayEvent.event(
        for: model,
        publicHoliday: "Public holiday",
        store: EKEventStore(),
        calendar: calendar
      )
    )

    #expect(event.isAllDay)
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
