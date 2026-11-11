import CalendarKit
import CalendarScreens
import XCTest

/// MR-01 AC5 and the paging pair: the month bar keeps Today, Previous month and Next month in the
/// bar on every month, so Next month is always one tap. The screenshots of the bar at both text
/// sizes are attached for the design review.
final class MonthToolbarTests: XCTestCase {
  private static let todayTitle = "Asoj 2083"

  /// The width of the smallest supported phone: the iPhone SE the first-run snapshot renders at
  /// 375 by 667 points. The bar's buttons must not need more than that.
  private static let smallestPhoneWidth: CGFloat = 375

  /// A month at each end of the range, both away from today.
  private static let awayMonths = [(year: 2199, month: 11), (year: 1901, month: 2)]

  /// `UICTContentSizeCategoryAccessibilityL` is Dynamic Type's accessibility2.
  private static let accessibility2 = [
    "-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityL",
  ]

  @MainActor
  func testBarKeepsPagingAndTodayAwayFromToday() throws {
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
          "\(label) is missing from the bar of \(title)"
        )
        XCTAssertTrue(button.isHittable, "\(label) is not hittable in the bar of \(title)")
      }
      XCTAssertFalse(
        bar.buttons["More"].exists,
        "the bar of \(title) moved a button into a More menu"
      )
      // The trailing group holds Settings, Previous month and Next month; three items fit where
      // the four of the old layout did not. Today is on the leading side.
      for label in ["Settings", "Previous month", "Next month"] {
        let trailing = bar.buttons[label]
        XCTAssertGreaterThan(
          trailing.frame.midX,
          bar.frame.midX,
          "\(label) is not in the trailing group of \(title)"
        )
      }
      XCTAssertLessThan(
        bar.buttons["Today"].frame.midX,
        bar.frame.midX,
        "Today is not in the leading group of \(title)"
      )

      // Next month takes one tap from here, with no menu in between.
      let next = try XCTUnwrap(MonthKey(id: key.id + 1))
      bar.buttons["Next month"].tap()
      XCTAssertTrue(
        app.navigationBars[Self.title(of: next)].waitForExistence(timeout: 10),
        "one tap on Next month did not open \(Self.title(of: next))"
      )

      // Today is on the leading side and returns to today's month from the far end of the range.
      let today = app.navigationBars.buttons["Today"]
      XCTAssertTrue(
        today.waitForExistence(timeout: 5),
        "Today left the bar of \(Self.title(of: next))"
      )
      today.tap()
      XCTAssertTrue(
        app.navigationBars[Self.todayTitle].waitForExistence(timeout: 10),
        "Today did not return to \(Self.todayTitle)"
      )
      app.terminate()
    }
  }

  /// The bar on today's month and on a month away from today, at the default text size and at
  /// accessibility2. The reference images are attached to the test run for the design review.
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
      XCTAssertFalse(
        bar.buttons["More"].exists,
        "\(item.name): the bar moved a button into a More menu"
      )

      let footprint = Self.footprint(of: bar)
      print("BAR \(item.name) width=\(bar.frame.width) buttons=\(footprint)")
      XCTAssertLessThanOrEqual(
        footprint,
        Self.smallestPhoneWidth,
        "\(item.name): the bar needs \(footprint) points, more than the smallest phone's "
          + "\(Self.smallestPhoneWidth)"
      )

      let shot = XCTAttachment(screenshot: bar.screenshot())
      shot.name = item.name
      shot.lifetime = .keepAlways
      add(shot)
      app.terminate()
    }
  }

  /// The points the bar's buttons take from both ends: the leading group up to the title's left
  /// edge, and the trailing group from the title's right edge to the bar's right edge. The frames
  /// come from the running bar, so this is the width the same buttons need on any phone.
  @MainActor
  private static func footprint(of bar: XCUIElement) -> CGFloat {
    let labels = ["Settings", "Previous month", "Next month"]
    let left = labels.map { bar.buttons[$0].frame.minX }.min() ?? bar.frame.maxX
    let trailingWidth = bar.frame.maxX - left
    let today = bar.buttons["Today"]
    guard today.exists else { return trailingWidth }
    return today.frame.maxX - bar.frame.minX + trailingWidth
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
