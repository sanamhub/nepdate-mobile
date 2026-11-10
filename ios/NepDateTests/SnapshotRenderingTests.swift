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

  @Test("W-03 and W-04 render in both languages")
  func newWidgets() throws {
    for language in [AppLanguage.english, .nepali] {
      let locale = Locale(identifier: language == .nepali ? "ne" : "en")
      let suffix = language == .nepali ? "ne" : "en"
      let month = try WidgetTestData.monthEntry(language: language)
      let holiday = try WidgetTestData.entry(
        MonthDate(year: 2083, month: 6, day: 25),
        language: language
      )

      try render(
        MonthWidgetView(entry: month) { WidgetControlsPlaceholder() },
        name: "widget-month-large-\(suffix)",
        size: CGSize(width: 329, height: 345),
        scheme: .light,
        locale: locale
      )
      try render(
        LockScreenInlineView(entry: holiday),
        name: "widget-lock-inline-\(suffix)",
        size: CGSize(width: 257, height: 26),
        scheme: .light,
        locale: locale
      )
      try render(
        LockScreenCircularView(entry: holiday),
        name: "widget-lock-circular-\(suffix)",
        size: CGSize(width: 76, height: 76),
        scheme: .light,
        locale: locale
      )
      try render(
        LockScreenRectangularView(entry: holiday),
        name: "widget-lock-rectangular-\(suffix)",
        size: CGSize(width: 172, height: 76),
        scheme: .light,
        locale: locale
      )
    }
  }

<<<<<<< HEAD
  @Test("MR-15: first run renders in both schemes, Nepali and on an iPhone SE")
  func firstRun() throws {
    let today = MonthDate(year: 2083, month: 6, day: 25)
    let phone = CGSize(width: 390, height: 844)
    let se = CGSize(width: 375, height: 667)

    try render(
      FirstRunScreen(settings: Settings(), today: today) { _ in },
      name: "first-run-en-light",
      size: phone,
      scheme: .light,
      locale: Locale(identifier: "en")
    )
    try render(
      FirstRunScreen(settings: Settings(appearance: .dark), today: today) { _ in },
      name: "first-run-en-dark",
      size: phone,
      scheme: .dark,
      locale: Locale(identifier: "en")
    )
    try render(
      FirstRunScreen(settings: Settings(language: .nepali), today: today) { _ in },
      name: "first-run-ne-light",
      size: phone,
      scheme: .light,
      locale: Locale(identifier: "ne")
    )
    try render(
      FirstRunScreen(settings: Settings(), today: today) { _ in },
      name: "first-run-accessibility2",
      size: phone,
      scheme: .light,
      locale: Locale(identifier: "en"),
      type: .accessibility2
    )
    try render(
      FirstRunScreen(settings: Settings(), today: today) { _ in },
      name: "first-run-se-light",
      size: se,
      scheme: .light,
      locale: Locale(identifier: "en")
    )
  }

=======
>>>>>>> origin/docs/ios-parity-scope
  @Test("the four brand marks render in light and dark")
  func brandMarks() throws {
    for (scheme, suffix) in [(ColorScheme.light, "light"), (.dark, "dark")] {
      try render(
        BrandMarkSheet(),
        name: "brand-marks-\(suffix)",
        size: CGSize(width: 460, height: 150),
        scheme: scheme,
        locale: Locale(identifier: "en")
      )
    }
  }

  private func render<V: View>(
    _ view: V,
    name: String,
    size: CGSize,
    scheme: ColorScheme,
    locale: Locale,
    type: DynamicTypeSize = .large
  ) throws {
    let content = AnyView(
      view
        .environment(\.colorScheme, scheme)
        .environment(\.dynamicTypeSize, type)
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

/// The four brand marks side by side, each on the fill it uses in the app, so the rounding can be
/// compared with Android's at a glance.
private struct BrandMarkSheet: View {
  private struct Mark {
    let name: String
    let shape: AnyShape
    let fill: Color
    let ink: Color
  }

  private var marks: [Mark] {
    [
      Mark(
        name: "Sunny",
        shape: AnyShape(SunnyShape()),
        fill: BrandColor.today,
        ink: BrandColor.onToday
      ),
      Mark(
        name: "SoftBurst",
        shape: AnyShape(SoftBurstShape()),
        fill: BrandColor.holidayContainer,
        ink: BrandColor.onHolidayContainer
      ),
      Mark(
        name: "Diamond",
        shape: AnyShape(DiamondShape()),
        fill: BrandColor.holiday,
        ink: Color.clear
      ),
      Mark(
        name: "Cookie",
        shape: AnyShape(CookieShape()),
        fill: BrandColor.holidayContainer,
        ink: BrandColor.onHolidayContainer
      ),
    ]
  }

  var body: some View {
    HStack(spacing: 24) {
      ForEach(marks.indices, id: \.self) { index in
        VStack(spacing: 10) {
          marks[index].shape
            .fill(marks[index].fill)
            .frame(width: 88, height: 88)
            .overlay(
              Text("20")
                .font(BrandType.cellNumeral)
                .foregroundStyle(marks[index].ink)
            )
          Text(marks[index].name)
            .font(BrandType.caption)
        }
      }
    }
    .padding(24)
    .background(Color(uiColor: .systemBackground))
  }
}
