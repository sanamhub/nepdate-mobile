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

  @Test("a fresh store reads the defaults")
  func freshStore() {
    let store = Self.makeStore()
    defer { Self.remove(store) }
    let settings = store.load()

    #expect(settings.language == .system)
    #expect(settings.digits == .followLanguage)
    #expect(settings.dateFollows == .device)
    #expect(settings.showTithi == false)
    #expect(settings.dateFollowsSuggestionHandled == false)
    #expect(settings.appearance == .system)
    #expect(settings.showsADDates)
    #expect(settings.opensOn == .patro)
    #expect(settings.hapticsEnabled)
  }

  @Test("every setting round-trips")
  func roundTrip() {
    let store = Self.makeStore()
    defer { Self.remove(store) }

    store.setLanguage(.nepali)
    store.setDigits(.english)
    store.setDateFollows(.nepal)
    store.setShowTithi(true)
    store.setDateFollowsSuggestionHandled(true)
    store.setAppearance(.dark)
    store.setShowsADDates(false)
    store.setOpensOn(.festivals)
    store.setHapticsEnabled(false)

    let settings = store.load()
    #expect(settings.language == .nepali)
    #expect(settings.digits == .english)
    #expect(settings.dateFollows == .nepal)
    #expect(settings.showTithi)
    #expect(settings.dateFollowsSuggestionHandled)
    #expect(settings.appearance == .dark)
    #expect(settings.showsADDates == false)
    #expect(settings.opensOn == .festivals)
    #expect(settings.hapticsEnabled == false)
  }

  @Test("MR-18: Opens on maps to its tab")
  func opensOnTabs() {
    #expect(OpensOn.patro.tab == .patro)
    #expect(OpensOn.festivals.tab == .festivals)
    #expect(OpensOn.converter.tab == .converter)
  }

  @Test("unknown stored values fall back to the defaults")
  func unknownValues() throws {
    let store = Self.makeStore()
    defer { Self.remove(store) }
    let defaults = try #require(UserDefaults(suiteName: store.suiteName))
    defaults.set("MARS", forKey: "date_follows")
    defaults.set("ROMAN", forKey: "language")
    defaults.set("ROMAN", forKey: "digits")

    let settings = store.load()
    #expect(settings.dateFollows == .device)
    #expect(settings.language == .system)
    #expect(settings.digits == .followLanguage)
  }

  @Test("the default store is the App Group")
  func appGroup() {
    #expect(SettingsStore().suiteName == AppGroup.identifier)
    #expect(AppGroup.identifier == "group.np.nepdate")
  }

  @Test("the stored language overrides the bundle, System follows it")
  func displayLanguage() {
    #expect(LanguageSetting.english.resolved == .english)
    #expect(LanguageSetting.nepali.resolved == .nepali)
    #expect(LanguageSetting.system.resolved == AppLanguage.current)
    #expect(AppLanguage.preferred(from: ["ne-NP", "en"]) == .nepali)
    #expect(AppLanguage.preferred(from: ["en-US"]) == .english)
    #expect(AppLanguage.preferred(from: []) == .english)
  }

  @Test("the app ships Nepali strings")
  func bundleLanguage() {
    #expect(Bundle.main.localizations.contains("ne"))
  }
}
