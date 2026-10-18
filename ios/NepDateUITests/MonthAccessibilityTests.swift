import CalendarKit
import XCTest

/// MR-01 and MR-10 on the running app: the month screen opens on today, the picker reaches both
/// ends of the range, the pager recentres its window, and the system accessibility audit passes.
///
/// Every test pins today to Asoj 20, 2083 (`NEPDATE_UI_TEST_TODAY`, read by Debug builds only), so
/// the audit inspects the same month on every run and its counts below are exact.
final class MonthAccessibilityTests: XCTestCase {
  private static let today = MonthDate(year: 2083, month: 6, day: 20)

  /// The audit categories, one per call: the combined audit exceeds its time budget on this
  /// screen (run 39112640342), and a per-category call still reports every issue in it.
  private static let audits: [(name: String, type: XCUIAccessibilityAuditType)] = [
    ("contrast", .contrast),
    ("dynamicType", .dynamicType),
    ("elementDetection", .elementDetection),
    ("hitRegion", .hitRegion),
    ("sufficientElementDescription", .sufficientElementDescription),
    ("textClipped", .textClipped),
    ("trait", .trait),
  ]

  /// The findings the grid audit tolerates, per category, exactly. Each one is on a day numeral
  /// (BS or AD) drawn inside a day cell whose accessibility element is the full spoken date; the
  /// numeral itself is hidden from VoiceOver, but the audit still inspects the text node.
  ///
  /// - dynamicType: the grid keeps its numerals at a fixed size so seven columns fit. The grid
  ///   is replaced by the list at every accessibility text size (MR-10 AC2), and the list audit
  ///   below has none of these findings.
  ///
  /// Any other finding, or a different count, fails the test. Contrast has no tolerance: the
  /// colours are measured in `ios/README.md` and all reach WCAG 4.5:1.
  private static let gridTolerated: [String: Int] = ["dynamicType": 0]

  @MainActor
  func testMonthScreenOpensOnToday() throws {
    let model = try Self.model()
    let first = try XCTUnwrap(model.layout.cells.first { $0.isInMonth })
    let label = model.accessibilityLabel(for: first, publicHoliday: "public holiday")

    let app = Self.launch()

    XCTAssertTrue(
      app.buttons[label].waitForExistence(timeout: 10),
      "the pager did not open on \(model.monthTitle)"
    )

    let title = app.buttons["Choose month and year"]
    XCTAssertTrue(title.waitForExistence(timeout: 10))
    XCTAssertEqual(title.value as? String, "Asoj 2083")
  }

  @MainActor
  func testPickerReachesBothEndsOfTheRange() throws {
    let app = Self.launch()
    let title = app.buttons["Choose month and year"]
    XCTAssertTrue(title.waitForExistence(timeout: 10))

    pick(month: "Chaitra", year: "2199", in: app)
    XCTAssertEqual(title.value as? String, "Chaitra 2199")
    XCTAssertTrue(
      Self.cell(beginningWith: "Chaitra 1, 2199,", in: app).waitForExistence(timeout: 10),
      "the pager did not move to Chaitra 2199"
    )
    XCTAssertFalse(app.buttons["Next month"].isEnabled)
    XCTAssertTrue(app.buttons["Previous month"].isEnabled)

    pick(month: "Baisakh", year: "1901", in: app)
    XCTAssertEqual(title.value as? String, "Baisakh 1901")
    XCTAssertTrue(
      Self.cell(beginningWith: "Baisakh 1, 1901,", in: app).waitForExistence(timeout: 10),
      "the pager did not move to Baisakh 1901"
    )
    XCTAssertFalse(app.buttons["Previous month"].isEnabled)
    XCTAssertTrue(app.buttons["Next month"].isEnabled)

    app.buttons["Today"].tap()
    XCTAssertEqual(title.value as? String, "Asoj 2083")
  }

  /// The pager holds 49 months around the shown one and recentres when a swipe comes within five
  /// pages of an edge. Twenty-one swipes from Asoj 2083 cross that point once (at the twentieth).
  @MainActor
  func testSwipesCrossTheWindowRecentre() throws {
    let app = Self.launch()
    let title = app.buttons["Choose month and year"]
    XCTAssertTrue(title.waitForExistence(timeout: 10))

    var key = try XCTUnwrap(MonthKey(year: 2083, month: 6))
    for _ in 0..<21 {
      key = try XCTUnwrap(MonthKey(id: key.id + 1))
      let expected = "\(MonthNames.bs(key.month, .english)) \(key.year)"
      app.scrollViews.firstMatch.swipeLeft()
      let moved = NSPredicate(format: "value == %@", expected)
      let wait = expectation(for: moved, evaluatedWith: title)
      XCTAssertEqual(XCTWaiter.wait(for: [wait], timeout: 5), .completed, "expected \(expected)")
    }
    XCTAssertEqual(title.value as? String, "Asar 2085")
    XCTAssertTrue(
      Self.cell(beginningWith: "Asar 1, 2085,", in: app).waitForExistence(timeout: 10),
      "the page after the recentre is not Asar 2085"
    )
  }

  @MainActor
  func testGridHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch()
    XCTAssertTrue(app.buttons["Choose month and year"].waitForExistence(timeout: 10))

    let tolerated = try audit(app)
    XCTAssertEqual(tolerated, Self.gridTolerated.filter { $0.value > 0 })
  }

  @MainActor
  func testListAtAccessibilitySizeHasNoAuditFindings() throws {
    let app = Self.launch(arguments: [
      "-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityL",
    ])
    XCTAssertTrue(app.buttons["Choose month and year"].waitForExistence(timeout: 10))
    XCTAssertTrue(
      Self.cell(beginningWith: "Asoj 1, 2083,", in: app).waitForExistence(timeout: 10),
      "the list layout did not show Asoj 1"
    )

    let tolerated = try audit(app)
    XCTAssertEqual(tolerated, [:])
  }

  /// Runs every audit category. A finding on a hidden day numeral is counted per category and
  /// returned; any other finding fails the audit call itself.
  @MainActor
  private func audit(_ app: XCUIApplication) throws -> [String: Int] {
    var tolerated: [String: Int] = [:]
    for audit in Self.audits {
      try app.performAccessibilityAudit(for: audit.type) { issue in
        let label = issue.element?.label ?? ""
        let frame = issue.element?.frame ?? .zero
        let isHiddenNumeral =
          !label.isEmpty
          && label.allSatisfy(\.isNumber)
          && issue.detailedDescription.contains("SwiftUI.AccessibilityNode")
        print(
          "AUDIT \(audit.name) \(isHiddenNumeral ? "numeral" : "other") label=\(label) "
            + "frame=\(frame) \(issue.compactDescription) | \(issue.detailedDescription)"
        )
        guard isHiddenNumeral else { return false }
        tolerated[audit.name, default: 0] += 1
        return true
      }
    }
    print("AUDIT tolerated \(tolerated.sorted { $0.key < $1.key })")
    return tolerated
  }

  @MainActor
  private func pick(month: String, year: String, in app: XCUIApplication) {
    app.buttons["Choose month and year"].tap()
    let wheels = app.pickerWheels
    XCTAssertTrue(wheels.element(boundBy: 1).waitForExistence(timeout: 10))
    wheels.element(boundBy: 0).adjust(toPickerWheelValue: month)
    wheels.element(boundBy: 1).adjust(toPickerWheelValue: year)
    app.buttons["Go"].tap()
    XCTAssertTrue(app.buttons["Choose month and year"].waitForExistence(timeout: 10))
  }

  @MainActor
  private static func launch(arguments: [String] = []) -> XCUIApplication {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = "2083-06-20"
    app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"] + arguments
    app.launch()
    return app
  }

  @MainActor
  private static func cell(beginningWith prefix: String, in app: XCUIApplication) -> XCUIElement {
    app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", prefix)).firstMatch
  }

  private static func model() throws -> MonthViewModel {
    try XCTUnwrap(
      MonthViewModel(today: today, language: .english, digits: .english, showTithi: false)
    )
  }
}
