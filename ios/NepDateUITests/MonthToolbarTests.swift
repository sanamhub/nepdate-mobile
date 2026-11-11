import CalendarKit
import CalendarScreens
import XCTest

/// The month bar after the paging arrows left it: Settings, which the shell puts in the bar, and
/// the Today button, which shows only on a month away from today. Paging is by swipe, by the
/// named VoiceOver actions and by the hardware arrow keys, so the bar has two items at most and
/// cannot run out of room.
///
/// The cases below are today's month, a month away from it and the start of the range, at the
/// default text size and at accessibility2. Each one asserts the same three things: Settings is in
/// the bar, Today is there only away from today's month, and no button has moved into a More menu.
/// The screenshots of the bar are attached to the test run for the design review.
final class MonthToolbarTests: XCTestCase {
  private static let todayTitle = "Asoj 2083"

  /// `UICTContentSizeCategoryAccessibilityL` is Dynamic Type's accessibility2.
  private static let accessibility2 = [
    "-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityL",
  ]

  /// Settings stays, Today shows only away from today's month, the paging buttons are gone, and
  /// nothing overflows into a More menu, at both text sizes.
  @MainActor
  func testBarHoldsSettingsAndTodayAtBothTextSizes() throws {
    let away = try XCTUnwrap(MonthKey(year: 2199, month: 11))
    let first = try XCTUnwrap(MonthKey(year: 1901, month: 1))
    for arguments in [[], Self.accessibility2] {
      let size = arguments.isEmpty ? "default" : "accessibility2"
      Self.checkBar(
        month: nil,
        title: Self.todayTitle,
        isToday: true,
        size: size,
        arguments: arguments
      )
      Self.checkBar(
        month: away.description,
        title: Self.title(of: away),
        isToday: false,
        size: size,
        arguments: arguments
      )
      Self.checkBar(
        month: first.description,
        title: Self.title(of: first),
        isToday: false,
        size: size,
        arguments: arguments
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

  /// Opens one month and checks its bar, then taps Today when it is there to prove one tap returns
  /// to today's month.
  @MainActor
  private static func checkBar(
    month: String?,
    title: String,
    isToday: Bool,
    size: String,
    arguments: [String]
  ) {
    let app = launch(arguments: arguments, month: month)
    let bar = app.navigationBars[title]
    XCTAssertTrue(bar.waitForExistence(timeout: 10), "the bar does not name \(title)")
    print("BAR \(title) \(size) buttons=\(labels(of: bar))")

    XCTAssertTrue(
      bar.buttons["Settings"].exists,
      "Settings is missing from the bar of \(title): \(labels(of: bar))"
    )
    XCTAssertFalse(
      bar.buttons["More"].exists,
      "the bar of \(title) moved a button into a More menu: \(labels(of: bar))"
    )
    for label in ["Previous month", "Next month"] {
      XCTAssertFalse(
        bar.buttons[label].exists,
        "\(label) is back in the bar of \(title): \(labels(of: bar))"
      )
    }

    let today = bar.buttons["Today"]
    if isToday {
      XCTAssertFalse(today.exists, "Today is in the bar of today's month: \(labels(of: bar))")
      app.terminate()
      return
    }

    XCTAssertTrue(
      today.waitForExistence(timeout: 5),
      "Today is missing from the bar of \(title): \(labels(of: bar))"
    )
    XCTAssertTrue(today.isHittable, "Today is not hittable in the bar of \(title)")
    today.tap()

    let home = app.navigationBars[todayTitle]
    XCTAssertTrue(home.waitForExistence(timeout: 10), "Today did not return to \(todayTitle)")
    XCTAssertFalse(
      home.buttons["Today"].exists,
      "Today stayed in the bar of today's month: \(labels(of: home))"
    )
    app.terminate()
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
