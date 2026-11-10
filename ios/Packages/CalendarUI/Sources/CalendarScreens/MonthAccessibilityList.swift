import CalendarKit
import CalendarUI
import SwiftUI

/// The month as a list above the largest text size that fits seven columns (MR-10 AC2).
///
/// It is the month screen's accessibility layout: every day of the month, one row each, with the
/// same spoken label as its grid cell.
struct MonthAccessibilityList: View {
  let layout: MonthLayout
  let language: AppLanguage
  let digits: Digits
  let selected: MonthDate?
  let onSelect: (MonthDate) -> Void

  private var publicHoliday: String {
    AppText.string("public holiday", language)
  }

  private var todayLabel: String {
    AppText.string("Today", language)
  }

  var body: some View {
    ScrollView {
      LazyVStack(spacing: 0) {
        ForEach(Array(layout.cells.filter(\.isInMonth).enumerated()), id: \.offset) { item in
          let cell = item.element
          if let date = cell.date {
            row(cell, date: date)
          }
        }
      }
      .padding(.horizontal, 16)
      .padding(.bottom, 16)
    }
  }

  private func row(_ cell: MonthCell, date: MonthDate) -> some View {
    Button {
      onSelect(date)
    } label: {
      VStack(alignment: .leading, spacing: 3) {
        HStack(alignment: .firstTextBaseline) {
          Text(verbatim: bsDate(date))
            .font(.title3)
            .fontWeight(.semibold)
          Spacer(minLength: 8)
          if cell.isToday {
            Text(verbatim: todayLabel)
              .font(.caption)
              .fontWeight(.semibold)
              .foregroundStyle(BrandColor.accent)
          }
        }
        Text(verbatim: adLine(cell))
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)
        if cell.isHoliday {
          Text(verbatim: publicHoliday)
            .font(.subheadline)
            .foregroundStyle(BrandColor.secondaryText)
        }
        if let tithi = cell.tithi {
          Text(verbatim: digits.applying(to: tithi.name(language)))
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
      .background(background(for: cell), in: RoundedRectangle(cornerRadius: 8, style: .continuous))
      .contentShape(Rectangle())
      .accessibilityHidden(true)
    }
    .buttonStyle(.plain)
    .accessibilityElement(children: .ignore)
    .accessibilityLabel(
      Text(
        layout.accessibilityLabel(
          for: cell,
          language: language,
          digits: digits,
          publicHoliday: publicHoliday
        )
      )
    )
    .accessibilityValue(cell.isToday ? Text(todayLabel) : Text(""))
    .accessibilityAddTraits(.isButton)
    .accessibilityAddTraits(date == selected ? .isSelected : [])
  }

  private func background(for cell: MonthCell) -> Color {
    if cell.date == selected { return BrandColor.selection }
    if cell.isHoliday { return BrandColor.holidayFill }
    return .clear
  }

  private func bsDate(_ date: MonthDate) -> String {
    "\(MonthNames.bs(date.month, language)) \(digits.format(date.day)), "
      + "\(digits.format(date.year))"
  }

  private func adLine(_ cell: MonthCell) -> String {
    let day = digits.format(cell.adDay)
    let month = MonthNames.ad(cell.adMonth, language)
    let year = digits.format(cell.adYear)
    return "\(cell.weekdayName(language)) · \(day) \(month) \(year)"
  }
}
