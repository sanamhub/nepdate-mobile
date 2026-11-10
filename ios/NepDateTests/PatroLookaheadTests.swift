import CalendarKit
import Testing

/// MR-22: the next-holiday look-ahead behind the today card's last chip.
@Suite("Next holiday")
struct PatroLookaheadTests {
  @Test("MR-22: the next holiday after Asoj 25 is Fulpati in 6 days")
  func nextHoliday() throws {
    let next = try #require(
      PatroLookahead.nextHoliday(after: MonthDate(year: 2083, month: 6, day: 25))
    )
    #expect(next.date == MonthDate(year: 2083, month: 6, day: 31))
    #expect(next.days == 6)
    #expect(next.name?.english.contains("Fulpati") == true)
  }

  @Test("MR-22: the look-ahead starts the day after today")
  func nextHolidayFromAsoj20() throws {
    let next = try #require(
      PatroLookahead.nextHoliday(after: MonthDate(year: 2083, month: 6, day: 20))
    )
    #expect(next.date == MonthDate(year: 2083, month: 6, day: 25))
    #expect(next.days == 5)
    #expect(next.name?.english.contains("Ghatasthapana") == true)
  }

  @Test("MR-22: a window with no holiday finds none, and so does the end of coverage")
  func emptyWindows() {
    #expect(
      PatroLookahead.nextHoliday(
        after: MonthDate(year: 2083, month: 6, day: 25),
        withinDays: 3
      ) == nil
    )
    #expect(
      PatroLookahead.nextHoliday(
        after: MonthDate(year: 2083, month: 6, day: 25),
        withinDays: 0
      ) == nil
    )
    #expect(
      PatroLookahead.nextHoliday(after: MonthDate(year: 2084, month: 8, day: 20)) == nil
    )
  }
}
