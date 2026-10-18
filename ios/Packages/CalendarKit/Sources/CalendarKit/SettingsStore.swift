import Foundation

/// Reads and writes ``Settings`` in the App Group's `UserDefaults` (I1-01).
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
      dateFollowsSuggestionHandled: defaults?.bool(forKey: Key.suggestion) ?? false
    )
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

  private enum Key {
    static let language = "language"
    static let digits = "digits"
    static let dateFollows = "date_follows"
    static let showTithi = "show_tithi"
    static let suggestion = "date_follows_suggestion_handled"
  }
}
