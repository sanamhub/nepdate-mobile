import CalendarKit
import Foundation
import SwiftUI
import UIKit

/// The month screen (MR-01): a pager of month pages, a header that opens the month picker, the
/// previous and next buttons and a Today button that appears only off today's month.
public struct MonthScreen: View {
  @State private var model: MonthViewModel
  @State private var scrolledMonth: MonthKey?
  @State private var showsPicker = false

  /// Creates the screen.
  /// - Parameter model: The month to open on and the settings to draw it with.
  public init(model: MonthViewModel) {
    _model = State(initialValue: model)
    _scrolledMonth = State(initialValue: model.month)
  }

  /// The screen's content.
  public var body: some View {
    VStack(spacing: 0) {
      MonthHeader(
        model: model,
        onPrevious: { go(to: previousMonth) },
        onNext: { go(to: nextMonth) },
        onToday: { go(to: todayMonth) },
        onChoose: { showsPicker = true }
      )
      ScrollView(.horizontal) {
        LazyHStack(spacing: 0) {
          ForEach(MonthKey.all) { key in
            MonthPageView(
              key: key,
              today: model.today,
              showTithi: model.showTithi,
              language: model.language,
              digits: model.digits,
              selected: model.selected,
              onSelect: { date in
                guard key == model.month else { return }
                model.select(date)
              }
            )
            .containerRelativeFrame([.horizontal, .vertical])
            .id(key)
          }
        }
        .scrollTargetLayout()
      }
      .scrollTargetBehavior(.paging)
      .scrollPosition(id: $scrolledMonth)
      .scrollIndicators(.hidden)
      .onChange(of: scrolledMonth) { _, newValue in
        guard let newValue, newValue != model.month else { return }
        model.go(to: newValue)
      }
    }
    .background(Color(uiColor: .systemBackground))
    .sheet(isPresented: $showsPicker) {
      MonthPickerSheet(
        initial: model.month,
        language: model.language,
        digits: model.digits,
        years: model.pickerYears,
        onPick: { go(to: $0) }
      )
    }
  }

  private var previousMonth: MonthKey? {
    MonthKey(id: model.month.id - 1)
  }

  private var nextMonth: MonthKey? {
    MonthKey(id: model.month.id + 1)
  }

  private var todayMonth: MonthKey? {
    MonthKey(monthOf: model.today)
  }

  private func go(to key: MonthKey?) {
    guard let key, key != model.month else { return }
    model.go(to: key)
    withAnimation(.snappy) {
      scrolledMonth = key
    }
  }
}

/// The header and one month page, without the pager. The snapshot tests render this, because a
/// pager container adds no pixels of its own and an off-screen `ScrollView` cannot be positioned
/// reliably in a test.
public struct MonthScreenContent: View {
  private let model: MonthViewModel

  /// Creates the content.
  /// - Parameter model: The month to draw and the settings to draw it with.
  public init(model: MonthViewModel) {
    self.model = model
  }

  /// The content.
  public var body: some View {
    VStack(spacing: 0) {
      MonthHeader(
        model: model,
        onPrevious: {},
        onNext: {},
        onToday: {},
        onChoose: {}
      )
      MonthPageView(
        key: model.month,
        today: model.today,
        showTithi: model.showTithi,
        language: model.language,
        digits: model.digits,
        selected: model.selected,
        onSelect: { _ in }
      )
      .frame(maxHeight: .infinity)
    }
    .background(Color(uiColor: .systemBackground))
  }
}

private struct MonthHeader: View {
  let model: MonthViewModel
  let onPrevious: () -> Void
  let onNext: () -> Void
  let onToday: () -> Void
  let onChoose: () -> Void

  @Environment(\.dynamicTypeSize) private var dynamicTypeSize

  var body: some View {
    Group {
      if dynamicTypeSize.isAccessibilitySize {
        VStack(alignment: .leading, spacing: 0) {
          title
          navigation
        }
      } else {
        HStack(alignment: .firstTextBaseline, spacing: 4) {
          title
          Spacer(minLength: 4)
          navigation
        }
      }
    }
    .padding(.horizontal, 16)
    .padding(.top, 8)
    .padding(.bottom, 4)
  }

  private var title: some View {
    Button(action: onChoose) {
      VStack(alignment: .leading, spacing: 2) {
        Text(model.monthTitle)
          .font(.largeTitle)
          .fontWeight(.bold)
          .foregroundStyle(.primary)
          .fixedSize(horizontal: false, vertical: true)
        Text(model.adSpan)
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)
      }
      .frame(minHeight: 44, alignment: .leading)
      .contentShape(Rectangle())
    }
    .buttonStyle(.plain)
    .accessibilityLabel(Text("Choose month and year"))
    .accessibilityValue(Text(model.monthTitle))
  }

  private var navigation: some View {
    HStack(spacing: 0) {
      Button(action: onPrevious) {
        Image(systemName: "chevron.left")
          .font(.body.weight(.semibold))
          .frame(minWidth: 44, minHeight: 44)
          .contentShape(Rectangle())
      }
      .disabled(!model.canGoPrevious)
      .accessibilityLabel(Text("Previous month"))

      if model.showsTodayButton {
        Button(action: onToday) {
          Text("Today")
            .font(.subheadline)
            .fontWeight(.semibold)
            .foregroundStyle(BrandColor.today)
            .frame(minWidth: 44, minHeight: 44)
            .contentShape(Rectangle())
        }
        .accessibilityLabel(Text("Today"))
      }

      Button(action: onNext) {
        Image(systemName: "chevron.right")
          .font(.body.weight(.semibold))
          .frame(minWidth: 44, minHeight: 44)
          .contentShape(Rectangle())
      }
      .disabled(!model.canGoNext)
      .accessibilityLabel(Text("Next month"))
    }
    .buttonStyle(.plain)
  }
}

private struct MonthPageView: View {
  let key: MonthKey
  let today: MonthDate
  let showTithi: Bool
  let language: AppLanguage
  let digits: Digits
  let selected: MonthDate?
  let onSelect: (MonthDate) -> Void

  @Environment(\.dynamicTypeSize) private var dynamicTypeSize

  var body: some View {
    if let layout = MonthLayout(
      year: key.year,
      month: key.month,
      showTithi: showTithi,
      today: today
    ) {
      Group {
        if dynamicTypeSize.isAccessibilitySize {
          MonthList(
            layout: layout,
            language: language,
            digits: digits,
            selected: selected,
            onSelect: onSelect
          )
        } else {
          VStack(spacing: 0) {
            MonthWeekdayRow(language: language)
            MonthGrid(
              layout: layout,
              language: language,
              digits: digits,
              selected: selected,
              onSelect: onSelect
            )
          }
        }
      }
      .frame(maxWidth: .infinity, maxHeight: .infinity)
    } else {
      Color.clear
    }
  }
}

private struct MonthWeekdayRow: View {
  let language: AppLanguage

  var body: some View {
    HStack(spacing: 0) {
      let names = MonthLayout.weekdayNames(language)
      ForEach(names.indices, id: \.self) { index in
        Text(names[index])
          .font(.caption)
          .fontWeight(.medium)
          .foregroundStyle(index == 6 ? BrandColor.holiday : BrandColor.secondaryText)
          .lineLimit(1)
          .minimumScaleFactor(0.6)
          .frame(maxWidth: .infinity)
          .padding(.vertical, 4)
      }
    }
    .accessibilityHidden(true)
  }
}

private struct MonthGrid: View {
  let layout: MonthLayout
  let language: AppLanguage
  let digits: Digits
  let selected: MonthDate?
  let onSelect: (MonthDate) -> Void

  private let publicHoliday = String(localized: "public holiday")
  private let todayLabel = String(localized: "Today")

  var body: some View {
    VStack(spacing: 0) {
      ForEach(0..<layout.rowCount, id: \.self) { row in
        HStack(spacing: 0) {
          ForEach(0..<7, id: \.self) { column in
            let cell = layout.cells[row * 7 + column]
            if cell.date == nil {
              Color.clear
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
              DayCell(
                cell: cell,
                language: language,
                digits: digits,
                isSelected: cell.date == selected,
                label: layout.accessibilityLabel(
                  for: cell,
                  language: language,
                  digits: digits,
                  publicHoliday: publicHoliday
                ),
                todayLabel: todayLabel,
                onSelect: onSelect
              )
              .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
          }
        }
        .frame(maxHeight: .infinity)

        if row < layout.rowCount - 1 {
          Rectangle()
            .fill(Color.secondary.opacity(0.2))
            .frame(height: 0.5)
            .accessibilityHidden(true)
        }
      }
    }
    .frame(maxHeight: .infinity)
  }
}

private struct DayCell: View {
  let cell: MonthCell
  let language: AppLanguage
  let digits: Digits
  let isSelected: Bool
  let label: String
  let todayLabel: String
  let onSelect: (MonthDate) -> Void

  var body: some View {
    if cell.isSelectable, let date = cell.date {
      Button {
        onSelect(date)
      } label: {
        content
      }
      .buttonStyle(.plain)
      .accessibilityElement(children: .ignore)
      .accessibilityLabel(Text(label))
      .accessibilityValue(cell.isToday ? Text(todayLabel) : Text(""))
      .accessibilityAddTraits(.isButton)
      .accessibilityAddTraits(isSelected ? .isSelected : [])
    } else {
      content
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(label))
        .accessibilityValue(cell.isToday ? Text(todayLabel) : Text(""))
    }
  }

  private var content: some View {
    VStack(spacing: 1) {
      Text(cell.bsNumber(digits))
        .font(.title2)
        .fontWeight(.semibold)
        .monospacedDigit()
        .foregroundStyle(numeralColor)
        .lineLimit(1)
        .minimumScaleFactor(0.7)
      if isSelected && cell.isHoliday {
        Rectangle()
          .fill(BrandColor.holiday)
          .frame(width: 18, height: 2)
      }
      if let tithi = cell.tithi {
        Text(tithi.name(language))
          .font(.caption2)
          .multilineTextAlignment(.center)
          .foregroundStyle(BrandColor.secondaryText)
          .lineLimit(2)
          .minimumScaleFactor(0.8)
          .fixedSize(horizontal: false, vertical: true)
      }
      if cell.hasEvents {
        Circle()
          .fill(cell.isHoliday || cell.isSaturday ? BrandColor.holiday : BrandColor.secondaryText)
          .frame(width: 4, height: 4)
      }
    }
    .padding(2)
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .background {
      RoundedRectangle(cornerRadius: 8, style: .continuous)
        .fill(background)
        .padding(2)
    }
    .overlay(alignment: .topTrailing) {
      Text(cell.adNumber(digits))
        .font(.caption2)
        .foregroundStyle(BrandColor.secondaryText)
        .padding(.top, 2)
        .padding(.trailing, 4)
    }
    .overlay {
      if cell.isToday {
        RoundedRectangle(cornerRadius: 8, style: .continuous)
          .stroke(BrandColor.today, lineWidth: 2)
          .padding(2)
      }
    }
    .contentShape(Rectangle())
  }

  private var background: Color {
    if isSelected { return BrandColor.selection }
    if cell.isHoliday { return BrandColor.holidayFill }
    return .clear
  }

  private var numeralColor: Color {
    guard cell.isInMonth else { return BrandColor.secondaryText }
    return cell.isHoliday || cell.isSaturday ? BrandColor.holiday : .primary
  }
}

private struct MonthList: View {
  let layout: MonthLayout
  let language: AppLanguage
  let digits: Digits
  let selected: MonthDate?
  let onSelect: (MonthDate) -> Void

  private let publicHoliday = String(localized: "public holiday")
  private let todayLabel = String(localized: "Today")

  var body: some View {
    ScrollView {
      LazyVStack(spacing: 0) {
        ForEach(Array(layout.cells.filter(\.isInMonth).enumerated()), id: \.offset) { item in
          let cell = item.element
          if let date = cell.date {
            MonthListRow(
              cell: cell,
              date: date,
              language: language,
              digits: digits,
              isSelected: date == selected,
              label: layout.accessibilityLabel(
                for: cell,
                language: language,
                digits: digits,
                publicHoliday: publicHoliday
              ),
              todayLabel: todayLabel,
              onSelect: onSelect
            )
          }
        }
      }
      .padding(.horizontal, 16)
      .padding(.bottom, 16)
    }
  }
}

private struct MonthListRow: View {
  let cell: MonthCell
  let date: MonthDate
  let language: AppLanguage
  let digits: Digits
  let isSelected: Bool
  let label: String
  let todayLabel: String
  let onSelect: (MonthDate) -> Void

  var body: some View {
    Button {
      onSelect(date)
    } label: {
      VStack(alignment: .leading, spacing: 3) {
        HStack(alignment: .firstTextBaseline) {
          Text(verbatim: bsDate)
            .font(.title3)
            .fontWeight(.semibold)
          Spacer(minLength: 8)
          if cell.isToday {
            Text(todayLabel)
              .font(.caption)
              .fontWeight(.semibold)
              .foregroundStyle(BrandColor.today)
          }
        }
        Text(verbatim: adLine)
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)
        if cell.isHoliday {
          Text("Public holiday")
            .font(.subheadline)
            .foregroundStyle(BrandColor.secondaryText)
        }
        if let tithi = cell.tithi {
          Text(verbatim: tithi.name(language))
            .font(.subheadline)
            .foregroundStyle(BrandColor.secondaryText)
        }
        if let event = cell.events.first {
          Text(verbatim: digits.applying(to: event.name(language)))
            .font(.subheadline)
            .foregroundStyle(BrandColor.secondaryText)
        }
      }
      .frame(maxWidth: .infinity, alignment: .leading)
      .padding(.horizontal, 8)
      .padding(.vertical, 10)
      .background(background, in: RoundedRectangle(cornerRadius: 8, style: .continuous))
      .contentShape(Rectangle())
    }
    .buttonStyle(.plain)
    .accessibilityElement(children: .ignore)
    .accessibilityLabel(Text(label))
    .accessibilityValue(cell.isToday ? Text(todayLabel) : Text(""))
    .accessibilityAddTraits(.isButton)
    .accessibilityAddTraits(isSelected ? .isSelected : [])
  }

  private var background: Color {
    if isSelected { return BrandColor.selection }
    if cell.isHoliday { return BrandColor.holidayFill }
    return .clear
  }

  private var bsDate: String {
    "\(MonthNames.bs(date.month, language)) \(digits.format(date.day)), \(digits.format(date.year))"
  }

  private var adLine: String {
    let day = digits.format(cell.adDay)
    let month = MonthNames.ad(cell.adMonth, language)
    let year = digits.format(cell.adYear)
    return "\(cell.weekdayName(language)) · \(day) \(month) \(year)"
  }
}

private struct MonthPickerSheet: View {
  @State private var month: Int
  @State private var year: Int
  let language: AppLanguage
  let digits: Digits
  let years: ClosedRange<Int>
  let onPick: (MonthKey) -> Void
  @Environment(\.dismiss) private var dismiss

  init(
    initial: MonthKey,
    language: AppLanguage,
    digits: Digits,
    years: ClosedRange<Int>,
    onPick: @escaping (MonthKey) -> Void
  ) {
    _month = State(initialValue: initial.month)
    _year = State(initialValue: initial.year)
    self.language = language
    self.digits = digits
    self.years = years
    self.onPick = onPick
  }

  var body: some View {
    NavigationStack {
      HStack(spacing: 0) {
        Picker("Month", selection: $month) {
          ForEach(1...12, id: \.self) { value in
            Text(MonthNames.bs(value, language)).tag(value)
          }
        }
        .pickerStyle(.wheel)
        .frame(maxWidth: .infinity)
        .accessibilityLabel(Text("Month"))

        Picker("Year", selection: $year) {
          ForEach(years, id: \.self) { value in
            Text(digits.format(value)).tag(value)
          }
        }
        .pickerStyle(.wheel)
        .frame(maxWidth: .infinity)
        .accessibilityLabel(Text("Year"))
      }
      .navigationTitle("Choose month and year")
      .navigationBarTitleDisplayMode(.inline)
      .toolbar {
        ToolbarItem(placement: .cancellationAction) {
          Button("Cancel") {
            dismiss()
          }
        }
        ToolbarItem(placement: .confirmationAction) {
          Button("Go") {
            if let key = MonthKey(year: year, month: month) {
              onPick(key)
            }
            dismiss()
          }
        }
      }
    }
    .presentationDetents([.medium, .large])
    .presentationDragIndicator(.visible)
  }
}
