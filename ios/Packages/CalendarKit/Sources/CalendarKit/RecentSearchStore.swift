import Foundation

/// The last few festival names a search opened (MR-13 AC4).
///
/// The list lives in the App Group, newest first, without duplicates. A test passes its own suite
/// name.
public struct RecentSearchStore: Sendable {
  /// The defaults suite the store opens.
  public let suiteName: String

  /// The most entries the store keeps.
  public let limit: Int

  /// Creates a store.
  /// - Parameters:
  ///   - suiteName: The suite to open; defaults to ``AppGroup/identifier``.
  ///   - limit: The most entries to keep; 5 by default.
  public init(suiteName: String = AppGroup.identifier, limit: Int = 5) {
    self.suiteName = suiteName
    self.limit = limit
  }

  /// The stored queries, newest first.
  public func load() -> [String] {
    AppGroup.defaults(suiteName: suiteName)?.stringArray(forKey: Self.key) ?? []
  }

  /// Records a query at the front, dropping a duplicate and anything past the limit.
  /// - Parameter query: The query; an empty or whitespace-only query is not recorded.
  public func add(_ query: String) {
    let value = query.trimmingCharacters(in: .whitespacesAndNewlines)
    guard !value.isEmpty else { return }
    var values = load().filter { $0 != value }
    values.insert(value, at: 0)
    save(Array(values.prefix(limit)))
  }

  /// Removes one query.
  /// - Parameter query: The query to remove.
  public func remove(_ query: String) {
    save(load().filter { $0 != query })
  }

  /// Removes every query.
  public func clear() {
    save([])
  }

  private func save(_ values: [String]) {
    AppGroup.defaults(suiteName: suiteName)?.set(values, forKey: Self.key)
  }

  private static let key = "recent_searches"
}
