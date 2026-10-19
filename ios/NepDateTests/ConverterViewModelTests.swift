import CalendarKit
import CalendarScreens
import Testing

/// I1-05-01 and MR-04: the Converter view model, same cases as A1-05.
@Suite("Converter view model")
struct ConverterViewModelTests {
  private static let today = MonthDate(year: 2083, month: 6, day: 20)

  private static func model(
    language: AppLanguage = .english,
    digits: Digits = .english
  ) -> ConverterViewModel {
    ConverterViewModel(today: today, language: language, digits: digits)
  }

  @Test("MR-04 AC1 and AC2: Nepali BS input converts to AD")
  func nepaliInput() {
    var model = Self.model()
    model.fromBs("२०८३ असोज २०")

    #expect(model.result?.bs == Self.today)
    #expect(model.result?.adYear == 2026)
    #expect(model.result?.adMonth == 10)
    #expect(model.result?.adDay == 6)
    #expect(model.result?.daysFromToday == 0)
    #expect(model.error == nil)
    #expect(model.resultBsDate == "Asoj 20, 2083")
    #expect(model.resultWeekdayAndDate == "Tuesday, 6 October 2026")
    #expect(model.daysFromTodayText == "Today")
  }

  @Test("MR-04 AC1 and AC2: numeric BS input converts to AD")
  func numericInput() {
    var model = Self.model()
    model.fromBs("2083-06-20")

    #expect(model.result?.adDay == 6)
    #expect(model.result?.daysFromToday == 0)
    #expect(model.error == nil)
  }

  @Test("MR-04 AC1 and AC2: an AD date converts to BS")
  func adInput() {
    var model = Self.model()
    model.fromAd(year: 2026, month: 10, day: 6)

    #expect(model.result?.bs == Self.today)
    #expect(model.result?.daysFromToday == 0)
    #expect(model.error == nil)
  }

  @Test("MR-04 AC4: an invalid day keeps the previous result")
  func invalidDay() {
    var model = Self.model()
    model.fromBs("2083-06-20")
    let first = model.result

    model.fromBs("2083-06-32")

    #expect(model.result == first)
    #expect(model.error == .invalidDay(year: 2083, month: 6, monthLength: 31))
    #expect(model.errorText == "Asoj has 31 days in 2083")
  }

  @Test("MR-04 AC4: an invalid month is reported")
  func invalidMonth() {
    var model = Self.model()
    model.fromBs("2083-13-01")

    #expect(model.error == .invalidMonth)
    #expect(model.errorText == "Month must be 1 to 12")
    #expect(model.result == nil)
  }

  @Test("MR-04 AC3: a second date gives years, months and days")
  func comparison() {
    var model = Self.model()
    model.fromBs("2081-01-01")
    model.compareWithBs("2082-01-01")

    #expect(model.comparison?.years == 1)
    #expect(model.comparison?.months == 0)
    #expect(model.comparison?.days == 0)
    #expect(model.comparisonError == nil)
    #expect(model.comparisonRange == "Baisakh 1, 2081 to Baisakh 1, 2082")
    #expect(model.differenceText == "1 year, 0 months, 0 days")
  }

  @Test("MR-04 AC3: comparing without a first date is an error")
  func noFirstDate() {
    var model = Self.model()
    model.compareWithBs("2082-01-01")

    #expect(model.comparisonError == .noFirstDate)
    #expect(model.comparisonErrorText == "Convert a date before comparing")
  }

  @Test("MR-04 and MR-07: counts use the resolved Digits in Nepali")
  func nepaliDigits() {
    var model = Self.model(language: .nepali, digits: .nepali)
    model.fromBs("२०८३-०६-२५")

    #expect(model.resultBsDate == "असोज २५, २०८३")
    #expect(model.resultWeekdayAndDate == "शुक्रवार, ११ अक्टोबर २०२६")
    #expect(model.daysFromTodayText == "५ दिन पछि")

    var invalid = model
    invalid.fromBs("२०८३-०६-३२")
    #expect(invalid.errorText == "असोज २०८३ मा ३१ दिन छ")

    var compared = Self.model(language: .nepali, digits: .nepali)
    compared.fromBs("२०८१-०१-०१")
    compared.compareWithBs("२०८२-०१-०१")
    #expect(compared.differenceText == "१ वर्ष, ० महिना, ० दिन")
  }

  @Test("MR-04 AC4: unreadable text is reported and keeps the result")
  func unreadable() {
    var model = Self.model()
    model.fromBs("not a date")

    #expect(model.error == .unreadable)
    #expect(model.errorText == "Enter a BS date like 2083-06-20")
    #expect(model.result == nil)
  }
}
