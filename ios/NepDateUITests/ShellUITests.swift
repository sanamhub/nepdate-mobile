import XCTest

/// MR-15, MR-18 and the shell's state rules: the tabs, the first-run cover and a typed Converter
/// input that must survive a settings change.
final class ShellUITests: XCTestCase {
  private static let audits: [(name: String, type: XCUIAccessibilityAuditType)] = [
    ("contrast", .contrast),
    ("dynamicType", .dynamicType),
    ("elementDetection", .elementDetection),
    ("hitRegion", .hitRegion),
    ("sufficientElementDescription", .sufficientElementDescription),
    ("textClipped", .textClipped),
    ("trait", .trait),
  ]

  @MainActor
  func testFirstRunHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(screen: "firstRun")
    XCTAssertTrue(
      app.buttons["first_run_open"].waitForExistence(timeout: 10),
      "the first-run screen did not open"
    )

    let counts: [String: Int] = [:]
    for audit in Self.audits {
      try AccessibilityAudit.perform(on: app, audit.type) { issue in
        let label = issue.element?.label ?? ""
        print(
          "AUDIT \(audit.name) label=\(label) \(issue.compactDescription) | "
            + issue.detailedDescription
        )
        return false
      }
    }
    print("AUDIT tolerated \(counts.sorted { $0.key < $1.key })")
    XCTAssertEqual(counts, [:])
  }

  @MainActor
  func testConverterInputSurvivesASettingsChange() throws {
    let app = Self.launch()

    let converter = app.tabBars.buttons["Converter"]
    XCTAssertTrue(converter.waitForExistence(timeout: 10), "the Converter tab is missing")
    converter.tap()

    let field = app.textFields["converter_bs_input"]
    XCTAssertTrue(field.waitForExistence(timeout: 10))
    field.tap()
    field.typeText("2083-06-20")

    app.buttons["Settings"].tap()
    let dark = app.buttons["Dark"]
    XCTAssertTrue(dark.waitForExistence(timeout: 10), "the Appearance rows are missing")
    dark.tap()
    app.buttons["Close"].tap()

    XCTAssertTrue(field.waitForExistence(timeout: 10), "the Converter was rebuilt")
    XCTAssertEqual(field.value as? String, "2083-06-20")
  }

  @MainActor
  func testFirstRunShowsOnceAndOpensThePatro() throws {
    let app = Self.launch(firstRun: true)

    XCTAssertTrue(app.staticTexts["A light, fast patro of your own"].waitForExistence(timeout: 10))
    let open = app.buttons["first_run_open"]
    XCTAssertTrue(open.exists, "the Open the patro button is missing")
    XCTAssertTrue(app.buttons["first_run_language"].exists)

    open.tap()
    XCTAssertTrue(
      app.tabBars.buttons["Patro"].waitForExistence(timeout: 10),
      "the patro did not open after first run"
    )
  }

  @MainActor
  private static func launch(screen: String? = nil, firstRun: Bool = false) -> XCUIApplication {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = "2083-06-25"
    if let screen {
      app.launchEnvironment["NEPDATE_UI_TEST_SCREEN"] = screen
    }
    if firstRun {
      app.launchEnvironment["NEPDATE_UI_TEST_FIRST_RUN"] = "show"
    }
    app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
    app.launch()
    return app
  }
}
