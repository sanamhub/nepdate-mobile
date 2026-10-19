import CalendarKit
import CalendarScreens
import SwiftUI
import UIKit

@main
struct NepDateApp: App {
  var body: some Scene {
    WindowGroup {
      RootView()
    }
  }
}

private struct RootView: View {
  @State private var settings: Settings
  @State private var showsSettings = false

  init() {
    _settings = State(initialValue: SettingsStore().load())
  }

  var body: some View {
    let store = SettingsStore()
    let today = Self.testToday ?? TodayProvider().today(dateFollows: settings.dateFollows)
    let language = settings.language.resolved
    let digits = settings.digits.resolved(for: language)
    let suggestion = SettingsViewModel(settings: settings, store: store)
    let info = AboutInfo(
      appVersion: Self.appVersion,
      libraryVersion: Self.libraryVersion,
      osVersion: UIDevice.current.systemVersion
    )

    Group {
      if let screen = Self.testScreen {
        TestScreenView(
          screen: screen,
          settings: settings,
          today: today,
          info: info
        )
      } else {
        VStack(spacing: 0) {
          if Self.testToday == nil && suggestion.shouldSuggestNepalTime() {
            NepalTimeSuggestionView(
              onAccept: {
                var model = SettingsViewModel(settings: settings, store: store)
                model.acceptNepalTime()
                settings = model.settings
              },
              onDismiss: {
                var model = SettingsViewModel(settings: settings, store: store)
                model.dismissNepalTime()
                settings = model.settings
              }
            )
          }

          RootTabView(
            settings: settings,
            today: today,
            onSettings: { showsSettings = true }
          )
        }
      }
    }
    .background(Color(uiColor: .systemBackground))
    .sheet(isPresented: $showsSettings) {
      SettingsScreen(
        model: SettingsViewModel(settings: settings, store: store),
        info: info,
        onChanged: { settings = $0 }
      )
    }
  }

  private static var appVersion: String {
    let version = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String
    let build = Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String
    return "\(version ?? "?") (\(build ?? "?"))"
  }

  private static var libraryVersion: String {
    Bundle.main.object(forInfoDictionaryKey: "NepDateLibraryVersion") as? String ?? "unknown"
  }

  private static var testScreen: String? {
    #if DEBUG
      return ProcessInfo.processInfo.environment["NEPDATE_UI_TEST_SCREEN"]
    #else
      return nil
    #endif
  }

  /// The UI tests pin today with `NEPDATE_UI_TEST_TODAY=2083-06-20`, so every UI test sees the same
  /// month. Debug builds only; a release build always uses the clock.
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
  let settings: Settings
  let today: MonthDate
  let onSettings: () -> Void

  var body: some View {
    let language = settings.language.resolved
    let digits = settings.digits.resolved(for: language)

    TabView {
      if let model = MonthViewModel(
        today: today,
        language: language,
        digits: digits,
        showTithi: settings.showTithi
      ) {
        MonthScreen(model: model, onSettings: onSettings)
          .id(settings)
          .tabItem {
            Label("Month", systemImage: "calendar")
          }
      }

      if let upcoming = UpcomingViewModel(
        today: today,
        language: language,
        digits: digits
      ) {
        UpcomingScreen(
          model: upcoming,
          language: language,
          digits: digits,
          onSettings: onSettings
        )
        .id(settings)
        .tabItem {
          Label("Upcoming", systemImage: "bell")
        }
      }

      ConverterScreen(
        model: ConverterViewModel(today: today, language: language, digits: digits),
        onSettings: onSettings
      )
      .id(settings)
      .tabItem {
        Label("Converter", systemImage: "arrow.left.arrow.right")
      }
    }
  }
}

/// A single screen for the UI tests, so an audit does not see the other tabs.
private struct TestScreenView: View {
  let screen: String
  let settings: Settings
  let today: MonthDate
  let info: AboutInfo

  var body: some View {
    let language = settings.language.resolved
    let digits = settings.digits.resolved(for: language)
    Group {
      switch screen {
      case "upcoming":
        if let model = UpcomingViewModel(today: today, language: language, digits: digits) {
          UpcomingScreen(model: model, language: language, digits: digits)
        }
      case "converter":
        ConverterScreen(model: ConverterViewModel(today: today, language: language, digits: digits))
      case "settings":
        SettingsScreen(
          model: SettingsViewModel(settings: settings),
          info: info
        )
      case "about":
        NavigationStack {
          AboutScreen(
            content: AboutContent(
              info: info,
              language: language,
              digits: digits,
              settings: settings
            )
          )
        }
      default:
        Text("Unknown test screen")
      }
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
  }
}
