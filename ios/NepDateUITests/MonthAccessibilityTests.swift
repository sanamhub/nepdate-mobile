import CalendarKit
import XCTest

/// MR-10 AC4: the month screen opens on today and passes the system accessibility audit.
final class MonthAccessibilityTests: XCTestCase {
  @MainActor
  func testMonthScreenOpensOnToday() throws {
    let today = TodayProvider().today(dateFollows: .device)
    let language = AppLanguage.current
    let digits = DigitsSetting.followLanguage.resolved(for: language)
    let model = try XCTUnwrap(
      MonthViewModel(
        today: today,
        language: language,
        digits: digits,
        showTithi: false
      )
    )
    let first = try XCTUnwrap(model.layout.cells.first { $0.isInMonth })
    let publicHoliday = language == .nepali ? "सार्वजनिक बिदा" : "public holiday"
    let label = model.accessibilityLabel(for: first, publicHoliday: publicHoliday)

    let app = XCUIApplication()
    app.launch()

    XCTAssertTrue(
      app.buttons[label].waitForExistence(timeout: 10),
      "the pager did not open on \(model.monthTitle)"
    )

    let title = app.buttons["Choose month and year"]
    XCTAssertTrue(title.waitForExistence(timeout: 10))
    XCTAssertEqual(title.value as? String, model.monthTitle)
  }

  @MainActor
  func testMonthScreenHasNoAccessibilityIssues() throws {
    let app = XCUIApplication()
    app.launch()

    try app.performAccessibilityAudit()
  }
}
