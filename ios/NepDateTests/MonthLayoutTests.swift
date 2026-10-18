import CalendarKit
import Foundation
import NepDate
import NepDatePatro
import Testing

@Suite("Month layout")
struct MonthLayoutTests {
  private static let today = MonthDate(year: 2083, month: 6, day: 20)

  struct KnownMonth: Sendable, CustomTestStringConvertible {
    let year: Int
    let month: Int
    let cellCount: Int
    let leadingDays: Int
    let rowCount: Int
    let dayCount: Int?
    let holidayDays: [Int]?

    var testDescription: String { "\(year)-\(month)" }
  }

  static let knownMonths = [
    KnownMonth(
      year: 2083, month: 6, cellCount: 35, leadingDays: 4, rowCount: 5, dayCount: 31,
      holidayDays: [3, 9, 18, 25, 31]),
    KnownMonth(
      year: 2083, month: 3, cellCount: 35, leadingDays: 1, rowCount: 5, dayCount: 32,
      holidayDays: nil),
    KnownMonth(
      year: 2083, month: 2, cellCount: 42, leadingDays: 5, rowCount: 6, dayCount: nil,
      holidayDays: nil),
  ]

  static let coverageMonths: [MonthDate] = {
    var months: [MonthDate] = []
    var date = Patro.coverage.lowerBound
    while date <= Patro.coverage.upperBound {
      months.append(MonthDate(year: date.year, month: date.month, day: 1))
      guard let next = try? date.firstDayOfMonth.adding(months: 1) else { break }
      date = next
    }
    return months
  }()

  @Test(
    "I1-02: known months have 35 or 42 cells, Sunday first",
    arguments: MonthLayoutTests.knownMonths
  )
  func knownMonth(_ month: KnownMonth) throws {
    let layout = try #require(
      MonthLayout(year: month.year, month: month.month, showTithi: false, today: Self.today)
    )

    #expect(layout.cells.count == month.cellCount)
    #expect(layout.leadingDays == month.leadingDays)
    #expect(layout.rowCount == month.rowCount)
    #expect(layout.cells.prefix(month.leadingDays).allSatisfy { !$0.isInMonth })
    #expect(layout.cells.filter { $0.isInMonth }.count == layout.dayCount)
    if let dayCount = month.dayCount {
      #expect(layout.dayCount == dayCount)
    }
    if let holidayDays = month.holidayDays {
      #expect(layout.holidayDays == holidayDays)
    }
  }

  @Test("I1-02: Asoj 2083 marks its holidays, Saturdays, today and events")
  func asoj2083() throws {
    let layout = try #require(
      MonthLayout(year: 2083, month: 6, showTithi: false, today: Self.today)
    )

    #expect(layout.firstDay == MonthDate(year: 2083, month: 6, day: 1))
    #expect(layout.lastDay == MonthDate(year: 2083, month: 6, day: 31))
    #expect(layout.cells.first { $0.bsDay == 3 }?.isSaturday == true)
    #expect(layout.cells.first { $0.bsDay == 20 }?.isToday == true)
    #expect(layout.cells.first { $0.bsDay == 1 }?.hasEvents == true)
    #expect(layout.cells.first { $0.bsDay == 20 }?.isInMonth == true)
  }

  @Test("I1-02: Baisakh 2084 day 1 has a holiday, events and no tithi")
  func baisakh2084() throws {
    let layout = try #require(MonthLayout(year: 2084, month: 1, showTithi: true, today: Self.today))
    let dayOne = try #require(layout.cells.first { $0.isInMonth })

    #expect(dayOne.bsDay == 1)
    #expect(dayOne.isHoliday)
    #expect(dayOne.hasEvents)
    #expect(dayOne.tithi == nil)
  }

  @Test("I1-02: the tithi line follows the setting")
  func tithi() throws {
    let off = try #require(MonthLayout(year: 2083, month: 6, showTithi: false, today: Self.today))
    let on = try #require(MonthLayout(year: 2083, month: 6, showTithi: true, today: Self.today))

    #expect(off.cells.first { $0.isInMonth }?.tithi == nil)
    #expect(on.cells.first { $0.isInMonth }?.tithi != nil)
  }

  // Sunday = 0 for an AD date, from Foundation's Gregorian calendar rather than the library.
  private static func foundationWeekday(_ ad: (year: Int, month: Int, day: Int)) -> Int {
    var calendar = Calendar(identifier: .gregorian)
    calendar.timeZone = TimeZone(identifier: "UTC") ?? .gmt
    let components = DateComponents(year: ad.year, month: ad.month, day: ad.day)
    guard let date = calendar.date(from: components) else { return -1 }
    return calendar.component(.weekday, from: date) - 1
  }

  @Test(
    "I1-02: every month in the patro coverage matches the library",
    arguments: MonthLayoutTests.coverageMonths
  )
  func coverageMonth(_ start: MonthDate) throws {
    let layout = try #require(
      MonthLayout(year: start.year, month: start.month, showTithi: true, today: Self.today)
    )
    let first = try NepaliDate(year: start.year, month: start.month, day: 1)
    let last = first.lastDayOfMonth
    let leading = first.weekday.rawValue
    let expectedCells = leading + last.day <= 35 ? 35 : 42

    #expect(layout.leadingDays == leading, "\(start) leading days")
    #expect(leading == Self.foundationWeekday(first.gregorian), "\(start) weekday vs Foundation")
    #expect(layout.cells.count == expectedCells, "\(start) cell count")
    #expect(layout.firstDay == MonthDate(year: first.year, month: first.month, day: first.day))
    #expect(layout.lastDay == MonthDate(year: last.year, month: last.month, day: last.day))
    #expect(layout.cells.filter { $0.isInMonth }.count == last.day, "\(start) days in month")
    #expect(layout.cells.first { $0.isInMonth }?.date == layout.firstDay)
    #expect(layout.cells.last { $0.isInMonth }?.date == layout.lastDay)
    #expect(layout.cells.prefix(leading).allSatisfy { !$0.isInMonth })

    for (index, cell) in layout.cells.enumerated() {
      guard let date = cell.date else {
        #expect(!cell.isInMonth, "\(start) index \(index) is a blank in-month cell")
        continue
      }
      let libraryDate = try NepaliDate(year: date.year, month: date.month, day: date.day)
      #expect(cell.bsDay == libraryDate.day, "\(start) index \(index) BS day")
      #expect(cell.adDay == libraryDate.gregorian.day, "\(start) index \(index) AD day")
      #expect(
        cell.isSaturday == (libraryDate.weekday == .saturday),
        "\(start) index \(index) Saturday")
      #expect(cell.isToday == (date == Self.today), "\(start) index \(index) today")
      if cell.isInMonth {
        let info = libraryDate.calendarInfo
        #expect(cell.isHoliday == info.isPublicHoliday, "\(start) index \(index) holiday")
        #expect(
          cell.events.map(\.english) == info.events.map(\.english),
          "\(start) index \(index) events")
        #expect(
          cell.tithi?.english == info.tithi?.english,
          "\(start) index \(index) tithi")
      } else {
        #expect(!cell.isHoliday, "\(start) index \(index) neighbour holiday")
        #expect(!cell.hasEvents, "\(start) index \(index) neighbour events")
      }
    }
  }

  @Test("I1-02: the edges of the library range leave blank cells")
  func edges() throws {
    let first = try #require(
      MonthLayout(year: 1901, month: 1, showTithi: false, today: Self.today)
    )
    let last = try #require(
      MonthLayout(year: 2199, month: 12, showTithi: false, today: Self.today)
    )

    #expect(first.cells.filter { $0.isInMonth }.count == first.dayCount)
    #expect(last.cells.filter { $0.isInMonth }.count == last.dayCount)
    #expect(first.cells.filter { $0.date == nil }.allSatisfy { !$0.isInMonth })
    #expect(last.cells.contains { $0.date == nil && !$0.isInMonth })
  }

  @Test("I1-02: a month outside the library's range is refused")
  func outOfRange() {
    #expect(MonthLayout(year: 2083, month: 13, showTithi: false, today: Self.today) == nil)
    #expect(MonthLayout(year: 1800, month: 1, showTithi: false, today: Self.today) == nil)
  }

  @Test("I1-01: BS and AD numbers use the resolved digits")
  func digits() throws {
    let layout = try #require(
      MonthLayout(year: 2083, month: 6, showTithi: false, today: Self.today)
    )
    let first = try #require(layout.cells.first { $0.isInMonth })

    #expect(first.bsNumber(.nepali) == "१")
    #expect(first.adNumber(.nepali) == "१७")
    #expect(first.bsNumber(.english) == "1")
    #expect(first.adNumber(.english) == "17")
  }

  @Test("I1-02: the title and weekday row follow the language")
  func language() throws {
    let nepali = try #require(
      MonthLayout(year: 2083, month: 6, showTithi: false, today: Self.today)
    )
    #expect(nepali.title(.nepali, digits: .nepali) == "असोज २०८३")
    #expect(nepali.title(.nepali, digits: .english) == "असोज 2083")
    #expect(MonthLayout.weekdayNames(.nepali).first == "आइत")
    #expect(MonthLayout.weekdayNames(.nepali).last == "शनि")

    #expect(nepali.title(.english, digits: .english) == "Ashoj 2083")
    #expect(nepali.title(.english, digits: .nepali) == "Ashoj २०८३")
    #expect(MonthLayout.weekdayNames(.english).first == "Sun")
  }

  @Test("I1-02: a named text keeps both languages")
  func namedText() throws {
    let layout = try #require(MonthLayout(year: 2083, month: 6, showTithi: true, today: Self.today))
    let event = try #require(layout.cells.first { $0.hasEvents }?.events.first)

    #expect(!event.english.isEmpty)
    #expect(!event.nepali.isEmpty)
    #expect(event.name(.english) == event.english)
    #expect(event.name(.nepali) == event.nepali)
  }
}
