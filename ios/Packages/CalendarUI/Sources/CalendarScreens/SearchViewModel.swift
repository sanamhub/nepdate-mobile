import CalendarKit
import Foundation

/// The search screen's state (MR-13): the typed query, its name results grouped by BS month, the
/// date-jump readings, the recent searches and the next three festivals.
public struct SearchViewModel: Sendable {
  /// The first day the landing list and the jump start from.
  public let today: MonthDate

  /// The language of the names.
  public let language: AppLanguage

  /// The resolved Digits setting.
  public let digits: Digits

  /// The trimmed query.
  public let query: String

  /// The date readings of the query.
  public let jump: DateJump

  /// The name matches, grouped by BS month.
  public let groups: [FestivalGroup]

  /// The next three festival days, for the landing state.
  public let nextFestivals: [FestivalDay]

  /// The recent searches, newest first.
  public let recent: [String]

  /// Creates the state.
  /// - Parameters:
  ///   - today: The first day the landing list and the jump start from.
  ///   - language: The language of the names.
  ///   - digits: The resolved Digits setting.
  ///   - query: The typed text.
  ///   - recentStore: The store the recent searches come from.
  public init(
    today: MonthDate,
    language: AppLanguage,
    digits: Digits,
    query: String,
    recentStore: RecentSearchStore = RecentSearchStore()
  ) {
    self.today = today
    self.language = language
    self.digits = digits
    let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines)
    self.query = trimmed
    self.jump = DateJump(trimmed)
    let days = trimmed.isEmpty ? [] : FestivalDirectory.all.filter { $0.matches(trimmed) }
    self.groups = FestivalsViewModel.group(days, language: language, digits: digits)
    self.nextFestivals = FestivalDirectory.next(after: today, limit: 3)
    self.recent = recentStore.load()
  }

  /// Whether the landing state shows: no query.
  public var isLanding: Bool {
    query.isEmpty
  }

  /// The number of name matches.
  public var resultCount: Int {
    groups.reduce(0) { $0 + $1.days.count }
  }

  /// Whether the query matched no name and read as no date either.
  public var showsEmptyState: Bool {
    !query.isEmpty && groups.isEmpty && jump.isUnreadable
  }
}
