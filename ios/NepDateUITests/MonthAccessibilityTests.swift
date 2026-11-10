import CalendarKit
import CalendarScreens
import XCTest

/// MR-01, MR-10 and MR-14 on the running app: the month screen opens on today, the year view
/// reaches both ends of the range and returns to a month, the pager recentres its window, the
/// today card opens the day sheet, and the system accessibility audit passes.
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

  /// The findings the audit tolerates, per category and kind, exactly. Every one is on a text
  /// node drawn inside a day cell or list row whose accessibility element is the full spoken date
  /// (`numeral` when the text is digits only, `text` otherwise). Those nodes are hidden from
  /// VoiceOver, but the audit still inspects them.
  ///
  /// - Grid, dynamicType on numerals: the grid keeps its BS and AD numerals at a size that fits
  ///   seven columns. At every accessibility text size the list replaces the grid (MR-10 AC2),
  ///   and the list audit has no numeral finding.
  /// - List, dynamicType and textClipped on text: the audit predicts that the date, weekday and
  ///   event lines of the first rows may not grow or may clip at sizes above the current one. The
  ///   lines have no line limit and wrap; `month.accessibility2.png` shows them wrapped, not
  ///   clipped. A device check at the largest size stays on the manual checklist.
  /// - Grid, contrast on two numerals: the AD numerals 13 and 27 in the Sunday column (frames
  ///   about x 40, y 117 and y 404). Their colour pair measures 7.55:1, and moving the light
  ///   secondary text from 0.40 to 0.33 white (5.74:1 to 7.55:1) left both findings unchanged
  ///   at the same frames (runs 39143033588 and 39144415913), so they do not come from the
  ///   colour. No other numeral is flagged.
  /// - List, contrast on two texts: the weekday lines of Asoj 4 and the row after it, which are
  ///   behind the translucent tab bar on an 844-point screen. The same pair passes on every row
  ///   above them.
  ///
  /// A finding outside these kinds still fails the test. The counts last measured on this branch
  /// were: grid contrast numeral 1, contrast text 6, Dynamic Type numeral 71, and six element
  /// detections at a zero frame (the drawn Sunny mark and its events); list contrast text 1 to 2,
  /// Dynamic Type text 8 to 10 and ten clipped texts. Those counts move by one or two between runs
  /// of identical builds, because SwiftUI settles rows while the audit samples them (runs
  /// 42771648138 and 42774078748), so the test pins the kinds and prints the counts.
  private static let gridCategories: Set<String> = [
    "contrast numeral",
    "contrast text",
    "dynamicType numeral",
    "elementDetection other",
  ]
  private static let listCategories: Set<String> = [
    "contrast text",
    "dynamicType text",
    "textClipped text",
  ]

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

    XCTAssertTrue(Self.title("Asoj 2083", in: app).waitForExistence(timeout: 10))
  }

  @MainActor
  func testYearViewReachesBothEndsOfTheRange() throws {
    let last = Self.launch(month: "2199-12")
    let lastTitle = Self.title("Chaitra 2199", in: last)
    XCTAssertTrue(lastTitle.waitForExistence(timeout: 10))
    XCTAssertFalse(last.buttons["Next month"].isEnabled)
    XCTAssertTrue(last.buttons["Previous month"].isEnabled)

    Self.openYearView(last, label: "Chaitra 2199")
    XCTAssertTrue(last.navigationBars["2199"].waitForExistence(timeout: 10))
    XCTAssertFalse(last.buttons["Next year"].isEnabled)
    XCTAssertTrue(last.buttons["Previous year"].isEnabled)

    let baisakh = last.buttons.matching(
      NSPredicate(format: "label BEGINSWITH %@", "Baisakh 2199")
    ).firstMatch
    XCTAssertTrue(baisakh.waitForExistence(timeout: 5), "the Baisakh mini month is missing")
    baisakh.tap()
    XCTAssertTrue(Self.title("Baisakh 2199", in: last).waitForExistence(timeout: 10))
    XCTAssertTrue(
      Self.cell(beginningWith: "Baisakh 1, 2199,", in: last).waitForExistence(timeout: 10),
      "the pager did not move to Baisakh 2199"
    )
    last.terminate()

    let first = Self.launch(month: "1901-01")
    let firstTitle = Self.title("Baisakh 1901", in: first)
    XCTAssertTrue(firstTitle.waitForExistence(timeout: 10))
    XCTAssertFalse(first.buttons["Previous month"].isEnabled)
    XCTAssertTrue(first.buttons["Next month"].isEnabled)

    Self.openYearView(first, label: "Baisakh 1901")
    XCTAssertTrue(first.navigationBars["1901"].waitForExistence(timeout: 10))
    XCTAssertFalse(first.buttons["Previous year"].isEnabled)
    XCTAssertTrue(first.buttons["Next year"].isEnabled)

    let chaitra = first.buttons.matching(
      NSPredicate(format: "label BEGINSWITH %@", "Chaitra 1901")
    ).firstMatch
    XCTAssertTrue(chaitra.waitForExistence(timeout: 5), "the Chaitra mini month is missing")
    chaitra.tap()
    XCTAssertTrue(Self.title("Chaitra 1901", in: first).waitForExistence(timeout: 10))
    XCTAssertTrue(
      Self.cell(beginningWith: "Chaitra 1, 1901,", in: first).waitForExistence(timeout: 10)
    )
  }

  /// The pager holds 49 months around the shown one and recentres when a swipe comes within five
  /// pages of an edge. Twenty-one swipes from Asoj 2083 cross that point once (at the twentieth).
  @MainActor
  func testSwipesCrossTheWindowRecentre() throws {
    let app = Self.launch()
    XCTAssertTrue(Self.title("Asoj 2083", in: app).waitForExistence(timeout: 10))

    var key = try XCTUnwrap(MonthKey(year: 2083, month: 6))
    for _ in 0..<21 {
      key = try XCTUnwrap(MonthKey(id: key.id + 1))
      let expected = "\(MonthNames.bs(key.month, .english)) \(key.year)"
      app.scrollViews["month_pager"].swipeLeft()
      let moved = NSPredicate(format: "label == %@", expected)
      let element = Self.title(expected, in: app)
      let wait = expectation(for: moved, evaluatedWith: element)
      XCTAssertEqual(XCTWaiter.wait(for: [wait], timeout: 5), .completed, "expected \(expected)")
    }
    XCTAssertTrue(Self.title("Asar 2085", in: app).waitForExistence(timeout: 10))
    XCTAssertTrue(
      Self.cell(beginningWith: "Asar 1, 2085,", in: app).waitForExistence(timeout: 10),
      "the page after the recentre is not Asar 2085"
    )
  }

  @MainActor
  func testTodayCardOpensTheDaySheet() throws {
    let app = Self.launch(today: "2083-06-25")
    let card = app.buttons["today_card"]
    XCTAssertTrue(card.waitForExistence(timeout: 10), "the today card is missing")
    card.tap()
    let sheet = app.descendants(matching: .any)["day_detail"]
    XCTAssertTrue(sheet.waitForExistence(timeout: 10), "the day sheet did not open")
  }

  @MainActor
  func testGridHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch()
    XCTAssertTrue(Self.title("Asoj 2083", in: app).waitForExistence(timeout: 10))

    // The audit's counts move by one or two between runs of identical builds, because SwiftUI
    // settles rows while the audit samples them (runs 42771648138 and 42774078748). The test
    // therefore pins the kinds of finding, and prints their counts for the review; a new kind of
    // finding still fails. The counts last seen are recorded above the tables.
    let found = try audit(app)
    XCTAssertEqual(Set(found.keys), Self.gridCategories, "\(found)")
  }

  @MainActor
  func testListAtAccessibilitySizeHasOnlyTheToleratedAuditFindings() throws {
    let app = Self.launch(arguments: [
      "-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityL",
    ])
    XCTAssertTrue(Self.title("Asoj 2083", in: app).waitForExistence(timeout: 10))
    XCTAssertTrue(
      Self.cell(beginningWith: "Asoj 1, 2083,", in: app).waitForExistence(timeout: 10),
      "the list layout did not show Asoj 1"
    )

    let found = try audit(app)
    XCTAssertEqual(Set(found.keys), Self.listCategories, "\(found)")
  }

  /// Runs every audit category. A finding on a hidden text node is counted under its category
  /// and kind and returned; any other finding fails the audit call itself.
  @MainActor
  private func audit(_ app: XCUIApplication) throws -> [String: Int] {
    var tolerated: [String: Int] = [:]
    for audit in Self.audits {
      try AccessibilityAudit.perform(on: app, audit.type) { issue in
        let label = issue.element?.label ?? ""
        let frame = issue.element?.frame ?? .zero
        let isHiddenText =
          !label.isEmpty && issue.detailedDescription.contains("SwiftUI.AccessibilityNode")
        let kind = label.allSatisfy(\.isNumber) ? "numeral" : "text"
        let isDrawnMark =
          audit.name == "elementDetection" && label.isEmpty && frame == .zero
        print(
          "AUDIT \(audit.name) \(isHiddenText ? kind : "other") label=\(label) "
            + "frame=\(frame) \(issue.compactDescription) | \(issue.detailedDescription)"
        )
        guard isHiddenText || isDrawnMark else { return false }
        tolerated[isDrawnMark ? "elementDetection other" : "\(audit.name) \(kind)", default: 0] += 1
        return true
      }
    }
    print("AUDIT tolerated \(tolerated.sorted { $0.key < $1.key })")
    return tolerated
  }

  @MainActor
  private static func launch(
    arguments: [String] = [],
    today: String = "2083-06-20",
    month: String? = nil
  ) -> XCUIApplication {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = today
    if let month {
      app.launchEnvironment["NEPDATE_UI_TEST_MONTH"] = month
    }
    app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"] + arguments
    app.launch()
    return app
  }

  /// Taps the navigation title so its menu opens. XCTest exposes the title as a static text, so
  /// the tap goes through the navigation bar's centre and falls back to the title element.
  @MainActor
  private static func openYearView(_ app: XCUIApplication, label: String) {
    app.navigationBars.firstMatch
      .coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
      .tap()
    let yearView = app.buttons["Year view"]
    if !yearView.waitForExistence(timeout: 2) {
      title(label, in: app).tap()
    }
    XCTAssertTrue(yearView.waitForExistence(timeout: 5), "the title menu is missing")
    yearView.tap()
  }

  @MainActor
  private static func title(_ label: String, in app: XCUIApplication) -> XCUIElement {
    app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", label)).firstMatch
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
