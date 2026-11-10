import CalendarKit
import CalendarScreens
import CalendarUI
import Observation
import SwiftUI
import UIKit
import WidgetKit

@main
struct NepDateApp: App {
  @State private var commands = AppCommands()

  var body: some Scene {
    WindowGroup {
      RootView(commands: commands)
    }
    .commands {
      CommandMenu("Go") {
        Button("Go to Today") {
          commands.goToToday()
        }
        .keyboardShortcut("t", modifiers: .command)

        Button("Previous Month") {
          commands.previousMonth()
        }
        .keyboardShortcut(.leftArrow, modifiers: .command)

        Button("Next Month") {
          commands.nextMonth()
        }
        .keyboardShortcut(.rightArrow, modifiers: .command)

        Divider()

        Button("Search") {
          commands.search()
        }
        .keyboardShortcut("f", modifiers: .command)
      }
    }
  }
}

/// A place the menu asks the shell to open.
struct AppRequest: Sendable, Hashable {
  /// The tab to select.
  let tab: AppTab

  /// The month change to apply, when the request came from a month command.
  let month: MonthCommand?
}

/// The actions the iPad and hardware-keyboard menu sends to the shell (MR-01 AC5, MR-13).
///
/// The menu lives on the scene and the state it changes lives in the root view, so the menu records
/// a request and the root applies it once, then clears it.
@Observable
final class AppCommands {
  var request: AppRequest?

  func goToToday() {
    request = AppRequest(tab: .patro, month: .today)
  }

  func previousMonth() {
    request = AppRequest(tab: .patro, month: .previous)
  }

  func nextMonth() {
    request = AppRequest(tab: .patro, month: .next)
  }

  func search() {
    request = AppRequest(tab: .search, month: nil)
  }
}

private struct RootView: View {
  @State private var settings: Settings
  @State private var today: MonthDate
  @State private var tab: AppTab
  @State private var route: AppRoute?
  @State private var monthCommand: MonthCommand?
  @State private var monthRequest: MonthKey?
  @State private var showsSettings = false
  @State private var showsFirstRun: Bool
  @Environment(\.scenePhase) private var scenePhase

  let commands: AppCommands

  /// Asks WidgetKit to reload when the settings, the day or the clock change (I2-05).
  private let refresher = WidgetRefresher { WidgetCenter.shared.reloadAllTimelines() }

  init(commands: AppCommands) {
    let store = SettingsStore()
    let settings = store.load()
    self.commands = commands
    _settings = State(initialValue: settings)
    _today = State(initialValue: Self.day(settings: settings))
    _tab = State(initialValue: settings.opensOn.tab)
    _showsFirstRun = State(initialValue: Self.firstRunIsDue)
  }

  var body: some View {
    let store = SettingsStore()
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
    .preferredColorScheme(settings.appearance.colorScheme)
    .environment(\.hapticsEnabled, settings.hapticsEnabled)
    // Widget taps. An unknown or malformed URL just opens the app where it was.
    .onOpenURL { url in
      guard let opened = AppRoute(url: url) else { return }
      open(opened)
    }
    .onAppear {
      activate()
    }
    .onChange(of: commands.request) { _, request in
      apply(request)
    }
    .onChange(of: scenePhase) { _, phase in
      if phase == .active { activate() }
    }
    .onChange(of: settings.dateFollows) { _, _ in
      refreshToday()
    }
    .onReceive(NotificationCenter.default.publisher(for: .NSSystemTimeZoneDidChange)) { _ in
      refreshToday()
      refresher.timeChanged()
    }
    .onReceive(
      NotificationCenter.default.publisher(for: UIApplication.significantTimeChangeNotification)
    ) { _ in
      refreshToday()
      refresher.timeChanged()
    }
    .task(id: settings.dateFollows) {
      await refreshAtMidnight()
    }.sheet(isPresented: $showsSettings) {
      SettingsScreen(
        model: SettingsViewModel(settings: settings, store: store),
        info: info,
        onChanged: { changed in
          settings = changed
          refresher.settingsChanged()
        }
      )
    }
    .fullScreenCover(isPresented: $showsFirstRun) {
      FirstRunScreen(settings: settings, today: today) { updated in
        var model = SettingsViewModel(settings: settings, store: store)
        model.setAppearance(updated.appearance)
        settings = model.settings
        FirstRunStore().markShown()
        showsFirstRun = false
        refresher.settingsChanged()
      }
      .interactiveDismissDisabled()
    }
  }

  /// Opens a route: its tab, and for the Patro tab the day or today.
  private func open(_ opened: AppRoute) {
    showsSettings = false
    tab = opened.tab
    route = opened.tab == .patro ? opened : nil
  }

  /// Runs when the app becomes active: reloads the widgets on a new day, then opens the route a
  /// control left in the App Group, or the one a UI test passes.
  private func activate() {
    refreshToday()
    refresher.becameActive(on: today)
    if let pending = PendingRouteStore().consume() {
      open(pending)
    }
    #if DEBUG
      if let value = ProcessInfo.processInfo.environment["NEPDATE_UI_TEST_URL"],
        let url = URL(string: value),
        let opened = AppRoute(url: url)
      {
        open(opened)
      }
    #endif
  }

  /// Points the menu commands at the shell's state.
  private func apply(_ request: AppRequest?) {
    guard let request else { return }
    tab = request.tab
    if let month = request.month {
      monthCommand = month
    }
    commands.request = nil
  }

  /// Reads the clock again; a new day reloads the widgets.
  private func refreshToday() {
    let value = Self.day(settings: settings)
    guard value != today else { return }
    today = value
    refresher.timeChanged()
  }

  /// Waits for the next local midnight and reads the clock again, for as long as the app is open.
  @MainActor
  private func refreshAtMidnight() async {
    let zone = MidnightRefresh.zone(for: settings.dateFollows)
    while !Task.isCancelled {
      let wait = MidnightRefresh.secondsUntilNextMidnight(now: Date(), zone: zone)
      do {
        try await Task.sleep(for: .seconds(wait))
      } catch {
        return
      }
      refreshToday()
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
            refresher.settingsChanged()
          },
          onDismiss: {
            var model = SettingsViewModel(settings: settings, store: store)
            model.dismissNepalTime()
            settings = model.settings
            refresher.settingsChanged()
          }
        )
      }

      RootTabView(
        settings: settings,
        today: today,
        initialMonth: Self.testMonth,
        tab: $tab,
        route: route,
        monthCommand: monthCommand,
        monthRequest: monthRequest,
        onRouteHandled: { route = nil },
        onMonthCommandHandled: { monthCommand = nil },
        onMonthRequestHandled: { monthRequest = nil },
        onOpenDay: { date in
          tab = .patro
          route = .day(date)
        },
        onOpenMonth: { key in
          tab = .patro
          monthRequest = key
        },
        onSettings: { showsSettings = true }
      )
    }
  }

  /// Today in the zone the settings choose, or the day a UI test pins.
  private static func day(settings: Settings) -> MonthDate {
    testToday ?? TodayProvider().today(dateFollows: settings.dateFollows)
  }

  private static var appVersion: String {
    let version = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String
    let build = Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String
    return "\(version ?? "?") (\(build ?? "?"))"
  }

  private static var libraryVersion: String {
    Bundle.main.object(forInfoDictionaryKey: "NepDateLibraryVersion") as? String ?? "unknown"
  }

  /// Whether the first-run screen is due: the App Group flag, unless a UI test overrides it. The
  /// other UI tests would otherwise start behind the cover, so a pinned day hides it by default.
  private static var firstRunIsDue: Bool {
    #if DEBUG
      switch ProcessInfo.processInfo.environment["NEPDATE_UI_TEST_FIRST_RUN"] {
      case "show": return true
      case "hide": return false
      default: break
      }
      if testToday != nil { return false }
    #endif
    return !FirstRunStore().hasShown
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

  /// A UI test can open the Patro tab on a chosen month with `NEPDATE_UI_TEST_MONTH=2199-12`, so
  /// the year-view range test starts at an end instead of swiping there. Debug builds only.
  private static var testMonth: MonthKey? {
    #if DEBUG
      guard let value = ProcessInfo.processInfo.environment["NEPDATE_UI_TEST_MONTH"] else {
        return nil
      }
      let parts = value.split(separator: "-").compactMap { Int($0) }
      guard parts.count == 2 else { return nil }
      return MonthKey(year: parts[0], month: parts[1])
    #else
      return nil
    #endif
  }
}

private struct RootTabView: View {
  let settings: Settings
  let today: MonthDate
  let initialMonth: MonthKey?
  @Binding var tab: AppTab
  let route: AppRoute?
  let monthCommand: MonthCommand?
  let monthRequest: MonthKey?
  let onRouteHandled: () -> Void
  let onMonthCommandHandled: () -> Void
  let onMonthRequestHandled: () -> Void
  let onOpenDay: (MonthDate) -> Void
  let onOpenMonth: (MonthKey) -> Void
  let onSettings: () -> Void

  var body: some View {
    let language = settings.language.resolved
    let digits = settings.digits.resolved(for: language)

    TabView(selection: $tab) {
      Tab("Patro", systemImage: "calendar", value: AppTab.patro) {
        NavigationStack {
          if let model = MonthViewModel(
            today: today,
            language: language,
            digits: digits,
            showTithi: settings.showTithi,
            showsADDates: settings.showsADDates,
            month: initialMonth
          ) {
            MonthScreen(
              model: model,
              settings: settings,
              today: today,
              route: route,
              monthCommand: monthCommand,
              monthRequest: monthRequest,
              onRouteHandled: onRouteHandled,
              onMonthCommandHandled: onMonthCommandHandled,
              onMonthRequestHandled: onMonthRequestHandled
            )
            .settingsToolbar(onSettings)
          }
        }
      }

      Tab("Festivals", systemImage: "sparkles", value: AppTab.festivals) {
        NavigationStack {
          FestivalsScreen(
            today: today,
            language: language,
            digits: digits,
            onOpenDay: onOpenDay,
            onOpenMonth: onOpenMonth
          )
          .settingsToolbar(onSettings)
        }
      }

      Tab("Converter", systemImage: "arrow.left.arrow.right", value: AppTab.converter) {
        NavigationStack {
          ConverterScreen(
            model: ConverterViewModel(today: today, language: language, digits: digits),
            onOpenDay: onOpenDay
          )
          .settingsToolbar(onSettings)
        }
      }

      Tab(
        "Search",
        systemImage: "magnifyingglass",
        value: AppTab.search,
        role: .search
      ) {
        NavigationStack {
          SearchScreen(
            today: today,
            language: language,
            digits: digits,
            onOpenDay: onOpenDay
          )
          .settingsToolbar(onSettings)
        }
      }
    }
    .tabViewStyle(.sidebarAdaptable)
  }
}

private struct SettingsToolbar: ViewModifier {
  let action: () -> Void

  func body(content: Content) -> some View {
    content.toolbar {
      ToolbarItem(placement: .topBarTrailing) {
        Button(action: action) {
          Image(systemName: "gearshape")
        }
        .accessibilityLabel(Text("Settings"))
      }
    }
  }
}

extension View {
  fileprivate func settingsToolbar(_ action: @escaping () -> Void) -> some View {
    modifier(SettingsToolbar(action: action))
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
        case "festivals":
          NavigationStack {
            FestivalsScreen(
              today: today,
              language: language,
              digits: digits,
              onOpenDay: { _ in },
              onOpenMonth: { _ in }
            )
          }
        case "search":
          NavigationStack {
            SearchScreen(
              today: today,
              language: language,
              digits: digits,
              onOpenDay: { _ in }
            )
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
        case "firstRun":
          FirstRunScreen(settings: settings, today: today) { _ in }
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
