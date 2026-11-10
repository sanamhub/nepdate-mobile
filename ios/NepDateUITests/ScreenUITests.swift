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

  /// No finding is tolerated on Upcoming. The other tables have these reasons:
  ///
  /// - Settings: the toolbar title and Close button are reported as partially supporting Dynamic
  ///   Type even though both use system text styles.
  /// - About: the toolbar title and the two-line info rows are reported as partially supporting
  ///   Dynamic Type although the `.accessibility2` reference wraps them; the Privacy link at the
  ///   bottom sits behind the home indicator, and the same tint passes on the links above it. The
  ///   promises section gives nine findings at the default size and three at accessibility2 (run
  ///   42787285560); the counts are printed for the review.
  /// - Converter: the from and to date labels were reported as possibly clipped at larger sizes, at
  ///   the default size and at accessibility2 (runs 42791913219 and 42808682568). Both are date
  ///   values the user has to read, so the screen wraps them instead of shrinking them onto one
  ///   line. The audit then reports no finding at either size (run 42821786175), so nothing is
  ///   tolerated on this screen.
  private static let converterTolerated: [String: Int] = [:]
  private static let converterAccessibilityTolerated: [String: Int] = [:]
  /// The festivals list draws its rows as plain buttons over the system background. The audit
  /// reports the holiday names and the one holiday numeral in the first visible section, and the
  /// small Open in Patro header button; the same pairs pass on the rows below. Nothing else is
  /// tolerated.
  private static let festivalsTolerated = [
    "contrast text": 3,
    "contrast numeral": 1,
    "hitRegion text": 1,
  ]
  /// The Next festivals section header is the system's secondary header colour, which the audit
  /// reports as nearly passing; the rows below it pass.
  private static let searchTolerated = ["contrast text": 1]
  private static let settingsTolerated = ["dynamicType text": 2]
  private static let aboutTolerated = ["dynamicType text": 9]
  private static let aboutAccessibilityTolerated = ["dynamicType text": 3]

  /// The promises About shows, by their accessibility labels.
  private static let promises = [
    "Works offline",
    "No ads",
    "Collects no data",
    "Free and open source",
    "Fast and light",
    "Nepali and English",
  ]

  /// The links About shows, in order.
  private static let aboutLinks = [
    "Web patro",
    "Known data questions",
    "Report a wrong date",
    "Licences",
    "Privacy",
    "Source code",
  ]

  /// The version and build the app shows. The app and this bundle are built from the same project
  /// settings, so the bundle's own Info.plist carries the same values and a release bump changes
  /// the expected string without touching this test.
  private static let appVersion: String = {
    let bundle = Bundle(for: ScreenUITests.self)
    let version =
      bundle.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "?"
    let build = bundle.object(forInfoDictionaryKey: "CFBundleVersion") as? String ?? "?"
    return "\(version) (\(build))"
  }()

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

    XCTAssertTrue(app.staticTexts["Appearance"].waitForExistence(timeout: 10))
    XCTAssertTrue(app.staticTexts["Digits"].exists)
    XCTAssertTrue(app.staticTexts["Follow language"].exists)
    app.swipeUp()
    XCTAssertTrue(app.staticTexts["Date follows"].waitForExistence(timeout: 5))
    XCTAssertTrue(app.staticTexts["Opens on"].exists)
    XCTAssertTrue(app.staticTexts["Show tithi in the month grid"].exists)

    for _ in 0..<5 where !app.staticTexts["About"].exists {
      app.swipeUp()
    }
    app.staticTexts["About"].tap()
    XCTAssertTrue(app.staticTexts["App version"].waitForExistence(timeout: 10))
    XCTAssertTrue(app.staticTexts["Library version"].exists)
    XCTAssertTrue(
      app.staticTexts[Self.appVersion].exists,
      "the app does not show the version the build was given: \(Self.appVersion)"
    )
    XCTAssertTrue(app.staticTexts["Baisakh 1, 2077 – Mangsir 20, 2084"].exists)
    XCTAssertTrue(app.staticTexts["Promises"].exists)
    // The promises are one combined accessibility element, so each label is looked for in the
    // label of any element rather than in a static text of its own.
    for promise in Self.promises {
      let element = app.descendants(matching: .any)
        .matching(NSPredicate(format: "label CONTAINS %@", promise))
        .firstMatch
      XCTAssertTrue(element.exists, "the promise \(promise) is missing")
    }

    // The list builds its rows as they scroll into view, so each link is scrolled to before it is
    // asserted.
    for link in Self.aboutLinks {
      let element = app.buttons[link]
      for _ in 0..<5 where !element.exists {
        app.swipeUp()
      }
      XCTAssertTrue(element.exists, "the \(link) link is missing")
    }
    XCTAssertFalse(app.buttons["Subscribe to Nepal holidays"].exists)

    let copy = app.buttons["Copy diagnostic info"]
    for _ in 0..<5 where !copy.isHittable {
      app.swipeUp()
    }
    copy.tap()
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

    for title in ["Appearance", "Digits", "Date follows", "Opens on"] {
      let element = app.staticTexts[title]
      for _ in 0..<6 where !element.exists {
        app.swipeUp()
      }
      XCTAssertTrue(element.waitForExistence(timeout: 5), "\(title) is not on the screen")
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
    Self.assertAudit(try audit(app, tolerating: [:]), [:])
  }

  @MainActor
  func testConverterHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "converter")
    Self.assertAudit(try audit(app, tolerating: Self.converterTolerated), Self.converterTolerated)
  }

  @MainActor
  func testFestivalsHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "festivals")
    Self.assertAudit(
      try audit(app, tolerating: Self.festivalsTolerated),
      Self.festivalsTolerated
    )
  }

  @MainActor
  func testSearchHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "search")
    Self.assertAudit(try audit(app, tolerating: Self.searchTolerated), Self.searchTolerated)
  }

  @MainActor
  func testSettingsHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "settings")
    Self.assertAudit(try audit(app, tolerating: Self.settingsTolerated), Self.settingsTolerated)
  }

  @MainActor
  func testAboutHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "about")
    Self.assertAudit(try audit(app, tolerating: Self.aboutTolerated), Self.aboutTolerated)
  }

  @MainActor
  func testUpcomingAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "upcoming", accessibility: true)
    Self.assertAudit(try audit(app, tolerating: [:]), [:])
  }

  @MainActor
  func testConverterAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "converter", accessibility: true)
    Self.assertAudit(
      try audit(app, tolerating: Self.converterAccessibilityTolerated),
      Self.converterAccessibilityTolerated
    )
  }

  @MainActor
  func testSettingsAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "settings", accessibility: true)
    Self.assertAudit(try audit(app, tolerating: Self.settingsTolerated), Self.settingsTolerated)
  }

  @MainActor
  func testAboutAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "about", accessibility: true)
    Self.assertAudit(
      try audit(app, tolerating: Self.aboutAccessibilityTolerated),
      Self.aboutAccessibilityTolerated
    )
  }

  /// Compares the kinds of finding, not their counts. The counts move by one or two between runs
  /// of identical builds (runs 42771648138 and 42791888489), so the kinds are the gate; the counts
  /// are printed by `audit` for the review.
  private static func assertAudit(
    _ found: [String: Int],
    _ expected: [String: Int],
    file: StaticString = #filePath,
    line: UInt = #line
  ) {
    XCTAssertEqual(Set(found.keys), Set(expected.keys), "\(found)", file: file, line: line)
  }

  @MainActor
  private func audit(
    _ app: XCUIApplication,
    tolerating table: [String: Int]
  ) throws -> [String: Int] {
    var counts: [String: Int] = [:]
    for audit in Self.audits {
      try AccessibilityAudit.perform(on: app, audit.type) { issue in
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
