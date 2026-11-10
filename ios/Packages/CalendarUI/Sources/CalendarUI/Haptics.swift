import SwiftUI

/// Whether the app plays haptics (MR-19 AC2). The root reads the Haptic feedback setting once and
/// every `hapticFeedback` call in the app reads this value, so switching it off leaves no haptic
/// anywhere.
public struct HapticsEnabledKey: EnvironmentKey {
  /// Haptics are on unless the root turns them off.
  public static let defaultValue = true
}

extension EnvironmentValues {
  /// Whether haptics are on.
  public var hapticsEnabled: Bool {
    get { self[HapticsEnabledKey.self] }
    set { self[HapticsEnabledKey.self] = newValue }
  }
}

extension View {
  /// Plays a haptic when a value changes, unless the user turned haptics off (MR-19).
  /// - Parameters:
  ///   - feedback: The haptic to play.
  ///   - trigger: The value whose change plays it.
  /// - Returns: The view with the feedback attached.
  public func hapticFeedback<T: Equatable>(
    _ feedback: SensoryFeedback,
    trigger: T
  ) -> some View {
    modifier(HapticFeedbackModifier(feedback: feedback, trigger: trigger))
  }
}

private struct HapticFeedbackModifier<T: Equatable>: ViewModifier {
  let feedback: SensoryFeedback
  let trigger: T
  @Environment(\.hapticsEnabled) private var enabled

  func body(content: Content) -> some View {
    content.sensoryFeedback(feedback, trigger: trigger) { _, _ in enabled }
  }
}
