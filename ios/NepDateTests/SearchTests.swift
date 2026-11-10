import CalendarKit
import CalendarScreens
import Foundation
import Testing

/// MR-13: the typed date jump, the recent searches and the two list models.
@Suite("Search")
struct SearchTests {
  @Test("MR-13: a date both calendars can read gives two readings, BS first")
  func bothCalendars() throws {
    let jump = DateJump("2083-06-25")
    #expect(jump.readings.count == 2)
    #expect(jump.bs?.date == MonthDate(year: 2083, month: 6, day: 25))
    #expect(jump.ad != nil)
    #expect(jump.preferred?.kind == .bs)
  }

  @Test("MR-13: 2150 is a BS year and not an AD one")
  func bsOnly() {
    let jump = DateJump("2150-01-01")
    #expect(jump.bs?.date == MonthDate(year: 2150, month: 1, day: 1))
    #expect(jump.ad == nil)
    #expect(jump.readings.count == 1)
  }

  @Test("MR-13: 1900 is an AD year and not a BS one")
  func adOnly() {
    let jump = DateJump("1900-01-01")
    #expect(jump.bs == nil)
    #expect(jump.ad != nil)
    #expect(jump.readings.count == 1)
  }

  @Test("MR-13: Devanagari digits read the same date")
  func devanagari() {
    let jump = DateJump("२०८३-०६-२५")
    #expect(jump.bs?.date == MonthDate(year: 2083, month: 6, day: 25))
  }

  @Test("MR-13: eight bare digits read as YYYYMMDD")
  func bareDigits() {
    let jump = DateJump("20830625")
    #expect(jump.bs?.date == MonthDate(year: 2083, month: 6, day: 25))
  }

  @Test(
    "MR-13: the three separators all read", arguments: ["2083-06-25", "2083/06/25", "2083.06.25"])
  func separators(_ text: String) {
    let jump = DateJump(text)
    #expect(jump.bs?.date == MonthDate(year: 2083, month: 6, day: 25))
  }

  @Test("MR-13: text that is not a date, or a day that does not exist, has no reading")
  func unreadable() {
    #expect(DateJump("hello").isUnreadable)
    #expect(DateJump("2083-06-32").isUnreadable)
    #expect(DateJump("2083-13-01").isUnreadable)
    #expect(DateJump("").isUnreadable)
  }

  @Test("MR-13: the recent searches keep five, newest first, without duplicates")
  func recentSearches() {
    let suite = "np.nepdate.tests.recent.\(UUID().uuidString)"
    defer { UserDefaults(suiteName: suite)?.removePersistentDomain(forName: suite) }
    let store = RecentSearchStore(suiteName: suite)

    for value in ["one", "two", "three", "two", "four", "five", "six"] {
      store.add(value)
    }
    #expect(store.load() == ["six", "five", "four", "two", "three"])
    store.remove("four")
    #expect(store.load() == ["six", "five", "two", "three"])
    store.clear()
    #expect(store.load().isEmpty)
  }

  @Test("MR-13: the festivals list starts today and ends at the coverage end")
  func festivalsList() throws {
    let model = FestivalsViewModel(
      today: MonthDate(year: 2083, month: 6, day: 25),
      language: .english,
      digits: .english
    )
    #expect(!model.isEmpty)
    #expect(model.groups.first?.year == 2083)
    #expect(model.groups.first?.month == 6)
    #expect(model.nextHoliday != nil)
    #expect(model.dataEndsOn.contains("2084"))

    let holidaysOnly = FestivalsViewModel(
      today: MonthDate(year: 2083, month: 6, day: 25),
      language: .english,
      digits: .english,
      holidaysOnly: true
    )
    #expect(holidaysOnly.groups.allSatisfy { group in group.days.allSatisfy(\.isHoliday) })
  }

  @Test("MR-13: a name query matches English and Nepali across the coverage")
  func nameQuery() throws {
    let english = SearchViewModel(
      today: MonthDate(year: 2083, month: 6, day: 25),
      language: .english,
      digits: .english,
      query: "Ghatasthapana"
    )
    #expect(!english.groups.isEmpty)
    #expect(english.resultCount > 0)

    let day = try #require(FestivalDirectory.all.first { !$0.names.isEmpty })
    let needle = String(try #require(day.names.first).nepali.prefix(4))
    let nepali = SearchViewModel(
      today: MonthDate(year: 2083, month: 6, day: 25),
      language: .nepali,
      digits: .nepali,
      query: needle
    )
    #expect(!nepali.groups.isEmpty)
  }

  @Test("MR-13: the landing state lists the next three festivals and no results")
  func landing() {
    let model = SearchViewModel(
      today: MonthDate(year: 2083, month: 6, day: 25),
      language: .english,
      digits: .english,
      query: "zzzz"
    )
    #expect(model.showsEmptyState)
    #expect(model.groups.isEmpty)
    #expect(model.nextFestivals.count == 3)
  }
}
