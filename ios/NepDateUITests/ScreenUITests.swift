import UIKit
import XCTest

/// MR-03, MR-04, MR-06, MR-07, MR-10, MR-11, MR-12: screen content and the seven
/// accessibility audit categories at the default and accessibility text sizes.
final class ScreenUITests: XCTestCase {
  private static let audits: [(name: String, type: XCUIAccessibilityAuditType)] = [
    ("contrast", .contrast),
    ("dynamicType", .dynamicType),
    ("elementDetection", .elementDetection),
    ("hitRegion", .hitRegion),
    ("sufficientElementDescription", .sufficientElementDescription),
    ("textClipped", .textClipped),
    ("trait", .trait),
  ]

  /// No finding is tolerated on Upcoming or Converter. The other tables have these reasons:
  ///
  /// - Settings: the toolbar title and Close button are reported as partially supporting Dynamic
  ///   Type even though both use system text styles.
  /// - About: the toolbar title and the two-line info rows are reported as partially supporting
  ///   Dynamic Type although the `.accessibility2` reference wraps them; the Privacy link at the
  ///   bottom sits behind the home indicator, and the same tint passes on the links above it.
  private static let converterTolerated: [String: Int] = [:]
  private static let settingsTolerated = ["dynamicType text": 2]
  private static let aboutTolerated = ["contrast text": 1, "dynamicType text": 5]
  private static let aboutAccessibilityTolerated = ["dynamicType text": 4]

  @MainActor
  func testUpcomingShowsTodayAndOpensTheDay() throws {
    let app = Self.launch(screen: "upcoming")

    let target = app.staticTexts["Asoj 2083"]
    XCTAssertTrue(target.waitForExistence(timeout: 10))
    XCTAssertTrue(
      app.buttons["Indira Ekadashi Brata, Asoj 20, 2083, 6 October 2026, Today"].exists
    )
    XCTAssertTrue(app.buttons["Ekadashi Shraddha, Asoj 20, 2083, 6 October 2026, Today"].exists)

    let holiday = app.buttons[
      "Ghatasthapana Brata, Public holiday, Asoj 25, 2083, 11 October 2026, in 5 days"
    ]
    for _ in 0..<4 where !holiday.exists {
      app.swipeUp()
    }
    XCTAssertTrue(holiday.exists)
    holiday.tap()
    XCTAssertTrue(app.staticTexts["Asoj 25, 2083"].waitForExistence(timeout: 10))
  }

  @MainActor
  func testSettingsAndAboutShowTheirValues() throws {
    let app = Self.launch(screen: "settings")

    XCTAssertTrue(app.staticTexts["Digits"].exists)
    XCTAssertTrue(app.staticTexts["Follow language"].exists)
    XCTAssertTrue(app.staticTexts["Date follows"].exists)
    XCTAssertTrue(app.staticTexts["Show tithi in the month grid"].exists)

    app.swipeUp()
    app.staticTexts["About"].tap()
    XCTAssertTrue(app.staticTexts["App version"].waitForExistence(timeout: 10))
    XCTAssertTrue(app.staticTexts["Library version"].exists)
    XCTAssertTrue(app.staticTexts["0.1.0 (1)"].exists)
    XCTAssertTrue(app.staticTexts["Baisakh 1, 2077 – Mangsir 20, 2084"].exists)
    XCTAssertTrue(app.buttons["Known data questions"].exists)
    XCTAssertTrue(app.buttons["Report a wrong date"].exists)
    XCTAssertTrue(app.buttons["Licences"].exists)
    XCTAssertTrue(app.buttons["Privacy"].exists)
    app.swipeUp()
    XCTAssertTrue(app.buttons["Source code"].exists)
    app.swipeUp()
    XCTAssertFalse(app.buttons["Subscribe to Nepal holidays"].exists)

    app.buttons["Copy diagnostic info"].tap()
    let copied = app.staticTexts["Copied"]
    XCTAssertTrue(copied.waitForExistence(timeout: 2))

    let hidden = expectation(for: NSPredicate(format: "exists == false"), evaluatedWith: copied)
    XCTAssertEqual(
      XCTWaiter.wait(for: [hidden], timeout: 4),
      .completed,
      "the Copied confirmation stayed visible"
    )
  }

  @MainActor
  func testSettingsSectionTitlesCarryTheHeaderTrait() throws {
    let app = Self.launch(screen: "settings")

    for title in ["Digits", "Date follows"] {
      let element = app.staticTexts[title]
      XCTAssertTrue(element.waitForExistence(timeout: 10), "\(title) is not on the screen")
      XCTAssertTrue(Self.hasHeaderTrait(element), "\(title) is not a header")
    }
  }

  @MainActor
  func testLanguageRowOpensTheAppSettingsURL() throws {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = "2083-06-20"
    app.launchEnvironment["NEPDATE_UI_TEST_SCREEN"] = "settings"
    app.launchEnvironment["NEPDATE_UI_TEST_CAPTURE_SETTINGS_URL"] = "1"
    app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
    app.launch()

    let row = app.buttons["language_row"]
    XCTAssertTrue(row.waitForExistence(timeout: 10))
    row.tap()

    let opened = app.staticTexts["settings_opened_url"]
    XCTAssertTrue(opened.waitForExistence(timeout: 5))
    XCTAssertEqual(opened.label, UIApplication.openSettingsURLString)
  }

  @MainActor
  func testUpcomingHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "upcoming")
    XCTAssertEqual(try audit(app, tolerating: [:]), [:])
  }

  @MainActor
  func testConverterHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "converter")
    XCTAssertEqual(try audit(app, tolerating: Self.converterTolerated), Self.converterTolerated)
  }

  @MainActor
  func testSettingsHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "settings")
    XCTAssertEqual(try audit(app, tolerating: Self.settingsTolerated), Self.settingsTolerated)
  }

  @MainActor
  func testAboutHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "about")
    XCTAssertEqual(try audit(app, tolerating: Self.aboutTolerated), Self.aboutTolerated)
  }

  @MainActor
  func testUpcomingAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "upcoming", accessibility: true)
    XCTAssertEqual(try audit(app, tolerating: [:]), [:])
  }

  @MainActor
  func testConverterAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "converter", accessibility: true)
    XCTAssertEqual(try audit(app, tolerating: Self.converterTolerated), Self.converterTolerated)
  }

  @MainActor
  func testSettingsAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "settings", accessibility: true)
    XCTAssertEqual(try audit(app, tolerating: Self.settingsTolerated), Self.settingsTolerated)
  }

  @MainActor
  func testAboutAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "about", accessibility: true)
    XCTAssertEqual(
      try audit(app, tolerating: Self.aboutAccessibilityTolerated),
      Self.aboutAccessibilityTolerated
    )
  }

  @MainActor
  private func audit(
    _ app: XCUIApplication,
    tolerating table: [String: Int]
  ) throws -> [String: Int] {
    var counts: [String: Int] = [:]
    for audit in Self.audits {
      try app.performAccessibilityAudit(for: audit.type) { issue in
        let label = issue.element?.label ?? ""
        let frame = issue.element?.frame ?? .zero
        let kind = !label.isEmpty && label.allSatisfy(\.isNumber) ? "numeral" : "text"
        print(
          "AUDIT \(audit.name) \(kind) label=\(label) frame=\(frame) "
            + "\(issue.compactDescription) | \(issue.detailedDescription)"
        )
        guard table["\(audit.name) \(kind)"] != nil else { return false }
        counts["\(audit.name) \(kind)", default: 0] += 1
        return true
      }
    }
    print("AUDIT tolerated \(counts.sorted { $0.key < $1.key })")
    return counts
  }

  @MainActor
  private static func hasHeaderTrait(_ element: XCUIElement) -> Bool {
    guard let traits = element.value(forKey: "traits") as? NSNumber else { return false }
    return UIAccessibilityTraits(rawValue: traits.uint64Value).contains(.header)
  }

  @MainActor
  private static func launch(screen: String, accessibility: Bool = false) -> XCUIApplication {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = "2083-06-20"
    app.launchEnvironment["NEPDATE_UI_TEST_SCREEN"] = screen
    app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
    if accessibility {
      app.launchArguments += [
        "-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityL",
      ]
    }
    app.launch()
    return app
  }
}
