import CalendarKit
import CalendarUI
import Foundation
import SwiftUI
import WidgetKit

/// The Today widget: the Bikram Sambat date at a glance.
struct TodayWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "TodayWidget", provider: TodayProvider()) { entry in
      TodayWidgetView(summary: entry.summary)
        .containerBackground(.background, for: .widget)
    }
    .configurationDisplayName("NepDate")
    .description("Today's Bikram Sambat date.")
    .supportedFamilies([.systemSmall, .systemMedium])
  }
}

/// One entry of the Today widget.
struct TodayEntry: TimelineEntry {
  let date: Date
  let summary: TodaySummary
}

/// Supplies the widget's entries. The M0 placeholder reads the clock once per entry; I2 moves the
/// midnight scheduling into CalendarKit.
struct TodayProvider: TimelineProvider {
  func placeholder(in context: Context) -> TodayEntry {
    TodayEntry(date: Date(), summary: current)
  }

  func getSnapshot(in context: Context, completion: @escaping (TodayEntry) -> Void) {
    completion(TodayEntry(date: Date(), summary: current))
  }

  func getTimeline(in context: Context, completion: @escaping (Timeline<TodayEntry>) -> Void) {
    let entry = TodayEntry(date: Date(), summary: current)
    completion(Timeline(entries: [entry], policy: .atEnd))
  }

  private var current: TodaySummary {
    TodaySummary.make(language: AppLanguage.current)
  }
}
