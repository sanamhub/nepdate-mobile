import CalendarKit
import CalendarUI
import Foundation
import SwiftUI
import WidgetKit

/// The small Today widget (W-01): the Bikram Sambat date at a glance.
struct TodayWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "TodayWidget", provider: TodayTimeline()) { entry in
      TodayWidgetView(entry: entry.entry)
        .containerBackground(.background, for: .widget)
    }
    .configurationDisplayName("NepDate")
    .description("Today's Bikram Sambat date.")
    .supportedFamilies([.systemSmall])
  }
}

/// The medium Today and upcoming widget (W-02): today on the left and the next three events.
struct UpcomingWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "UpcomingWidget", provider: TodayTimeline()) { entry in
      UpcomingWidgetView(entry: entry.entry)
        .containerBackground(.background, for: .widget)
    }
    .configurationDisplayName("NepDate")
    .description("Today and the next three holidays or events.")
    .supportedFamilies([.systemMedium])
  }
}

/// One entry of the Today or upcoming widget.
struct TodayEntry: TimelineEntry {
  let date: Date
  let entry: WidgetEntry
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

  private var current: TodayEntry {
    let settings = SettingsStore().load()
    let entry =
      WidgetTimeline.entries(now: Date(), settings: settings).first
      ?? WidgetTimeline.preview(settings: settings)
    return TodayEntry(date: entry.date, entry: entry)
  }
}
