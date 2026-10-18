import CalendarKit
import SwiftUI

/// One day cell: the Bikram Sambat numeral, the small Gregorian day, and the holiday and today
/// marks of the design system.
struct DayCell: View {
  let day: MonthDay
  let language: AppLanguage

  var body: some View {
    Text(day.number(language))
      .font(.title2)
      .fontWeight(.semibold)
      .monospacedDigit()
      .foregroundStyle(day.isHoliday ? BrandColor.holiday : Color.primary)
      .frame(maxWidth: .infinity, minHeight: 44)
      .background(day.isHoliday ? BrandColor.holidayFill : Color.clear)
      .overlay(alignment: .topTrailing) {
        Text(String(day.adDay))
          .font(.caption2)
          .foregroundStyle(.tertiary)
          .padding(.top, 2)
          .padding(.trailing, 4)
      }
      .overlay {
        if day.isToday {
          RoundedRectangle(cornerRadius: 8)
            .stroke(BrandColor.today, lineWidth: 2)
            .padding(2)
        }
      }
  }
}
