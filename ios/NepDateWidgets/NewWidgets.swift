import AppIntents
import CalendarKit
import CalendarUI
import NepDate
import NepDatePatro
import Foundation
import SwiftUI
import WidgetKit

/// The festival countdown (W-06), as a small widget and a rectangular Lock Screen widget.
struct FestivalCountdownWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(
      kind: FestivalCountdownKind.value,
      provider: FestivalCountdownProvider()
    ) { entry in
      FestivalCountdownWidgetView(entry: entry)
        .containerBackground(.background, for: .widget)
    }
    .configurationDisplayName("NepDate countdown")
    .description("Counts down to the next festival and steps to the one after it.")
    .supportedFamilies([.systemSmall, .accessoryRectangular])
  }
}

enum FestivalCountdownKind {
  static let value = "FestivalCountdownWidget"
}

struct FestivalCountdownEntry: TimelineEntry {
  let date: Date
  let events: [WidgetEvent]
  let index: Int

  var shown: WidgetEvent? {
    guard events.indices.contains(index) else { return events.first }
    return events[index]
  }

  var canPrevious: Bool { index > 0 }
  var canNext: Bool { index < events.count - 1 }
}

struct FestivalCountdownWidgetView: View {
  let entry: FestivalCountdownEntry
  @Environment(\.widgetFamily) private var family

  var body: some View {
    if family == .accessoryRectangular {
      content
    } else {
      VStack(spacing: 4) {
        content
        HStack(spacing: 8) {
          Button(intent: PreviousFestivalIntent()) {
            Image(systemName: "chevron.left")
          }
          .disabled(!entry.canPrevious)
          Button(intent: NextFestivalIntent()) {
            Image(systemName: "chevron.right")
          }
          .disabled(!entry.canNext)
        }
        .font(.caption)
      }
    }
  }

  @ViewBuilder
  private var content: some View {
    if let event = entry.shown {
      FestivalCountdownView(event: event)
    } else {
      Text("No festivals left")
    }
  }
}

struct FestivalCountdownProvider: TimelineProvider {
  func placeholder(in context: Context) -> FestivalCountdownEntry {
    preview
  }

  func getSnapshot(in context: Context, completion: @escaping (FestivalCountdownEntry) -> Void) {
    completion(preview)
  }

  func getTimeline(
    in context: Context,
    completion: @escaping (Timeline<FestivalCountdownEntry>) -> Void
  ) {
    let now = Date()
    let entry = makeEntry(now: now)
    let settings = SettingsStore().load()
    let zone = MidnightRefresh.zone(for: settings.dateFollows)
    let wait = MidnightRefresh.secondsUntilNextMidnight(now: now, zone: zone)
    completion(
      Timeline(entries: [entry], policy: .after(now.addingTimeInterval(wait)))
    )
  }

  private var preview: FestivalCountdownEntry {
    let entry = WidgetTimeline.preview()
    let events = WidgetTimeline.festivalWindow(
      now: entry.date,
      settings: .defaults,
      deviceZone: CalendarDates.nepal
    )
    return FestivalCountdownEntry(date: entry.date, events: events, index: 0)
  }

  private func makeEntry(now: Date) -> FestivalCountdownEntry {
    let settings = SettingsStore().load()
    let events = WidgetTimeline.festivalWindow(now: now, settings: settings)
    let today = TodayProvider(now: { now }).today(dateFollows: settings.dateFollows)
    let index = FestivalWindowNavigation().index(on: today, count: events.count)
    return FestivalCountdownEntry(date: now, events: events, index: index)
  }
}

/// Steps the festival countdown one day to the left.
struct PreviousFestivalIntent: AppIntent {
  static let title: LocalizedStringResource = "Previous festival"

  func perform() async throws -> some IntentResult {
    step(-1)
    WidgetCenter.shared.reloadTimelines(ofKind: FestivalCountdownKind.value)
    return .result()
  }

  private func step(_ value: Int) {
    let settings = SettingsStore().load()
    let today = TodayProvider().today(dateFollows: settings.dateFollows)
    let events = WidgetTimeline.festivalWindow(now: Date(), settings: settings)
    FestivalWindowNavigation().step(value, today: today, count: events.count)
  }
}

/// Steps the festival countdown one day to the right.
struct NextFestivalIntent: AppIntent {
  static let title: LocalizedStringResource = "Next festival"

  func perform() async throws -> some IntentResult {
    step(1)
    WidgetCenter.shared.reloadTimelines(ofKind: FestivalCountdownKind.value)
    return .result()
  }

  private func step(_ value: Int) {
    let settings = SettingsStore().load()
    let today = TodayProvider().today(dateFollows: settings.dateFollows)
    let events = WidgetTimeline.festivalWindow(now: Date(), settings: settings)
    FestivalWindowNavigation().step(value, today: today, count: events.count)
  }
}

/// The festivals list (W-07): the coming days, with a Holidays only configuration.
struct FestivalsListWidget: Widget {
  var body: some WidgetConfiguration {
    AppIntentConfiguration(
      kind: "FestivalsListWidget",
      intent: FestivalsConfigurationIntent.self,
      provider: FestivalsListProvider()
    ) { entry in
      FestivalsListWidgetView(
        days: entry.days,
        language: entry.language,
        dataEndsOn: entry.dataEndsOn
      )
      .containerBackground(.background, for: .widget)
    }
    .configurationDisplayName("NepDate festivals")
    .description("Lists the coming festivals and holidays.")
    .supportedFamilies([.systemLarge])
  }
}

struct FestivalsConfigurationIntent: WidgetConfigurationIntent {
  static let title: LocalizedStringResource = "Festivals"
  static let description = IntentDescription("Choose whether only public holidays are listed.")

  @Parameter(title: "Holidays only", default: false)
  var holidaysOnly: Bool
}

struct FestivalsListEntry: TimelineEntry {
  let date: Date
  let days: [FestivalDay]
  let language: AppLanguage
  let dataEndsOn: String?
}

struct FestivalsListProvider: AppIntentTimelineProvider {
  func placeholder(in context: Context) -> FestivalsListEntry {
    preview
  }

  func snapshot(for configuration: FestivalsConfigurationIntent, in context: Context) async
    -> FestivalsListEntry
  {
    entry(now: Date(), holidaysOnly: configuration.holidaysOnly)
  }

  func timeline(for configuration: FestivalsConfigurationIntent, in context: Context) async
    -> Timeline<FestivalsListEntry>
  {
    let now = Date()
    let settings = SettingsStore().load()
    let zone = MidnightRefresh.zone(for: settings.dateFollows)
    let wait = MidnightRefresh.secondsUntilNextMidnight(now: now, zone: zone)
    return Timeline(
      entries: [entry(now: now, holidaysOnly: configuration.holidaysOnly)],
      policy: .after(now.addingTimeInterval(wait))
    )
  }

  private var preview: FestivalsListEntry {
    let date = WidgetTimeline.preview().date
    return entry(now: date, holidaysOnly: false, zone: CalendarDates.nepal)
  }

  private func entry(
    now: Date,
    holidaysOnly: Bool,
    zone: TimeZone = .current
  ) -> FestivalsListEntry {
    let settings = SettingsStore().load()
    let language = settings.language.resolved
    let digits = settings.digits.resolved(for: language)
    let today = TodayProvider(now: { now }, deviceZone: zone)
      .today(dateFollows: settings.dateFollows)
    let all = FestivalDirectory.next(after: today, limit: 24)
    let days = holidaysOnly ? all.filter(\.isHoliday) : all
    let end = Patro.coverage.upperBound
    let dataEndsOn: String? =
      days.count < 8
      ? "\(MonthNames.bs(end.month, language)) \(digits.format(end.day)), "
        + "\(digits.format(end.year))"
      : nil
    return FestivalsListEntry(
      date: now,
      days: days,
      language: language,
      dataEndsOn: dataEndsOn.map { String(localized: "Holiday data ends on \($0)") }
    )
  }
}

/// The this-week widget (W-10).
struct ThisWeekWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "ThisWeekWidget", provider: ThisWeekProvider()) { entry in
      if let week = entry.week {
        ThisWeekWidgetView(week: week)
          .containerBackground(.background, for: .widget)
      } else {
        Text("No week")
          .containerBackground(.background, for: .widget)
      }
    }
    .configurationDisplayName("NepDate this week")
    .description("Shows the week and the next festival or event.")
    .supportedFamilies([.systemMedium])
  }
}

struct ThisWeekEntry: TimelineEntry {
  let date: Date
  let week: WidgetWeek?
}

struct ThisWeekProvider: TimelineProvider {
  func placeholder(in context: Context) -> ThisWeekEntry {
    preview
  }

  func getSnapshot(in context: Context, completion: @escaping (ThisWeekEntry) -> Void) {
    completion(preview)
  }

  func getTimeline(in context: Context, completion: @escaping (Timeline<ThisWeekEntry>) -> Void) {
    let now = Date()
    let settings = SettingsStore().load()
    let zone = MidnightRefresh.zone(for: settings.dateFollows)
    let wait = MidnightRefresh.secondsUntilNextMidnight(now: now, zone: zone)
    completion(
      Timeline(
        entries: [entry(now: now)],
        policy: .after(now.addingTimeInterval(wait))
      )
    )
  }

  private var preview: ThisWeekEntry {
    let date = WidgetTimeline.preview().date
    return entry(now: date, zone: CalendarDates.nepal)
  }

  private func entry(now: Date, zone: TimeZone = .current) -> ThisWeekEntry {
    let settings = SettingsStore().load()
    return ThisWeekEntry(
      date: now,
      week: WidgetWeekBuilder.week(now: now, settings: settings, deviceZone: zone)
    )
  }
}
