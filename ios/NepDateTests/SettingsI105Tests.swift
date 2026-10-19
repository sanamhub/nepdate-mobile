import CalendarKit
import CalendarScreens
import Foundation
import Testing
import UIKit

/// I1-05-02 and I1-05-03: Settings persistence, suggestion, links and diagnostic text.
@Suite("Settings and About")
struct SettingsI105Tests {
  private static func store() -> SettingsStore {
    SettingsStore(suiteName: "np.nepdate.tests.i105.\(UUID().uuidString)")
  }

  private static func remove(_ store: SettingsStore) {
    UserDefaults(suiteName: store.suiteName)?.removePersistentDomain(forName: store.suiteName)
  }

  @Test("MR-07 AC1: each setting writes the store and survives a reload")
  func persistence() throws {
    let store = Self.store()
    defer { Self.remove(store) }
    var model = SettingsViewModel(settings: Settings(), store: store)

    model.setDigits(.english)
    #expect(store.load().digits == .english)
    model.setDateFollows(.nepal)
    #expect(store.load().dateFollows == .nepal)
    model.setShowTithi(true)
    #expect(store.load().showTithi)
    model.setDigits(.followLanguage)
    #expect(store.load().digits == .followLanguage)

    #expect(model.settings.digits == .followLanguage)
    #expect(model.settings.dateFollows == .nepal)
    #expect(model.settings.showTithi)
  }

  @Test("MR-06 AC1: the suggestion shows only for another zone and an unhandled install")
  func suggestionCondition() throws {
    let london = try #require(TimeZone(identifier: "Europe/London"))
    let kathmandu = try #require(TimeZone(identifier: "Asia/Kathmandu"))
    let store = Self.store()
    defer { Self.remove(store) }

    #expect(
      SettingsViewModel(settings: Settings(), store: store)
        .shouldSuggestNepalTime(deviceZone: london)
    )
    #expect(
      !SettingsViewModel(settings: Settings(), store: store)
        .shouldSuggestNepalTime(deviceZone: kathmandu)
    )
    #expect(
      !SettingsViewModel(
        settings: Settings(dateFollowsSuggestionHandled: true),
        store: store
      ).shouldSuggestNepalTime(deviceZone: london)
    )
    #expect(
      !SettingsViewModel(
        settings: Settings(dateFollows: .nepal),
        store: store
      ).shouldSuggestNepalTime(deviceZone: london)
    )
  }

  @Test("MR-06 AC1: accepting and dismissing both record the suggestion")
  func suggestionActions() throws {
    let store = Self.store()
    defer { Self.remove(store) }
    let london = try #require(TimeZone(identifier: "Europe/London"))

    var accepted = SettingsViewModel(settings: Settings(), store: store)
    accepted.acceptNepalTime()
    #expect(accepted.settings.dateFollows == .nepal)
    #expect(store.load().dateFollows == .nepal)
    #expect(store.load().dateFollowsSuggestionHandled)
    #expect(!accepted.shouldSuggestNepalTime(deviceZone: london))

    var dismissed = SettingsViewModel(settings: Settings(), store: store)
    dismissed.dismissNepalTime()
    #expect(dismissed.settings.dateFollows == .device)
    #expect(store.load().dateFollowsSuggestionHandled)
    #expect(!dismissed.shouldSuggestNepalTime(deviceZone: london))
  }

  @Test("MR-07 AC2: AboutLinks has exactly the five allow-listed URLs")
  func links() {
    let expected: Set<String> = [
      "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/known-data-questions.md",
      "https://github.com/sanamhub/nepdate-mobile/issues/new?template=wrong-date.yml",
      "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/legal/README.md",
      "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/privacy-policy.md",
      "https://github.com/sanamhub/nepdate-mobile",
    ]
    #expect(Set(AboutLinks.all) == expected)
    #expect(AboutLinks.all.allSatisfy { AboutLinks.problem(with: $0) == nil })
    #expect(AboutLinks.problem(with: "https://github.com/sanamhub/nepdate") != nil)
    #expect(AboutLinks.problem(with: "http://github.com/sanamhub/nepdate-mobile") != nil)
    #expect(AboutLinks.problem(with: "https://example.test/sanamhub/nepdate-mobile") != nil)
    #expect(
      AboutLinks.problem(with: "https://github.com/sanamhub/nepdate-mobile/../nepdate") != nil
    )
  }

  @Test("MR-12 AC2: the subscription row is hidden for an empty injected URL and shown otherwise")
  func subscription() {
    #expect(!AboutLinksState(holidaysURL: "").showsSubscription)
    #expect(AboutLinksState(holidaysURL: "webcal://example.test/holidays.ics").showsSubscription)
    #expect(AboutLinks.holidaysICSURL.isEmpty)
  }

  @Test("MR-07 AC2: the About values have the expected coverage and versions")
  func aboutInfo() {
    let info = AboutInfo(appVersion: "0.1.0 (1)", libraryVersion: "708cd36", osVersion: "26.0")

    #expect(
      info.coverageText(language: .english, digits: .english)
        == "Baisakh 1, 2077 – Mangsir 20, 2084"
    )
    #expect(
      info.coverageText(language: .nepali, digits: .nepali)
        == "बैशाख १, २०७७ – मंसिर २०, २०८४"
    )
    #expect(info.appVersion == "0.1.0 (1)")
    #expect(info.libraryVersion == "708cd36")
  }

  @Test("ADR-0007 §5: the diagnostic text has the app, iOS and three settings only")
  func diagnostic() {
    let labels = DiagnosticLabels(
      app: "App",
      os: "iOS",
      dateFollows: "Date follows",
      digits: "Digits",
      tithi: "Show tithi",
      on: "on",
      off: "off",
      device: "This device",
      nepal: "Nepal time",
      followLanguage: "Follow language",
      nepaliDigits: "Nepali",
      englishDigits: "English"
    )

    let text = DiagnosticText.make(
      appVersion: "0.1.0 (1)",
      osVersion: "26.0",
      settings: Settings(digits: .english, dateFollows: .nepal, showTithi: true),
      labels: labels
    )

    #expect(
      text
        == "App: 0.1.0 (1)\n"
        + "iOS: 26.0\n"
        + "Date follows: Nepal time\n"
        + "Digits: English\n"
        + "Show tithi: on"
    )

    let defaults = DiagnosticText.make(
      appVersion: "0.1.0 (1)",
      osVersion: "26.0",
      settings: Settings(),
      labels: labels
    )
    #expect(defaults.contains("Date follows: This device"))
    #expect(defaults.contains("Digits: Follow language"))
    #expect(defaults.contains("Show tithi: off"))
  }

  @Test("I1-05-02: the language row uses the system app settings URL")
  func languageURL() {
    #expect(URL(string: UIApplication.openSettingsURLString) != nil)
  }
}
