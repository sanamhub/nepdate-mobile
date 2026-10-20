import Observation
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

/// Keeps one copy confirmation visible for its interval. Showing it again cancels the previous
/// interval, so an earlier copy cannot hide the confirmation for a later one.
@MainActor
@Observable
final class CopyConfirmationTimer {
  /// Whether the confirmation is visible.
  private(set) var isShowing: Bool

  /// The current hide task. Showing the confirmation replaces it.
  private(set) var hideTask: Task<Void, Never>?

  private let duration: Duration
  private let sleep: @Sendable (Duration) async throws -> Void

  /// Creates a timer.
  /// - Parameters:
  ///   - isShowing: Whether the confirmation starts visible, for snapshots.
  ///   - duration: How long each confirmation stays visible.
  ///   - sleep: The wait between showing and hiding, injectable for tests.
  init(
    isShowing: Bool = false,
    duration: Duration = CopyConfirmation.duration,
    sleep: @escaping @Sendable (Duration) async throws -> Void = { try await Task.sleep(for: $0) }
  ) {
    self.isShowing = isShowing
    self.duration = duration
    self.sleep = sleep
  }

  /// Shows the confirmation and starts a new hide interval.
  func show() {
    isShowing = true
    hideTask?.cancel()
    hideTask = Task { @MainActor [weak self] in
      guard let self else { return }
      do {
        try await sleep(duration)
      } catch {
        return
      }
      guard !Task.isCancelled else { return }
      isShowing = false
    }
  }
}
