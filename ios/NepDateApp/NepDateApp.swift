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
    let digits = settings.digits.resolved(for: language)
    if let model = MonthViewModel(
      today: today,
      language: language,
      digits: digits,
      showTithi: settings.showTithi
    ) {
      RootTabView(model: model)
    } else {
      Text("No date")
    }
  }
}

private struct RootTabView: View {
  let model: MonthViewModel

  var body: some View {
    TabView {
      MonthScreen(model: model)
        .tabItem {
          Label("Month", systemImage: "calendar")
        }

      PlaceholderScreen(title: "Upcoming", systemImage: "bell")
        .tabItem {
          Label("Upcoming", systemImage: "bell")
        }

      PlaceholderScreen(title: "Converter", systemImage: "arrow.left.arrow.right")
        .tabItem {
          Label("Converter", systemImage: "arrow.left.arrow.right")
        }
    }
  }
}

private struct PlaceholderScreen: View {
  let title: String
  let systemImage: String

  var body: some View {
    NavigationStack {
      ContentUnavailableView {
        Label {
          Text(verbatim: title)
        } icon: {
          Image(systemName: systemImage)
        }
      } description: {
        Text("Coming soon")
      }
      .navigationTitle(Text(title))
    }
  }
}
