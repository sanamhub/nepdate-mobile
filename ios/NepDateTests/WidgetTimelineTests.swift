import CalendarKit
import Foundation
import NepDate
import Testing

/// The widget timeline: one entry for the build instant and one at each of the next seven local
/// midnights, in the zone the settings choose.
@Suite("Widget timeline")
struct WidgetTimelineTests {
  private static func settings(dateFollows: DateFollows = .nepal) -> Settings {
    Settings(language: .english, digits: .english, dateFollows: dateFollows)
  }

  private static func instant(
    _ year: Int,
    _ month: Int,
    _ day: Int,
    hour: Int = 12,
    minute: Int = 0,
    zone: TimeZone
  ) throws -> Date {
    var calendar = Calendar(identifier: .gregorian)
    calendar.timeZone = zone
    return try #require(
      calendar.date(
        from: DateComponents(
          year: year,
          month: month,
          day: day,
          hour: hour,
          minute: minute
        )
      )
    )
  }

  private static func bsInstant(
    _ date: MonthDate,
    hour: Int = 12,
    zone: TimeZone
  ) throws -> Date {
    let nepal = try #require(
      try? NepaliDate(year: date.year, month: date.month, day: date.day)
    )
    return nepal.date(in: zone).addingTimeInterval(TimeInterval(hour * 3_600))
  }

  @Test("a timeline has the build entry and seven local midnights")
  func kathmanduMidnights() throws {
    let zone = try #require(TimeZone(identifier: "Asia/Kathmandu"))
    let now = try Self.instant(2026, 10, 20, hour: 23, minute: 58, zone: zone)
    let entries = WidgetTimeline.entries(
      now: now,
      settings: Self.settings(),
      deviceZone: zone
    )

    #expect(entries.count == 8)
    #expect(entries[0].date == now)

    var calendar = Calendar(identifier: .gregorian)
    calendar.timeZone = zone
    let start = calendar.startOfDay(for: now)
    for (index, entry) in entries.dropFirst().enumerated() {
      let expected = try #require(
        calendar.date(byAdding: .day, value: index + 1, to: start)
      )
      #expect(entry.date == expected)
    }
    #expect(entries[1].day.date == MonthDate(year: 2083, month: 7, day: 4))
  }

  @Test("New York keeps local midnight across spring forward and fall back")
  func newYorkDST() throws {
    let zone = try #require(TimeZone(identifier: "America/New_York"))

    let spring = WidgetTimeline.entries(
      now: try Self.instant(2026, 3, 7, zone: zone),
      settings: Self.settings(dateFollows: .device),
      deviceZone: zone
    )
    #expect(spring[2].date.timeIntervalSince(spring[1].date) == 23 * 3_600)

    let fall = WidgetTimeline.entries(
      now: try Self.instant(2026, 10, 31, zone: zone),
      settings: Self.settings(dateFollows: .device),
      deviceZone: zone
    )
    #expect(fall[2].date.timeIntervalSince(fall[1].date) == 25 * 3_600)
  }

  @Test("Sydney keeps local midnight across spring forward")
  func sydneyDST() throws {
    let zone = try #require(TimeZone(identifier: "Australia/Sydney"))
    let entries = WidgetTimeline.entries(
      now: try Self.instant(2026, 10, 3, zone: zone),
      settings: Self.settings(dateFollows: .device),
      deviceZone: zone
    )

    #expect(entries[2].date.timeIntervalSince(entries[1].date) == 23 * 3_600)
  }

  @Test("Nepal time and this device pick different days")
  func nepalAndDeviceZones() throws {
    let newYork = try #require(TimeZone(identifier: "America/New_York"))
    let now = Date(timeIntervalSince1970: 1_722_364_200)

    let nepal = WidgetTimeline.entries(
      now: now,
      settings: Self.settings(dateFollows: .nepal),
      deviceZone: newYork
    )
    let device = WidgetTimeline.entries(
      now: now,
      settings: Self.settings(dateFollows: .device),
      deviceZone: newYork
    )

    #expect(nepal[0].day.date == MonthDate(year: 2081, month: 4, day: 16))
    #expect(device[0].day.date == MonthDate(year: 2081, month: 4, day: 15))
    #expect(nepal[0].day.showsNepalLabel)
    #expect(!device[0].day.showsNepalLabel)
  }

  @Test("an entry carries the day, the next holiday and three upcoming entries")
  func entryContent() throws {
    let zone = try #require(TimeZone(identifier: "Asia/Kathmandu"))
    let now = try Self.bsInstant(
      MonthDate(year: 2083, month: 6, day: 20),
      zone: zone
    )
    let entry = try #require(
      WidgetTimeline.entries(
        now: now,
        settings: Self.settings(),
        deviceZone: zone
      ).first
    )

    #expect(entry.day.day == "20")
    #expect(entry.day.month == "Asoj")
    #expect(entry.day.weekday == "Tuesday")
    #expect(entry.day.adDate == "6 October 2026")
    #expect(!entry.day.isHoliday)
    #expect(entry.day.holidayName == nil)
    #expect(entry.nextHoliday?.date == MonthDate(year: 2083, month: 6, day: 25))
    #expect(entry.nextHoliday?.name == "Ghatasthapana Brata")
    #expect(entry.nextHoliday?.daysFromToday == 5)
    #expect(entry.upcoming.count == 3)
    #expect(entry.upcoming[0].name == "Indira Ekadashi Brata")
    #expect(entry.upcoming[1].name == "Ekadashi Shraddha")
    #expect(entry.dataEndsOn == nil)
  }

  @Test("the fixed preview is the Asoj 25 holiday")
  func preview() {
    let entry = WidgetTimeline.preview()

    #expect(entry.day.date == MonthDate(year: 2083, month: 6, day: 25))
    #expect(entry.day.isHoliday)
  }

  @Test("the footer names the last covered date when fewer than three entries remain")
  func dataEnd() throws {
    let zone = try #require(TimeZone(identifier: "Asia/Kathmandu"))
    let now = try Self.bsInstant(
      MonthDate(year: 2084, month: 8, day: 20),
      zone: zone
    )
    let entry = try #require(
      WidgetTimeline.entries(
        now: now,
        settings: Self.settings(),
        deviceZone: zone
      ).first
    )

    #expect(entry.upcoming.count < 3)
    #expect(entry.dataEndsOn == "Mangsir 20, 2084")
  }
}
