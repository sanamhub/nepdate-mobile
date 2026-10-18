import CalendarKit
import Foundation
import Testing

/// The Digits setting follows the app language by default (MR-07 AC1), as on Android
/// (`FollowLanguageDigitsTest`).
@Suite("Digits follow the language")
struct FollowLanguageDigitsTests {
  private static let today = MonthDate(year: 2083, month: 6, day: 20)

  private static func makeStore() -> SettingsStore {
    SettingsStore(suiteName: "np.nepdate.tests.digits.\(UUID().uuidString)")
  }

  private static func remove(_ store: SettingsStore) {
    UserDefaults(suiteName: store.suiteName)?.removePersistentDomain(forName: store.suiteName)
  }

  // The BS number of day 25 of Asoj 2083 as the month grid draws it with these settings.
  private static func dayTwentyFive(_ settings: Settings) throws -> String {
    let layout = try #require(
      MonthLayout(year: 2083, month: 6, showTithi: false, today: today)
    )
    let cell = try #require(layout.cells.first { $0.isInMonth && $0.bsDay == 25 })
    return cell.bsNumber(settings.digits.resolved(for: settings.language.resolved))
  }

  @Test("a fresh install in English shows 25")
  func freshEnglish() throws {
    let store = Self.makeStore()
    defer { Self.remove(store) }
    store.setLanguage(.english)

    #expect(try Self.dayTwentyFive(store.load()) == "25")
  }

  @Test("a fresh install in Nepali shows २५")
  func freshNepali() throws {
    let store = Self.makeStore()
    defer { Self.remove(store) }
    store.setLanguage(.nepali)

    #expect(try Self.dayTwentyFive(store.load()) == "२५")
  }

  @Test("switching the language flips the digits and keeps the setting")
  func languageSwitch() throws {
    let store = Self.makeStore()
    defer { Self.remove(store) }

    store.setLanguage(.english)
    #expect(try Self.dayTwentyFive(store.load()) == "25")
    store.setLanguage(.nepali)
    #expect(try Self.dayTwentyFive(store.load()) == "२५")
    store.setLanguage(.english)
    #expect(try Self.dayTwentyFive(store.load()) == "25")
    #expect(store.load().digits == .followLanguage)
  }

  @Test("an explicit choice overrides the language both ways")
  func explicitOverrides() throws {
    let store = Self.makeStore()
    defer { Self.remove(store) }

    store.setLanguage(.english)
    store.setDigits(.nepali)
    #expect(try Self.dayTwentyFive(store.load()) == "२५")

    store.setLanguage(.nepali)
    store.setDigits(.english)
    #expect(try Self.dayTwentyFive(store.load()) == "25")
  }

  @Test("every setting resolves for both languages")
  func resolution() {
    #expect(DigitsSetting.followLanguage.resolved(for: .english) == .english)
    #expect(DigitsSetting.followLanguage.resolved(for: .nepali) == .nepali)
    #expect(DigitsSetting.english.resolved(for: .nepali) == .english)
    #expect(DigitsSetting.nepali.resolved(for: .english) == .nepali)
    #expect(Digits.nepali.format(2083) == "२०८३")
    #expect(Digits.english.format(2083) == "2083")
  }

  @Test("the widget summary uses the stored zone, language and digits")
  func widgetSummary() throws {
    // 2024-07-30T18:30:00Z: BS 2081-04-16 in Nepal, 2081-04-15 in New York.
    let newYork = try #require(TimeZone(identifier: "America/New_York"))
    let provider = TodayProvider(
      now: { Date(timeIntervalSince1970: 1_722_364_200) }, deviceZone: newYork)

    let nepal = TodaySummary.make(
      settings: Settings(language: .nepali, dateFollows: .nepal), provider: provider)
    #expect(nepal.day == "१६")

    let device = TodaySummary.make(
      settings: Settings(language: .nepali, digits: .english), provider: provider)
    #expect(device.day == "15")
  }
}
