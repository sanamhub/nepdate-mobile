import AppIntents
import SwiftUI
import WidgetKit

/// The "Open converter" control (W-05), for Control Center, the Lock Screen and the Action button.
struct OpenConverterControl: ControlWidget {
  var body: some ControlWidgetConfiguration {
    StaticControlConfiguration(kind: "OpenConverterControl") {
      ControlWidgetButton(action: OpenConverterIntent()) {
        Label("Open converter", systemImage: "arrow.left.arrow.right")
      }
    }
    .displayName("Open converter")
    .description("Opens the converter.")
  }
}

/// The "Open today" control (W-05).
struct OpenTodayControl: ControlWidget {
  var body: some ControlWidgetConfiguration {
    StaticControlConfiguration(kind: "OpenTodayControl") {
      ControlWidgetButton(action: OpenTodayIntent()) {
        Label("Open today", systemImage: "calendar")
      }
    }
    .displayName("Open today")
    .description("Opens today's month.")
  }
}
