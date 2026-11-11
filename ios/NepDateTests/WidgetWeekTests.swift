import CalendarKit
import Testing

/// W-10: the this-week widget starts on Sunday, marks today and the holiday, and names the next
/// day in the week that has a holiday or event.
struct WidgetWeekTests {
  private static let today = MonthDate(year: 2083, month: 6, day: 20)

  @Test("the week starts on Sunday and marks today and the holiday")
  func week() throws {
    let week = try #require(
      WidgetWeekBuilder.week(
        now: try WidgetTestData.noon(Self.today),
        settings: WidgetTestData.settings(),
        deviceZone: WidgetTestData.zone
      )
    )

    #expect(week.days.count == 7)
    #expect(week.days[0].date == MonthDate(year: 2083, month: 6, day: 18))
    #expect(week.days[2].isToday)
    #expect(week.days[2].day == "20")
    #expect(week.days[6].date == MonthDate(year: 2083, month: 6, day: 24))
  }

  @Test("the agenda names the next holiday in the week")
  func agenda() throws {
    let week = try #require(
      WidgetWeekBuilder.week(
        now: try WidgetTestData.noon(Self.today),
        settings: WidgetTestData.settings(),
        deviceZone: WidgetTestData.zone
      )
    )
    let next = try #require(week.nextEvent)

    #expect(next.date == MonthDate(year: 2083, month: 6, day: 21))
    #expect(next.daysFromToday == 1)
    #expect(!next.isHoliday)
    #expect(next.name == "Dwadashi Shraddha")
  }
}
