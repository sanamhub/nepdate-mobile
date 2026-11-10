import CalendarKit
import SwiftUI

/// The SwiftUI colour scheme an Appearance setting asks for (MR-16 AC1).
extension Appearance {
  /// The scheme to apply at the root, or `nil` to follow the system.
  public var colorScheme: ColorScheme? {
    switch self {
    case .system: return nil
    case .light: return .light
    case .dark: return .dark
    }
  }
}
