import CalendarKit
import Foundation
import NepDate

/// The locale a model uses to resolve its strings in tests and snapshots.
extension AppLanguage {
  var locale: Locale {
    Locale(identifier: self == .nepali ? "ne" : "en")
  }

  /// The library's language for the names.
  var nepDateLang: Lang {
    self == .nepali ? .nepali : .english
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
