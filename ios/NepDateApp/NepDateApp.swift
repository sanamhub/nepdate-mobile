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
  @State private var tab = RootTab.month
  @State private var route: AppRoute?

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
      #if DEBUG
        if let screen = Self.testScreen {
          TestScreenView(
            screen: screen,
            settings: settings,
            today: today,
            info: info
          )
        } else {
          tabs(today: today, store: store, suggestion: suggestion)
        }
      #else
        tabs(today: today, store: store, suggestion: suggestion)
      #endif
    }
    .background(Color(uiColor: .systemBackground))
    // Widget taps (I2-02). An unknown or malformed URL just opens the app where it was.
    .onOpenURL { url in
      guard let opened = AppRoute(url: url) else { return }
      showsSettings = false
      tab = .month
      route = opened
    }
    .sheet(isPresented: $showsSettings) {
      SettingsScreen(
        model: SettingsViewModel(settings: settings, store: store),
        info: info,
        onChanged: { settings = $0 }
      )
    }
  }

  private func tabs(
    today: MonthDate,
    store: SettingsStore,
    suggestion: SettingsViewModel
  ) -> some View {
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
        tab: $tab,
        route: route,
        onRouteHandled: { route = nil },
        onSettings: { showsSettings = true }
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

  #if DEBUG
    /// The UI tests open one screen alone with `NEPDATE_UI_TEST_SCREEN`. Debug builds only.
    private static var testScreen: String? {
      ProcessInfo.processInfo.environment["NEPDATE_UI_TEST_SCREEN"]
    }
  #endif

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

/// The app's tabs, so a widget tap can switch to the month.
private enum RootTab: Hashable {
  case month
  case upcoming
  case converter
}

private struct RootTabView: View {
  let settings: Settings
  let today: MonthDate
  @Binding var tab: RootTab
  let route: AppRoute?
  let onRouteHandled: () -> Void
  let onSettings: () -> Void

  var body: some View {
    let language = settings.language.resolved
    let digits = settings.digits.resolved(for: language)

    TabView(selection: $tab) {
      if let model = MonthViewModel(
        today: today,
        language: language,
        digits: digits,
        showTithi: settings.showTithi
      ) {
        MonthScreen(
          model: model,
          onSettings: onSettings,
          route: route,
          onRouteHandled: onRouteHandled
        )
        .id(settings)
        .tabItem {
          Label("Month", systemImage: "calendar")
        }
        .tag(RootTab.month)
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
        .tag(RootTab.upcoming)
      }

      ConverterScreen(
        model: ConverterViewModel(today: today, language: language, digits: digits),
        onSettings: onSettings
      )
      .id(settings)
      .tabItem {
        Label("Converter", systemImage: "arrow.left.arrow.right")
      }
      .tag(RootTab.converter)
    }
  }
}

#if DEBUG
  /// A single screen for the UI tests, so an audit does not see the other tabs.
  private struct TestScreenView: View {
    let screen: String
    let settings: Settings
    let today: MonthDate
    let info: AboutInfo
    @State private var openedSettingsURL: String? = nil

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
          ConverterScreen(
            model: ConverterViewModel(today: today, language: language, digits: digits)
          )
        case "settings":
          SettingsScreen(
            model: SettingsViewModel(settings: settings),
            info: info,
            opener: SettingsURLOpener { url in
              if capturesSettingsURL {
                openedSettingsURL = url.absoluteString
              } else {
                UIApplication.shared.open(url)
              }
            }
          )
          .overlay(alignment: .top) {
            if let openedSettingsURL {
              Text(verbatim: openedSettingsURL)
                .font(.caption)
                .padding(8)
                .background(Color(uiColor: .secondarySystemBackground))
                .accessibilityIdentifier("settings_opened_url")
            }
          }
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

    private var capturesSettingsURL: Bool {
      ProcessInfo.processInfo.environment["NEPDATE_UI_TEST_CAPTURE_SETTINGS_URL"] != nil
    }
  }
#endif
