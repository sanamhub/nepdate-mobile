import CalendarUI
import Foundation
import Testing

/// The Lock Screen countdown gauge (W-06): a ring that fills over the twelve-day window.
struct FestivalGaugeTests {
  @Test("the ring fills from the festival day back to twelve days before")
  func fraction() {
    #expect(FestivalCountdownGaugeView.fraction(days: 0) == 0)
    #expect(FestivalCountdownGaugeView.fraction(days: 6) == 0.5)
    #expect(FestivalCountdownGaugeView.fraction(days: 12) == 1)
  }

  @Test("a wait outside the window stays inside the ring")
  func clamped() {
    #expect(FestivalCountdownGaugeView.fraction(days: -3) == 0)
    #expect(FestivalCountdownGaugeView.fraction(days: 30) == 1)
  }

  @Test("the disc says Today on the festival day instead of zero")
  func festivalDay() {
    #expect(
      FestivalCountdownText.disc(
        days: 0,
        count: "0",
        locale: Locale(identifier: "en")
      ) == "Today"
    )
    #expect(
      FestivalCountdownText.disc(
        days: 0,
        count: "०",
        locale: Locale(identifier: "ne")
      ) == "आज"
    )
  }

  @Test("the disc keeps the day count before the festival")
  func beforeTheFestival() {
    #expect(
      FestivalCountdownText.disc(
        days: 2,
        count: "2",
        locale: Locale(identifier: "en")
      ) == "2"
    )
  }
}
