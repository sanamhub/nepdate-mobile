import AppIntents
import CalendarKit
import Foundation
import SwiftUI

/// Opens today's month from Siri, Spotlight or the Shortcuts app.
struct AppOpenTodayIntent: AppIntent {
  static let title: LocalizedStringResource = "Open today"
  static let openAppWhenRun: Bool = true

  func perform() async throws -> some IntentResult {
    PendingRouteStore().write(.today)
    return .result()
  }
}

/// Opens a typed date in the Patro tab.
struct GoToDateIntent: AppIntent {
  static let title: LocalizedStringResource = "Go to date"
  static let openAppWhenRun: Bool = true

  @Parameter(title: "Date")
  var date: Date

  func perform() async throws -> some IntentResult {
    guard let nepali = try? NepaliDate(date, in: CalendarDates.nepal) else {
      return .result()
    }
    PendingRouteStore().write(
      .day(MonthDate(year: nepali.year, month: nepali.month, day: nepali.day))
    )
    return .result()
  }
}

/// Answers with the next public holiday and its day count.
struct NextHolidayIntent: AppIntent {
  static let title: LocalizedStringResource = "Next public holiday"

  func perform() async throws -> some IntentResult & ShowsSnippetView {
    let today = TodayProvider().today(dateFollows: .nepal)
    guard let next = PatroLookahead.nextHoliday(after: today, withinDays: 400) else {
      return .result(view: NextHolidaySnippetView(title: "No public holiday left", detail: ""))
    }
    let name = next.name?.english ?? "Public holiday"
    let detail: String
    switch next.days {
    case 0: detail = "Today"
    case 1: detail = "Tomorrow"
    default: detail = "in \(next.days) days"
    }
    return .result(view: NextHolidaySnippetView(title: name, detail: detail))
  }
}

/// The snippet under the next-holiday answer.
struct NextHolidaySnippetView: View {
  let title: String
  let detail: String

  var body: some View {
    VStack(alignment: .leading, spacing: 2) {
      Text(verbatim: title)
        .font(.headline)
      if !detail.isEmpty {
        Text(verbatim: detail)
          .font(.subheadline)
          .foregroundStyle(.secondary)
      }
    }
    .frame(maxWidth: .infinity, alignment: .leading)
  }
}

/// The App Shortcuts the app offers, with no setup screen.
struct NepDateShortcuts: AppShortcutsProvider {
  static var appShortcuts: [AppShortcut] {
    AppShortcut(
      intent: AppOpenTodayIntent(),
      phrases: [
        "Open \(.applicationName) today",
        "Show today in \(.applicationName)",
      ],
      shortTitle: "Open today",
      systemImage: "calendar"
    )
    AppShortcut(
      intent: GoToDateIntent(),
      phrases: [
        "Open a date in \(.applicationName)",
        "Open \(\.$date) in \(.applicationName)",
      ],
      shortTitle: "Go to date",
      systemImage: "calendar.badge.clock"
    )
    AppShortcut(
      intent: NextHolidayIntent(),
      phrases: [
        "Next holiday in \(.applicationName)",
        "When is the next holiday in \(.applicationName)",
      ],
      shortTitle: "Next holiday",
      systemImage: "sparkles"
    )
  }
}

/// One festival day, for Spotlight and Siri.
struct FestivalEntity: AppEntity {
  static let typeDisplayRepresentation: TypeDisplayRepresentation = "Festival"
  static let defaultQuery = FestivalEntityQuery()

  let id: String
  let name: String
  let dateText: String

  var displayRepresentation: DisplayRepresentation {
    DisplayRepresentation(title: "\(name)", subtitle: "\(dateText)")
  }
}

/// Looks a festival entity up by its date identity.
struct FestivalEntityQuery: EntityQuery {
  func entities(for identifiers: [String]) async throws -> [FestivalEntity] {
    all.filter { identifiers.contains($0.id) }
  }

  func suggestedEntities() async throws -> [FestivalEntity] {
    all
  }

  private var all: [FestivalEntity] {
    FestivalDirectory.all.map { day in
      FestivalEntity(
        id: "\(day.date.year)-\(day.date.month)-\(day.date.day)",
        name: day.names.first?.english ?? "Public holiday",
        dateText: "\(day.date.year)-\(day.date.month)-\(day.date.day)"
      )
    }
  }
}
