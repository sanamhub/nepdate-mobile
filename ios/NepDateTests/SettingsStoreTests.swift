import CalendarKit
import Foundation
import Testing

@Suite("Settings")
struct SettingsStoreTests {
  private static func makeStore() -> SettingsStore {
    SettingsStore(suiteName: "np.nepdate.tests.settings.\(UUID().uuidString)")
  }

  private static func remove(_ store: SettingsStore) {
    UserDefaults(suiteName: store.suiteName)?.removePersistentDomain(forName: store.suiteName)
  }

  @Test("I1-01: a fresh store reads the defaults")
  func freshStore() {
    let store = Self.makeStore()
    defer { Self.remove(store) }
    let settings = store.load()

    #expect(settings.language == .system)
    #expect(settings.dateFollows == .device)
    #expect(settings.showTithi == false)
    #expect(settings.dateFollowsSuggestionHandled == false)
  }

  @Test("I1-01: every setting round-trips")
  func roundTrip() {
    let store = Self.makeStore()
    defer { Self.remove(store) }

    store.setLanguage(.nepali)
    store.setDateFollows(.nepal)
    store.setShowTithi(true)
    store.setDateFollowsSuggestionHandled(true)

    let settings = store.load()
    #expect(settings.language == .nepali)
    #expect(settings.dateFollows == .nepal)
    #expect(settings.showTithi)
    #expect(settings.dateFollowsSuggestionHandled)
  }

  @Test("I1-01: unknown stored values fall back to the defaults")
  func unknownValues() throws {
    let store = Self.makeStore()
    defer { Self.remove(store) }
    let defaults = try #require(UserDefaults(suiteName: store.suiteName))
    defaults.set("MARS", forKey: "date_follows")
    defaults.set("ROMAN", forKey: "language")

    let settings = store.load()
    #expect(settings.dateFollows == .device)
    #expect(settings.language == .system)
  }

  @Test("I1-01: the default store is the App Group")
  func appGroup() {
    #expect(SettingsStore().suiteName == AppGroup.identifier)
    #expect(AppGroup.identifier == "group.np.nepdate")
  }

  @Test("I1-01: the stored language overrides the bundle, System follows it")
  func displayLanguage() {
    #expect(LanguageSetting.english.resolved == .english)
    #expect(LanguageSetting.nepali.resolved == .nepali)
    #expect(LanguageSetting.system.resolved == AppLanguage.current)
    #expect(AppLanguage.preferred(from: ["ne-NP", "en"]) == .nepali)
    #expect(AppLanguage.preferred(from: ["en-US"]) == .english)
    #expect(AppLanguage.preferred(from: []) == .english)
  }

  @Test("I1-01: the app ships Nepali strings")
  func bundleLanguage() {
    #expect(Bundle.main.localizations.contains("ne"))
  }
}
