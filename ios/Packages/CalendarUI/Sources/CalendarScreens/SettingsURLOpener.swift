import Foundation
import UIKit

/// Opens the app's page in Settings. A test injects the open closure.
@MainActor
public struct SettingsURLOpener {
  private let open: (URL) -> Void

  /// Creates an opener.
  /// - Parameter open: Handles the app settings URL.
  public init(open: @escaping (URL) -> Void = { UIApplication.shared.open($0) }) {
    self.open = open
  }

  /// Opens the app's page in Settings.
  public func openAppSettings() {
    guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
    open(url)
  }
}
