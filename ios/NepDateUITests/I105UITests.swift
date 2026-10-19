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
    let app = Self.launch()
    app.tabBars.buttons["Upcoming"].tap()

    XCTAssertTrue(app.staticTexts["Asoj 2083"].waitForExistence(timeout: 10))
    XCTAssertTrue(Self.entry("Indira Ekadashi Brata", in: app).exists)
    XCTAssertTrue(Self.entry("Ekadashi Shraddha", in: app).exists)
    XCTAssertTrue(Self.entry("Ghatasthapana Brata", in: app).exists)
    XCTAssertTrue(
      app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "in 5 days")).firstMatch.exists
    )

    Self.entry("Ghatasthapana Brata", in: app).tap()
    XCTAssertTrue(app.staticTexts["Asoj 25, 2083"].waitForExistence(timeout: 10))
  }

  @MainActor
  func testSettingsAndAboutShowTheirValues() throws {
    let app = Self.launch()
    Self.openSettings(app)

    XCTAssertTrue(app.staticTexts["Digits"].exists)
    XCTAssertTrue(app.staticTexts["Follow language"].exists)
    XCTAssertTrue(app.staticTexts["Date follows"].exists)
    XCTAssertTrue(app.staticTexts["Show tithi in the month grid"].exists)

    app.buttons["About"].tap()
    XCTAssertTrue(app.staticTexts["App version"].waitForExistence(timeout: 10))
    XCTAssertTrue(app.staticTexts["Library version"].exists)
    XCTAssertTrue(app.staticTexts["0.1.0 (1)"].exists)
    XCTAssertTrue(app.staticTexts["Baisakh 1, 2077 – Mangsir 20, 2084"].exists)
    XCTAssertTrue(app.links["Known data questions"].exists)
    XCTAssertTrue(app.links["Report a wrong date"].exists)
    XCTAssertTrue(app.links["Licences"].exists)
    XCTAssertTrue(app.links["Privacy"].exists)
    XCTAssertTrue(app.links["Source code"].exists)
    XCTAssertFalse(app.buttons["Subscribe to Nepal holidays"].exists)

    app.buttons["Copy diagnostic info"].tap()
    XCTAssertTrue(app.staticTexts["Copied"].waitForExistence(timeout: 2))
  }

  @MainActor
  func testUpcomingHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch()
    app.tabBars.buttons["Upcoming"].tap()
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testConverterHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch()
    app.tabBars.buttons["Converter"].tap()
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testSettingsHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch()
    Self.openSettings(app)
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testAboutHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch()
    Self.openSettings(app)
    app.buttons["About"].tap()
    XCTAssertTrue(app.staticTexts["App version"].waitForExistence(timeout: 10))
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testUpcomingAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(accessibility: true)
    app.tabBars.buttons["Upcoming"].tap()
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testConverterAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(accessibility: true)
    app.tabBars.buttons["Converter"].tap()
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testSettingsAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(accessibility: true)
    Self.openSettings(app)
    XCTAssertEqual(try audit(app), Self.tolerated)
  }

  @MainActor
  func testAboutAtAccessibility2HasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(accessibility: true)
    Self.openSettings(app)
    app.buttons["About"].tap()
    XCTAssertTrue(app.staticTexts["App version"].waitForExistence(timeout: 10))
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
  private static func openSettings(_ app: XCUIApplication) {
    let button = app.buttons["Settings"]
    XCTAssertTrue(button.waitForExistence(timeout: 10))
    button.tap()
    XCTAssertTrue(app.navigationBars["Settings"].waitForExistence(timeout: 10))
  }

  @MainActor
  private static func entry(_ name: String, in app: XCUIApplication) -> XCUIElement {
    app.buttons
      .matching(NSPredicate(format: "label BEGINSWITH %@", name))
      .firstMatch
  }

  @MainActor
  private static func launch(accessibility: Bool = false) -> XCUIApplication {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = "2083-06-20"
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
