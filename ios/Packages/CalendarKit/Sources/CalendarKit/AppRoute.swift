import Foundation
import NepDate

/// A place in the app that a URL opens: the widgets' taps and, later, the controls. It owns the
/// `nepdate://` URL format in both directions, so building and parsing can't drift apart.
public enum AppRoute: Sendable, Hashable {
  /// Today's month.
  case today

  /// One day's detail, opened on its month.
  case day(MonthDate)

  /// The app's custom scheme, declared in `NepDateApp-Info.plist`.
  public static let scheme = "nepdate"

  /// The URL for this route: `nepdate://today` or `nepdate://day/2083-06-25`, in ASCII digits.
  public var url: URL {
    switch self {
    case .today:
      return URL(string: "\(Self.scheme)://today")!
    case .day(let date):
      let value = String(format: "%04d-%02d-%02d", date.year, date.month, date.day)
      return URL(string: "\(Self.scheme)://day/\(value)")!
    }
  }

  /// Reads a route from a URL. Fails for another scheme, an unknown host, a malformed date or a
  /// date outside BS 1901 to 2199, so a bad link opens the app where it was instead of guessing.
  /// - Parameter url: A URL opened by the system.
  public init?(url: URL) {
    guard url.scheme?.lowercased() == Self.scheme else { return nil }
    switch url.host?.lowercased() ?? "" {
    case "today" where url.path.isEmpty || url.path == "/":
      self = .today
    case "day":
      guard let date = Self.date(fromPath: url.path) else { return nil }
      self = .day(date)
    default:
      return nil
    }
  }

  /// Parses `/yyyy-MM-dd` with ASCII digits and checks the date exists.
  private static func date(fromPath path: String) -> MonthDate? {
    let parts = path.dropFirst().split(separator: "-", omittingEmptySubsequences: false)
    let isASCIIDigits = { (part: Substring) in
      part.allSatisfy { character in character.isASCII && character.isNumber }
    }
    guard
      path.first == "/",
      parts.count == 3,
      parts[0].count == 4,
      parts[1].count == 2,
      parts[2].count == 2,
      parts.allSatisfy(isASCIIDigits),
      let year = Int(parts[0]),
      let month = Int(parts[1]),
      let day = Int(parts[2]),
      (try? NepaliDate(year: year, month: month, day: day)) != nil
    else { return nil }
    return MonthDate(year: year, month: month, day: day)
  }
}
