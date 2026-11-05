import CalendarKit
import SwiftUI
import WidgetKit

/// The inline Lock Screen widget (W-04): one line, for example `Asoj 20, Tuesday`.
public struct LockScreenInlineView: View {
  private let entry: WidgetEntry

  /// Creates the view.
  /// - Parameter entry: The values to draw.
  public init(entry: WidgetEntry) {
    self.entry = entry
  }

  /// The view's content.
  public var body: some View {
    Text(verbatim: entry.lockScreen.inline)
      .widgetURL(AppRoute.today.url)
      .accessibilityLabel(Text(verbatim: WidgetSpeech.today(entry, words: .localized)))
  }
}

/// The circular Lock Screen widget (W-04): the day over the short month, underlined on a holiday.
public struct LockScreenCircularView: View {
  private let entry: WidgetEntry

  /// Creates the view.
  /// - Parameter entry: The values to draw.
  public init(entry: WidgetEntry) {
    self.entry = entry
  }

  /// The view's content.
  public var body: some View {
    let content = entry.lockScreen
    ZStack {
      AccessoryWidgetBackground()
      VStack(spacing: 1) {
        Text(verbatim: content.circularDay)
          .font(.system(size: 22, weight: .semibold, design: .rounded))
          .minimumScaleFactor(0.6)
          .lineLimit(1)
          .widgetAccentable()
        Text(verbatim: content.circularMonth)
          .font(.system(size: 10))
          .lineLimit(1)
        Capsule()
          .fill(content.isHoliday ? Color.primary : Color.clear)
          .frame(width: 18, height: 2)
          .widgetAccentable()
      }
    }
    .widgetURL(AppRoute.today.url)
    .accessibilityElement(children: .ignore)
    .accessibilityLabel(Text(verbatim: WidgetSpeech.today(entry, words: .localized)))
  }
}

/// The rectangular Lock Screen widget (W-04): the date and today's or the next holiday with the
/// day count.
public struct LockScreenRectangularView: View {
  private let entry: WidgetEntry

  /// Creates the view.
  /// - Parameter entry: The values to draw.
  public init(entry: WidgetEntry) {
    self.entry = entry
  }

  /// The view's content.
  public var body: some View {
    let content = entry.lockScreen
    VStack(alignment: .leading, spacing: 2) {
      Text(verbatim: content.rectangularDate)
        .font(.headline)
        .lineLimit(1)
        .widgetAccentable()
      if let holiday = content.rectangularHoliday {
        if let name = holiday.name {
          Text(verbatim: name)
            .font(.caption)
            .lineLimit(1)
        } else {
          Text("Public holiday")
            .font(.caption)
            .lineLimit(1)
        }
        relativeText(holiday)
          .font(.caption2)
          .foregroundStyle(.secondary)
      }
    }
    .frame(maxWidth: .infinity, alignment: .leading)
    .widgetURL(AppRoute.today.url)
    .accessibilityElement(children: .ignore)
    .accessibilityLabel(Text(verbatim: spoken))
  }

  private var spoken: String {
    var text = WidgetSpeech.today(entry, words: .localized)
    if let holiday = entry.lockScreen.rectangularHoliday, !entry.day.isHoliday {
      text += ", " + WidgetSpeech.event(holiday, words: .localized)
    }
    return text
  }

  private func relativeText(_ event: WidgetEvent) -> Text {
    switch event.daysFromToday {
    case 0:
      return Text("Today")
    case 1:
      return Text("Tomorrow")
    default:
      return Text("in \(event.count) days")
    }
  }
}

extension SpokenWords {
  /// The words in the language the running bundle shows, for the widgets' spoken labels.
  static var localized: SpokenWords {
    SpokenWords(
      publicHoliday: String(localized: "public holiday"),
      today: String(localized: "Today"),
      tomorrow: String(localized: "Tomorrow"),
      inDays: { count in String(localized: "in \(count) days") }
    )
  }
}
