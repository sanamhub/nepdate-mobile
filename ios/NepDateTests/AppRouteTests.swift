import CalendarKit
import Foundation
import Testing

/// The `nepdate://` URLs the widgets open, built and parsed by `AppRoute` alone.
@Suite("App routes")
struct AppRouteTests {
  @Test("today and day URLs have the documented shape")
  func todayAndDayURLs() {
    #expect(AppRoute.today.url.absoluteString == "nepdate://today")
    #expect(
      AppRoute.day(MonthDate(year: 2083, month: 6, day: 25)).url.absoluteString
        == "nepdate://day/2083-06-25"
    )
    #expect(
      AppRoute.day(MonthDate(year: 2084, month: 8, day: 20)).url.absoluteString
        == "nepdate://day/2084-08-20"
    )
  }

  @Test("parsing a route's URL gives the same route")
  func roundTrip() {
    let routes: [AppRoute] = [
      .today,
      .day(MonthDate(year: 2083, month: 6, day: 25)),
      .day(MonthDate(year: 2084, month: 8, day: 20)),
      .day(MonthDate(year: 1901, month: 1, day: 1)),
      .day(MonthDate(year: 2199, month: 12, day: 30)),
    ]
    for route in routes {
      #expect(AppRoute(url: route.url) == route)
    }
  }

  @Test(
    "a malformed or foreign URL is no route",
    arguments: [
      "https://today",
      "nepdate://tomorrow",
      "nepdate://today/extra",
      "nepdate://day",
      "nepdate://day/2083-6-25",
      "nepdate://day/2083-06-25/x",
      "nepdate://day/२०८३-०६-२५",
      "nepdate://day/2083-13-01",
      "nepdate://day/2083-06-33",
      "nepdate://day/1900-12-30",
      "nepdate://day/2200-01-01",
    ]
  )
  func rejects(_ value: String) throws {
    let url = try #require(URL(string: value))
    #expect(AppRoute(url: url) == nil)
  }
}
