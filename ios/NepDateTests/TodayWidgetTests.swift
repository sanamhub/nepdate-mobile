import CalendarKit
import CalendarScreens
import CalendarUI
import Testing

/// W-01 and MR-22: the Today widget's main-calendar face and the today card's chip rule.
struct TodayWidgetTests {
  private static let day = MonthDate(year: 2083, month: 6, day: 25)

  @Test("W-01: the intent's Gregorian face leads with the AD day and keeps the BS date")
  func gregorianFace() throws {
    let entry = try WidgetTestData.entry(Self.day)
    let ad = TodayWidgetPresentation(entry: entry, mainCalendar: .ad)
    let bs = TodayWidgetPresentation(entry: entry, mainCalendar: .bs)

    #expect(ad.heroDay == "11")
    #expect(ad.heroMonth == "October")
    #expect(ad.otherCalendarLine == "Asoj 25")

    #expect(bs.heroDay == "25")
    #expect(bs.heroMonth == "Asoj")
    #expect(bs.otherCalendarLine == "11 October 2026")
  }

  @Test("W-07: the festivals list keeps only public holidays when the filter is on")
  func holidaysOnly() {
    let all = FestivalDirectory.next(after: Self.day, limit: 24)
    let holidays = FestivalDirectory.next(after: Self.day, limit: 24, holidaysOnly: true)

    #expect(!holidays.isEmpty)
    #expect(holidays.allSatisfy { $0.isHoliday })
    #expect(holidays.count < all.count)
    #expect(holidays.allSatisfy { all.contains($0) })
  }

  @Test("MR-22: a quiet day keeps its events and the next holiday")
  func quietDay() {
    let plan = TodayCardPlan(eventCount: 2, hasNextHoliday: true)

    #expect(plan.eventIndices == [0, 1])
    #expect(plan.moreCount == 0)
    #expect(plan.showsNextHoliday)
  }

  @Test("MR-22: three events fill the row and hide the next holiday")
  func threeEvents() {
    let plan = TodayCardPlan(eventCount: 3, hasNextHoliday: true)

    #expect(plan.eventIndices == [0, 1, 2])
    #expect(plan.moreCount == 0)
    #expect(!plan.showsNextHoliday)
  }

  @Test("MR-22: more than three events show two and a +N more chip")
  func moreThanThree() {
    let plan = TodayCardPlan(eventCount: 5, hasNextHoliday: true)

    #expect(plan.eventIndices == [0, 1])
    #expect(plan.moreCount == 3)
    #expect(!plan.showsNextHoliday)
  }

  @Test("MR-22: a day without events still points at the next holiday")
  func noEvents() {
    let plan = TodayCardPlan(eventCount: 0, hasNextHoliday: true)

    #expect(plan.eventIndices.isEmpty)
    #expect(plan.moreCount == 0)
    #expect(plan.showsNextHoliday)
  }
}
