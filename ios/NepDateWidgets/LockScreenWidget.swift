import CalendarKit
import CalendarUI
import SwiftUI
import WidgetKit

/// The Lock Screen widget (W-04): inline, circular and rectangular.
struct LockScreenWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "LockScreenWidget", provider: TodayTimeline()) { entry in
      LockScreenWidgetView(entry: entry.entry)
        .containerBackground(.background, for: .widget)
    }
    .configurationDisplayName("NepDate lock screen")
    .description("Today's date and the next holiday.")
    .supportedFamilies([.accessoryInline, .accessoryCircular, .accessoryRectangular])
  }
}

/// Picks the view for the family the system asks for.
struct LockScreenWidgetView: View {
  @Environment(\.widgetFamily) private var family
  let entry: WidgetEntry

  var body: some View {
    switch family {
    case .accessoryInline:
      LockScreenInlineView(entry: entry)
    case .accessoryCircular:
      LockScreenCircularView(entry: entry)
    default:
      LockScreenRectangularView(entry: entry)
    }
  }
}
