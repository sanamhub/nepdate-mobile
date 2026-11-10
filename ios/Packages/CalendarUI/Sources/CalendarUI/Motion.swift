import SwiftUI

/// The animations the app uses, and their Reduce Motion fallback (MR-01, MR-14, MR-20).
///
/// A custom transition moves a view; with Reduce Motion on it cross-fades instead. The system's
/// own transitions, such as paging and sheet presentation, already honour the setting.
public enum Motion {
  /// An animation, or a short cross-fade when the user asked for less motion.
  /// - Parameters:
  ///   - animation: The animation to use normally.
  ///   - reduceMotion: The Reduce Motion setting.
  /// - Returns: The animation to pass to `withAnimation`.
  public static func animation(_ animation: Animation, reduceMotion: Bool) -> Animation {
    reduceMotion ? .easeInOut(duration: 0.2) : animation
  }

  /// A transition, or a cross-fade when the user asked for less motion.
  /// - Parameters:
  ///   - transition: The transition to use normally.
  ///   - reduceMotion: The Reduce Motion setting.
  /// - Returns: The transition to apply.
  public static func transition(
    _ transition: AnyTransition,
    reduceMotion: Bool
  ) -> AnyTransition {
    reduceMotion ? .opacity : transition
  }
}
