import CalendarKit
import CalendarScreens
import XCTest

/// The month bar on a month away from today, which has four buttons to place: Settings from the
/// shell, Previous month, Today and Next month.
///
/// The bar cannot hold all four. With an inline title the title sits against the leading edge and
/// the trailing cluster gets the rest of the bar: on the 402-point iPhone 17 it starts at about
/// 230 points for the current month, so it has about 152 points, which is what Settings, Previous
/// month and Next month need. A leading button moves the title to about 137 points, because the
/// title follows the leading item, and the cluster is then left with about 143 points. That is
/// short by about 9, so the system moves Next month and Today into the bar's More menu and Next
/// month takes two taps (measured on 2026-11-11, run 42944867382).
///
/// The test records that behaviour so a change to it is noticed, and prints the buttons the bar
/// holds. The screenshots of the bar, on today's month and on a month away, at the default text
/// size and at accessibility2, are attached to the test run for the design review.
final class MonthToolbarTests: XCTestCase {
  private static let todayTitle = "Asoj 2083"

  /// A month at each end of the range, both away from today.
  private static let awayMonths = [(year: 2199, month: 11), (year: 1901, month: 2)]

  /// `UICTContentSizeCategoryAccessibilityL` is Dynamic Type's accessibility2.
  private static let accessibility2 = [
    "-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityL",
  ]

  /// What the bar does with four buttons: Previous month stays in it, and the More menu holds
  /// Today and Next month.
  @MainActor
  func testBarMovesTodayAndNextMonthIntoTheMoreMenu() throws {
    for month in Self.awayMonths {
      let key = try XCTUnwrap(MonthKey(year: month.year, month: month.month))
      let title = Self.title(of: key)
      let app = Self.launch(month: key.description)
      let bar = app.navigationBars[title]
      XCTAssertTrue(bar.waitForExistence(timeout: 10), "the bar does not name \(title)")

      let previous = bar.buttons["Previous month"]
      XCTAssertTrue(
        previous.waitForExistence(timeout: 5),
        "Previous month is missing from the bar of \(title): \(Self.labels(of: bar))"
      )
      XCTAssertTrue(previous.isHittable, "Previous month is not hittable in the bar of \(title)")

      // Both work when they are in the bar; when either appears, this test needs rewriting.
      XCTAssertFalse(
        bar.buttons["Next month"].exists,
        "Next month is in the bar of \(title) now: \(Self.labels(of: bar))"
      )
      XCTAssertFalse(
        bar.buttons["Today"].exists,
        "Today is in the bar of \(title) now: \(Self.labels(of: bar))"
      )

      // Next month is two taps: the More menu, then the item.
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

      let next = try XCTUnwrap(MonthKey(id: key.id + 1))
      app.buttons["Next month"].tap()
      XCTAssertTrue(
        app.navigationBars[Self.title(of: next)].waitForExistence(timeout: 10),
        "Next month from the More menu did not open \(Self.title(of: next))"
      )
      app.terminate()
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
