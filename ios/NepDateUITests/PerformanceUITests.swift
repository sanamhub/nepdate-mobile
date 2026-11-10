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
}
