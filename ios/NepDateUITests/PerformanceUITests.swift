import XCTest

/// MR-09: the launch time of the app under the UI test runner.
///
/// The result is reported in `docs/benchmarks.md`; it is not a gate, because a shared CI runner
/// cannot judge an absolute launch time.
final class PerformanceUITests: XCTestCase {
  @MainActor
  func testAppLaunch() {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = "2083-06-25"
    measure(metrics: [XCTApplicationLaunchMetric()]) {
      app.launch()
      app.terminate()
    }
  }

  /// MR-09 AC2: the month swipe's scroll deceleration, measured on the simulator. A shared runner
  /// cannot judge an absolute frame rate, so the number is reported in `docs/benchmarks.md` and is
  /// not a gate.
  @MainActor
  func testMonthSwipe() {
    let app = XCUIApplication()
    app.launchEnvironment["NEPDATE_UI_TEST_TODAY"] = "2083-06-25"
    app.launchArguments += ["-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
    app.launch()

    let pager = app.scrollViews["month_pager"]
    XCTAssertTrue(pager.waitForExistence(timeout: 10), "the month pager is missing")

    measure(metrics: [XCTOSSignpostMetric.scrollDecelerationMetric]) {
      pager.swipeLeft(velocity: .slow)
    }

    app.terminate()
  }
}
