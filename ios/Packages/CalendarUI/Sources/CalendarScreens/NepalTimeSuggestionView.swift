import CalendarUI
import SwiftUI

/// The once-only first-launch "Nepal time" suggestion (MR-06 AC1).
public struct NepalTimeSuggestionView: View {
  private let onAccept: () -> Void
  private let onDismiss: () -> Void

  /// Creates the suggestion.
  /// - Parameters:
  ///   - onAccept: Switches to Nepal time.
  ///   - onDismiss: Keeps this device.
  public init(onAccept: @escaping () -> Void, onDismiss: @escaping () -> Void) {
    self.onAccept = onAccept
    self.onDismiss = onDismiss
  }

  /// The suggestion's content.
  public var body: some View {
    VStack(alignment: .leading, spacing: 6) {
      Text("Use Nepal time?")
        .font(.headline)
      Text("Your device is in a different time zone. The month view can follow Nepal time instead.")
        .font(.subheadline)
        .foregroundStyle(.secondary)
        .fixedSize(horizontal: false, vertical: true)

      HStack(spacing: 8) {
        Spacer(minLength: 0)
        Button("Not now", action: onDismiss)
          .frame(minHeight: 44)
        Button("Use Nepal time", action: onAccept)
          .buttonStyle(.borderedProminent)
          .tint(BrandColor.today)
          .frame(minHeight: 44)
      }
    }
    .padding(.horizontal, 16)
    .padding(.vertical, 8)
    .background(Color(uiColor: .secondarySystemBackground))
  }
}
