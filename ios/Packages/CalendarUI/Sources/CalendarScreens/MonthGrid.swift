import CalendarKit
import CalendarUI
import SwiftUI

/// The small AD number a grid cell draws, or `nil` when the setting turns it off (MR-17).
public enum MonthCellText {
  /// The AD day to draw above the BS numeral.
  /// - Parameters:
  ///   - cell: The grid cell.
  ///   - digits: The resolved Digits setting.
  ///   - showsADDates: Whether the setting is on.
  /// - Returns: The formatted AD day, or `nil`.
  public static func adNumber(
    _ cell: MonthCell,
    digits: Digits,
    showsADDates: Bool
  ) -> String? {
    showsADDates ? cell.adNumber(digits) : nil
  }
}

/// The 7-column month grid (MR-01, MR-10): the BS numeral, the small AD day, the today mark, the
/// holiday fill, the event diamond and the optional tithi line.
///
/// The grid follows Android's `MonthGrid.kt` and `DayInk.kt`. Its column width comes from the
/// available width and its rows grow with their content, so no cell has a fixed height.
struct MonthGrid: View {
  let layout: MonthLayout
  let language: AppLanguage
  let digits: Digits
  let selected: MonthDate?
  let showTithi: Bool
  let showsADDates: Bool
  let publicHoliday: String
  let todayLabel: String
  let zoomNamespace: Namespace.ID
  let onSelect: (MonthDate) -> Void

  @Namespace private var rotorNamespace

  private let columns = Array(
    repeating: GridItem(.flexible(), spacing: 0, alignment: .top),
    count: 7
  )

  var body: some View {
    LazyVGrid(columns: columns, spacing: 2) {
      ForEach(layout.cells.indices, id: \.self) { index in
        let cell = layout.cells[index]
        if cell.date != nil {
          DayCell(
            cell: cell,
            language: language,
            digits: digits,
            isSelected: cell.date == selected,
            showsADDates: showsADDates,
            showTithi: showTithi,
            label: accessibilityLabel(cell),
            todayLabel: todayLabel,
            rotorNamespace: rotorNamespace,
            zoomNamespace: zoomNamespace,
            onSelect: onSelect
          )
        } else {
          Color.clear.frame(minHeight: 44)
        }
      }
    }
    .accessibilityRotor(Text("Holidays")) {
      ForEach(holidayDays) { day in
        AccessibilityRotorEntry(Text(verbatim: day.label), id: day.date, in: rotorNamespace)
      }
    }
    .accessibilityRotor(Text("Events")) {
      ForEach(eventDays) { day in
        AccessibilityRotorEntry(Text(verbatim: day.label), id: day.date, in: rotorNamespace)
      }
    }
  }

  private var holidayDays: [RotorDay] {
    rotorDays { $0.isHoliday }
  }

  private var eventDays: [RotorDay] {
    rotorDays { $0.hasEvents }
  }

  private func rotorDays(_ include: (MonthCell) -> Bool) -> [RotorDay] {
    layout.cells.compactMap { cell in
      guard cell.isInMonth, include(cell), let date = cell.date else { return nil }
      return RotorDay(date: date, label: accessibilityLabel(cell))
    }
  }

  private struct RotorDay: Identifiable {
    let date: MonthDate
    let label: String
    var id: MonthDate { date }
  }

  private func accessibilityLabel(_ cell: MonthCell) -> String {
    layout.accessibilityLabel(
      for: cell,
      language: language,
      digits: digits,
      publicHoliday: publicHoliday
    )
  }
}

private struct DayCell: View {
  let cell: MonthCell
  let language: AppLanguage
  let digits: Digits
  let isSelected: Bool
  let showsADDates: Bool
  let showTithi: Bool
  let label: String
  let todayLabel: String
  let rotorNamespace: Namespace.ID
  let zoomNamespace: Namespace.ID
  let onSelect: (MonthDate) -> Void

  var body: some View {
    if cell.isSelectable, let date = cell.date {
      Button {
        onSelect(date)
      } label: {
        content
          .accessibilityHidden(true)
      }
      .buttonStyle(.plain)
      .accessibilityElement(children: .ignore)
      .accessibilityLabel(Text(label))
      .accessibilityValue(cell.isToday ? Text(todayLabel) : Text(""))
      .accessibilityAddTraits(.isButton)
      .accessibilityAddTraits(isSelected ? .isSelected : [])
      .accessibilityRotorEntry(id: date, in: rotorNamespace)
      .matchedTransitionSource(id: date, in: zoomNamespace)
    } else {
      content
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(label))
        .accessibilityValue(cell.isToday ? Text(todayLabel) : Text(""))
    }
  }

  private var content: some View {
    VStack(spacing: 1) {
      if let adNumber = MonthCellText.adNumber(
        cell,
        digits: digits,
        showsADDates: showsADDates
      ) {
        Text(verbatim: adNumber)
          .font(.system(size: 10))
          .foregroundStyle(secondaryInk)
          .lineLimit(1)
          .accessibilityHidden(true)
      }

      ZStack {
        if cell.isToday {
          SunnyShape()
            .fill(BrandColor.today)
        }
        Text(verbatim: cell.bsNumber(digits))
          .font(BrandType.cellNumeral)
          .fontWeight(cell.isToday ? .bold : .semibold)
          .foregroundStyle(cell.isToday ? BrandColor.onToday : primaryInk)
          .lineLimit(1)
          .minimumScaleFactor(0.7)
          .monospacedDigit()
      }
      .frame(minWidth: 36, minHeight: 36)
      .overlay(alignment: .bottom) {
        if isSelected && cell.isHoliday {
          Rectangle()
            .fill(BrandColor.holiday)
            .frame(width: 18, height: 2)
            .padding(.bottom, 2)
        }
      }

      ZStack {
        if cell.hasEvents {
          DiamondShape()
            .fill(eventInk)
            .frame(width: 5, height: 6)
        }
      }
      .frame(height: 6)

      if showTithi, let tithi = cell.tithi {
        Text(verbatim: digits.applying(to: tithi.name(language)))
          .font(.caption2)
          .multilineTextAlignment(.center)
          .foregroundStyle(secondaryInk)
          .lineLimit(2)
          .minimumScaleFactor(0.7)
          .fixedSize(horizontal: false, vertical: true)
      }
    }
    .padding(.horizontal, 1)
    .padding(.vertical, 3)
    .frame(maxWidth: .infinity)
    .background {
      RoundedRectangle(cornerRadius: cell.isHoliday && !isSelected ? 16 : 8, style: .continuous)
        .fill(fill)
        .padding(1)
    }
    .contentShape(Rectangle())
  }

  private var fill: Color {
    if isSelected { return BrandColor.selection }
    if cell.isHoliday { return BrandColor.holidayFill }
    return .clear
  }

  private var primaryInk: Color {
    guard cell.isInMonth else { return BrandColor.secondaryText }
    return cell.isHoliday || cell.isSaturday ? BrandColor.holiday : .primary
  }

  private var secondaryInk: Color {
    guard cell.isInMonth else { return BrandColor.secondaryText }
    return cell.isHoliday || cell.isSaturday ? BrandColor.holiday : BrandColor.secondaryText
  }

  private var eventInk: Color {
    guard cell.isInMonth else { return BrandColor.secondaryText }
    return cell.isHoliday || cell.isSaturday ? BrandColor.holiday : BrandColor.secondaryText
  }
}

/// The weekday headings, Sunday first (MR-01).
struct MonthWeekdayRow: View {
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
      }
    }
    .padding(.vertical, 2)
    .accessibilityHidden(true)
  }
}

/// The days of the shown month that have a public holiday or an event (MR-23).
struct MonthDayList: View {
  let cells: [MonthCell]
  let language: AppLanguage
  let digits: Digits
  let publicHoliday: String
  let onSelect: (MonthDate) -> Void

  var body: some View {
    VStack(alignment: .leading, spacing: 0) {
      Text("Holidays and events")
        .font(.subheadline.weight(.semibold))
        .foregroundStyle(BrandColor.secondaryText)
        .padding(.horizontal, 4)
        .padding(.top, 8)
        .padding(.bottom, 2)
        .accessibilityAddTraits(.isHeader)
        // The heading is text: nothing inside it is a control and its 144 by 18 point frame is not
        // a tap target. Saying so keeps the hit region audit off it, which otherwise reports it as
        // too small to interact with (runs 42966820203, 42995574157 and 43009488402). The header
        // trait above stays, so VoiceOver still reads it as a heading.
        .accessibilityRespondsToUserInteraction(false)

      ForEach(cells, id: \.date) { cell in
        if let date = cell.date {
          Button {
            onSelect(date)
          } label: {
            row(cell)
          }
          .buttonStyle(.plain)
          .accessibilityElement(children: .ignore)
          .accessibilityLabel(Text(verbatim: spoken(cell)))
          .accessibilityAddTraits(.isButton)
        }
      }
    }
    .padding(.horizontal, 4)
  }

  private func row(_ cell: MonthCell) -> some View {
    HStack(alignment: .top, spacing: 10) {
      VStack(alignment: .leading, spacing: 1) {
        Text(verbatim: dateLine(cell))
          .font(.subheadline.weight(.semibold))
          .foregroundStyle(cell.isHoliday ? BrandColor.holiday : .primary)
        Text(verbatim: cell.weekdayName(language))
          .font(.caption)
          .foregroundStyle(BrandColor.secondaryText)
      }
      .frame(width: 88, alignment: .leading)

      VStack(alignment: .leading, spacing: 2) {
        if cell.isHoliday {
          Text(verbatim: publicHoliday)
            .font(.subheadline)
            .foregroundStyle(BrandColor.holiday)
        }
        ForEach(Array(cell.events.enumerated()), id: \.offset) { event in
          Text(verbatim: digits.applying(to: event.element.name(language)))
            .font(.subheadline)
            .foregroundStyle(.primary)
            .fixedSize(horizontal: false, vertical: true)
        }
      }
      .frame(maxWidth: .infinity, alignment: .leading)
    }
    .frame(minHeight: 44)
    .padding(.vertical, 4)
    .contentShape(Rectangle())
  }

  private func dateLine(_ cell: MonthCell) -> String {
    "\(MonthNames.bs(cell.date?.month ?? 1, language)) \(digits.format(cell.bsDay))"
  }

  private func spoken(_ cell: MonthCell) -> String {
    var parts = [dateLine(cell), cell.weekdayName(language)]
    if cell.isHoliday { parts.append(publicHoliday) }
    for event in cell.events {
      parts.append(digits.applying(to: event.name(language)))
    }
    return parts.joined(separator: ", ")
  }
}
