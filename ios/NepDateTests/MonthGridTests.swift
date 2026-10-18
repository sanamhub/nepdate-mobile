import CalendarKit
import Foundation
import Testing

@Suite("Month grid")
struct MonthGridTests {
  @Test("M0-03: the grid has a cell for every day and whole weeks")
  func layout() throws {
    let grid = try #require(MonthGrid(year: 2083, month: 6, language: .english))
    #expect(grid.dayCount >= 29 && grid.dayCount <= 32)
    #expect(grid.cells.count % 7 == 0)
    #expect(grid.cells.compactMap { $0 }.count == grid.dayCount)
    #expect(grid.cells.compactMap { $0 }.first?.day == 1)
    #expect(grid.leadingBlanks >= 0 && grid.leadingBlanks <= 6)
    #expect(grid.cells.prefix(grid.leadingBlanks).allSatisfy { $0 == nil })
  }

  @Test("M0-03: the title and the weekday row follow the language")
  func language() throws {
    let nepali = try #require(MonthGrid(year: 2083, month: 6, language: .nepali))
    #expect(nepali.title.contains("असोज"))
    #expect(nepali.title.contains("२०८३"))
    #expect(nepali.weekdayNames.first == "आइत")
    #expect(nepali.weekdayNames.last == "शनि")

    let english = try #require(MonthGrid(year: 2083, month: 6, language: .english))
    #expect(english.title.contains("Ashoj"))
    #expect(english.title.contains("2083"))
    #expect(english.weekdayNames.first == "Sun")
  }

  @Test("M0-03: a public holiday is marked in the grid")
  func holiday() throws {
    // 2081-01-01 is New Year, a public holiday (nepdate-swift PatroTests anchors).
    let grid = try #require(MonthGrid(year: 2081, month: 1, language: .english))
    let first = try #require(grid.cells.compactMap { $0 }.first)
    #expect(first.day == 1)
    #expect(first.isHoliday)
  }

  @Test("M0-03: today is the only ringed cell")
  func today() throws {
    let grid = try #require(
      MonthGrid(year: 2083, month: 6, language: .english, today: (2083, 6, 18))
    )
    #expect(grid.cells.compactMap { $0 }.filter { $0.isToday }.map { $0.day } == [18])
  }

  @Test("M0-03: a month outside the library's range is refused")
  func outOfRange() {
    #expect(MonthGrid(year: 2083, month: 13, language: .english) == nil)
    #expect(MonthGrid(year: 1800, month: 1, language: .english) == nil)
  }

  @Test("M0-03: the display language follows the first preferred language")
  func preferredLanguage() {
    #expect(AppLanguage.preferred(from: ["ne-NP", "en"]) == .nepali)
    #expect(AppLanguage.preferred(from: ["en-US"]) == .english)
    #expect(AppLanguage.preferred(from: []) == .english)
  }

  @Test("M0-03: both day numbers follow the language's digits")
  func cellDigits() throws {
    // Ashoj 2083 starts on 17 September 2026.
    let nepali = try #require(MonthGrid(year: 2083, month: 6, language: .nepali))
    let first = try #require(nepali.cells.compactMap { $0 }.first)
    #expect(first.number(.nepali) == "१")
    #expect(first.adNumber(.nepali) == "१७")
    #expect(first.number(.english) == "1")
    #expect(first.adNumber(.english) == "17")
  }

  @Test("M0-03: the app ships Nepali strings and reads its language from the bundle")
  func bundleLanguage() {
    #expect(Bundle.main.localizations.contains("ne"))
    #expect(AppLanguage.current == AppLanguage.preferred(from: Bundle.main.preferredLocalizations))
  }

  @Test("M0-03: today's summary uses the library's names and digits")
  func summary() throws {
    let nepali = try #require(TodaySummary(year: 2081, month: 1, day: 1, language: .nepali))
    #expect(nepali.day == "१")
    #expect(nepali.isHoliday)
    #expect(!nepali.month.isEmpty)
    #expect(!nepali.weekday.isEmpty)

    let english = try #require(TodaySummary(year: 2081, month: 1, day: 1, language: .english))
    #expect(english.day == "1")
    #expect(english.month == "Baishakh")
  }
}
