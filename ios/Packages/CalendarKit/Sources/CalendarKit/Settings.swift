import NepDate

/// Which zone "today" follows (MR-06 AC1).
public enum DateFollows: String, Sendable, Hashable, CaseIterable {
  /// The device's current zone. The fresh-install default.
  case device

  /// `Asia/Kathmandu`, whatever the device zone is.
  case nepal
}

/// The Language setting (MR-07 AC1).
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

/// The numerals the app draws.
public enum Digits: Sendable, Hashable {
  /// 0123456789.
  case english

  /// ०१२३४५६७८९.
  case nepali

  /// A number in these digits.
  /// - Parameter value: The number.
  /// - Returns: `25` or `२५`.
  public func format(_ value: Int) -> String {
    self == .nepali ? nepaliDigits(value) : String(value)
  }
}

/// The Digits setting (MR-07 AC1). The fresh-install default follows the language, so a user
/// who never opens Settings sees `25` in English and `२५` in Nepali; the other two values
/// override it.
public enum DigitsSetting: String, Sendable, Hashable, CaseIterable {
  /// The digits of the displayed language: Nepali for Nepali, English for everything else.
  case followLanguage = "follow_language"

  /// Always ASCII digits.
  case english

  /// Always Devanagari digits.
  case nepali

  /// The digits to draw, resolved at render time so a language change needs no new setting.
  /// - Parameter language: The displayed language.
  /// - Returns: The digits for this setting in that language.
  public func resolved(for language: AppLanguage) -> Digits {
    switch self {
    case .followLanguage: return language == .nepali ? .nepali : .english
    case .english: return .english
    case .nepali: return .nepali
    }
  }
}

/// The Appearance setting (MR-16). System follows the device; the other two fix the scheme.
public enum Appearance: String, Sendable, Hashable, CaseIterable {
  /// Follow the device's light or dark appearance.
  case system

  /// Always draw light.
  case light

  /// Always draw dark.
  case dark
}

/// The tab the app opens on (MR-18 AC1).
public enum OpensOn: String, Sendable, Hashable, CaseIterable {
  /// The month screen.
  case patro

  /// The festivals screen.
  case festivals

  /// The converter.
  case converter

  /// The tab this choice opens.
  public var tab: AppTab {
    switch self {
    case .patro: return .patro
    case .festivals: return .festivals
    case .converter: return .converter
    }
  }
}

/// The persisted preferences from MR-06, MR-07, MR-16, MR-17, MR-18 and MR-19.
public struct Settings: Sendable, Hashable {
  /// The Language setting. The month's BS and AD digits follow it.
  public var language: LanguageSetting

  /// The Digits setting. Resolve it with ``DigitsSetting/resolved(for:)`` before drawing.
  public var digits: DigitsSetting

  /// Which zone "today" and the widgets follow.
  public var dateFollows: DateFollows

  /// Whether the month grid shows a tithi line (MR-01 AC8).
  public var showTithi: Bool

  /// Whether the first-launch "Nepal time" suggestion was shown (MR-06 AC1).
  public var dateFollowsSuggestionHandled: Bool

  /// The Appearance setting (MR-16).
  public var appearance: Appearance

  /// Whether a month cell draws its small AD day (MR-17).
  public var showsADDates: Bool

  /// The tab a cold launch opens (MR-18).
  public var opensOn: OpensOn

  /// Whether the app plays haptics (MR-19).
  public var hapticsEnabled: Bool

  /// The fresh-install values: System, follow language, This device, no tithi, suggestion not
  /// shown, follow the system appearance, AD dates on, Patro, haptics on.
  public static let defaults = Settings()

  /// Creates settings.
  /// - Parameters:
  ///   - language: The Language setting; defaults to System.
  ///   - digits: The Digits setting; defaults to follow language.
  ///   - dateFollows: The zone choice; defaults to This device.
  ///   - showTithi: Whether the month grid shows tithi; defaults to false.
  ///   - dateFollowsSuggestionHandled: Whether the first-launch suggestion was shown; defaults
  ///     to false.
  ///   - appearance: The Appearance setting; defaults to System.
  ///   - showsADDates: Whether month cells draw the AD day; defaults to true.
  ///   - opensOn: The tab a cold launch opens; defaults to Patro.
  ///   - hapticsEnabled: Whether the app plays haptics; defaults to true.
  public init(
    language: LanguageSetting = .system,
    digits: DigitsSetting = .followLanguage,
    dateFollows: DateFollows = .device,
    showTithi: Bool = false,
    dateFollowsSuggestionHandled: Bool = false,
    appearance: Appearance = .system,
    showsADDates: Bool = true,
    opensOn: OpensOn = .patro,
    hapticsEnabled: Bool = true
  ) {
    self.language = language
    self.digits = digits
    self.dateFollows = dateFollows
    self.showTithi = showTithi
    self.dateFollowsSuggestionHandled = dateFollowsSuggestionHandled
    self.appearance = appearance
    self.showsADDates = showsADDates
    self.opensOn = opensOn
    self.hapticsEnabled = hapticsEnabled
  }
}
