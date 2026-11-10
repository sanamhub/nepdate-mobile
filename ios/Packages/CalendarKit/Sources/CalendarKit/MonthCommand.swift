/// A month change asked for from outside the month screen, for example by a keyboard command.
///
/// The shell hands one command to the Patro tab, which applies it and clears it, so the same
/// request is never replayed when the tab comes back.
public enum MonthCommand: Sendable, Hashable {
  /// Show today's month.
  case today

  /// Show the month before the one on screen.
  case previous

  /// Show the month after the one on screen.
  case next
}
