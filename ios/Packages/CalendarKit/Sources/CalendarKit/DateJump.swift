import NepDate

/// One calendar's reading of a typed date (MR-13 AC2).
public struct JumpReading: Sendable, Hashable, Identifiable {
  /// The calendar the typed date was read in.
  public enum Kind: Sendable, Hashable {
    /// Bikram Sambat.
    case bs

    /// Gregorian, mapped to its Bikram Sambat date.
    case ad
  }

  /// The calendar.
  public let kind: Kind

  /// The year as typed.
  public let year: Int

  /// The month as typed.
  public let month: Int

  /// The day as typed.
  public let day: Int

  /// The Bikram Sambat date the reading opens.
  public let date: MonthDate

  /// The identity of the reading.
  public var id: Kind {
    kind
  }
}

/// A typed jump date and the calendars that can read it (MR-13 AC2 and AC3).
///
/// The text is `year-month-day` with `-`, `/` or `.` as the separator, in ASCII or Devanagari
/// digits, or eight bare digits read as YYYYMMDD. A year is read as BS when the BS calendar has
/// that year and as AD when the Gregorian calendar does, so one text can produce both readings.
public struct DateJump: Sendable, Hashable {
  /// The BS reading, when the BS calendar can make the date.
  public let bs: JumpReading?

  /// The AD reading, when the Gregorian calendar can make the date.
  public let ad: JumpReading?

  /// Parses a jump date.
  /// - Parameter text: The typed text.
  public init(_ text: String) {
    let ascii = Self.asciiDigits(text)
    guard let parts = Self.parts(ascii) else {
      bs = nil
      ad = nil
      return
    }
    bs = Self.bsReading(year: parts.year, month: parts.month, day: parts.day)
    ad = Self.adReading(year: parts.year, month: parts.month, day: parts.day)
  }

  /// The readings, BS first.
  public var readings: [JumpReading] {
    [bs, ad].compactMap { $0 }
  }

  /// The reading the keyboard's return opens: the BS one when both can read the text.
  public var preferred: JumpReading? {
    bs ?? ad
  }

  /// Whether neither calendar can read the text.
  public var isUnreadable: Bool {
    bs == nil && ad == nil
  }

  private static func bsReading(year: Int, month: Int, day: Int) -> JumpReading? {
    guard (1901...2199).contains(year) else { return nil }
    guard let date = try? NepaliDate(year: year, month: month, day: day) else { return nil }
    return JumpReading(
      kind: .bs,
      year: year,
      month: month,
      day: day,
      date: MonthDate(year: date.year, month: date.month, day: date.day)
    )
  }

  private static func adReading(year: Int, month: Int, day: Int) -> JumpReading? {
    guard let date = try? NepaliDate(gregorianYear: year, month: month, day: day) else {
      return nil
    }
    return JumpReading(
      kind: .ad,
      year: year,
      month: month,
      day: day,
      date: MonthDate(year: date.year, month: date.month, day: date.day)
    )
  }

  /// Rewrites Devanagari digits as ASCII and trims the ends.
  private static func asciiDigits(_ text: String) -> String {
    let devanagari: [Character] = ["०", "१", "२", "३", "४", "५", "६", "७", "८", "९"]
    let ascii: [Character] = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"]
    var output = ""
    output.reserveCapacity(text.count)
    for character in text {
      if let index = devanagari.firstIndex(of: character) {
        output.append(ascii[index])
      } else {
        output.append(character)
      }
    }
    return output.trimmingCharacters(in: .whitespacesAndNewlines)
  }

  /// Splits the text into a year, a month and a day, or returns `nil` when it is not a date.
  private static func parts(_ text: String) -> (year: Int, month: Int, day: Int)? {
    guard !text.isEmpty else { return nil }
    let allowed = text.allSatisfy { character in
      character.isNumber || character == "-" || character == "/" || character == "."
        || character == " "
    }
    guard allowed else { return nil }

    var spaced = text
    if text.count == 8, text.allSatisfy(\.isNumber) {
      let year = text.prefix(4)
      let month = text.dropFirst(4).prefix(2)
      let day = text.suffix(2)
      spaced = "\(year)-\(month)-\(day)"
    }

    let raw = spaced.split(
      omittingEmptySubsequences: false,
      whereSeparator: { $0 == "-" || $0 == "/" || $0 == "." }
    )
    guard raw.count == 3 else { return nil }
    let fields = raw.map { String($0).trimmingCharacters(in: .whitespaces) }
    guard
      fields[0].count == 4,
      (1...2).contains(fields[1].count),
      (1...2).contains(fields[2].count),
      fields.allSatisfy({ !$0.isEmpty && $0.allSatisfy(\.isNumber) }),
      let year = Int(fields[0]),
      let month = Int(fields[1]),
      let day = Int(fields[2])
    else { return nil }
    return (year, month, day)
  }
}
