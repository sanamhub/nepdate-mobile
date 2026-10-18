import Foundation
import NepDate

/// The language of names and digits the app shows.
public enum AppLanguage: Sendable, Hashable, CaseIterable {
  /// English names and ASCII digits.
  case english

  /// Nepali names and Devanagari digits.
  case nepali

  /// The language the running bundle's strings are shown in.
  ///
  /// Names and digits follow the app language, so this reads the localization the system chose
  /// for the bundle, not the first system language: with system languages `hi`, `ne` the strings
  /// come out in Nepali, and so must the names and digits.
  public static var current: AppLanguage {
    preferred(from: Bundle.main.preferredLocalizations)
  }

  /// Chooses the language from a list of language identifiers.
  ///
  /// The first identifier wins: `ne` and `ne-NP` select `.nepali`, anything else selects
  /// `.english`.
  /// - Parameter preferredLanguages: BCP 47 identifiers, most preferred first.
  /// - Returns: The language to display.
  public static func preferred(from preferredLanguages: [String]) -> AppLanguage {
    guard let first = preferredLanguages.first else { return .english }
    return first.hasPrefix("ne") ? .nepali : .english
  }

  /// The matching language of nepdate-swift.
  var lang: Lang {
    self == .nepali ? .nepali : .english
  }
}
