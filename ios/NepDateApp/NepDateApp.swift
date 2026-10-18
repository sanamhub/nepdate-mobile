import CalendarKit
import CalendarScreens
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
    let today = Self.testToday ?? TodayProvider().today(dateFollows: settings.dateFollows)
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

  /// The UI tests pin today with `NEPDATE_UI_TEST_TODAY=2083-06-20`, so the audit sees the same
  /// month on every run. Debug builds only; a release build always uses the clock.
  private static var testToday: MonthDate? {
    #if DEBUG
      guard let value = ProcessInfo.processInfo.environment["NEPDATE_UI_TEST_TODAY"] else {
        return nil
      }
      let parts = value.split(separator: "-").compactMap { Int($0) }
      guard parts.count == 3 else { return nil }
      return MonthDate(year: parts[0], month: parts[1], day: parts[2])
    #else
      return nil
    #endif
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
