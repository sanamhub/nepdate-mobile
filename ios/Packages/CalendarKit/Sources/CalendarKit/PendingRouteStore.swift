import Foundation

/// Hands a route from a control (Control Center, Lock Screen or Action button) to the app.
///
/// A control's intent writes the route and the system opens the app; the app reads the route once
/// when it becomes active and clears it. The store holds no state of its own: every call opens the
/// suite, so the extension and the app see the same value. A test passes its own suite name.
public struct PendingRouteStore: Sendable {
  /// The defaults suite the store opens.
  public let suiteName: String

  /// Creates a store.
  /// - Parameter suiteName: The suite to open; defaults to ``AppGroup/identifier``.
  public init(suiteName: String = AppGroup.identifier) {
    self.suiteName = suiteName
  }

  /// Stores the route the app should open next, replacing any earlier one.
  /// - Parameter route: The route to open.
  public func write(_ route: AppRoute) {
    AppGroup.defaults(suiteName: suiteName)?.set(route.url.absoluteString, forKey: Self.key)
  }

  /// Reads the stored route and clears it, so it opens once.
  /// - Returns: The route, or `nil` when none is stored or the stored value is not a route.
  public func consume() -> AppRoute? {
    guard let defaults = AppGroup.defaults(suiteName: suiteName) else { return nil }
    let value = defaults.string(forKey: Self.key)
    defaults.removeObject(forKey: Self.key)
    guard let value, let url = URL(string: value) else { return nil }
    return AppRoute(url: url)
  }

  private static let key = "pending_route"
}
