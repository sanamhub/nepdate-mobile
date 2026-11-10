import CalendarKit
import SwiftUI
import WidgetKit

/// The festival countdown (W-06): the next festival's name, its day count on the SoftBurst, and
/// the holiday mark when the day is a public holiday.
public struct FestivalCountdownView: View {
  private let event: WidgetEvent

  /// Creates the view.
  /// - Parameter event: The festival day to show.
  public init(event: WidgetEvent) {
    self.event = event
  }

  /// The view's content.
  public var body: some View {
    VStack(alignment: .leading, spacing: 6) {
      HStack(alignment: .center, spacing: 8) {
        ZStack {
          SoftBurstShape()
            .fill(BrandColor.holidayContainer)
          Text(verbatim: event.count)
            .font(.headline.weight(.bold))
            .foregroundStyle(BrandColor.onHolidayContainer)
            .minimumScaleFactor(0.6)
            .lineLimit(1)
        }
        .frame(width: 44, height: 44)
        .widgetAccentable()

        VStack(alignment: .leading, spacing: 1) {
          Text(verbatim: event.name ?? String(localized: "Public holiday"))
            .font(.headline)
            .lineLimit(2)
          Text(verbatim: relativeText)
            .font(.caption)
            .foregroundStyle(BrandColor.secondaryText)
        }
      }

      if event.isHoliday {
        Rectangle()
          .fill(BrandColor.holiday)
          .frame(height: 1.5)
          .widgetAccentable()
      }

      Spacer(minLength: 0)
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    .widgetURL(AppRoute.day(event.date).url)
    .accessibilityElement(children: .ignore)
    .accessibilityLabel(Text(verbatim: spoken))
  }

  private var relativeText: String {
    switch event.daysFromToday {
    case 0: return String(localized: "Today")
    case 1: return String(localized: "Tomorrow")
    default: return String(localized: "in \(event.count) days")
    }
  }

  private var spoken: String {
    let name = event.name ?? String(localized: "Public holiday")
    var parts = [name, relativeText]
    if event.isHoliday { parts.append(String(localized: "Public holiday")) }
    return parts.joined(separator: ", ")
  }
}

/// The festivals list (W-07): the coming festival days, with the holiday mark and the data-ends
/// line.
public struct FestivalsListWidgetView: View {
  private let days: [FestivalDay]
  private let language: AppLanguage
  private let dataEndsOn: String?

  /// Creates the view.
  /// - Parameters:
  ///   - days: The days to list, in date order.
  ///   - language: The language of the names.
  ///   - dataEndsOn: The data-ends text, when the list is near the end of coverage.
  public init(days: [FestivalDay], language: AppLanguage, dataEndsOn: String? = nil) {
    self.days = days
    self.language = language
    self.dataEndsOn = dataEndsOn
  }

  /// The view's content.
  public var body: some View {
    VStack(alignment: .leading, spacing: 4) {
      ForEach(Array(days.prefix(8))) { day in
        Link(destination: AppRoute.day(day.date).url) {
          VStack(alignment: .leading, spacing: 1) {
            Text(verbatim: day.name(language) ?? String(localized: "Public holiday"))
              .font(.caption.weight(.semibold))
              .foregroundStyle(day.isHoliday ? BrandColor.holiday : .primary)
              .lineLimit(1)
            if day.isHoliday {
              Rectangle()
                .fill(BrandColor.holiday)
                .frame(width: 20, height: 1.5)
                .widgetAccentable()
            }
          }
          .frame(maxWidth: .infinity, alignment: .leading)
        }
        .foregroundStyle(.primary)
      }

      Spacer(minLength: 0)

      if let dataEndsOn {
        Text(verbatim: dataEndsOn)
          .font(.caption2)
          .foregroundStyle(.secondary)
          .lineLimit(2)
      }
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    .accessibilityElement(children: .contain)
  }
}

/// The this-week widget (W-10): the seven days of the BS week and the next festival or event.
public struct ThisWeekWidgetView: View {
  private let week: WidgetWeek

  /// Creates the view.
  /// - Parameter week: The week to draw.
  public init(week: WidgetWeek) {
    self.week = week
  }

  /// The view's content.
  public var body: some View {
    VStack(alignment: .leading, spacing: 8) {
      HStack(alignment: .top, spacing: 2) {
        ForEach(Array(week.days.enumerated()), id: \.offset) { _, day in
          VStack(spacing: 2) {
            Text(verbatim: day.weekday)
              .font(.system(size: 9))
              .foregroundStyle(BrandColor.secondaryText)
              .lineLimit(1)
              .minimumScaleFactor(0.6)
            ZStack {
              if day.isToday {
                SunnyShape().fill(BrandColor.today)
              } else if day.isHoliday {
                RoundedRectangle(cornerRadius: 6, style: .continuous)
                  .fill(BrandColor.holidayFill)
              }
              Text(verbatim: day.day)
                .font(.caption.weight(day.isToday ? .bold : .semibold))
                .foregroundStyle(ink(day))
                .lineLimit(1)
                .minimumScaleFactor(0.6)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 28)
            .overlay(alignment: .bottom) {
              if day.hasEvents {
                DiamondShape()
                  .fill(day.isHoliday ? BrandColor.holiday : BrandColor.secondaryText)
                  .frame(width: 4, height: 5)
                  .offset(y: 4)
              }
            }
          }
          .frame(maxWidth: .infinity)
        }
      }
      .widgetAccentable()

      Spacer(minLength: 0)

      if let next = week.nextEvent {
        VStack(alignment: .leading, spacing: 1) {
          Text(verbatim: next.name ?? String(localized: "Public holiday"))
            .font(.caption.weight(.semibold))
            .lineLimit(1)
          Text(verbatim: relative(next))
            .font(.caption2)
            .foregroundStyle(.secondary)
        }
        .accessibilityLabel(Text(verbatim: WidgetSpeech.event(next, words: .localized)))
      }
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
    .widgetURL(AppRoute.today.url)
  }

  private func ink(_ day: WidgetWeekDay) -> Color {
    if day.isToday { return BrandColor.onToday }
    return day.isHoliday ? BrandColor.holiday : .primary
  }

  private func relative(_ event: WidgetEvent) -> String {
    switch event.daysFromToday {
    case 0: return String(localized: "Today")
    case 1: return String(localized: "Tomorrow")
    default: return String(localized: "in \(event.count) days")
    }
  }
}
