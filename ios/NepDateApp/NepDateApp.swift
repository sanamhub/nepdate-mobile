import CalendarKit
import CalendarUI
import Foundation
import SwiftUI

@main
struct NepDateApp: App {
  var body: some Scene {
    WindowGroup {
      RootView()
    }
  }
}

private struct RootView: View {
  private let language = AppLanguage.preferred(from: Locale.preferredLanguages)

  var body: some View {
    let today = CalendarDates.today()
    if let grid = MonthGrid(
      year: today.year,
      month: today.month,
      language: language,
      today: today
    ) {
      MonthScreen(grid: grid, language: language)
    } else {
      Text("No date")
    }
  }
}
