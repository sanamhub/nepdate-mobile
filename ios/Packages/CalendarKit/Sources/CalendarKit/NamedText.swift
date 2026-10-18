import NepDatePatro

/// A tithi or event name in both languages.
///
/// The layout keeps both, so changing the app language does not need a new layout.
public struct NamedText: Sendable, Hashable {
  /// The Nepali name.
  public let nepali: String

  /// The English name.
  public let english: String

  init(_ named: Named) {
    self.nepali = named.nepali
    self.english = named.english
  }

  /// The name in the given language.
  /// - Parameter language: The language to show.
  /// - Returns: The Nepali or the English name.
  public func name(_ language: AppLanguage) -> String {
    language == .nepali ? nepali : english
  }
}
