import CalendarKit
import SwiftUI
import UIKit

/// The month screen: the month title, the weekday row and the grid of days.
public struct MonthScreen: View {
  private let layout: MonthLayout
  private let language: AppLanguage
  private let digits: Digits

  /// Creates the screen.
  /// - Parameters:
  ///   - layout: The month to draw.
  ///   - language: The language of the names.
  ///   - digits: The resolved Digits setting.
  public init(layout: MonthLayout, language: AppLanguage, digits: Digits) {
    self.layout = layout
    self.language = language
    self.digits = digits
  }

  /// The screen's content.
  public var body: some View {
    let weekdays = MonthLayout.weekdayNames(language)
    return VStack(alignment: .leading, spacing: 12) {
      HStack(alignment: .firstTextBaseline) {
        Text(layout.title(language, digits: digits))
          .font(.largeTitle)
          .fontWeight(.bold)
        Spacer(minLength: 8)
        Text("Today")
          .font(.headline)
          .foregroundStyle(BrandColor.today)
      }
      LazyVGrid(columns: Self.columns, spacing: 0) {
        ForEach(weekdays.indices, id: \.self) { index in
          Text(weekdays[index])
            .font(.caption)
            .fontWeight(.medium)
            .foregroundStyle(index == 6 ? BrandColor.holiday : Color.secondary)
            .frame(maxWidth: .infinity)
            .padding(.bottom, 6)
        }
        ForEach(layout.cells.indices, id: \.self) { index in
          let cell = layout.cells[index]
          if cell.date == nil {
            Color.clear
              .frame(height: 44)
          } else {
            DayCell(cell: cell, language: language, digits: digits)
          }
        }
      }
    }
    .padding()
    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    .background(Color(uiColor: .systemBackground))
  }

  private static let columns = Array(
    repeating: GridItem(.flexible(), spacing: 0, alignment: .top),
    count: 7
  )
}
