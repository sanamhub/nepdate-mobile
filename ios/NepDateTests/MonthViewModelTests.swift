import CalendarKit
import CalendarScreens
import Testing

/// I1-03: the month view model. The screen's rules live here so they run without a simulator.
@Suite("Month view model")
struct MonthViewModelTests {
  private static let today = MonthDate(year: 2083, month: 6, day: 20)

  private static func model(
    language: AppLanguage = .english,
    digits: Digits = .english,
    showTithi: Bool = false,
    month: MonthKey? = nil
  ) throws -> MonthViewModel {
    try #require(
      MonthViewModel(
        today: today,
        language: language,
        digits: digits,
        showTithi: showTithi,
        month: month
      )
    )
  }

  @Test("MR-01 AC5: next and previous change the month")
  func nextAndPrevious() throws {
    var model = try Self.model()
    #expect(model.month.year == 2083)
    #expect(model.month.month == 6)
    #expect(!model.showsTodayButton)

    model.goToNext()
    #expect(model.month.year == 2083)
    #expect(model.month.month == 7)
    #expect(model.showsTodayButton)
    #expect(model.monthTitle == "Kartik 2083")

    model.goToPrevious()
    #expect(model.month.month == 6)
    #expect(!model.showsTodayButton)
    #expect(model.monthTitle == "Asoj 2083")
  }

  @Test("MR-01 AC5: Today returns to today's month and hides itself")
  func todayButton() throws {
    var model = try Self.model(month: MonthKey(year: 2084, month: 1))
    #expect(model.showsTodayButton)

    model.goToToday()
    #expect(model.month.year == 2083)
    #expect(model.month.month == 6)
    #expect(!model.showsTodayButton)
  }

  @Test("MR-01 AC5 and AC6: the two ends of the range stop paging")
  func bounds() throws {
    var first = try Self.model(month: MonthKey(year: 1901, month: 1))
    #expect(!first.canGoPrevious)
    #expect(first.canGoNext)
    first.goToPrevious()
    #expect(first.month.year == 1901)
    #expect(first.month.month == 1)

    var last = try Self.model(month: MonthKey(year: 2199, month: 12))
    #expect(last.canGoPrevious)
    #expect(!last.canGoNext)
    last.goToNext()
    #expect(last.month.year == 2199)
    #expect(last.month.month == 12)
  }

  @Test("MR-01 AC6: the picker covers BS 1901 to 2199 without gaps")
  func pickerRange() throws {
    let model = try Self.model()
    #expect(model.pickerYears == 1901...2199)
    #expect(model.weekdayNames.first == "Sun")
    #expect(model.dayCells.count == model.layout.dayCount)
    #expect(MonthKey.count == 3588)
    #expect(MonthKey.all.count == 3588)
    #expect(MonthKey.all.first?.year == 1901)
    #expect(MonthKey.all.first?.month == 1)
    #expect(MonthKey.all.last?.year == 2199)
    #expect(MonthKey.all.last?.month == 12)

    #expect(MonthKey.all.allSatisfy { MonthKey(id: $0.id) == $0 })

    #expect(MonthKey(year: 1900, month: 1) == nil)
    #expect(MonthKey(year: 2200, month: 1) == nil)
    #expect(MonthKey(year: 2083, month: 13) == nil)
    #expect(MonthKey(id: -1) == nil)
    #expect(MonthKey(id: 3588) == nil)
    #expect(MonthKey(monthOf: MonthDate(year: 1800, month: 1, day: 1)) == nil)
  }

  @Test("MR-01 AC7: a dimmed neighbouring cell cannot be selected")
  func dimmedCells() throws {
    var model = try Self.model()
    let dimmed = try #require(model.layout.cells.first { !$0.isInMonth && $0.date != nil })
    #expect(!model.isSelectable(dimmed))
    model.select(dimmed)
    #expect(model.selected == nil)

    let day = try #require(model.layout.cells.first { $0.isInMonth && $0.bsDay == 25 })
    #expect(model.isSelectable(day))
    model.select(day)
    #expect(model.selected == MonthDate(year: 2083, month: 6, day: 25))

    model.select(MonthDate(year: 2083, month: 6, day: 26))
    #expect(model.selected == MonthDate(year: 2083, month: 6, day: 26))
    model.select(MonthDate(year: 2083, month: 7, day: 1))
    #expect(model.selected == MonthDate(year: 2083, month: 6, day: 26))

    #expect(
      model.accessibilityLabel(
        for: MonthDate(year: 2083, month: 7, day: 1),
        publicHoliday: "public holiday"
      ) == ""
    )
  }

  @Test("I1-03: a view model needs a date inside the library's range")
  func invalidToday() {
    #expect(
      MonthViewModel(
        today: MonthDate(year: 1800, month: 1, day: 1),
        language: .english,
        digits: .english,
        showTithi: false
      ) == nil
    )
  }

  @Test("MR-10 AC1: Asoj 25 2083 has the requirement's label")
  func asoj25Label() throws {
    let model = try Self.model()
    let label = model.accessibilityLabel(
      for: MonthDate(year: 2083, month: 6, day: 25),
      publicHoliday: "public holiday"
    )

    #expect(
      label == "Asoj 25, 2083, Sunday, 11 October 2026, public holiday, Ghatasthapana Brata"
    )
  }

  @Test("MR-10 AC1: a neighbouring day speaks only its date")
  func neighbourLabel() throws {
    let model = try Self.model()
    let neighbour = try #require(
      model.layout.cells.first { !$0.isInMonth && $0.date != nil }
    )
    let date = try #require(neighbour.date)

    #expect(
      model.accessibilityLabel(for: date, publicHoliday: "public holiday")
        == "Bhadra 28, 2083, Sunday, 13 September 2026"
    )
  }

  @Test("MR-07 AC1: digits follow the setting, not the language")
  func digitsAreResolvedNotLanguage() throws {
    let model = try Self.model(language: .english, digits: .nepali)
    #expect(model.monthTitle == "Asoj २०८३")
    #expect(model.adSpan == "Sep – Oct २०२६")

    let label = model.accessibilityLabel(
      for: MonthDate(year: 2083, month: 6, day: 25),
      publicHoliday: "public holiday"
    )
    #expect(label.hasPrefix("Asoj २५, २०८३, Sunday, ११ October २०२६"))
    #expect(label.hasSuffix("Ghatasthapana Brata"))

    let nepali = try Self.model(language: .nepali, digits: .english)
    #expect(nepali.monthTitle == "असोज 2083")
    #expect(nepali.adSpan == "सेप्टेम्बर – अक्टोबर 2026")
    #expect(
      nepali.accessibilityLabel(
        for: MonthDate(year: 2083, month: 6, day: 25),
        publicHoliday: "सार्वजनिक बिदा"
      ).hasPrefix("असोज 25, 2083, आइतवार, 11 अक्टोबर 2026")
    )
  }

  @Test("MR-01 AC2: the header's AD span follows the language and digits")
  func adSpan() throws {
    let english = try Self.model()
    #expect(english.adSpan == "Sep – Oct 2026")

    let nepali = try Self.model(language: .nepali, digits: .nepali)
    #expect(nepali.adSpan == "सेप्टेम्बर – अक्टोबर २०२६")

    let crossYear = try Self.model(month: MonthKey(year: 2083, month: 9))
    #expect(crossYear.adSpan == "Dec 2026 – Jan 2027")
  }

  @Test("I1-03: the product month names replace the library's spellings")
  func monthNames() {
    #expect(MonthNames.bs(1, .english) == "Baisakh")
    #expect(MonthNames.bs(3, .english) == "Asar")
    #expect(MonthNames.bs(6, .english) == "Asoj")
    #expect(MonthNames.bs(6, .nepali) == "असोज")
    #expect(MonthNames.bs(0, .english) == "")
    #expect(MonthNames.bs(13, .nepali) == "")

    #expect(MonthNames.ad(10, .english, short: true) == "Oct")
    #expect(MonthNames.ad(10, .english) == "October")
    #expect(MonthNames.ad(10, .nepali) == "अक्टोबर")
    #expect(MonthNames.ad(13, .english) == "")
  }

  @Test("MR-07 AC1: digits inside names are rewritten too")
  func digitsInNames() {
    #expect(Digits.nepali.applying(to: "Event 2064") == "Event २०६४")
    #expect(Digits.english.applying(to: "घट २०६४") == "घट 2064")
    #expect(Digits.english.applying(to: "Ghatasthapana") == "Ghatasthapana")
  }
}
