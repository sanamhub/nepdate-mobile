import XCTest

/// A widget or control URL opens the right place in the app.
final class WidgetRoutingUITests: XCTestCase {
  @MainActor
  func testDayAndConverter() throws {
    let day = Self.launch(url: "nepdate://day/2083-06-25")
    XCTAssertTrue(
      day.staticTexts["Asoj 25, 2083"].waitForExistence(timeout: 10),
      "the day sheet for Asoj 25 did not open"
    )
    day.terminate()

    let converter = Self.launch(url: "nepdate://converter")
    let tab = converter.tabBars.buttons["Converter"]
    XCTAssertTrue(tab.waitForExistence(timeout: 10), "the Converter tab is missing")
    let selected = NSPredicate(format: "isSelected == true")
    let wait = expectation(for: selected, evaluatedWith: tab)
    XCTAssertEqual(XCTWaiter.wait(for: [wait], timeout: 10), .completed, "Converter not selected")
  }

  @MainActor
  private static func launch(url: String) -> XCUIApplication {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = "2083-06-20"
    app.launchEnvironment["NEPDATE_UI_TEST_URL"] = url
    app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
    app.launch()
    return app
  }
}
