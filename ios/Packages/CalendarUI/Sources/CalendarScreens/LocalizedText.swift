import CalendarKit
import Foundation

/// The locale a model uses to resolve its strings in tests and snapshots.
extension AppLanguage {
  var locale: Locale {
    Locale(identifier: self == .nepali ? "ne" : "en")
  }
}

/// Resolves a localised string for a language without relying on the process locale.
enum AppText {
  /// The text in `language`.
  /// - Parameters:
  ///   - value: The localisation key.
  ///   - language: The app language.
  /// - Returns: The localised string.
  static func string(_ value: String.LocalizationValue, _ language: AppLanguage) -> String {
    String(localized: value, locale: language.locale)
  }
}
