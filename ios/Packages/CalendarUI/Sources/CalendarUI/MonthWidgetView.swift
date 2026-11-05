import CalendarKit
import SwiftUI
import WidgetKit

/// The large month widget (W-03): the month grid with BS numerals, AD numerals, holiday marks,
/// event dots and the today ring.
///
/// The header's buttons come from the widget extension, because `Button(intent:)` needs the
/// extension's App Intents. A snapshot test passes plain images instead. Each day in the shown
/// month links to its detail.
public struct MonthWidgetView<Controls: View>: View {
  private let entry: MonthWidgetEntry
  private let controls: Controls

  /// Creates the view.
  /// - Parameters:
  ///   - entry: The values to draw.
  ///   - controls: The Previous, Today and Next buttons, drawn at the right of the header.
  public init(entry: MonthWidgetEntry, @ViewBuilder controls: () -> Controls) {
    self.entry = entry
    self.controls = controls()
  }

  /// The view's content.
  public var body: some View {
    VStack(spacing: 4) {
      header
      weekdayRow
      grid
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
    .widgetURL(AppRoute.today.url)
  }

  private var header: some View {
    HStack(alignment: .center, spacing: 6) {
      VStack(alignment: .leading, spacing: 0) {
        Text(verbatim: entry.title)
          .font(.headline)
          .lineLimit(1)
        Text(verbatim: entry.adSpan)
          .font(.caption2)
          .foregroundStyle(BrandColor.secondaryText)
          .lineLimit(1)
      }
      Spacer(minLength: 0)
      controls
    }
  }

  private var weekdayRow: some View {
    HStack(spacing: 0) {
      ForEach(Array(entry.weekdays.enumerated()), id: \.offset) { index, name in
        Text(verbatim: name)
          .font(.caption2)
          .foregroundStyle(index == 6 ? BrandColor.holiday : BrandColor.secondaryText)
          .lineLimit(1)
          .frame(maxWidth: .infinity)
      }
    }
    .accessibilityHidden(true)
  }

  private var grid: some View {
    VStack(spacing: 1) {
      ForEach(0..<entry.rowCount, id: \.self) { row in
        HStack(spacing: 1) {
          ForEach(0..<7, id: \.self) { column in
            cell(entry.cells[row * 7 + column])
          }
        }
      }
    }
  }

  @ViewBuilder
  private func cell(_ cell: MonthWidgetCell) -> some View {
    if let url = cell.url {
      Link(destination: url) {
        content(of: cell)
      }
      .foregroundStyle(.primary)
    } else {
      content(of: cell)
        .opacity(cell.date == nil ? 0 : 0.4)
    }
  }

  private func content(of cell: MonthWidgetCell) -> some View {
    VStack(spacing: 0) {
      Text(verbatim: cell.bs)
        .font(.system(size: 14, weight: .semibold, design: .rounded))
        .monospacedDigit()
        .foregroundStyle(cell.isHoliday ? BrandColor.holiday : Color.primary)
        .widgetAccentable(cell.isHoliday || cell.isToday)
      Text(verbatim: cell.ad)
        .font(.system(size: 8))
        .foregroundStyle(BrandColor.secondaryText)
      Circle()
        .fill(cell.hasEvents ? BrandColor.today : Color.clear)
        .frame(width: 3, height: 3)
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .background(
      RoundedRectangle(cornerRadius: 6)
        .fill(cell.isHoliday ? BrandColor.holidayFill : Color.clear)
    )
    .overlay(
      RoundedRectangle(cornerRadius: 6)
        .strokeBorder(cell.isToday ? BrandColor.today : Color.clear, lineWidth: 1.5)
    )
    .accessibilityElement(children: .ignore)
    .accessibilityLabel(Text(verbatim: spoken(cell)))
  }

  private func spoken(_ cell: MonthWidgetCell) -> String {
    cell.spoken(publicHoliday: SpokenWords.localized.publicHoliday)
  }
}
