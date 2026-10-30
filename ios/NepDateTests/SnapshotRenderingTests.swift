import CalendarKit
import CalendarScreens
import CalendarUI
import Foundation
import SwiftUI
import Testing
import UIKit

/// Renders the screens and the widget to PNG files for review on the workflow artefact. The
/// comparison snapshots are in `MonthSnapshotTests`; this suite is the human-readable artefact.
@MainActor
@Suite("Screenshots", .serialized)
struct SnapshotRenderingTests {
  @Test("I1-03: the month screen renders in English and Nepali, light and dark")
  func monthScreen() throws {
    let today = MonthDate(year: 2083, month: 6, day: 20)
    let english = try #require(
      MonthViewModel(
        today: today,
        language: .english,
        digits: .english,
        showTithi: false
      )
    )
    let nepali = try #require(
      MonthViewModel(
        today: today,
        language: .nepali,
        digits: .nepali,
        showTithi: true
      )
    )
    let size = CGSize(width: 390, height: 780)

    try render(
      MonthScreenContent(model: english),
      name: "month-en-light",
      size: size,
      scheme: .light,
      locale: Locale(identifier: "en")
    )
    try render(
      MonthScreenContent(model: english),
      name: "month-en-dark",
      size: size,
      scheme: .dark,
      locale: Locale(identifier: "en")
    )
    try render(
      MonthScreenContent(model: nepali),
      name: "month-ne-light",
      size: size,
      scheme: .light,
      locale: Locale(identifier: "ne")
    )
    try render(
      MonthScreenContent(model: nepali),
      name: "month-ne-dark",
      size: size,
      scheme: .dark,
      locale: Locale(identifier: "ne")
    )
  }

  @Test("W-01 and W-02 render on a holiday and a normal day in both languages")
  func widget() throws {
    let small = CGSize(width: 170, height: 170)
    let medium = CGSize(width: 329, height: 155)

    for language in [AppLanguage.english, .nepali] {
      let locale = Locale(identifier: language == .nepali ? "ne" : "en")
      let suffix = language == .nepali ? "ne" : "en"
      let holidayEntry = try WidgetTestData.entry(
        MonthDate(year: 2083, month: 6, day: 25),
        language: language
      )
      let normalEntry = try WidgetTestData.entry(
        MonthDate(year: 2083, month: 6, day: 20),
        language: language
      )

      try render(
        TodayWidgetView(entry: holidayEntry),
        name: "widget-today-small-\(suffix)-holiday",
        size: small,
        scheme: .light,
        locale: locale
      )
      try render(
        TodayWidgetView(entry: normalEntry),
        name: "widget-today-small-\(suffix)-normal",
        size: small,
        scheme: .light,
        locale: locale
      )
      try render(
        UpcomingWidgetView(entry: holidayEntry),
        name: "widget-upcoming-medium-\(suffix)-holiday",
        size: medium,
        scheme: .light,
        locale: locale
      )
      try render(
        UpcomingWidgetView(entry: normalEntry),
        name: "widget-upcoming-medium-\(suffix)-normal",
        size: medium,
        scheme: .light,
        locale: locale
      )
    }
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
