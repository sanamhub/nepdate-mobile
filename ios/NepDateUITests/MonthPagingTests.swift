import CalendarKit
import CalendarScreens
import XCTest

/// Paging the month without buttons: a swipe moves one month each way, and the pager stops at both
/// ends of the range instead of showing a month the app cannot draw. The bar's own contents are in
/// `MonthToolbarTests`; the named VoiceOver actions are checked in `NepDateTests`.
final class MonthPagingTests: XCTestCase {
  @MainActor
  func testSwipeMovesOneMonthEachWay() throws {
    let app = Self.launch()
    XCTAssertTrue(Self.title("Asoj 2083", in: app).waitForExistence(timeout: 10))
    let pager = app.scrollViews["month_pager"]
    XCTAssertTrue(pager.waitForExistence(timeout: 10), "the pager is missing")

    // The swipe is slow so that its momentum cannot carry the pager past the next month; the same
    // reason is recorded in `MonthAccessibilityTests.testSwipesCrossTheWindowRecentre`.
    pager.swipeLeft(velocity: .slow)
    XCTAssertTrue(
      Self.title("Kartik 2083", in: app).waitForExistence(timeout: 10),
      "a left swipe did not open Kartik 2083"
    )
    XCTAssertTrue(
      Self.cell(beginningWith: "Kartik 1, 2083,", in: app).waitForExistence(timeout: 10),
      "the page after the swipe is not Kartik 2083"
    )

    pager.swipeRight(velocity: .slow)
    XCTAssertTrue(
      Self.title("Asoj 2083", in: app).waitForExistence(timeout: 10),
      "a right swipe did not return to Asoj 2083"
    )
    app.terminate()
  }

  @MainActor
  func testSwipeStaysInsideTheRangeAtBothEnds() throws {
    let last = Self.launch(month: "2199-12")
    XCTAssertTrue(Self.title("Chaitra 2199", in: last).waitForExistence(timeout: 10))
    let lastPager = last.scrollViews["month_pager"]
    XCTAssertTrue(lastPager.waitForExistence(timeout: 10), "the pager is missing at the end")
    lastPager.swipeLeft(velocity: .slow)
    Thread.sleep(forTimeInterval: 2)
    XCTAssertTrue(
      Self.title("Chaitra 2199", in: last).exists,
      "a left swipe paged past Chaitra 2199, the end of the range"
    )
    XCTAssertTrue(
      Self.cell(beginningWith: "Chaitra 1, 2199,", in: last).exists,
      "the pager no longer draws Chaitra 2199 after the swipe"
    )
    last.terminate()

    let first = Self.launch(month: "1901-01")
    XCTAssertTrue(Self.title("Baisakh 1901", in: first).waitForExistence(timeout: 10))
    let firstPager = first.scrollViews["month_pager"]
    XCTAssertTrue(firstPager.waitForExistence(timeout: 10), "the pager is missing at the start")
    firstPager.swipeRight(velocity: .slow)
    Thread.sleep(forTimeInterval: 2)
    XCTAssertTrue(
      Self.title("Baisakh 1901", in: first).exists,
      "a right swipe paged before Baisakh 1901, the start of the range"
    )
    XCTAssertTrue(
      Self.cell(beginningWith: "Baisakh 1, 1901,", in: first).exists,
      "the pager no longer draws Baisakh 1901 after the swipe"
    )
    first.terminate()
  }

  /// The month title. The screen draws an inline title, so the navigation bar carries the month as
  /// its identifier, and the identifier follows the month as the pager changes it.
  @MainActor
  private static func title(_ label: String, in app: XCUIApplication) -> XCUIElement {
    app.navigationBars[label]
  }

  @MainActor
  private static func cell(beginningWith prefix: String, in app: XCUIApplication) -> XCUIElement {
    app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", prefix)).firstMatch
  }

  @MainActor
  private static func launch(month: String? = nil) -> XCUIApplication {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = "2083-06-20"
    if let month {
      app.launchEnvironment["NEPDATE_UI_TEST_MONTH"] = month
    }
    app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
    app.launch()
    return app
  }
}
