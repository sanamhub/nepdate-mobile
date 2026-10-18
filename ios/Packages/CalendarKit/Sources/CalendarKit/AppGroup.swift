import Foundation

/// The App Group shared by the app and the widget extension (ADR-0004).
public enum AppGroup {
  /// The identifier in `project.yml` and both entitlements files.
  public static let identifier = "group.np.nepdate"

  /// A defaults store in the App Group.
  /// - Parameter suiteName: The suite to open; defaults to the app group.
  /// - Returns: The defaults for the suite, or `nil` when the suite cannot be opened.
  public static func defaults(suiteName: String = AppGroup.identifier) -> UserDefaults? {
    UserDefaults(suiteName: suiteName)
  }
}
