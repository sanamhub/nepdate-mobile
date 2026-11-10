import CalendarKit
import CalendarScreens
import Foundation
import Testing

/// MR-14: the year view's months, holiday counts, limits and language.
@Suite("Year view")
struct YearViewModelTests {
  private static let today = MonthDate(year: 2083, month: 6, day: 25)

  private static func model(
    year: Int = 2083,
    language: AppLanguage = .english,
    digits: Digits = .english
  ) throws -> YearViewModel {
    try #require(
      YearViewModel(year: year, today: today, language: language, digits: digits)
    )
  }

  @Test("MR-14: a year has twelve months, Baisakh first")
  func months() throws {
    let model = try Self.model()
    #expect(model.months.count == 12)
    #expect(model.months.first?.name == "Baisakh")
    #expect(model.months.last?.name == "Chaitra")
    #expect(model.months.first?.key == MonthKey(year: 2083, month: 1))
    #expect(model.title == "2083")
  }

  @Test("MR-14: each month carries the holiday count the month layout has")
  func holidayCounts() throws {
    let model = try Self.model()
    let layout = try #require(
      MonthLayout(year: 2083, month: 6, showTithi: false, today: Self.today)
    )
    let asoj = try #require(model.months.first { $0.name == "Asoj" })
    #expect(asoj.holidayCount == layout.holidayDays.count)
    #expect(asoj.holidayCount > 0)
    #expect(asoj.spokenLabel.contains("Asoj 2083"))
    #expect(asoj.spokenLabel.contains("public holiday"))
  }

  @Test("MR-14: the year buttons stop at BS 1901 and 2199")
  func limits() throws {
    let first = try Self.model(year: 1901)
    #expect(first.canGoPrevious == false)
    #expect(first.canGoNext)
    #expect(first.previous == nil)
    #expect(first.next?.year == 1902)

    let last = try Self.model(year: 2199)
    #expect(last.canGoNext == false)
    #expect(last.canGoPrevious)
    #expect(last.next == nil)
    #expect(last.previous?.year == 2198)
  }

  @Test("MR-14: another year keeps the language and the digits")
  func languageKept() throws {
    let model = try Self.model(language: .nepali, digits: .nepali)
    let previous = try #require(model.previous)
    #expect(previous.year == 2082)
    #expect(previous.language == .nepali)
    #expect(previous.digits == .nepali)
    #expect(previous.title == "२०८२")
  }

  @Test("MR-14: a year outside 1901 to 2199 does not exist")
  func outOfRange() {
    #expect(
      YearViewModel(year: 1900, today: Self.today, language: .english, digits: .english) == nil
    )
    #expect(
      YearViewModel(year: 2200, today: Self.today, language: .english, digits: .english) == nil
    )
  }
}
