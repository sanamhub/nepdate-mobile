/// Which zone "today" follows (MR-06 AC1).
public enum DateFollows: String, Sendable, Hashable, CaseIterable {
  /// The device's current zone. The fresh-install default.
  case device

  /// `Asia/Kathmandu`, whatever the device zone is.
  case nepal
}

/// The Language setting (MR-07 AC1). Digits follow this language; there is no separate setting.
public enum LanguageSetting: String, Sendable, Hashable, CaseIterable {
  /// The language the system chose for the app.
  case system

  /// English names and ASCII digits.
  case english

  /// Nepali names and Devanagari digits.
  case nepali

  /// The language to display: the system's choice, or the stored override.
  public var resolved: AppLanguage {
    switch self {
    case .system: return .current
    case .english: return .english
    case .nepali: return .nepali
    }
  }
}

/// The persisted preferences from MR-06 and MR-07.
public struct Settings: Sendable, Hashable {
  /// The Language setting. The month's BS and AD digits follow it.
  public var language: LanguageSetting

  /// Which zone "today" and the widgets follow.
  public var dateFollows: DateFollows

  /// Whether the month grid shows a tithi line (MR-01 AC8).
  public var showTithi: Bool

  /// Whether the first-launch "Nepal time" suggestion was shown (MR-06 AC1).
  public var dateFollowsSuggestionHandled: Bool

  /// The fresh-install values: System, This device, no tithi, suggestion not shown.
  public static let defaults = Settings()

  /// Creates settings.
  /// - Parameters:
  ///   - language: The Language setting; defaults to System.
  ///   - dateFollows: The zone choice; defaults to This device.
  ///   - showTithi: Whether the month grid shows tithi; defaults to false.
  ///   - dateFollowsSuggestionHandled: Whether the first-launch suggestion was shown; defaults
  ///     to false.
  public init(
    language: LanguageSetting = .system,
    dateFollows: DateFollows = .device,
    showTithi: Bool = false,
    dateFollowsSuggestionHandled: Bool = false
  ) {
    self.language = language
    self.dateFollows = dateFollows
    self.showTithi = showTithi
    self.dateFollowsSuggestionHandled = dateFollowsSuggestionHandled
  }
}
