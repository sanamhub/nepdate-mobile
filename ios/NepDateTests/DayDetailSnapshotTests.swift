import CalendarKit
import CalendarScreens
import SnapshotTesting
import SwiftUI
import Testing
import UIKit

/// I1-04: the reference images of the day detail sheet for Asoj 9 2083, the A1-04 holiday with
/// scoped events (MR-02 AC1 and AC5). The PNGs live in `__Snapshots__/DayDetailSnapshotTests`.
@MainActor
@Suite("Day detail snapshots", .serialized)
struct DayDetailSnapshotTests {
  private static let date = MonthDate(year: 2083, month: 6, day: 9)
  private static let size = CGSize(width: 390, height: 844)

  @Test("I1-04: Asoj 9 2083 reference images")
  func asoj9() throws {
    snapshot(try Self.model(), named: "light", scheme: .light, type: .large, locale: "en")
    snapshot(
      try Self.model(),
      named: "dark",
      scheme: .dark,
      type: .large,
      locale: "en"
    )
    snapshot(
      try Self.model(language: .nepali, digits: .nepali),
      named: "nepali",
      scheme: .light,
      type: .large,
      locale: "ne"
    )
    snapshot(
      try Self.model(),
      named: "accessibility2",
      scheme: .light,
      type: .accessibility2,
      locale: "en"
    )
    snapshot(
      try Self.model(),
      named: "copied",
      scheme: .light,
      type: .large,
      locale: "en",
      showsCopied: true
    )
  }

  private static func model(
    language: AppLanguage = .english,
    digits: Digits = .english
  ) throws -> DayDetailViewModel {
    try #require(
      DayDetailViewModel(date: date, language: language, digits: digits)
    )
  }

  private func snapshot(
    _ model: DayDetailViewModel,
    named name: String,
    scheme: ColorScheme,
    type: DynamicTypeSize,
    locale: String,
    showsCopied: Bool = false
  ) {
    let content = DayDetailContent(
      model: model,
      onClose: {},
      showsCopiedInitially: showsCopied
    )
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
      testName: "daydetail"
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
