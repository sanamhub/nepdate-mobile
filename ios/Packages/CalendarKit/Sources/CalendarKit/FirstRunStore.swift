import Foundation

/// The "already shown" flag of the first-run screen (MR-15 AC3).
///
/// The flag lives in the App Group, so reinstalling the app shows the screen again and a widget
/// reload does not. A test passes its own suite name.
public struct FirstRunStore: Sendable {
  /// The defaults suite the store opens.
  public let suiteName: String

  /// Creates a store.
  /// - Parameter suiteName: The suite to open; defaults to ``AppGroup/identifier``.
  public init(suiteName: String = AppGroup.identifier) {
    self.suiteName = suiteName
  }

  /// Whether the first-run screen has been shown and dismissed.
  public var hasShown: Bool {
    AppGroup.defaults(suiteName: suiteName)?.bool(forKey: Self.key) ?? false
  }

  /// Records that the first-run screen was dismissed, so it never shows again.
  public func markShown() {
    AppGroup.defaults(suiteName: suiteName)?.set(true, forKey: Self.key)
  }

  private static let key = "first_run_shown"
}
