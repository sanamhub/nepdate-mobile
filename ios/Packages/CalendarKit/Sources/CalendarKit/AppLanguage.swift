import Foundation
import NepDate

/// The language of names and digits the app shows.
public enum AppLanguage: Sendable, Hashable, CaseIterable {
  /// English names and ASCII digits.
  case english

  /// Nepali names and Devanagari digits.
  case nepali

  /// Chooses the language from the user's preferred language identifiers.
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
