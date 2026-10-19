import CalendarKit
import CalendarScreens
import Testing

/// I1-05-01 and MR-03: the Upcoming view model, same cases as A1-05.
@Suite("Upcoming view model")
struct UpcomingViewModelTests {
  private static func model(
    today: MonthDate = MonthDate(year: 2083, month: 6, day: 20),
    language: AppLanguage = .english,
    digits: Digits = .english
  ) throws -> UpcomingViewModel {
    try #require(
      UpcomingViewModel(today: today, language: language, digits: digits)
    )
  }

  @Test("MR-03 AC1: the first 20 entries start with today's two events")
  func firstEntries() throws {
    let list = try Self.model()

    #expect(list.entries.count == 20)
    #expect(list.entries[0].name == "Indira Ekadashi Brata")
    #expect(list.entries[1].name == "Ekadashi Shraddha")
    #expect(list.entries[0].date == MonthDate(year: 2083, month: 6, day: 20))
    #expect(list.entries[0].daysFromToday == 0)
    #expect(list.entries[0].relativeText == "Today")
    #expect(!list.entries[0].isHoliday)
    #expect(list.dataEndsOn == nil)
    #expect(list.dataEndsText == nil)
  }

  @Test("MR-03 AC1: entries are grouped by BS month in date order")
  func groups() throws {
    let list = try Self.model()

    #expect(list.groups.map(\.year) == [2083])
    #expect(list.groups.map(\.month) == [6])
    #expect(list.groups[0].title == "Asoj 2083")
    #expect(list.entries == list.groups.flatMap(\.entries))
    #expect(zip(list.entries, list.entries.dropFirst()).allSatisfy { $0.0.date <= $0.1.date })
  }

  @Test("MR-03 AC1: the public holiday carries the mark")
  func holidayEntry() throws {
    let entry = try #require(
      try Self.model().entries.first {
        $0.date == MonthDate(year: 2083, month: 6, day: 25)
      }
    )

    #expect(entry.isHoliday)
    #expect(entry.name == "Ghatasthapana Brata")
    #expect(entry.daysFromToday == 5)
    #expect(entry.relativeText == "in 5 days")
  }

  @Test("MR-03 AC3: near the end the list ends at coverage with three entries")
  func coverageEnd() throws {
    let list = try Self.model(today: MonthDate(year: 2084, month: 8, day: 10))

    #expect(list.entries.count == 3)
    #expect(
      list.dataEndsOn == MonthDate(year: 2084, month: 8, day: 20)
    )
    #expect(list.dataEndsText == "Holiday data ends on Mangsir 20, 2084")
    #expect(
      list.entries.map(\.name)
        == ["Pradosh Brata", "Udhauli Festival", "Pradosh Brata"]
    )
    #expect(list.entries.map(\.isHoliday) == [false, true, false])
  }

  @Test("MR-03 and MR-07: the relative count follows Digits")
  func digits() throws {
    let entry = try #require(
      try Self.model(digits: .nepali).entries.first {
        $0.date == MonthDate(year: 2083, month: 6, day: 25)
      }
    )

    #expect(entry.relativeText == "in ५ days")
    #expect(entry.bsDate == "Asoj २५, २०८३")
    #expect(entry.adDate == "११ October २०२६")
  }

  @Test("MR-03: a date before coverage starts at the first covered day")
  func beforeCoverage() throws {
    let list = try Self.model(today: MonthDate(year: 2000, month: 1, day: 1))

    #expect(list.entries.count == 20)
    #expect(list.entries.allSatisfy { $0.daysFromToday >= 0 })
  }
}
