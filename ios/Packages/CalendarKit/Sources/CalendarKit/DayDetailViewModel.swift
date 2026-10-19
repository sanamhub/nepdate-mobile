import NepDate
import NepDatePatro

/// The day detail sheet's text (MR-02, A1-04).
///
/// The date, weekday, tithi, holiday flag and events are computed here so the sheet only draws
/// values and the strings are tested without a simulator. The patro data covers BS 2077-01-01 to
/// 2084-08-20; outside it ``hasData`` is false and the sheet shows the no-data message (MR-02 AC4).
public struct DayDetailViewModel: Sendable, Hashable {
  /// The date the sheet describes.
  public let date: MonthDate

  /// The language of the month, weekday, tithi and event names.
  public let language: AppLanguage

  /// The resolved Digits setting. The Devanagari date line always keeps Devanagari digits.
  public let digits: Digits

  private let nepaliDate: NepaliDate
  private let info: CalendarInfo

  /// Creates the sheet's values for a date.
  /// - Parameters:
  ///   - date: The date, inside the library's BS 1901 to 2199 range.
  ///   - language: The language of the names.
  ///   - digits: The resolved Digits setting.
  public init?(date: MonthDate, language: AppLanguage, digits: Digits) {
    guard
      let nepaliDate = try? NepaliDate(
        year: date.year,
        month: date.month,
        day: date.day
      )
    else { return nil }
    self.date = date
    self.language = language
    self.digits = digits
    self.nepaliDate = nepaliDate
    self.info = nepaliDate.calendarInfo
  }

  /// Whether the patro data covers this date, so the holiday, tithi and event fields are real.
  public var hasData: Bool {
    Patro.coverage.contains(nepaliDate)
  }

  /// The long BS date in the app language, for example `Asoj 9, 2083` or `असोज ९, २०८३`.
  public var bsDate: String {
    Self.longDate(nepaliDate, monthName: MonthNames.bs(date.month, language), digits: digits)
  }

  /// The long BS date in Devanagari, whatever the Digits setting is.
  ///
  /// The sheet shows this second line when the app language is English, because the line exists to
  /// show the Nepali script (MR-02 AC1): `असोज ९, २०८३`, never `असोज 9, 2083`.
  public var devanagariDate: String {
    Self.longDate(
      nepaliDate,
      monthName: MonthNames.bs(date.month, .nepali),
      digits: .nepali
    )
  }

  /// The weekday, for example `Friday` or `शुक्रवार`.
  public var weekday: String {
    nepaliDate.weekday.name(language.lang)
  }

  /// The AD date, for example `25 September 2026` or `२५ सेप्टेम्बर २०२६`.
  public var adDate: String {
    let gregorian = nepaliDate.gregorian
    let day = digits.format(gregorian.day)
    let month = MonthNames.ad(gregorian.month, language)
    let year = digits.format(gregorian.year)
    return "\(day) \(month) \(year)"
  }

  /// The weekday and AD date, the sheet's second line: `Friday, 25 September 2026`.
  public var weekdayAndDate: String {
    "\(weekday), \(adDate)"
  }

  /// The tithi in the app language, or `nil` when the data has none (MR-02 AC6).
  public var tithi: String? {
    guard hasData, let tithi = info.tithi else { return nil }
    return digits.applying(to: tithi.name(language.lang))
  }

  /// Whether the day is a public holiday (MR-02 AC1).
  public var isPublicHoliday: Bool {
    hasData && info.isPublicHoliday
  }

  /// Every event of the day in file order, in the app language (MR-02 AC1 and AC5).
  ///
  /// The names include their scope, such as `Indra Jatra (Kathmandu Valley Holiday)`, and are not
  /// shortened here or in the view.
  public var events: [String] {
    guard hasData else { return [] }
    return info.events.map { digits.applying(to: $0.name(language.lang)) }
  }

  /// The text "Copy date" puts on the pasteboard: the BS date in the app language with the
  /// resolved Digits (MR-02 AC3).
  public var copyText: String {
    bsDate
  }

  /// The title of the event handed to the system editor (MR-02 AC2).
  ///
  /// The first event wins, so a holiday with a named event keeps that name; a holiday without
  /// events uses `publicHoliday`; a day without either uses the BS date. The scope in the event
  /// name stays whole.
  /// - Parameter publicHoliday: The localised words for a public holiday.
  /// - Returns: The event title.
  public func calendarTitle(publicHoliday: String) -> String {
    if let event = events.first { return event }
    if isPublicHoliday { return publicHoliday }
    return bsDate
  }

  /// The AD date as year, month and day, for the system event editor.
  public var gregorianDate: (year: Int, month: Int, day: Int) {
    nepaliDate.gregorian
  }

  private static func longDate(
    _ date: NepaliDate,
    monthName: String,
    digits: Digits
  ) -> String {
    "\(monthName) \(digits.format(date.day)), \(digits.format(date.year))"
  }
}
