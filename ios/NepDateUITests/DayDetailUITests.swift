import XCTest

/// MR-02: the day sheet opens from a day cell, speaks its fields, copies the date and
/// passes the system accessibility audit with only the findings listed below.
final class DayDetailUITests: XCTestCase {
  /// The audit categories, one per call, as in the month screen's audit.
  private static let audits: [(name: String, type: XCUIAccessibilityAuditType)] = [
    ("contrast", .contrast),
    ("dynamicType", .dynamicType),
    ("elementDetection", .elementDetection),
    ("hitRegion", .hitRegion),
    ("sufficientElementDescription", .sufficientElementDescription),
    ("textClipped", .textClipped),
    ("trait", .trait),
  ]

  /// The sheet itself has no tolerated finding. The only exception is the drawn Sunny mark and its
  /// event lines, which the audit reports as element detections at a zero frame; those are counted
  /// under `elementDetection other`, as on the month screen. The counts are printed for the review
  /// because they move by one or two between runs of identical builds.
  private static let toleratedCategories: Set<String> = ["elementDetection other"]

  @MainActor
  func testSheetShowsItsFieldsAndCopiesTheDate() throws {
    let app = try Self.openSheet()

    XCTAssertTrue(app.staticTexts["Asoj 25, 2083"].exists)
    XCTAssertTrue(app.staticTexts["असोज २५, २०८३"].exists)
    XCTAssertTrue(app.staticTexts["Sunday, 11 October 2026"].exists)
    XCTAssertTrue(app.staticTexts["Tithi"].exists)
    XCTAssertTrue(app.staticTexts["Pratipadha"].exists)
    XCTAssertTrue(app.staticTexts["Public holiday"].exists)
    XCTAssertTrue(app.staticTexts["Ghatasthapana Brata"].exists)
    XCTAssertTrue(app.staticTexts["Navaratri Start"].exists)
    XCTAssertTrue(app.buttons["Add to calendar"].exists)
    XCTAssertTrue(app.buttons["Close"].exists)

    let copy = app.buttons["Copy date"]
    XCTAssertTrue(copy.waitForExistence(timeout: 5))
    if !copy.isHittable {
      app.swipeUp()
    }
    copy.tap()

    // The copied date stays on the button as its accessibility value, so a copy that ran is
    // observable after the confirmation line has gone. The line itself lasts two seconds
    // (`CopyConfirmation.duration`), and the runner needed nine seconds between finding the button
    // and synthesising the tap on run 42821786175, because it re-read the interrupting elements
    // before every event. A query that starts after the tap returns therefore always misses it, so
    // the line is not asserted here: `CopyConfirmationTests` asserts that showing it starts
    // visible, and the sheet's `copied` reference image shows it drawn.
    let copied = NSPredicate(format: "value == %@", "Asoj 25, 2083")
    let wait = expectation(for: copied, evaluatedWith: copy)
    XCTAssertEqual(
      XCTWaiter.wait(for: [wait], timeout: 10),
      .completed,
      "the copy did not run"
    )

    // The XCUITest runner's own pasteboard read waits on the system paste prompt (runs
    // 39184279428 and 39186125450), so `DayClipboardTests` reads the pasteboard in the app process
    // instead.
  }

  @MainActor
  func testNextAndPreviousDayStayInTheSheet() throws {
    let app = try Self.openSheet()

    let next = app.buttons["Next day"]
    XCTAssertTrue(next.waitForExistence(timeout: 5))
    next.tap()
    XCTAssertTrue(app.staticTexts["Asoj 26, 2083"].waitForExistence(timeout: 5))

    let previous = app.buttons["Previous day"]
    XCTAssertTrue(previous.exists)
    previous.tap()
    XCTAssertTrue(app.staticTexts["Asoj 25, 2083"].waitForExistence(timeout: 5))
  }

  @MainActor
  func testSheetHasOnlyTheToleratedAuditFindings() throws {
    let app = try Self.openSheet()

    let found = try audit(app)
    XCTAssertEqual(Set(found.keys), Self.toleratedCategories, "\(found)")
  }

  @MainActor
  private func audit(_ app: XCUIApplication) throws -> [String: Int] {
    var counts: [String: Int] = [:]
    for audit in Self.audits {
      try AccessibilityAudit.perform(on: app, audit.type) { issue in
        let label = issue.element?.label ?? ""
        let frame = issue.element?.frame ?? .zero
        let kind = label.allSatisfy(\.isNumber) ? "numeral" : "text"
        print(
          "AUDIT \(audit.name) \(kind) label=\(label) frame=\(frame) "
            + "\(issue.compactDescription) | \(issue.detailedDescription)"
        )
        let isDrawnMark = audit.name == "elementDetection" && label.isEmpty && frame == .zero
        let key = isDrawnMark ? "elementDetection other" : "\(audit.name) \(kind)"
        guard isDrawnMark || Self.toleratedCategories.contains(key) else { return false }
        counts[key, default: 0] += 1
        return true
      }
    }
    print("AUDIT tolerated \(counts.sorted { $0.key < $1.key })")
    return counts
  }

  @MainActor
  private static func openSheet() throws -> XCUIApplication {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = "2083-06-20"
    app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
    app.launch()

    let cell = app.buttons
      .matching(NSPredicate(format: "label BEGINSWITH %@", "Asoj 25, 2083,"))
      .firstMatch
    XCTAssertTrue(cell.waitForExistence(timeout: 10), "Asoj 25 is not on the month grid")
    cell.tap()

    let heading = app.staticTexts["Asoj 25, 2083"]
    XCTAssertTrue(heading.waitForExistence(timeout: 10), "the day sheet did not open")
    return app
  }
}
