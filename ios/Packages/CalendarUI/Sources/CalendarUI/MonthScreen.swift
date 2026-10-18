import CalendarKit
import SwiftUI
import UIKit

/// The month screen: the month title, the weekday row and the grid of days.
public struct MonthScreen: View {
  private let grid: MonthGrid
  private let language: AppLanguage

  /// Creates the screen.
  /// - Parameters:
  ///   - grid: The month to draw.
  ///   - language: The language of names and digits.
  public init(grid: MonthGrid, language: AppLanguage) {
    self.grid = grid
    self.language = language
  }

  /// The screen's content.
  public var body: some View {
    VStack(alignment: .leading, spacing: 12) {
      HStack(alignment: .firstTextBaseline) {
        Text(grid.title)
          .font(.largeTitle)
          .fontWeight(.bold)
        Spacer(minLength: 8)
        Text("Today")
          .font(.headline)
          .foregroundStyle(BrandColor.today)
      }
      LazyVGrid(columns: Self.columns, spacing: 0) {
        ForEach(grid.weekdayNames.indices, id: \.self) { index in
          Text(grid.weekdayNames[index])
            .font(.caption)
            .fontWeight(.medium)
            .foregroundStyle(index == 6 ? BrandColor.holiday : Color.secondary)
            .frame(maxWidth: .infinity)
            .padding(.bottom, 6)
        }
        ForEach(grid.cells.indices, id: \.self) { index in
          if let day = grid.cells[index] {
            DayCell(day: day, language: language)
          } else {
            Color.clear
              .frame(height: 44)
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
