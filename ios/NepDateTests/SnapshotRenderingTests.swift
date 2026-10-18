import CalendarKit
import CalendarUI
import Foundation
import SwiftUI
import Testing
import UIKit

/// Renders the screens and the widget to PNG files for review on the workflow artefact. These are
/// screenshots, not reference comparisons: I1 turns them into `swift-snapshot-testing` snapshots
/// when the screens are real (ADR-0008).
@MainActor
@Suite("Screenshots", .serialized)
struct SnapshotRenderingTests {
  @Test("M0-03: the month screen renders in English and Nepali, light and dark")
  func monthScreen() throws {
    let english = try #require(
      MonthGrid(year: 2083, month: 6, language: .english, today: (2083, 6, 18))
    )
    let nepali = try #require(
      MonthGrid(year: 2083, month: 6, language: .nepali, today: (2083, 6, 18))
    )
    let size = CGSize(width: 390, height: 520)

    try render(
      MonthScreen(grid: english, language: .english),
      name: "month-en-light",
      size: size,
      scheme: .light,
      locale: Locale(identifier: "en")
    )
    try render(
      MonthScreen(grid: english, language: .english),
      name: "month-en-dark",
      size: size,
      scheme: .dark,
      locale: Locale(identifier: "en")
    )
    try render(
      MonthScreen(grid: nepali, language: .nepali),
      name: "month-ne-light",
      size: size,
      scheme: .light,
      locale: Locale(identifier: "ne")
    )
    try render(
      MonthScreen(grid: nepali, language: .nepali),
      name: "month-ne-dark",
      size: size,
      scheme: .dark,
      locale: Locale(identifier: "ne")
    )
  }

  @Test("M0-03: the Today widget renders for the small and medium families")
  func widget() throws {
    let summary = try #require(TodaySummary(year: 2081, month: 1, day: 1, language: .nepali))
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
    let content = view
      .environment(\.colorScheme, scheme)
      .environment(\.locale, locale)
      .frame(width: size.width, height: size.height)
      .background(Color(uiColor: .systemBackground))
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
