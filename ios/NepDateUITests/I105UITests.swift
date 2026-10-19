import XCTest

/// I1-05 and MR-03, MR-04, MR-06, MR-07, MR-10, MR-11, MR-12: screen content and the seven
/// accessibility audit categories at the default and accessibility text sizes.
final class I105UITests: XCTestCase {
  private static let audits: [(name: String, type: XCUIAccessibilityAuditType)] = [
    ("contrast", .contrast),
    ("dynamicType", .dynamicType),
    ("elementDetection", .elementDetection),
    ("hitRegion", .hitRegion),
    ("sufficientElementDescription", .sufficientElementDescription),
    ("textClipped", .textClipped),
    ("trait", .trait),
  ]

  /// No finding is tolerated on these screens. Add an entry only with its evidence here and in
  /// `ios/README.md`.
  private static let tolerated: [String: Int] = [:]

  @MainActor
  func testUpcomingShowsTodayAndOpensTheDay() throws {
    let app = Self.launch(screen: "upcoming")

    XCTAssertTrue(app.staticTexts["Asoj 2083"].waitForExistence(timeout: 10))
    XCTAssertTrue(app.staticTexts["Indira Ekadashi Brata"].exists)
    XCTAssertTrue(app.staticTexts["Ekadashi Shraddha"].exists)
    XCTAssertTrue(app.staticTexts["in 5 days"].exists)
    app.staticTexts["Ghatasthapana Brata"].tap()
    XCTAssertTrue(app.staticTexts["Asoj 25, 2083"].waitForExistence(timeout: 10))
  }

  @MainActor
  func testSettingsAndAboutShowTheirValues() throws {
    let app = Self.launch(screen: "settings")

    XCTAssertTrue(app.staticTexts["Digits"].exists)
    XCTAssertTrue(app.staticTexts["Follow language"].exists)
    XCTAssertTrue(app.staticTexts["Date follows"].exists)
    XCTAssertTrue(app.staticTexts["Show tithi in the month grid"].exists)

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
    XCTAssertTrue(app.staticTexts["Copied"].waitForExistence(timeout: 2))
  }

  @MainActor
  func testUpcomingHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "upcoming")
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testConverterHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "converter")
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testSettingsHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "settings")
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testAboutHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "about")
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testUpcomingAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "upcoming", accessibility: true)
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testConverterAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "converter", accessibility: true)
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testSettingsAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "settings", accessibility: true)
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testAboutAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "about", accessibility: true)
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  private func audit(_ app: XCUIApplication) throws -> [String: Int] {
    var counts: [String: Int] = [:]
    for audit in Self.audits {
      try app.performAccessibilityAudit(for: audit.type) { issue in
        let label = issue.element?.label ?? ""
        let frame = issue.element?.frame ?? .zero
        let kind = label.allSatisfy(\.isNumber) ? "numeral" : "text"
        print(
          "AUDIT \(audit.name) \(kind) label=\(label) frame=\(frame) "
            + "\(issue.compactDescription) | \(issue.detailedDescription)"
        )
        guard Self.tolerated["\(audit.name) \(kind)"] != nil else { return false }
        counts["\(audit.name) \(kind)", default: 0] += 1
        return true
      }
    }
    print("AUDIT tolerated \(counts.sorted { $0.key < $1.key })")
    return counts
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
