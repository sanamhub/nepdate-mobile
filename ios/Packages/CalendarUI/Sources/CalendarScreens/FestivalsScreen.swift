import CalendarKit
import CalendarUI
import Foundation
import SwiftUI

/// The Festivals tab (MR-03, MR-13): the next public holiday, the days from today to the end of
/// coverage grouped by BS month, a name filter and the All / Public holidays filter.
public struct FestivalsScreen: View {
  @State private var model: FestivalsViewModel
  @State private var query = ""
  @State private var holidaysOnly = false
  private let today: MonthDate
  private let language: AppLanguage
  private let digits: Digits
  private let onOpenDay: (MonthDate) -> Void
  private let onOpenMonth: (MonthKey) -> Void

  /// Creates the screen.
  /// - Parameters:
  ///   - today: The first day the list covers.
  ///   - language: The language of the names.
  ///   - digits: The resolved Digits setting.
  ///   - onOpenDay: Opens a day.
  ///   - onOpenMonth: Opens a month in the Patro tab.
  public init(
    today: MonthDate,
    language: AppLanguage,
    digits: Digits,
    onOpenDay: @escaping (MonthDate) -> Void,
    onOpenMonth: @escaping (MonthKey) -> Void
  ) {
    self.today = today
    self.language = language
    self.digits = digits
    self.onOpenDay = onOpenDay
    self.onOpenMonth = onOpenMonth
    _model = State(
      initialValue: FestivalsViewModel(today: today, language: language, digits: digits)
    )
  }

  /// The screen's content.
  public var body: some View {
    VStack(spacing: 0) {
      Picker("Filter", selection: $holidaysOnly) {
        Text("All").tag(false)
        Text("Public holidays").tag(true)
      }
      .pickerStyle(.segmented)
      .padding(.horizontal, 16)
      .padding(.vertical, 8)
      .accessibilityIdentifier("festivals_filter")

      List {
        if query.isEmpty && !holidaysOnly, let next = model.nextHoliday {
          Section {
            nextHolidayCard(next)
          }
        }

        ForEach(model.groups) { group in
          Section {
            ForEach(group.days) { day in
              row(day)
            }
          } header: {
            HStack(spacing: 8) {
              Text(verbatim: model.holidayCountText(group.holidayCount))
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(BrandColor.secondaryText)
                .textCase(nil)
              Spacer(minLength: 4)
              Button {
                if let key = MonthKey(year: group.year, month: group.month) {
                  onOpenMonth(key)
                }
              } label: {
                Text("Open in Patro")
                  .font(.subheadline.weight(.semibold))
              }
              .buttonStyle(.plain)
              .foregroundStyle(BrandColor.accent)
            }
          }
        }

        Section {
          Text(verbatim: endsText)
            .font(.footnote)
            .foregroundStyle(BrandColor.secondaryText)
            .fixedSize(horizontal: false, vertical: true)
        }
      }
      .listStyle(.plain)
      .overlay {
        if model.isEmpty {
          ContentUnavailableView.search(text: query)
        }
      }
    }
    .navigationTitle("Festivals and holidays")
    .searchable(text: $query, prompt: Text("Search festivals"))
    .onChange(of: query) { _, _ in rebuild() }
    .onChange(of: holidaysOnly) { _, _ in rebuild() }
  }

  private func row(_ day: FestivalDay) -> some View {
    Button {
      onOpenDay(day.date)
    } label: {
      VStack(alignment: .leading, spacing: 3) {
        Text(verbatim: day.title(language, publicHoliday: publicHoliday))
          .font(.headline)
          .foregroundStyle(day.isHoliday ? BrandColor.holiday : .primary)
          .fixedSize(horizontal: false, vertical: true)

        if day.isHoliday {
          Rectangle()
            .fill(BrandColor.holiday)
            .frame(width: 24, height: 2)
            .accessibilityHidden(true)
        }

        Text(verbatim: dateLine(day))
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)
      }
      .frame(maxWidth: .infinity, alignment: .leading)
      .padding(.vertical, 4)
      .contentShape(Rectangle())
      .accessibilityHidden(true)
    }
    .buttonStyle(.plain)
    .accessibilityElement(children: .ignore)
    .accessibilityLabel(Text(verbatim: spoken(day)))
    .accessibilityAddTraits(.isButton)
  }

  private func nextHolidayCard(_ holiday: HolidayCountdown) -> some View {
    Button {
      onOpenDay(holiday.date)
    } label: {
      HStack(spacing: 12) {
        ZStack {
          SoftBurstShape()
            .fill(BrandColor.holidayContainer)
          Text(verbatim: model.digits.format(holiday.days))
            .font(.headline.weight(.bold))
            .foregroundStyle(BrandColor.onHolidayContainer)
        }
        .frame(width: 52, height: 52)
        .accessibilityHidden(true)

        VStack(alignment: .leading, spacing: 2) {
          Text("Next public holiday")
            .font(.caption)
            .foregroundStyle(BrandColor.secondaryText)
          Text(
            verbatim: holiday.name.map { digits.applying(to: $0.name(language)) }
              ?? publicHoliday
          )
          .font(.headline)
          .fixedSize(horizontal: false, vertical: true)
          Text(verbatim: relativeText(holiday.days))
            .font(.subheadline)
            .foregroundStyle(BrandColor.secondaryText)
        }
        .frame(maxWidth: .infinity, alignment: .leading)

        Image(systemName: "chevron.right")
          .font(.caption.weight(.semibold))
          .foregroundStyle(BrandColor.secondaryText)
          .accessibilityHidden(true)
      }
      .padding(.vertical, 4)
      .contentShape(Rectangle())
      .accessibilityElement(children: .ignore)
      .accessibilityLabel(Text(verbatim: nextHolidaySpeech(holiday)))
      .accessibilityAddTraits(.isButton)
    }
    .buttonStyle(.plain)
  }

  private func rebuild() {
    model = FestivalsViewModel(
      today: today,
      language: language,
      digits: digits,
      query: query,
      holidaysOnly: holidaysOnly
    )
  }

  private var publicHoliday: String {
    AppText.string("public holiday", language)
  }

  private var endsText: String {
    String(
      format: AppText.string("Holiday data ends on %@", language),
      model.dataEndsOn
    )
  }

  private func dateLine(_ day: FestivalDay) -> String {
    "\(MonthNames.bs(day.date.month, language)) \(digits.format(day.date.day)), "
      + "\(digits.format(day.date.year))"
  }

  private func spoken(_ day: FestivalDay) -> String {
    var parts = [dateLine(day)]
    if day.isHoliday { parts.append(publicHoliday) }
    parts.append(contentsOf: day.names.map { digits.applying(to: $0.name(language)) })
    return parts.joined(separator: ", ")
  }

  private func relativeText(_ days: Int) -> String {
    switch days {
    case 0: return AppText.string("Today", language)
    case 1: return AppText.string("Tomorrow", language)
    default:
      return String(
        format: AppText.string("in %@ days", language),
        digits.format(days)
      )
    }
  }

  private func nextHolidaySpeech(_ holiday: HolidayCountdown) -> String {
    let name = holiday.name.map { digits.applying(to: $0.name(language)) } ?? publicHoliday
    return "\(name), \(relativeText(holiday.days))"
  }
}
