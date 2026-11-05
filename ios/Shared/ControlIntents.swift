import AppIntents
import CalendarKit

/// Opens the Converter from a control. The route goes through the App Group and the app reads it
/// when it becomes active (W-05). The file is compiled into the app and the widget extension,
/// because an intent that opens the app has to exist in the app too.
struct OpenConverterIntent: AppIntent {
  static let title: LocalizedStringResource = "Open converter"
  static let openAppWhenRun: Bool = true

  func perform() async throws -> some IntentResult {
    PendingRouteStore().write(.converter)
    return .result()
  }
}

/// Opens today's month from a control, in the same way as ``OpenConverterIntent``.
struct OpenTodayIntent: AppIntent {
  static let title: LocalizedStringResource = "Open today"
  static let openAppWhenRun: Bool = true

  func perform() async throws -> some IntentResult {
    PendingRouteStore().write(.today)
    return .result()
  }
}
