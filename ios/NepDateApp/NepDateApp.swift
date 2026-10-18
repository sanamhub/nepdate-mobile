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
  private let language = AppLanguage.current

  var body: some View {
    let session = URLSession.shared
    let today = CalendarDates.today()
    let _ = session
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
