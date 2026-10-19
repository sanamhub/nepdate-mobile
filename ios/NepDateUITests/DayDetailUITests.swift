import UIKit
import XCTest

/// I1-04 and MR-02: the day sheet opens from a day cell, speaks its fields, copies the date and
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

  /// No finding is tolerated on the sheet. Add an entry here only with its evidence in this file
  /// and `ios/README.md`, as the month screen's table does.
  private static let tolerated: [String: Int] = [:]

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

    XCTAssertEqual(UIPasteboard.general.string, "Asoj 25, 2083")
  }

  @MainActor
  func testSheetHasOnlyTheToleratedAuditFindings() throws {
    let app = try Self.openSheet()

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
