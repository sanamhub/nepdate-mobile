import CalendarKit
import SwiftUI

/// The Today widget's content, drawn from the values passed in.
public struct TodayWidgetView: View {
  private let summary: TodaySummary

  /// Creates the view.
  /// - Parameter summary: The values to draw.
  public init(summary: TodaySummary) {
    self.summary = summary
  }

  /// The view's content.
  public var body: some View {
    VStack(alignment: .leading, spacing: 4) {
      HStack(alignment: .firstTextBaseline, spacing: 8) {
        Text(summary.day)
          .font(.system(size: 56, weight: .semibold, design: .rounded))
          .monospacedDigit()
          .foregroundStyle(summary.isHoliday ? BrandColor.holiday : Color.primary)
        Spacer(minLength: 0)
        Text(summary.weekday)
          .font(.caption)
          .foregroundStyle(.secondary)
      }
      Text(summary.month)
        .font(.headline)
      if summary.isHoliday {
        Rectangle()
          .fill(BrandColor.holiday)
          .frame(height: 1.5)
      }
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
  }
}
