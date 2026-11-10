import CalendarKit
import CalendarScreens
import Foundation
import SnapshotTesting
import SwiftUI
import Testing
import UIKit

/// Reference images of the year view (MR-14): both schemes, Nepali and the list layout at an
/// accessibility size.
@MainActor
@Suite("Year view snapshots", .serialized)
struct YearSnapshotTests {
  private static let today = MonthDate(year: 2083, month: 6, day: 25)
  private static let size = CGSize(width: 390, height: 844)

  @Test("Year view reference images")
  func year() throws {
    for variant in variants() {
      let model = try #require(
        YearViewModel(
          year: 2083,
          today: Self.today,
          language: variant.language,
          digits: variant.digits
        )
      )
      let screen = NavigationStack {
        YearScreen(model: model, onPick: { _ in })
      }
      let controller = UIHostingController(rootView: content(screen, variant: variant))
      controller.overrideUserInterfaceStyle = variant.scheme == .dark ? .dark : .light
      let traits = UITraitCollection(traitsFrom: [
        UITraitCollection(userInterfaceStyle: variant.scheme == .dark ? .dark : .light),
        UITraitCollection(preferredContentSizeCategory: Self.category(for: variant.type)),
      ])
      assertSnapshot(
        of: controller,
        as: .image(size: Self.size, traits: traits),
        named: variant.name,
        testName: "year"
      )
    }
  }

  private func content<V: View>(_ view: V, variant: Variant) -> some View {
    view
      .environment(\.colorScheme, variant.scheme)
      .environment(\.dynamicTypeSize, variant.type)
      .environment(\.locale, Locale(identifier: variant.locale))
  }

  private func variants() -> [Variant] {
    [
      Variant(
        name: "light", scheme: .light, type: .large, locale: "en", language: .english,
        digits: .english),
      Variant(
        name: "dark", scheme: .dark, type: .large, locale: "en", language: .english,
        digits: .english),
      Variant(
        name: "nepali", scheme: .light, type: .large, locale: "ne", language: .nepali,
        digits: .nepali),
      Variant(
        name: "accessibility2",
        scheme: .light,
        type: .accessibility2,
        locale: "en",
        language: .english,
        digits: .english
      ),
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
    let language: AppLanguage
    let digits: Digits
  }
}
