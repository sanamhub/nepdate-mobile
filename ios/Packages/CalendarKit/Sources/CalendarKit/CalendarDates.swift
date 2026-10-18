import Foundation

/// The fixed values the app falls back to.
public enum CalendarDates {
  /// Nepal time, the zone behind the "Nepal time" setting (MR-06).
  public static let nepal = TimeZone(identifier: "Asia/Kathmandu") ?? .current
}
