import CalendarKit
import CalendarUI
import SwiftUI

/// The today card above the grid (MR-22): the BS day on the Sunny mark, the weekday, the tithi,
/// the AD date, and up to three chips for the day's events and the next public holiday.
///
/// It follows Android's `TodayHero.kt`: the holiday chip comes first and takes the holiday tile,
/// a day with more than three events shows two and a "+N more" chip, and only a day with fewer
/// than three events points at the next holiday.
struct TodayCard: View {
  let cell: MonthCell
  let nextHoliday: HolidayCountdown?
  let language: AppLanguage
  let digits: Digits
  let todayLabel: String
  let publicHoliday: String
  let onOpenDay: (MonthDate) -> Void

  var body: some View {
    VStack(alignment: .leading, spacing: 12) {
      Button {
        if let date = cell.date { onOpenDay(date) }
      } label: {
        headerContent
          .contentShape(Rectangle())
      }
      .buttonStyle(.plain)
      .accessibilityElement(children: .ignore)
      .accessibilityLabel(Text(verbatim: spokenLabel))
      .accessibilityAddTraits(.isButton)
      .accessibilityIdentifier("today_card")

      FlowLayout(horizontalSpacing: 8, verticalSpacing: 8) {
        chips
      }
    }
    .padding(16)
    .frame(maxWidth: .infinity, alignment: .leading)
    .background(
      RoundedRectangle(cornerRadius: 28, style: .continuous)
        .fill(Color(uiColor: .secondarySystemBackground))
    )
  }

  private var headerContent: some View {
    HStack(alignment: .center, spacing: 16) {
      ZStack {
        SunnyShape()
          .fill(BrandColor.today)
        Text(verbatim: digits.format(cell.bsDay))
          .font(BrandType.numerals(.largeTitle).weight(.bold))
          .foregroundStyle(BrandColor.onToday)
          .minimumScaleFactor(0.6)
      }
      .frame(width: 84, height: 84)
      .accessibilityHidden(true)

      VStack(alignment: .leading, spacing: 2) {
        Text(verbatim: todayLabel)
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)
        Text(verbatim: cell.weekdayName(language))
          .font(.title3.weight(.semibold))
          .fixedSize(horizontal: false, vertical: true)
        if let tithi = cell.tithi {
          Text(verbatim: digits.applying(to: tithi.name(language)))
            .font(.body)
            .fixedSize(horizontal: false, vertical: true)
        }
        Text(verbatim: adDate)
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)
          .fixedSize(horizontal: false, vertical: true)
      }
      .frame(maxWidth: .infinity, alignment: .leading)
    }
  }

  @ViewBuilder
  private var chips: some View {
    let names = cell.events.map { digits.applying(to: $0.name(language)) }
    let shown = names.count > 3 ? Array(names.prefix(2)) : names
    ForEach(shown.indices, id: \.self) { index in
      if cell.isHoliday && index == 0 {
        chip(
          text: "\(publicHoliday): \(shown[index])",
          container: BrandColor.holidayContainer,
          ink: BrandColor.onHolidayContainer,
          mark: .holiday
        ) {
          open(cell)
        }
      } else {
        chip(
          text: shown[index],
          container: Color.primary.opacity(0.1),
          ink: .primary,
          mark: .event
        ) {
          open(cell)
        }
      }
    }

    if shown.count < names.count {
      chip(
        text: "+\(digits.format(names.count - shown.count)) \(AppText.string("more", language))",
        container: Color.primary.opacity(0.1),
        ink: .primary,
        mark: .event
      ) {
        open(cell)
      }
    } else if names.count < 3, let nextHoliday {
      chip(
        text: nextHolidayText(nextHoliday),
        container: Color.primary.opacity(0.1),
        ink: .primary,
        mark: .nextHoliday
      ) {
        onOpenDay(nextHoliday.date)
      }
    }
  }

  private enum ChipMark {
    case holiday
    case event
    case nextHoliday
  }

  private func chip(
    text: String,
    container: Color,
    ink: Color,
    mark: ChipMark,
    action: @escaping () -> Void
  ) -> some View {
    Button(action: action) {
      HStack(spacing: 8) {
        markView(mark, ink: ink)
        Text(verbatim: text)
          .font(.subheadline.weight(.medium))
          .foregroundStyle(ink)
          .lineLimit(1)
          .minimumScaleFactor(0.7)
      }
      .frame(minHeight: 40)
      .padding(.horizontal, 12)
      .background(container, in: Capsule())
      .contentShape(Capsule())
    }
    .buttonStyle(.plain)
  }

  @ViewBuilder
  private func markView(_ mark: ChipMark, ink: Color) -> some View {
    switch mark {
    case .holiday:
      SoftBurstShape()
        .fill(BrandColor.holiday)
        .frame(width: 12, height: 12)
    case .event:
      DiamondShape()
        .fill(ink)
        .frame(width: 7, height: 8)
    case .nextHoliday:
      SoftBurstShape()
        .fill(BrandColor.holiday)
        .frame(width: 14, height: 14)
    }
  }

  private var adDate: String {
    "\(digits.format(cell.adDay)) \(MonthNames.ad(cell.adMonth, language)) "
      + "\(digits.format(cell.adYear))"
  }

  private var spokenLabel: String {
    var parts = [
      "\(MonthNames.bs(cell.date?.month ?? 1, language)) \(digits.format(cell.bsDay))",
      cell.weekdayName(language),
      adDate,
    ]
    if cell.isHoliday { parts.append(publicHoliday) }
    if let tithi = cell.tithi {
      parts.append(digits.applying(to: tithi.name(language)))
    }
    return parts.joined(separator: ", ")
  }

  private func nextHolidayText(_ holiday: HolidayCountdown) -> String {
    let name = holiday.name.map { digits.applying(to: $0.name(language)) } ?? publicHoliday
    let relative: String
    switch holiday.days {
    case 0: relative = AppText.string("Today", language)
    case 1: relative = AppText.string("Tomorrow", language)
    default:
      relative = String(
        format: AppText.string("in %@ days", language),
        digits.format(holiday.days)
      )
    }
    return "\(name) \(relative)"
  }

  private func open(_ cell: MonthCell) {
    if let date = cell.date { onOpenDay(date) }
  }
}
