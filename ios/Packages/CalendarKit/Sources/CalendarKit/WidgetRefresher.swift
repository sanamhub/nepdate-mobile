import Foundation

/// Decides when the app asks WidgetKit to reload the widgets (I2-05).
///
/// The reload call is injected, so a test counts the calls and the app passes
/// `WidgetCenter.shared.reloadAllTimelines`. The last day the app was active is kept in the App
/// Group, so a launch on the same day does not reload again.
public struct WidgetRefresher: Sendable {
  private let suiteName: String
  private let reload: @Sendable () -> Void

  /// Creates a refresher.
  /// - Parameters:
  ///   - suiteName: The suite that holds the last active day; defaults to ``AppGroup/identifier``.
  ///   - reload: Reloads every widget timeline.
  public init(
    suiteName: String = AppGroup.identifier,
    reload: @escaping @Sendable () -> Void
  ) {
    self.suiteName = suiteName
    self.reload = reload
  }

  /// Reloads after a settings change, because every widget draws from the settings.
  public func settingsChanged() {
    reload()
  }

  /// Reloads when the device's zone or clock changed, because the widgets' midnights moved.
  public func timeChanged() {
    reload()
  }

  /// Reloads when the app becomes active on a day it was not active on before.
  /// - Parameter today: Today's date in the zone the settings choose.
  public func becameActive(on today: MonthDate) {
    let defaults = AppGroup.defaults(suiteName: suiteName)
    let text = today.description
    if defaults?.string(forKey: Self.key) == text { return }
    defaults?.set(text, forKey: Self.key)
    reload()
  }

  private static let key = "widget_last_active_day"
}
