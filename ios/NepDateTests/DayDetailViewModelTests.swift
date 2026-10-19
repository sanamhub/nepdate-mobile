import CalendarKit
import Testing

/// I1-04: the day detail strings for the three A1-04 dates (MR-02 AC1, AC3, AC4, AC5 and AC6).
@Suite("Day detail view model")
struct DayDetailViewModelTests {
  private static func model(
    _ date: MonthDate,
    language: AppLanguage = .english,
    digits: Digits = .english
  ) throws -> DayDetailViewModel {
    try #require(
      DayDetailViewModel(date: date, language: language, digits: digits)
    )
  }

  @Test("MR-02 AC1 and AC5: Asoj 9 2083 shows every field and event in file order")
  func asoj9() throws {
    let model = try Self.model(MonthDate(year: 2083, month: 6, day: 9))

    #expect(model.hasData)
    #expect(model.bsDate == "Asoj 9, 2083")
    #expect(model.devanagariDate == "असोज ९, २०८३")
    #expect(model.weekday == "Friday")
    #expect(model.adDate == "25 September 2026")
    #expect(model.weekdayAndDate == "Friday, 25 September 2026")
    #expect(model.tithi == "Chaturdashi")
    #expect(model.isPublicHoliday)
    #expect(
      model.events == [
        "Indra Jatra (Kathmandu Valley Holiday)",
        "Ananta Chaturdashi Brata",
        "World Pharmacist Day",
      ]
    )
    #expect(model.copyText == "Asoj 9, 2083")
    #expect(
      model.calendarTitle(publicHoliday: "Public holiday")
        == "Indra Jatra (Kathmandu Valley Holiday)"
    )
  }

  @Test("MR-02 AC6: Baisakh 1 2084 is a holiday with no tithi")
  func baisakh1() throws {
    let model = try Self.model(MonthDate(year: 2084, month: 1, day: 1))

    #expect(model.hasData)
    #expect(model.bsDate == "Baisakh 1, 2084")
    #expect(model.devanagariDate == "बैशाख १, २०८४")
    #expect(model.weekday == "Wednesday")
    #expect(model.adDate == "14 April 2027")
    #expect(model.weekdayAndDate == "Wednesday, 14 April 2027")
    #expect(model.tithi == nil)
    #expect(model.isPublicHoliday)
    #expect(model.events == ["New Year 2083"])
    #expect(model.copyText == "Baisakh 1, 2084")
    #expect(model.calendarTitle(publicHoliday: "Public holiday") == "New Year 2083")
  }

  @Test("MR-02 AC4: Baisakh 1 2090 is outside coverage and has no fields")
  func outsideCoverage() throws {
    let model = try Self.model(MonthDate(year: 2090, month: 1, day: 1))

    #expect(!model.hasData)
    #expect(model.bsDate == "Baisakh 1, 2090")
    #expect(model.devanagariDate == "बैशाख १, २०९०")
    #expect(model.weekday == "Friday")
    #expect(model.adDate == "15 April 2033")
    #expect(model.weekdayAndDate == "Friday, 15 April 2033")
    #expect(model.tithi == nil)
    #expect(!model.isPublicHoliday)
    #expect(model.events.isEmpty)
    #expect(model.copyText == "Baisakh 1, 2090")
    #expect(model.calendarTitle(publicHoliday: "Public holiday") == "Baisakh 1, 2090")
  }

  @Test("MR-02 AC1 and MR-07: the Devanagari line keeps Devanagari digits")
  func devanagariDigits() throws {
    let date = MonthDate(year: 2083, month: 6, day: 9)
    let englishWithNepaliDigits = try Self.model(date, language: .english, digits: .nepali)
    #expect(englishWithNepaliDigits.bsDate == "Asoj ९, २०८३")
    #expect(englishWithNepaliDigits.devanagariDate == "असोज ९, २०८३")
    #expect(englishWithNepaliDigits.weekday == "Friday")
    #expect(englishWithNepaliDigits.adDate == "२५ September २०२६")
    #expect(englishWithNepaliDigits.copyText == "Asoj ९, २०८३")

    let nepaliWithEnglishDigits = try Self.model(date, language: .nepali, digits: .english)
    #expect(nepaliWithEnglishDigits.bsDate == "असोज 9, 2083")
    #expect(nepaliWithEnglishDigits.devanagariDate == "असोज ९, २०८३")
    #expect(nepaliWithEnglishDigits.weekday == "शुक्रवार")
    #expect(nepaliWithEnglishDigits.adDate == "25 सेप्टेम्बर 2026")
    #expect(nepaliWithEnglishDigits.tithi == "चतुर्दशी")
    #expect(nepaliWithEnglishDigits.copyText == "असोज 9, 2083")
  }

  @Test("MR-02 AC3: Copy date uses the language and every Digits value")
  func copyTextForDigits() throws {
    let date = MonthDate(year: 2083, month: 6, day: 9)

    let englishEnglish = try Self.model(date, language: .english, digits: .english)
    #expect(englishEnglish.copyText == "Asoj 9, 2083")

    let englishNepali = try Self.model(date, language: .english, digits: .nepali)
    #expect(englishNepali.copyText == "Asoj ९, २०८३")

    let nepaliEnglish = try Self.model(date, language: .nepali, digits: .english)
    #expect(nepaliEnglish.copyText == "असोज 9, 2083")

    let nepaliNepali = try Self.model(date, language: .nepali, digits: .nepali)
    #expect(nepaliNepali.copyText == "असोज ९, २०८३")
  }

  @Test("MR-02 AC1: an event name's digits follow the setting, the scope stays whole")
  func eventScopeAndDigits() throws {
    let model = try Self.model(
      MonthDate(year: 2084, month: 1, day: 1),
      language: .english,
      digits: .nepali
    )

    #expect(model.events == ["New Year २०८३"])
    #expect(model.calendarTitle(publicHoliday: "Public holiday") == "New Year २०८३")
  }

  @Test("MR-02 AC2: the event title falls back to the holiday and then the date")
  func calendarTitleFallbacks() throws {
    let holidayOnly = try #require(
      DayDetailViewModel(
        date: MonthDate(year: 2083, month: 6, day: 9),
        language: .english,
        digits: .english
      )
    )
    #expect(
      holidayOnly.calendarTitle(publicHoliday: "सार्वजनिक बिदा")
        == "Indra Jatra (Kathmandu Valley Holiday)"
    )

    let outside = try Self.model(MonthDate(year: 2090, month: 1, day: 1))
    #expect(outside.calendarTitle(publicHoliday: "Public holiday") == "Baisakh 1, 2090")
  }

  @Test("I1-04: a date outside the library's range gives no view model")
  func invalidDate() {
    #expect(
      DayDetailViewModel(
        date: MonthDate(year: 1800, month: 1, day: 1),
        language: .english,
        digits: .english
      ) == nil
    )
  }
}
