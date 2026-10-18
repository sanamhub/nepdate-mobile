import CalendarKit
import CalendarUI
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
  var body: some View {
    let settings = SettingsStore().load()
    let today = TodayProvider().today(dateFollows: settings.dateFollows)
    let language = settings.language.resolved
    if let layout = MonthLayout(
      year: today.year,
      month: today.month,
      showTithi: settings.showTithi,
      today: today
    ) {
      MonthScreen(layout: layout, language: language)
    } else {
      Text("No date")
    }
  }
}
