import Foundation

/// Reads and writes ``Settings`` in the App Group's `UserDefaults`.
///
/// The store holds no state: every call opens the suite, so the app and the widget extension see
/// the same values. A test passes its own suite name and never touches `group.np.nepdate`.
public struct SettingsStore: Sendable {
  /// The defaults suite the store opens.
  public let suiteName: String

  /// Creates a store.
  /// - Parameter suiteName: The suite to open; defaults to ``AppGroup/identifier``.
  public init(suiteName: String = AppGroup.identifier) {
    self.suiteName = suiteName
  }

  /// The stored settings.
  ///
  /// Keys that were never written, and values that no longer decode, fall back to
  /// ``Settings/defaults``.
  /// - Returns: The settings.
  public func load() -> Settings {
    let defaults = AppGroup.defaults(suiteName: suiteName)
    return Settings(
      language: LanguageSetting(rawValue: defaults?.string(forKey: Key.language) ?? "") ?? .system,
      digits: DigitsSetting(rawValue: defaults?.string(forKey: Key.digits) ?? "")
        ?? .followLanguage,
      dateFollows: DateFollows(rawValue: defaults?.string(forKey: Key.dateFollows) ?? "")
        ?? .device,
      showTithi: defaults?.bool(forKey: Key.showTithi) ?? false,
      dateFollowsSuggestionHandled: defaults?.bool(forKey: Key.suggestion) ?? false,
      appearance: Appearance(rawValue: defaults?.string(forKey: Key.appearance) ?? "") ?? .system,
      showsADDates: Self.bool(defaults, Key.showsADDates, default: true),
      opensOn: OpensOn(rawValue: defaults?.string(forKey: Key.opensOn) ?? "") ?? .patro,
      hapticsEnabled: Self.bool(defaults, Key.haptics, default: true)
    )
  }

  /// Reads a stored flag whose fresh-install value is `true`.
  private static func bool(_ defaults: UserDefaults?, _ key: String, default value: Bool) -> Bool {
    guard let defaults, defaults.object(forKey: key) != nil else { return value }
    return defaults.bool(forKey: key)
  }

  /// Stores the Language choice (MR-07 AC1).
  /// - Parameter value: The setting.
  public func setLanguage(_ value: LanguageSetting) {
    AppGroup.defaults(suiteName: suiteName)?.set(value.rawValue, forKey: Key.language)
  }

  /// Stores the Digits choice (MR-07 AC1).
  /// - Parameter value: The setting.
  public func setDigits(_ value: DigitsSetting) {
    AppGroup.defaults(suiteName: suiteName)?.set(value.rawValue, forKey: Key.digits)
  }

  /// Stores the "Date follows" choice (MR-06 AC1).
  /// - Parameter value: The setting.
  public func setDateFollows(_ value: DateFollows) {
    AppGroup.defaults(suiteName: suiteName)?.set(value.rawValue, forKey: Key.dateFollows)
  }

  /// Stores whether the month grid shows a tithi line (MR-01 AC8).
  /// - Parameter value: `true` to show the line.
  public func setShowTithi(_ value: Bool) {
    AppGroup.defaults(suiteName: suiteName)?.set(value, forKey: Key.showTithi)
  }

  /// Records that the first-launch "Nepal time" suggestion was shown (MR-06 AC1).
  /// - Parameter value: `true` once it was accepted or dismissed.
  public func setDateFollowsSuggestionHandled(_ value: Bool) {
    AppGroup.defaults(suiteName: suiteName)?.set(value, forKey: Key.suggestion)
  }

  /// Stores the Appearance choice (MR-16).
  /// - Parameter value: The setting.
  public func setAppearance(_ value: Appearance) {
    AppGroup.defaults(suiteName: suiteName)?.set(value.rawValue, forKey: Key.appearance)
  }

  /// Stores the Show AD dates choice (MR-17).
  /// - Parameter value: The setting.
  public func setShowsADDates(_ value: Bool) {
    AppGroup.defaults(suiteName: suiteName)?.set(value, forKey: Key.showsADDates)
  }

  /// Stores the Opens on choice (MR-18).
  /// - Parameter value: The setting.
  public func setOpensOn(_ value: OpensOn) {
    AppGroup.defaults(suiteName: suiteName)?.set(value.rawValue, forKey: Key.opensOn)
  }

  /// Stores the Haptic feedback choice (MR-19).
  /// - Parameter value: The setting.
  public func setHapticsEnabled(_ value: Bool) {
    AppGroup.defaults(suiteName: suiteName)?.set(value, forKey: Key.haptics)
  }

  private enum Key {
    static let language = "language"
    static let digits = "digits"
    static let dateFollows = "date_follows"
    static let showTithi = "show_tithi"
    static let suggestion = "date_follows_suggestion_handled"
    static let appearance = "appearance"
    static let showsADDates = "show_ad_dates"
    static let opensOn = "opens_on"
    static let haptics = "haptics_enabled"
  }
}
