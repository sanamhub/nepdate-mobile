import CalendarKit
import CalendarScreens
import XCTest

/// The month bar on a month away from today, which has four buttons to place: Settings from the
/// shell, Previous month, Today and Next month.
///
/// At the default text size all four are in the bar and Next month is one tap. At accessibility2
/// the system no longer fits them: it keeps Settings and Previous month and moves Today and Next
/// month into the bar's More menu, so Next month takes two taps at that size (measured on the
/// iPhone 17, run 42947768870).
///
/// The screenshots of the bar, on today's month and on a month away, at both text sizes, are
/// attached to the test run for the design review.
final class MonthToolbarTests: XCTestCase {
  private static let todayTitle = "Asoj 2083"

  /// A month at each end of the range and one in the middle, all away from today. The end of the
  /// range has no next month to open.
  private static let awayMonths = [
    (year: 2199, month: 12, hasNext: false),
    (year: 2199, month: 11, hasNext: true),
    (year: 1901, month: 2, hasNext: true),
  ]

  /// `UICTContentSizeCategoryAccessibilityL` is Dynamic Type's accessibility2.
  private static let accessibility2 = [
    "-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityL",
  ]

  /// Every button of a month away from today is in the bar, and Next month is one tap.
  @MainActor
  func testBarKeepsEveryButtonAwayFromToday() throws {
    for month in Self.awayMonths {
      let key = try XCTUnwrap(MonthKey(year: month.year, month: month.month))
      let title = Self.title(of: key)
      let app = Self.launch(month: key.description)
      let bar = app.navigationBars[title]
      XCTAssertTrue(bar.waitForExistence(timeout: 10), "the bar does not name \(title)")

      for label in ["Previous month", "Next month", "Today"] {
        let button = bar.buttons[label]
        XCTAssertTrue(
          button.waitForExistence(timeout: 5),
          "\(label) is missing from the bar of \(title): \(Self.labels(of: bar))"
        )
        XCTAssertTrue(button.isHittable, "\(label) is not hittable in the bar of \(title)")
      }
      XCTAssertEqual(
        bar.buttons["Next month"].isEnabled,
        month.hasNext,
        "Next month is enabled wrongly in the bar of \(title)"
      )
      XCTAssertFalse(
        bar.buttons["More"].exists,
        "the bar of \(title) moved a button into a More menu: \(Self.labels(of: bar))"
      )

      if month.hasNext {
        let next = try XCTUnwrap(MonthKey(id: key.id + 1))
        bar.buttons["Next month"].tap()
        XCTAssertTrue(
          app.navigationBars[Self.title(of: next)].waitForExistence(timeout: 10),
          "one tap on Next month did not open \(Self.title(of: next))"
        )
      }

      let today = app.navigationBars.buttons["Today"]
      XCTAssertTrue(
        today.waitForExistence(timeout: 5),
        "Today left the bar of \(title) after paging"
      )
      today.tap()
      XCTAssertTrue(
        app.navigationBars[Self.todayTitle].waitForExistence(timeout: 10),
        "Today did not return to \(Self.todayTitle)"
      )
      app.terminate()
    }
  }

  /// At accessibility2 the bar holds Settings and Previous month, and Today and Next month move
  /// into the More menu.
  @MainActor
  func testBarCollapsesTheTodayButtonAtAccessibility2() throws {
    let key = try XCTUnwrap(MonthKey(year: 2199, month: 11))
    let title = Self.title(of: key)
    let app = Self.launch(arguments: Self.accessibility2, month: key.description)
    let bar = app.navigationBars[title]
    XCTAssertTrue(bar.waitForExistence(timeout: 10), "the bar does not name \(title)")

    XCTAssertTrue(
      bar.buttons["Previous month"].waitForExistence(timeout: 5),
      "Previous month is missing from the bar of \(title): \(Self.labels(of: bar))"
    )
    XCTAssertFalse(
      bar.buttons["Next month"].exists,
      "Next month is in the bar of \(title) now: \(Self.labels(of: bar))"
    )

    let more = bar.buttons["More"]
    XCTAssertTrue(
      more.waitForExistence(timeout: 5),
      "the bar of \(title) has no More menu: \(Self.labels(of: bar))"
    )
    more.tap()
    for label in ["Today", "Next month"] {
      XCTAssertTrue(
        app.buttons[label].waitForExistence(timeout: 5),
        "the More menu of \(title) does not list \(label)"
      )
    }
  }

  /// The bar on today's month and on a month away from today, at the default text size and at
  /// accessibility2, attached to the test run for the design review.
  @MainActor
  func testMonthBarScreenshots() throws {
    let away = try XCTUnwrap(MonthKey(year: 2199, month: 11))
    let awayTitle = Self.title(of: away)
    let cases: [(name: String, month: String?, title: String, arguments: [String])] = [
      ("month-bar-current-default", nil, Self.todayTitle, []),
      ("month-bar-away-default", away.description, awayTitle, []),
      ("month-bar-current-accessibility2", nil, Self.todayTitle, Self.accessibility2),
      ("month-bar-away-accessibility2", away.description, awayTitle, Self.accessibility2),
    ]

    for item in cases {
      let app = Self.launch(arguments: item.arguments, month: item.month)
      let bar = app.navigationBars[item.title]
      XCTAssertTrue(
        bar.waitForExistence(timeout: 10),
        "\(item.name): the bar does not name \(item.title)"
      )
      print("BAR \(item.name) width=\(bar.frame.width) buttons=\(Self.labels(of: bar))")

      let shot = XCTAttachment(screenshot: bar.screenshot())
      shot.name = item.name
      shot.lifetime = .keepAlways
      add(shot)
      app.terminate()
    }
  }

  /// The accessible buttons the bar holds, for the log and the failure messages.
  @MainActor
  private static func labels(of bar: XCUIElement) -> [String] {
    bar.buttons.allElementsBoundByIndex.map(\.label)
  }

  private static func title(of key: MonthKey) -> String {
    "\(MonthNames.bs(key.month, .english)) \(key.year)"
  }

  @MainActor
  private static func launch(arguments: [String] = [], month: String? = nil) -> XCUIApplication {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = "2083-06-20"
    if let month {
      app.launchEnvironment["NEPDATE_UI_TEST_MONTH"] = month
    }
    app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"] + arguments
    app.launch()
    return app
  }
}
