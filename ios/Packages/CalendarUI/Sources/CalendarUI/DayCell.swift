import CalendarKit
import SwiftUI

/// One day cell: the Bikram Sambat numeral, the small Gregorian day, the optional tithi line, the
/// event dot and the holiday and today marks of the design system.
struct DayCell: View {
  let cell: MonthCell
  let language: AppLanguage
  let digits: Digits

  var body: some View {
    VStack(spacing: 1) {
      Text(cell.bsNumber(digits))
        .font(.title2)
        .fontWeight(.semibold)
        .monospacedDigit()
        .foregroundStyle(numeralColor)
      if let tithi = cell.tithi {
        Text(tithi.name(language))
          .font(.caption2)
          .lineLimit(1)
          .foregroundStyle(.secondary)
      }
      if cell.hasEvents {
        Circle()
          .fill(BrandColor.holiday)
          .frame(width: 4, height: 4)
      }
    }
    .frame(maxWidth: .infinity, minHeight: 44)
    .opacity(cell.isInMonth ? 1 : 0.35)
    .background(cell.isHoliday ? BrandColor.holidayFill : Color.clear)
    .overlay(alignment: .topTrailing) {
      Text(cell.adNumber(digits))
        .font(.caption2)
        .foregroundStyle(.tertiary)
        .padding(.top, 2)
        .padding(.trailing, 4)
    }
    .overlay {
      if cell.isToday {
        RoundedRectangle(cornerRadius: 8)
          .stroke(BrandColor.today, lineWidth: 2)
          .padding(2)
      }
    }
  }

  private var numeralColor: Color {
    guard cell.isInMonth else { return .secondary }
    return cell.isHoliday || cell.isSaturday ? BrandColor.holiday : Color.primary
  }
}
