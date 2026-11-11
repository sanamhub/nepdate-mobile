import CalendarKit
import CalendarScreens
import XCTest

/// The month bar, which has four buttons to place on a month away from today: Settings from the
/// shell, Previous month, Today and Next month.
///
/// Whether all four fit depends on the month and on the text size, and this class records what the
/// system does with them, measured on the 402-point iPhone 17 on 2026-11-11:
///
/// - Chaitra 2199 and Falgun 2199 at the default text size hold Settings, Previous month, Today
///   and Next month, so Next month is one tap.
/// - Baisakh 1901 at the default text size, the start of the range, does not: Next month is not in
///   the bar, so it takes the bar's More menu and a second tap. The paging pair sits at the edge of
///   what the bar can hold, and one month more of title width tips it over.
/// - At accessibility2 the bar holds Settings, Previous month and a More menu on a month away from
///   today, and Next month takes two taps there as well.
///
/// The screenshots of the bar, on today's month and on a month away, at both text sizes, are
/// attached to the test run for the design review.
final class MonthToolbarTests: XCTestCase {
  private static let todayTitle = "Asoj 2083"

  /// `UICTContentSizeCategoryAccessibilityL` is Dynamic Type's accessibility2.
  private static let accessibility2 = [
    "-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityL",
  ]

  /// Two months away from today where the bar holds every button, the end of the range among
  /// them, so Next month is disabled there.
  @MainActor
  func testBarKeepsEveryButtonOnAMonthAwayFromToday() throws {
    for month in [(year: 2199, month: 12), (year: 2199, month: 11)] {
      let key = try XCTUnwrap(MonthKey(year: month.year, month: month.month))
      let title = Self.title(of: key)
      let hasNext = key.id < MonthKey.count - 1
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
        hasNext,
        "Next month is enabled wrongly in the bar of \(title)"
      )
      XCTAssertFalse(
        bar.buttons["More"].exists,
        "the bar of \(title) moved a button into a More menu: \(Self.labels(of: bar))"
      )

      if hasNext {
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

  /// The start of the range is where the bar runs out of room at the default text size: Next
  /// month is not in the bar, so it takes the More menu and a second tap.
  @MainActor
  func testBarMovesNextMonthIntoTheMoreMenuAtTheStartOfTheRange() throws {
    let key = try XCTUnwrap(MonthKey(year: 1901, month: 1))
    let title = Self.title(of: key)
    let app = Self.launch(month: key.description)
    let bar = app.navigationBars[title]
    XCTAssertTrue(bar.waitForExistence(timeout: 10), "the bar does not name \(title)")

    let previous = bar.buttons["Previous month"]
    XCTAssertTrue(
      previous.waitForExistence(timeout: 5),
      "Previous month is missing from the bar of \(title): \(Self.labels(of: bar))"
    )
    XCTAssertFalse(previous.isEnabled, "Previous month is enabled at the start of the range")
    XCTAssertFalse(
      bar.buttons["Next month"].exists,
      "Next month is in the bar of \(title) now: \(Self.labels(of: bar))"
    )
    print("BAR \(title) default buttons=\(Self.labels(of: bar))")
  }

  /// At accessibility2 the bar holds Settings, Previous month and the More menu, and the menu
  /// carries the Today button.
  @MainActor
  func testBarMovesTodayIntoTheMoreMenuAtAccessibility2() throws {
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
    XCTAssertTrue(
      app.buttons["Today"].waitForExistence(timeout: 5),
      "the More menu of \(title) does not list Today"
    )
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
