import Foundation

/// The URLs the widgets use to open the app.
public enum WidgetLink {
  /// The app's custom scheme.
  public static let scheme = "nepdate"

  /// Opens the app on today.
  public static var today: URL {
    URL(string: "\(scheme)://today")!
  }

  /// Opens the app on a day's detail.
  /// - Parameter date: The Bikram Sambat date.
  /// - Returns: For example `nepdate://day/2083-06-25`.
  public static func day(_ date: MonthDate) -> URL {
    let value = String(format: "%04d-%02d-%02d", date.year, date.month, date.day)
    return URL(string: "\(scheme)://day/\(value)")!
  }
}
