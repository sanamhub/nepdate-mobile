import CalendarKit
import SwiftUI
import WidgetKit

/// The Lock Screen countdown gauge for W-06: the days left to the festival as a ring, with the
/// same day count in the middle. The ring fills as the festival comes closer.
public struct FestivalCountdownGaugeView: View {
  private let event: WidgetEvent

  /// Creates the gauge.
  /// - Parameter event: The festival day to count down to.
  public init(event: WidgetEvent) {
    self.event = event
  }

  /// The gauge's content.
  public var body: some View {
    Gauge(value: Self.fraction(days: event.daysFromToday), in: 0...1) {
      Text(verbatim: discText)
    } currentValueLabel: {
      Text(verbatim: discText)
    }
    .gaugeStyle(.accessoryCircularCapacity)
    .widgetAccentable()
    .accessibilityLabel(Text(verbatim: spoken))
  }

  /// The filled share of the ring: 0 on the festival day, 1 twelve days before it. The widget's
  /// window is twelve days, so a longer wait is clamped rather than drawn as a full ring.
  public static func fraction(days: Int) -> Double {
    Double(min(max(days, 0), 12)) / 12
  }

  private var discText: String {
    FestivalCountdownText.disc(days: event.daysFromToday, count: event.count)
  }

  private var spoken: String {
    let name = event.name ?? String(localized: "Public holiday")
    switch event.daysFromToday {
    case 0: return "\(name), \(String(localized: "Today"))"
    case 1: return "\(name), \(String(localized: "Tomorrow"))"
    default: return "\(name), \(String(localized: "in \(event.count) days"))"
    }
  }
}
