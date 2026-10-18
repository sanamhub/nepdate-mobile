import CalendarKit
import CalendarUI
import SnapshotTesting
import SwiftUI
import Testing
import UIKit

/// I1-03: the reference images of Asoj 2083 with today = Asoj 20 (MR-01 AC1–AC4, AC7, AC8 and
/// MR-10 AC2). The PNGs live in `__Snapshots__/MonthSnapshotTests` and are committed; the test gate
/// compares them with `SNAPSHOT_TESTING_RECORD=never`.
@MainActor
@Suite("Month screen snapshots", .serialized)
struct MonthSnapshotTests {
  private static let today = MonthDate(year: 2083, month: 6, day: 20)
  private static let size = CGSize(width: 390, height: 844)

  @Test("I1-03: Asoj 2083 reference images")
  func asoj2083() throws {
    var light = try Self.model()
    light.select(MonthDate(year: 2083, month: 6, day: 25))
    snapshot(light, named: "light", scheme: .light, type: .large, locale: "en")

    snapshot(try Self.model(), named: "dark", scheme: .dark, type: .large, locale: "en")
    snapshot(
      try Self.model(language: .nepali, digits: .nepali, showTithi: true),
      named: "nepali",
      scheme: .light,
      type: .large,
      locale: "ne"
    )
    snapshot(
      try Self.model(showTithi: true),
      named: "tithi",
      scheme: .light,
      type: .large,
      locale: "en"
    )
    snapshot(
      try Self.model(),
      named: "xxxlarge",
      scheme: .light,
      type: .xxxLarge,
      locale: "en"
    )
    snapshot(
      try Self.model(),
      named: "accessibility2",
      scheme: .light,
      type: .accessibility2,
      locale: "en"
    )
  }

  private static func model(
    language: AppLanguage = .english,
    digits: Digits = .english,
    showTithi: Bool = false
  ) throws -> MonthViewModel {
    try #require(
      MonthViewModel(
        today: today,
        language: language,
        digits: digits,
        showTithi: showTithi
      )
    )
  }

  private func snapshot(
    _ model: MonthViewModel,
    named name: String,
    scheme: ColorScheme,
    type: DynamicTypeSize,
    locale: String
  ) {
    let content = MonthScreenContent(model: model)
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
      testName: "month"
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
