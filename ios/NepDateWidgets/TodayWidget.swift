import AppIntents
import CalendarKit
import CalendarUI
import Foundation
import SwiftUI
import WidgetKit

/// The small Today widget (W-01): the Bikram Sambat date at a glance.
struct TodayWidget: Widget {
  var body: some WidgetConfiguration {
    AppIntentConfiguration(
      kind: "TodayWidget",
      intent: TodayConfigurationIntent.self,
      provider: TodayIntentProvider()
    ) { entry in
      TodayWidgetView(
        entry: entry.entry,
        mainCalendar: entry.mainCalendar,
        showsDetails: entry.showsDetails
      )
      .containerBackground(.background, for: .widget)
    }
    .configurationDisplayName("NepDate today")
    .description("Shows today's date, its tithi and the next holiday.")
    .supportedFamilies([.systemSmall])
  }
}

/// The medium Today and upcoming widget (W-02): today on the left and the next four events.
struct UpcomingWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "UpcomingWidget", provider: TodayTimeline()) { entry in
      UpcomingWidgetView(entry: entry.entry)
        .containerBackground(.background, for: .widget)
    }
    .configurationDisplayName("NepDate upcoming")
    .description("Shows today and the next four days with a holiday or event.")
    .supportedFamilies([.systemMedium])
  }
}

/// One entry of the Today or upcoming widget.
struct TodayEntry: TimelineEntry {
  let date: Date
  let entry: WidgetEntry
  var mainCalendar: MainCalendar = .bs
  var showsDetails = true
}

/// The Today widget's configuration (W-01): the main calendar and whether the details show.
struct TodayConfigurationIntent: WidgetConfigurationIntent {
  static let title: LocalizedStringResource = "Today"
  static let description = IntentDescription(
    "Choose the main calendar and whether the tithi and the next holiday show."
  )

  @Parameter(title: "Main calendar", default: MainCalendar.bs)
  var calendar: MainCalendar

  @Parameter(title: "Show details", default: true)
  var showDetails: Bool
}

extension MainCalendar: AppEnum {
  /// The calendar setting's name in the widget's configuration.
  public static var typeDisplayRepresentation: TypeDisplayRepresentation {
    TypeDisplayRepresentation(name: "Main calendar")
  }

  /// The label of each calendar in the configuration picker.
  public static var caseDisplayRepresentations: [MainCalendar: DisplayRepresentation] {
    [
      .bs: DisplayRepresentation(title: "Bikram Sambat"),
      .ad: DisplayRepresentation(title: "Gregorian"),
    ]
  }
}

/// Supplies the Today widget's entries from the shared settings.
struct TodayIntentProvider: AppIntentTimelineProvider {
  func placeholder(in context: Context) -> TodayEntry {
    TodayEntry(date: Date(), entry: WidgetTimeline.preview())
  }

  func snapshot(for configuration: TodayConfigurationIntent, in context: Context) async
    -> TodayEntry
  {
    TodayEntry(
      date: Date(),
      entry: WidgetTimeline.preview(),
      mainCalendar: configuration.calendar,
      showsDetails: configuration.showDetails
    )
  }

  func timeline(for configuration: TodayConfigurationIntent, in context: Context) async
    -> Timeline<TodayEntry>
  {
    let settings = SettingsStore().load()
    let entries = WidgetTimeline.entries(now: Date(), settings: settings).map { entry in
      TodayEntry(
        date: entry.date,
        entry: entry,
        mainCalendar: configuration.calendar,
        showsDetails: configuration.showDetails
      )
    }
    return Timeline(entries: entries, policy: .atEnd)
  }
}

/// Supplies the widget's entries from the shared settings, so the widgets show the same day,
/// language and digits as the app. The next seven local midnights come from `WidgetTimeline`.
struct TodayTimeline: TimelineProvider {
  func placeholder(in context: Context) -> TodayEntry {
    let entry = WidgetTimeline.preview()
    return TodayEntry(date: entry.date, entry: entry)
  }

  func getSnapshot(in context: Context, completion: @escaping (TodayEntry) -> Void) {
    let entry = WidgetTimeline.preview()
    completion(TodayEntry(date: entry.date, entry: entry))
  }

  func getTimeline(in context: Context, completion: @escaping (Timeline<TodayEntry>) -> Void) {
    let settings = SettingsStore().load()
    let entries = WidgetTimeline.entries(now: Date(), settings: settings)
    let timelineEntries = entries.map { TodayEntry(date: $0.date, entry: $0) }
    completion(Timeline(entries: timelineEntries, policy: .atEnd))
  }
}
