import CalendarKit
import Testing

/// The widget tap URLs use the app's `nepdate` scheme and ASCII digits.
@Suite("Widget links")
struct WidgetLinkTests {
  @Test("today and day URLs have the documented shape")
  func urls() {
    #expect(WidgetLink.today.absoluteString == "nepdate://today")
    #expect(
      WidgetLink.day(MonthDate(year: 2083, month: 6, day: 25)).absoluteString
        == "nepdate://day/2083-06-25"
    )
    #expect(
      WidgetLink.day(MonthDate(year: 2084, month: 8, day: 20)).absoluteString
        == "nepdate://day/2084-08-20"
    )
  }
}
