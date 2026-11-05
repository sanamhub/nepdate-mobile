import AppIntents
import CalendarKit
import CalendarUI
import SwiftUI
import WidgetKit

/// The large month widget (W-03) with Previous, Today and Next buttons.
struct MonthWidget: Widget {
  static let kind = "MonthWidget"

  var body: some WidgetConfiguration {
    StaticConfiguration(kind: Self.kind, provider: MonthTimelineProvider()) { entry in
      MonthWidgetView(entry: entry.entry) {
        HStack(spacing: 2) {
          Button(intent: PreviousMonthIntent()) {
            Image(systemName: "chevron.left")
              .frame(width: 28, height: 24)
          }
          .accessibilityLabel("Previous")
          Button(intent: TodayMonthIntent()) {
            Text("Today")
              .font(.caption)
              .frame(height: 24)
          }
          .accessibilityLabel("Today")
          Button(intent: NextMonthIntent()) {
            Image(systemName: "chevron.right")
              .frame(width: 28, height: 24)
          }
          .accessibilityLabel("Next")
        }
      }
      .containerBackground(.background, for: .widget)
    }
    .configurationDisplayName("NepDate month")
    .description("The month, with buttons to browse it.")
    .supportedFamilies([.systemLarge])
  }
}

/// One entry of the month widget.
struct MonthEntry: TimelineEntry {
  let date: Date
  let entry: MonthWidgetEntry
}

/// Supplies the month widget's entries: now and the next seven local midnights, each showing the
/// stored month only on the day it was chosen.
struct MonthTimelineProvider: TimelineProvider {
  func placeholder(in context: Context) -> MonthEntry {
    preview
  }

  func getSnapshot(in context: Context, completion: @escaping (MonthEntry) -> Void) {
    completion(preview)
  }

  func getTimeline(in context: Context, completion: @escaping (Timeline<MonthEntry>) -> Void) {
    let settings = SettingsStore().load()
    let entries = MonthWidgetTimeline.entries(now: Date(), settings: settings)
      .map { MonthEntry(date: $0.date, entry: $0) }
    completion(Timeline(entries: entries.isEmpty ? [preview] : entries, policy: .atEnd))
  }

  private var preview: MonthEntry {
    let instant = WidgetTimeline.preview().date
    let entry = MonthWidgetTimeline.entry(
      at: instant,
      shown: nil,
      settings: .defaults,
      deviceZone: CalendarDates.nepal
    )
    return MonthEntry(date: instant, entry: entry!)
  }
}

/// Moves the month widget to the previous month.
struct PreviousMonthIntent: AppIntent {
  static let title: LocalizedStringResource = "Previous month"

  func perform() async throws -> some IntentResult {
    MonthNavigation().perform(.previous)
    WidgetCenter.shared.reloadTimelines(ofKind: MonthWidget.kind)
    return .result()
  }
}

/// Moves the month widget to the next month.
struct NextMonthIntent: AppIntent {
  static let title: LocalizedStringResource = "Next month"

  func perform() async throws -> some IntentResult {
    MonthNavigation().perform(.next)
    WidgetCenter.shared.reloadTimelines(ofKind: MonthWidget.kind)
    return .result()
  }
}

/// Shows the current month in the month widget again.
struct TodayMonthIntent: AppIntent {
  static let title: LocalizedStringResource = "Today"

  func perform() async throws -> some IntentResult {
    MonthNavigation().perform(.today)
    WidgetCenter.shared.reloadTimelines(ofKind: MonthWidget.kind)
    return .result()
  }
}
