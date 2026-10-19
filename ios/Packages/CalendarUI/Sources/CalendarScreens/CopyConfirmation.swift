import UIKit

/// The shared confirmation for copy actions: it stays for two seconds and speaks one announcement.
@MainActor
enum CopyConfirmation {
  /// How long the confirmation stays visible.
  static let duration: Duration = .seconds(2)

  /// Speaks the confirmation once.
  /// - Parameter text: The text to announce.
  static func announce(_ text: String) {
    UIAccessibility.post(notification: .announcement, argument: text)
  }
}
