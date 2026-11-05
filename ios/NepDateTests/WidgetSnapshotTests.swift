import CalendarKit
import CalendarUI
import Foundation
import SnapshotTesting
import SwiftUI
import Testing
import UIKit
import WidgetKit

/// The widget references, plus the proof that a dropped rendering mode is byte-identical to the
/// full-colour reference.
@MainActor
@Suite("Widget snapshots", .serialized)
struct WidgetSnapshotTests {
  private static let size = CGSize(width: 390, height: 844)
  private static let monthSize = CGSize(width: 329, height: 345)
  private static let inlineSize = CGSize(width: 257, height: 26)
  private static let circularSize = CGSize(width: 76, height: 76)
  private static let rectangularSize = CGSize(width: 172, height: 76)
  private static let holiday = MonthDate(year: 2083, month: 6, day: 25)
  private static let normal = MonthDate(year: 2083, month: 6, day: 20)

  @Test("W-01 and W-02 reference images")
  func todayAndUpcoming() throws {
    for language in [AppLanguage.english, .nepali] {
      let suffix = language == .nepali ? "ne" : "en"
      let holiday = try WidgetTestData.entry(Self.holiday, language: language)
      let normal = try WidgetTestData.entry(Self.normal, language: language)

      snapshot(
        TodayWidgetView(entry: holiday),
        named: "today-small-holiday-\(suffix)",
        size: CGSize(width: 170, height: 170),
        locale: language
      )
      snapshot(
        TodayWidgetView(entry: normal),
        named: "today-small-normal-\(suffix)",
        size: CGSize(width: 170, height: 170),
        locale: language
      )
      snapshot(
        UpcomingWidgetView(entry: holiday),
        named: "upcoming-medium-holiday-\(suffix)",
        size: CGSize(width: 329, height: 155),
        locale: language
      )
      snapshot(
        UpcomingWidgetView(entry: normal),
        named: "upcoming-medium-normal-\(suffix)",
        size: CGSize(width: 329, height: 155),
        locale: language
      )
    }
  }

  @Test("W-03 month widget reference images, Jestha 2083")
  func monthLarge() throws {
    for language in [AppLanguage.english, .nepali] {
      let suffix = language == .nepali ? "ne" : "en"
      let entry = try WidgetTestData.monthEntry(language: language)
      #expect(entry.rowCount == 6)
      snapshot(
        MonthWidgetView(entry: entry) { WidgetControlsPlaceholder() },
        named: "month-large-jestha-\(suffix)",
        size: Self.monthSize,
        locale: language
      )
    }
  }

  @Test("W-04 Lock Screen reference images on the holiday")
  func lockScreen() throws {
    for language in [AppLanguage.english, .nepali] {
      let suffix = language == .nepali ? "ne" : "en"
      let entry = try WidgetTestData.entry(Self.holiday, language: language)
      snapshot(
        LockScreenInlineView(entry: entry),
        named: "lock-inline-holiday-\(suffix)",
        size: Self.inlineSize,
        locale: language
      )
      snapshot(
        LockScreenCircularView(entry: entry),
        named: "lock-circular-holiday-\(suffix)",
        size: Self.circularSize,
        locale: language
      )
      snapshot(
        LockScreenRectangularView(entry: entry),
        named: "lock-rectangular-holiday-\(suffix)",
        size: Self.rectangularSize,
        locale: language
      )
    }
  }

  @Test("collapsed rendering modes are byte-identical to full colour")
  func collapsedModesMatch() throws {
    let entry = try WidgetTestData.entry(Self.holiday)
    let todaySize = CGSize(width: 170, height: 170)
    let upcomingSize = CGSize(width: 329, height: 155)

    let today = try imageData(
      TodayWidgetView(entry: entry),
      size: todaySize,
      mode: .fullColor
    )
    let todayAccented = try imageData(
      TodayWidgetView(entry: entry),
      size: todaySize,
      mode: .accented
    )
    let todayVibrant = try imageData(
      TodayWidgetView(entry: entry),
      size: todaySize,
      mode: .vibrant
    )
    #expect(todayAccented == today, "the accented Today mode differs; keep its reference")
    #expect(todayVibrant == today, "the vibrant Today mode differs; keep its reference")

    let upcoming = try imageData(
      UpcomingWidgetView(entry: entry),
      size: upcomingSize,
      mode: .fullColor
    )
    let upcomingAccented = try imageData(
      UpcomingWidgetView(entry: entry),
      size: upcomingSize,
      mode: .accented
    )
    let upcomingVibrant = try imageData(
      UpcomingWidgetView(entry: entry),
      size: upcomingSize,
      mode: .vibrant
    )
    #expect(
      upcomingAccented == upcoming,
      "the accented upcoming mode differs; keep its reference"
    )
    #expect(
      upcomingVibrant == upcoming,
      "the vibrant upcoming mode differs; keep its reference"
    )

    let month = try WidgetTestData.monthEntry()
    try expectCollapsed(
      MonthWidgetView(entry: month) { WidgetControlsPlaceholder() },
      size: Self.monthSize,
      name: "month"
    )
    try expectCollapsed(
      LockScreenInlineView(entry: entry),
      size: Self.inlineSize,
      name: "lock inline"
    )
    try expectCollapsed(
      LockScreenCircularView(entry: entry),
      size: Self.circularSize,
      name: "lock circular"
    )
    try expectCollapsed(
      LockScreenRectangularView(entry: entry),
      size: Self.rectangularSize,
      name: "lock rectangular"
    )
  }

  /// Renders `view` in the accented and vibrant modes and compares each with full colour.
  private func expectCollapsed<V: View>(_ view: V, size: CGSize, name: String) throws {
    let full = try imageData(view, size: size, mode: .fullColor)
    let accented = try imageData(view, size: size, mode: .accented)
    let vibrant = try imageData(view, size: size, mode: .vibrant)
    #expect(accented == full, "the accented \(name) mode differs; keep its reference")
    #expect(vibrant == full, "the vibrant \(name) mode differs; keep its reference")
  }

  private func snapshot<V: View>(
    _ view: V,
    named name: String,
    size: CGSize,
    locale: AppLanguage
  ) {
    let content =
      view
      .environment(\.colorScheme, .light)
      .environment(\.locale, Locale(identifier: locale == .nepali ? "ne" : "en"))
    let controller = UIHostingController(rootView: content)
    controller.overrideUserInterfaceStyle = .light
    let traits = UITraitCollection(traitsFrom: [
      UITraitCollection(userInterfaceStyle: .light),
      UITraitCollection(preferredContentSizeCategory: .large),
    ])

    assertSnapshot(
      of: controller,
      as: .image(size: size, traits: traits),
      named: name,
      testName: "widget"
    )
  }

  private func imageData<V: View>(
    _ view: V,
    size: CGSize,
    mode: WidgetRenderingMode
  ) throws -> Data {
    let content =
      view
      .environment(\.widgetRenderingMode, mode)
      .environment(\.colorScheme, .light)
      .environment(\.locale, Locale(identifier: "en"))
      .frame(width: size.width, height: size.height)
      .background(Color(uiColor: .systemBackground))
    let renderer = ImageRenderer(content: content)
    renderer.scale = 2
    return try #require(renderer.uiImage?.pngData())
  }
}

/// Stands in for the extension's Previous, Today and Next buttons, which need its App Intents.
struct WidgetControlsPlaceholder: View {
  var body: some View {
    HStack(spacing: 2) {
      Image(systemName: "chevron.left").frame(width: 28, height: 24)
      Text("Today").font(.caption).frame(height: 24)
      Image(systemName: "chevron.right").frame(width: 28, height: 24)
    }
  }
}
