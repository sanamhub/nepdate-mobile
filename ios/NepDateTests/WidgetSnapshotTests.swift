import CalendarKit
import CalendarUI
import Foundation
import SnapshotTesting
import SwiftUI
import Testing
import UIKit
import WidgetKit

/// The Today and upcoming widget references, plus the proof that a dropped rendering mode is
/// byte-identical to the full-colour reference.
@MainActor
@Suite("Widget snapshots", .serialized)
struct WidgetSnapshotTests {
  private static let size = CGSize(width: 390, height: 844)
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
