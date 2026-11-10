import CalendarKit
import CalendarScreens
import Foundation
import SnapshotTesting
import SwiftUI
import Testing
import UIKit

/// Reference images of the first-run screen (MR-15): both schemes, Nepali, the largest
/// accessibility size and an iPhone SE (3rd generation) at the default text size.
@MainActor
@Suite("First run snapshots", .serialized)
struct FirstRunSnapshotTests {
  private static let today = MonthDate(year: 2083, month: 6, day: 25)

  @Test("First run reference images")
  func firstRun() {
    for variant in variants() {
      let settings = Settings(appearance: variant.appearance)
      let screen = FirstRunScreen(settings: settings, today: Self.today) { _ in }
      let controller = UIHostingController(rootView: content(screen, variant: variant))
      controller.overrideUserInterfaceStyle = variant.scheme == .dark ? .dark : .light
      let traits = UITraitCollection(traitsFrom: [
        UITraitCollection(userInterfaceStyle: variant.scheme == .dark ? .dark : .light),
        UITraitCollection(preferredContentSizeCategory: Self.category(for: variant.type)),
      ])
      assertSnapshot(
        of: controller,
        as: .image(size: variant.size, traits: traits),
        named: variant.name,
        testName: "firstRun"
      )
    }
  }

  private func content(_ screen: FirstRunScreen, variant: Variant) -> some View {
    screen
      .environment(\.colorScheme, variant.scheme)
      .environment(\.dynamicTypeSize, variant.type)
      .environment(\.locale, Locale(identifier: variant.locale))
  }

  private func variants() -> [Variant] {
    [
      Variant(
        name: "light", scheme: .light, type: .large, locale: "en", appearance: .system,
        size: CGSize(width: 390, height: 844)),
      Variant(
        name: "dark", scheme: .dark, type: .large, locale: "en", appearance: .dark,
        size: CGSize(width: 390, height: 844)),
      Variant(
        name: "nepali", scheme: .light, type: .large, locale: "ne", appearance: .system,
        size: CGSize(width: 390, height: 844)),
      Variant(
        name: "accessibility2", scheme: .light, type: .accessibility2, locale: "en",
        appearance: .system, size: CGSize(width: 390, height: 844)),
      Variant(
        name: "se", scheme: .light, type: .large, locale: "en", appearance: .system,
        size: CGSize(width: 375, height: 667)),
    ]
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

  private struct Variant {
    let name: String
    let scheme: ColorScheme
    let type: DynamicTypeSize
    let locale: String
    let appearance: Appearance
    let size: CGSize
  }
}
