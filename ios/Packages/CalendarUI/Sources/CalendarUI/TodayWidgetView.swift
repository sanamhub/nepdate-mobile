import CalendarKit
import SwiftUI
import WidgetKit

/// The small Today widget (W-01): the BS day numeral, month, weekday, AD date or holiday name,
/// the holiday mark, the next holiday and the Nepal label.
public struct TodayWidgetView: View {
  private let entry: WidgetEntry

  /// Creates the view.
  /// - Parameter entry: The values to draw.
  public init(entry: WidgetEntry) {
    self.entry = entry
  }

  /// The view's content.
  public var body: some View {
    VStack(alignment: .leading, spacing: 4) {
      HStack(alignment: .firstTextBaseline, spacing: 8) {
        Text(verbatim: entry.day.day)
          .font(.system(size: 56, weight: .semibold, design: .rounded))
          .monospacedDigit()
          .foregroundStyle(entry.day.isHoliday ? BrandColor.holiday : Color.primary)
          .widgetAccentable()
        Spacer(minLength: 0)
        Text(verbatim: entry.day.weekday)
          .font(.caption)
          .foregroundStyle(.secondary)
      }

      Text(verbatim: entry.day.month)
        .font(.headline)

      if entry.day.isHoliday {
        Rectangle()
          .fill(BrandColor.holiday)
          .frame(height: 1.5)
          .widgetAccentable()
      }

      if let holiday = entry.day.holidayName {
        Text(verbatim: holiday)
          .font(.caption2)
          .foregroundStyle(BrandColor.secondaryText)
          .lineLimit(1)
      } else if entry.day.isHoliday {
        Text("Public holiday")
          .font(.caption2)
          .foregroundStyle(BrandColor.secondaryText)
          .lineLimit(1)
      } else {
        Text(verbatim: entry.day.adDate)
          .font(.caption2)
          .foregroundStyle(BrandColor.secondaryText)
          .lineLimit(1)
      }

      if entry.day.showsNepalLabel {
        Text("Nepal")
          .font(.caption2)
          .foregroundStyle(BrandColor.today)
      }

      Spacer(minLength: 0)

      if let next = entry.nextHoliday {
        HStack(alignment: .firstTextBaseline, spacing: 4) {
          eventName(next)
            .lineLimit(1)
          Spacer(minLength: 0)
          relativeText(next)
            .foregroundStyle(.secondary)
        }
        .font(.caption2)
      }
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    .widgetURL(AppRoute.today.url)
  }

  @ViewBuilder
  private func eventName(_ event: WidgetEvent) -> some View {
    if let name = event.name {
      Text(verbatim: name)
    } else {
      Text("Public holiday")
    }
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

/// The medium Today and upcoming widget (W-02): W-01's content on the left and the next three
/// events on the right.
public struct UpcomingWidgetView: View {
  private let entry: WidgetEntry

  /// Creates the view.
  /// - Parameter entry: The values to draw.
  public init(entry: WidgetEntry) {
    self.entry = entry
  }

  /// The view's content.
  public var body: some View {
    HStack(alignment: .top, spacing: 12) {
      TodayWidgetView(entry: entry)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)

      Divider()

      VStack(alignment: .leading, spacing: 5) {
        ForEach(entry.upcoming, id: \.id) { event in
          Link(destination: AppRoute.day(event.date).url) {
            VStack(alignment: .leading, spacing: 1) {
              eventName(event)
                .font(.caption.weight(.semibold))
                .lineLimit(1)
              relativeText(event)
                .font(.caption2)
                .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
          }
          .foregroundStyle(.primary)
        }

        Spacer(minLength: 0)

        if let end = entry.dataEndsOn {
          Text("Holiday data ends on \(end)")
            .font(.caption2)
            .foregroundStyle(.secondary)
            .lineLimit(2)
        }
      }
      .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    .widgetURL(AppRoute.today.url)
  }

  @ViewBuilder
  private func eventName(_ event: WidgetEvent) -> some View {
    if let name = event.name {
      Text(verbatim: name)
    } else {
      Text("Public holiday")
    }
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
