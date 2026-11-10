import CalendarKit
import CalendarUI
import SwiftUI

/// The year view (MR-14): twelve mini months with a holiday-count badge and a legend, the two
/// year buttons, and a month tap that returns to that month.
///
/// At accessibility text sizes the mini months become a list of months with their holiday counts.
public struct YearScreen: View {
  @State private var model: YearViewModel
  @ScaledMetric(relativeTo: .caption2) private var miniDayHeight: CGFloat = 14
  private let onPick: (MonthKey) -> Void
  @Environment(\.dynamicTypeSize) private var dynamicTypeSize
  @Environment(\.dismiss) private var dismiss

  /// Creates the screen.
  /// - Parameters:
  ///   - model: The year to show.
  ///   - onPick: Called with the month to open; the screen dismisses itself first.
  public init(model: YearViewModel, onPick: @escaping (MonthKey) -> Void) {
    _model = State(initialValue: model)
    self.onPick = onPick
  }

  /// The screen's content.
  public var body: some View {
    Group {
      if dynamicTypeSize.isAccessibilitySize {
        monthList
      } else {
        monthGrid
      }
    }
    .navigationTitle(model.title)
    .navigationBarTitleDisplayMode(.inline)
    .toolbar {
      ToolbarItem(placement: .topBarLeading) {
        Button(action: showPreviousYear) {
          Image(systemName: "chevron.left")
        }
        .disabled(!model.canGoPrevious)
        .accessibilityLabel(Text("Previous year"))
      }
      ToolbarItem(placement: .topBarTrailing) {
        Button(action: showNextYear) {
          Image(systemName: "chevron.right")
        }
        .disabled(!model.canGoNext)
        .accessibilityLabel(Text("Next year"))
      }
    }
    .background(Color(uiColor: .systemBackground))
    .accessibilityIdentifier("year_view")
  }

  private var monthGrid: some View {
    ScrollView {
      VStack(alignment: .leading, spacing: 14) {
        legend
        LazyVGrid(
          columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: 3),
          spacing: 14
        ) {
          ForEach(model.months) { month in
            Button {
              pick(month.key)
            } label: {
              miniMonth(month)
            }
            .buttonStyle(.plain)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(Text(verbatim: month.spokenLabel))
            .accessibilityAddTraits(.isButton)
          }
        }
      }
      .padding(.horizontal, 16)
      .padding(.bottom, 24)
    }
  }

  private var monthList: some View {
    List {
      Section {
        legend
          .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
      }
      Section {
        ForEach(model.months) { month in
          Button {
            pick(month.key)
          } label: {
            HStack {
              Text(verbatim: "\(month.name) \(digitsYear)")
                .font(.headline)
              Spacer(minLength: 8)
              Text(verbatim: countText(month.holidayCount))
                .font(.subheadline)
                .foregroundStyle(BrandColor.secondaryText)
            }
            .frame(minHeight: 44)
            .contentShape(Rectangle())
          }
          .buttonStyle(.plain)
          .foregroundStyle(.primary)
          .accessibilityLabel(Text(verbatim: month.spokenLabel))
        }
      }
    }
    .listStyle(.plain)
  }

  private var legend: some View {
    FlowLayout(horizontalSpacing: 14, verticalSpacing: 6) {
      legendItem(text: "Today") {
        SunnyShape()
          .fill(BrandColor.today)
          .frame(width: 14, height: 14)
      }
      legendItem(text: "Public holiday") {
        RoundedRectangle(cornerRadius: 4, style: .continuous)
          .fill(BrandColor.holidayFill)
          .frame(width: 14, height: 14)
      }
      legendItem(text: "Saturday") {
        Text(verbatim: "7")
          .font(.caption2.weight(.semibold))
          .foregroundStyle(BrandColor.holiday)
          .frame(width: 14, height: 14)
      }
    }
    .accessibilityElement(children: .combine)
  }

  private func legendItem<Mark: View>(
    text: LocalizedStringKey,
    @ViewBuilder mark: () -> Mark
  ) -> some View {
    HStack(spacing: 5) {
      mark()
      Text(text)
        .font(.caption)
        .foregroundStyle(BrandColor.secondaryText)
    }
  }

  private func miniMonth(_ month: YearMonthSummary) -> some View {
    VStack(alignment: .leading, spacing: 4) {
      HStack(spacing: 4) {
        Text(verbatim: month.name)
          .font(.subheadline.weight(.semibold))
          .lineLimit(1)
          .minimumScaleFactor(0.7)
        Spacer(minLength: 2)
        Text(verbatim: model.digits.format(month.holidayCount))
          .font(.caption2.weight(.semibold))
          .foregroundStyle(BrandColor.onHolidayContainer)
          .padding(.horizontal, 5)
          .padding(.vertical, 1)
          .background(BrandColor.holidayContainer, in: Capsule())
      }

      LazyVGrid(
        columns: Array(repeating: GridItem(.flexible(), spacing: 0), count: 7),
        spacing: 1
      ) {
        ForEach(month.cells.indices, id: \.self) { index in
          let cell = month.cells[index]
          if cell.isInMonth, let date = cell.date {
            miniDay(cell, date: date)
          } else {
            Color.clear.frame(height: miniDayHeight)
          }
        }
      }
    }
    .padding(8)
    .background(
      RoundedRectangle(cornerRadius: 12, style: .continuous)
        .fill(Color(uiColor: .secondarySystemBackground))
    )
    .contentShape(Rectangle())
  }

  private func miniDay(_ cell: MonthCell, date: MonthDate) -> some View {
    ZStack {
      if cell.isToday {
        SunnyShape().fill(BrandColor.today)
      } else if cell.isHoliday {
        RoundedRectangle(cornerRadius: 3, style: .continuous)
          .fill(BrandColor.holidayFill)
      }
      Text(verbatim: model.digits.format(date.day))
        .font(.caption2)
        .foregroundStyle(ink(cell))
        .lineLimit(1)
        .minimumScaleFactor(0.6)
    }
    .frame(height: miniDayHeight)
    .accessibilityHidden(true)
  }

  private func ink(_ cell: MonthCell) -> Color {
    if cell.isToday { return BrandColor.onToday }
    return cell.isHoliday || cell.isSaturday ? BrandColor.holiday : .primary
  }

  private var digitsYear: String {
    model.digits.format(model.year)
  }

  private func countText(_ count: Int) -> String {
    let word =
      count == 1
      ? AppText.string("public holiday", model.language)
      : AppText.string("public holidays", model.language)
    return "\(model.digits.format(count)) \(word)"
  }

  private func showPreviousYear() {
    if let previous = model.previous { model = previous }
  }

  private func showNextYear() {
    if let next = model.next { model = next }
  }

  private func pick(_ key: MonthKey) {
    dismiss()
    onPick(key)
  }
}
