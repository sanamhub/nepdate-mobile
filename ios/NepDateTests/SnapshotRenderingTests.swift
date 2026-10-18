import CalendarKit
import CalendarUI
import Foundation
import SwiftUI
import Testing
import UIKit

/// Renders the screens and the widget to PNG files for review on the workflow artefact. The
/// comparison snapshots come with I1-03, when the month screen has its variants.
@MainActor
@Suite("Screenshots", .serialized)
struct SnapshotRenderingTests {
  @Test("I1-02: the month screen renders in English and Nepali, light and dark")
  func monthScreen() throws {
    let today = MonthDate(year: 2083, month: 6, day: 18)
    let english = try #require(
      MonthLayout(year: 2083, month: 6, showTithi: false, today: today)
    )
    let nepali = try #require(
      MonthLayout(year: 2083, month: 6, showTithi: true, today: today)
    )
    let size = CGSize(width: 390, height: 520)

    try render(
      MonthScreen(layout: english, language: .english, digits: .english),
      name: "month-en-light",
      size: size,
      scheme: .light,
      locale: Locale(identifier: "en")
    )
    try render(
      MonthScreen(layout: english, language: .english, digits: .english),
      name: "month-en-dark",
      size: size,
      scheme: .dark,
      locale: Locale(identifier: "en")
    )
    try render(
      MonthScreen(layout: nepali, language: .nepali, digits: .nepali),
      name: "month-ne-light",
      size: size,
      scheme: .light,
      locale: Locale(identifier: "ne")
    )
    try render(
      MonthScreen(layout: nepali, language: .nepali, digits: .nepali),
      name: "month-ne-dark",
      size: size,
      scheme: .dark,
      locale: Locale(identifier: "ne")
    )
  }

  @Test("M0-03: the Today widget renders for the small and medium families")
  func widget() throws {
    let summary = try #require(
      TodaySummary(year: 2081, month: 1, day: 1, language: .nepali, digits: .nepali)
    )
    try render(
      TodayWidgetView(summary: summary),
      name: "widget-small-ne",
      size: CGSize(width: 170, height: 170),
      scheme: .light,
      locale: Locale(identifier: "ne")
    )
    try render(
      TodayWidgetView(summary: summary),
      name: "widget-medium-ne",
      size: CGSize(width: 329, height: 155),
      scheme: .light,
      locale: Locale(identifier: "ne")
    )
  }

  private func render<V: View>(
    _ view: V,
    name: String,
    size: CGSize,
    scheme: ColorScheme,
    locale: Locale
  ) throws {
    let content = AnyView(
      view
        .environment(\.colorScheme, scheme)
        .environment(\.locale, locale)
        .frame(width: size.width, height: size.height)
        .background(Color(uiColor: .systemBackground))
    )
    let renderer = ImageRenderer(content: content)
    renderer.scale = 2
    guard let image = renderer.uiImage, let data = image.pngData() else {
      throw RenderFailure(name: name)
    }
    try FileManager.default.createDirectory(
      at: Self.outputDirectory,
      withIntermediateDirectories: true
    )
    try data.write(to: Self.outputDirectory.appendingPathComponent("\(name).png"))
  }

  private static var outputDirectory: URL {
    URL(fileURLWithPath: #filePath)
      .deletingLastPathComponent()
      .deletingLastPathComponent()
      .appendingPathComponent("build/snapshots", isDirectory: true)
  }
}

private struct RenderFailure: Error {
  let name: String
}
