import CalendarKit
import CalendarScreens
import CalendarUI
import Foundation
import SwiftUI
import Testing
import UIKit
import WidgetKit

/// Renders the App Store screenshots at the 6.9-inch iPhone size, 1320 by 2868 pixels.
///
/// The App Store accepts six screenshots per language. These are drawn from the real screens and
/// widgets at a fixed today, in the light theme, with no system status bar: the renderer draws the
/// SwiftUI content alone. The workflow uploads `build/store-screenshots` for review; nothing here
/// is compared as a reference image.
@MainActor
@Suite("App Store screenshots", .serialized)
struct StoreScreenshotTests {
  /// 440 by 956 points at three times, the accepted 6.9-inch size.
  private static let size = CGSize(width: 440, height: 956)
  private static let scale: CGFloat = 3
  fileprivate static let today = MonthDate(year: 2083, month: 6, day: 20)
  fileprivate static let dataDay = MonthDate(year: 2083, month: 6, day: 25)

  @Test("six screens in English and Nepali at 1320 by 2868")
  func screens() throws {
    for language in [AppLanguage.english, .nepali] {
      let suffix = language == .nepali ? "ne" : "en"
      let locale = Locale(identifier: suffix)
      let digits: Digits = language == .nepali ? .nepali : .english
      let month = try #require(
        MonthViewModel(
          today: Self.today,
          language: language,
          digits: digits,
          showTithi: false
        )
      )
      let day = try #require(
        DayDetailViewModel(date: Self.dataDay, language: language, digits: digits)
      )
      let year = try #require(
        YearViewModel(
          year: Self.today.year,
          today: Self.today,
          language: language,
          digits: digits
        )
      )

      try render(
        NavigationStack {
          MonthScreen(
            model: month,
            settings: Self.settings(language),
            today: Self.today
          )
          .navigationBarTitleDisplayMode(.inline)
        },
        name: "01-patro-\(suffix)",
        locale: locale
      )

      try render(
        NavigationStack {
          DayDetailContent(
            model: day,
            today: Self.today,
            onClose: {}
          )
          .navigationTitle(Text("Day"))
          .navigationBarTitleDisplayMode(.inline)
        },
        name: "02-day-\(suffix)",
        locale: locale
      )

      try render(
        NavigationStack {
          FestivalsScreen(
            today: Self.today,
            language: language,
            digits: digits,
            onOpenDay: { _ in },
            onOpenMonth: { _ in }
          )
        },
        name: "03-parva-\(suffix)",
        locale: locale
      )

      try render(
        NavigationStack {
          ConverterScreen(
            model: ConverterViewModel(today: Self.today, language: language, digits: digits)
          )
        },
        name: "04-converter-\(suffix)",
        locale: locale
      )

      try render(
        NavigationStack {
          YearScreen(model: year, onPick: { _ in })
        },
        name: "05-year-\(suffix)",
        locale: locale
      )

      try render(
        WidgetGallery(language: language),
        name: "06-widgets-\(suffix)",
        locale: locale
      )
    }
  }

  private func render<V: View>(
    _ view: V,
    name: String,
    locale: Locale
  ) throws {
    let content = AnyView(
      view
        .environment(\.colorScheme, .light)
        .environment(\.dynamicTypeSize, .large)
        .environment(\.locale, locale)
        .frame(width: Self.size.width, height: Self.size.height)
        .background(Color(uiColor: .systemBackground))
    )
    let controller = UIHostingController(rootView: content)
    controller.overrideUserInterfaceStyle = .light
    controller.view.frame = CGRect(origin: .zero, size: Self.size)
    controller.view.backgroundColor = .systemBackground

    // SwiftUI lays out text and lists only when its hosting controller is in a window. The window
    // is not shown over the test runner; it exists for the layout pass and the layer render.
    let window = UIWindow(frame: CGRect(origin: .zero, size: Self.size))
    window.rootViewController = controller
    window.isHidden = false
    controller.view.setNeedsLayout()
    controller.view.layoutIfNeeded()

    let format = UIGraphicsImageRendererFormat()
    format.scale = Self.scale
    format.opaque = true
    let image = UIGraphicsImageRenderer(size: Self.size, format: format).image { context in
      if !controller.view.drawHierarchy(
        in: controller.view.bounds,
        afterScreenUpdates: true
      ) {
        controller.view.layer.render(in: context.cgContext)
      }
    }
    window.isHidden = true
    guard let data = image.pngData() else {
      throw StoreScreenshotFailure(name: name)
    }
    try FileManager.default.createDirectory(
      at: Self.outputDirectory,
      withIntermediateDirectories: true
    )
    try data.write(to: Self.outputDirectory.appendingPathComponent("\(name).png"))
  }

  private static func settings(_ language: AppLanguage) -> Settings {
    Settings(
      language: language == .nepali ? .nepali : .english,
      digits: language == .nepali ? .nepali : .english,
      dateFollows: .nepal
    )
  }

  private static var outputDirectory: URL {
    URL(fileURLWithPath: #filePath)
      .deletingLastPathComponent()
      .deletingLastPathComponent()
      .appendingPathComponent("build/store-screenshots", isDirectory: true)
  }
}

private struct StoreScreenshotFailure: Error {
  let name: String
}

/// The widget screenshot: the real widget views in plain cards, so the reviewer can see all four
/// product widgets without a Home Screen and without system chrome.
private struct WidgetGallery: View {
  let language: AppLanguage

  var body: some View {
    let suffix = language == .nepali ? "ne" : "en"
    let entry = try? WidgetTestData.entry(StoreScreenshotTests.dataDay, language: language)
    let month = try? WidgetTestData.monthEntry(language: language)
    let window: [WidgetEvent]? = {
      guard let entry = try? WidgetTestData.entry(StoreScreenshotTests.dataDay, language: language)
      else { return nil }
      return WidgetTimeline.festivalWindow(
        now: entry.date,
        settings: WidgetTestData.settings(language: language),
        deviceZone: CalendarDates.nepal
      )
    }()

    VStack(spacing: 18) {
      HStack(spacing: 18) {
        card {
          if let entry {
            TodayWidgetView(entry: entry)
          }
        }
        .frame(width: 176, height: 176)

        card {
          if let event = window?.first {
            FestivalCountdownView(event: event)
          }
        }
        .frame(width: 176, height: 176)
      }

      card {
        if let entry {
          UpcomingWidgetView(entry: entry)
        }
      }
      .frame(width: 360, height: 170)

      card {
        if let month {
          MonthWidgetView(entry: month) { WidgetControlsPlaceholder() }
        }
      }
      .frame(width: 360, height: 360)
    }
    .padding(24)
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .background(Color(uiColor: .secondarySystemBackground))
    .accessibilityIdentifier("widget_gallery_\(suffix)")
  }

  private func card<Content: View>(@ViewBuilder content: () -> Content) -> some View {
    content()
      .padding(12)
      .background(
        RoundedRectangle(cornerRadius: 28, style: .continuous)
          .fill(Color(uiColor: .systemBackground))
      )
  }
}
