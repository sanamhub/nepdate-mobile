import CalendarKit
import CalendarScreens
import Foundation
import SnapshotTesting
import SwiftUI
import Testing
import UIKit

/// Reference images of Upcoming, Converter, Settings and About.
@MainActor
@Suite("Screen snapshots", .serialized)
struct ScreenSnapshotTests {
  private static let today = MonthDate(year: 2083, month: 6, day: 20)
  private static let size = CGSize(width: 390, height: 844)
  private static let info = AboutInfo(
    appVersion: "0.1.0 (1)",
    libraryVersion: "708cd36",
    osVersion: "26.0"
  )

  @Test("Upcoming reference images")
  func upcoming() throws {
    for (name, language, digits, scheme, type, locale) in variants() {
      let model = try #require(
        UpcomingViewModel(today: Self.today, language: language, digits: digits)
      )
      snapshot(
        UpcomingScreen(model: model, language: language, digits: digits),
        named: "upcoming-\(name)",
        scheme: scheme,
        type: type,
        locale: locale
      )
    }
  }

  @Test("Converter reference images")
  func converter() {
    for (name, language, digits, scheme, type, locale) in variants() {
      var model = ConverterViewModel(today: Self.today, language: language, digits: digits)
      model.fromBs("2083-06-20")
      model.compareWithBs("2082-01-01")
      snapshot(
        ConverterScreen(
          model: model,
          bsInput: "2083-06-20",
          compareInput: "2082-01-01"
        ),
        named: "converter-\(name)",
        scheme: scheme,
        type: type,
        locale: locale
      )
    }
  }

  @Test("Settings reference images")
  func settings() {
    for (name, language, digits, scheme, type, locale) in variants() {
      let store = SettingsStore(suiteName: "np.nepdate.tests.snapshot.\(UUID().uuidString)")
      let screen = SettingsSnapshot(
        model: SettingsViewModel(
          settings: Settings(language: language == .nepali ? .nepali : .english),
          store: store
        ),
        info: Self.info
      )
      snapshot(screen, named: "settings-\(name)", scheme: scheme, type: type, locale: locale)
      UserDefaults(suiteName: store.suiteName)?.removePersistentDomain(forName: store.suiteName)
    }
  }

  @Test("About reference images")
  func about() {
    for (name, language, digits, scheme, type, locale) in variants() {
      let screen = AboutSnapshot(
        info: Self.info,
        language: language,
        digits: digits,
        settings: Settings(language: language == .nepali ? .nepali : .english)
      )
      snapshot(screen, named: "about-\(name)", scheme: scheme, type: type, locale: locale)
    }
  }

  private func variants() -> [(String, AppLanguage, Digits, ColorScheme, DynamicTypeSize, String)] {
    [
      ("light", .english, .english, .light, .large, "en"),
      ("dark", .english, .english, .dark, .large, "en"),
      ("nepali", .nepali, .nepali, .light, .large, "ne"),
      ("accessibility2", .english, .english, .light, .accessibility2, "en"),
    ]
  }

  private func snapshot<V: View>(
    _ view: V,
    named name: String,
    scheme: ColorScheme,
    type: DynamicTypeSize,
    locale: String
  ) {
    let content =
      view
      .environment(\.colorScheme, scheme)
      .environment(\.dynamicTypeSize, type)
      .environment(\.locale, Locale(identifier: locale))
    let controller = UIHostingController(rootView: content)
    controller.overrideUserInterfaceStyle = scheme == .dark ? .dark : .light
    let traits = UITraitCollection(traitsFrom: [
      UITraitCollection(userInterfaceStyle: scheme == .dark ? .dark : .light),
      UITraitCollection(preferredContentSizeCategory: Self.category(for: type)),
    ])

    assertSnapshot(
      of: controller,
      as: .image(size: Self.size, traits: traits),
      named: name,
      testName: "screen"
    )
  }

  private static func category(for type: DynamicTypeSize) -> UIContentSizeCategory {
    switch type {
    case .xSmall: return .extraSmall
    case .small: return .small
    case .medium: return .medium
    case .large: return .large
    case .xLarge: return .extraLarge
    case .xxLarge: return .extraExtraLarge
    case .xxxLarge: return .extraExtraExtraLarge
    case .accessibility1: return .accessibilityMedium
    case .accessibility2: return .accessibilityLarge
    case .accessibility3: return .accessibilityExtraLarge
    case .accessibility4: return .accessibilityExtraExtraLarge
    case .accessibility5: return .accessibilityExtraExtraExtraLarge
    default: return .large
    }
  }
}

private struct SettingsSnapshot: View {
  @State private var model: SettingsViewModel
  let info: AboutInfo

  init(model: SettingsViewModel, info: AboutInfo) {
    _model = State(initialValue: model)
    self.info = info
  }

  var body: some View {
    VStack(spacing: 0) {
      Text("Settings")
        .font(.largeTitle)
        .fontWeight(.bold)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 16)
        .padding(.top, 12)
        .padding(.bottom, 4)
      SettingsContent(model: $model, info: info)
    }
  }
}

private struct AboutSnapshot: View {
  let info: AboutInfo
  let language: AppLanguage
  let digits: Digits
  let settings: Settings

  var body: some View {
    VStack(spacing: 0) {
      Text("About")
        .font(.largeTitle)
        .fontWeight(.bold)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 16)
        .padding(.top, 12)
        .padding(.bottom, 4)
      AboutContent(
        info: info,
        language: language,
        digits: digits,
        settings: settings
      )
    }
  }
}
