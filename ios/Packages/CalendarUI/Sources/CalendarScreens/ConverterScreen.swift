import CalendarKit
import CalendarUI
import Foundation
import NepDate
import SwiftUI
import UIKit

/// The Converter tab (MR-04, MR-21): BS to AD, AD to BS, wheel pickers and a lenient text field,
/// a result card with Copy, Share and Show in Patro, and the difference between two dates.
public struct ConverterScreen: View {
  @State private var model: ConverterViewModel
  @State private var direction = Direction.bsToAd
  @State private var year: Int
  @State private var month: Int
  @State private var day: Int
  @State private var bsInput: String
  @State private var adDate: Date
  @State private var from: MonthDate
  @State private var to: MonthDate
  @State private var editing: ComparisonField?
  @State private var bsError: String?
  @State private var copyCount = 0
  private let onOpenDay: ((MonthDate) -> Void)?

  /// Creates the screen.
  /// - Parameters:
  ///   - model: The initial converter state.
  ///   - bsInput: The initial BS field.
  ///   - compareInput: The initial comparison field.
  ///   - onOpenDay: Opens a day in the Patro tab.
  public init(
    model: ConverterViewModel,
    bsInput: String = "",
    compareInput: String = "",
    onOpenDay: ((MonthDate) -> Void)? = nil
  ) {
    _model = State(initialValue: model)
    _year = State(initialValue: model.today.year)
    _month = State(initialValue: model.today.month)
    _day = State(initialValue: model.today.day)
    _bsInput = State(initialValue: bsInput)
    _adDate = State(initialValue: Self.date(for: model.today))
    _from = State(initialValue: model.comparison?.from ?? model.today)
    _to = State(initialValue: model.comparison?.to ?? model.today)
    self.onOpenDay = onOpenDay
  }

  /// The screen's content.
  public var body: some View {
    ScrollView {
      VStack(alignment: .leading, spacing: 16) {
        Picker("Direction", selection: $direction) {
          Text("BS to AD").tag(Direction.bsToAd)
          Text("AD to BS").tag(Direction.adToBs)
        }
        .pickerStyle(.segmented)
        .accessibilityIdentifier("converter_direction")
        .onChange(of: direction) { _, _ in convertCurrentInput() }

        if direction == .bsToAd {
          bsInputs
        } else {
          adInput
        }

        resultCard

        Divider()
        comparison
      }
      .padding(.horizontal, 16)
      .padding(.bottom, 24)
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .background(Color(uiColor: .systemBackground))
    .navigationTitle("Converter")
    .toolbar {
      if showsReset {
        ToolbarItem(placement: .topBarLeading) {
          Button("Reset") {
            reset()
          }
          .accessibilityIdentifier("converter_reset")
        }
      }
    }
    .sheet(item: $editing) { field in
      BSDatePickerSheet(
        initial: field == .from ? from : to,
        language: model.language,
        digits: model.digits
      ) { picked in
        if field == .from {
          from = picked
        } else {
          to = picked
        }
        model.compare(from: from, to: to)
      }
    }
    .hapticFeedback(.success, trigger: copyCount)
  }

  private var bsInputs: some View {
    VStack(alignment: .leading, spacing: 10) {
      HStack(spacing: 0) {
        Picker("Year", selection: $year) {
          ForEach(1901...2199, id: \.self) { value in
            Text(model.digits.format(value)).tag(value)
          }
        }
        .pickerStyle(.wheel)
        .frame(maxWidth: .infinity)
        .accessibilityLabel(Text("Year"))

        Picker("Month", selection: $month) {
          ForEach(1...12, id: \.self) { value in
            Text(MonthNames.bs(value, model.language)).tag(value)
          }
        }
        .pickerStyle(.wheel)
        .frame(maxWidth: .infinity)
        .accessibilityLabel(Text("Month"))

        Picker("Day", selection: $day) {
          ForEach(1...monthLength, id: \.self) { value in
            Text(model.digits.format(value)).tag(value)
          }
        }
        .pickerStyle(.wheel)
        .frame(maxWidth: .infinity)
        .accessibilityLabel(Text("Day"))
      }
      .frame(height: 120)
      .hapticFeedback(.selection, trigger: year * 10_000 + month * 100 + day)
      .onChange(of: year) { _, _ in clampAndConvert() }
      .onChange(of: month) { _, _ in clampAndConvert() }
      .onChange(of: day) { _, _ in convertWheels() }

      HStack(spacing: 8) {
        TextField("BS date", text: $bsInput)
          .textInputAutocapitalization(.never)
          .autocorrectionDisabled()
          .textFieldStyle(.roundedBorder)
          .accessibilityIdentifier("converter_bs_input")
          .onSubmit(convertText)

        Button("Convert", action: convertText)
          .buttonStyle(.bordered)
          .tint(BrandColor.accent)
          .accessibilityIdentifier("converter_convert")
      }

      if let bsError {
        Text(verbatim: bsError)
          .font(.subheadline)
          .foregroundStyle(.red)
          .fixedSize(horizontal: false, vertical: true)
          .accessibilityIdentifier("converter_error")
      }
    }
  }

  private var adInput: some View {
    DatePicker("Choose an AD date", selection: $adDate, displayedComponents: .date)
      .datePickerStyle(.compact)
      .onChange(of: adDate) { _, value in
        let parts = Calendar(identifier: .gregorian).dateComponents(
          [.year, .month, .day],
          from: value
        )
        if let year = parts.year, let month = parts.month, let day = parts.day {
          model.fromAd(year: year, month: month, day: day)
        }
        syncWheelsFromResult()
      }
      .accessibilityIdentifier("converter_ad_picker")
  }

  @ViewBuilder
  private var resultCard: some View {
    if let result = model.result {
      VStack(alignment: .leading, spacing: 6) {
        Text("Result")
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)

        if direction == .bsToAd {
          Text(verbatim: adText(result))
            .font(.title2.weight(.bold))
            .fixedSize(horizontal: false, vertical: true)
            .accessibilityIdentifier("converter_result")
          Text(verbatim: model.resultBsDate ?? "")
            .font(.body)
            .fixedSize(horizontal: false, vertical: true)
        } else {
          Text(verbatim: model.resultBsDate ?? "")
            .font(.title2.weight(.bold))
            .fixedSize(horizontal: false, vertical: true)
            .accessibilityIdentifier("converter_result")
          Text(verbatim: adText(result))
            .font(.body)
            .fixedSize(horizontal: false, vertical: true)
        }

        if let weekday = model.resultWeekdayAndDate {
          Text(verbatim: weekday)
            .font(.subheadline)
            .foregroundStyle(BrandColor.secondaryText)
        }
        if let distance = model.daysFromTodayText {
          Text(verbatim: distance)
            .font(.subheadline)
            .foregroundStyle(BrandColor.secondaryText)
        }

        HStack(spacing: 10) {
          Button {
            copy()
          } label: {
            Label("Copy date", systemImage: "doc.on.doc")
              .frame(maxWidth: .infinity, minHeight: 44)
          }
          .buttonStyle(.bordered)
          .tint(BrandColor.accent)

          ShareLink(item: shareText(result)) {
            Label("Share", systemImage: "square.and.arrow.up")
              .frame(maxWidth: .infinity, minHeight: 44)
          }
          .buttonStyle(.bordered)
          .tint(BrandColor.accent)

          if let onOpenDay {
            Button {
              onOpenDay(result.bs)
            } label: {
              Label("Show in Patro", systemImage: "calendar")
                .frame(maxWidth: .infinity, minHeight: 44)
            }
            .buttonStyle(.bordered)
            .tint(BrandColor.accent)
          }
        }
        .padding(.top, 4)
      }
      .padding(14)
      .frame(maxWidth: .infinity, alignment: .leading)
      .background(
        RoundedRectangle(cornerRadius: 20, style: .continuous)
          .fill(Color(uiColor: .secondarySystemBackground))
      )
    } else if let error = model.errorText {
      Text(verbatim: error)
        .font(.subheadline)
        .foregroundStyle(BrandColor.secondaryText)
        .fixedSize(horizontal: false, vertical: true)
    }
  }

  private var comparison: some View {
    VStack(alignment: .leading, spacing: 10) {
      Text("Between two dates")
        .font(.headline)
        .accessibilityAddTraits(.isHeader)

      HStack(spacing: 8) {
        dateButton("From", value: from, field: .from)
        Button {
          let old = from
          from = to
          to = old
          model.compare(from: from, to: to)
        } label: {
          Image(systemName: "arrow.left.arrow.right")
            .frame(width: 44, height: 44)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text("Swap"))
        dateButton("To", value: to, field: .to)
      }

      if let comparison = model.comparison {
        Text(verbatim: differenceText(comparison))
          .font(.title3.weight(.semibold))
          .fixedSize(horizontal: false, vertical: true)
          .accessibilityIdentifier("converter_difference")
        Text(verbatim: totalDaysText(comparison))
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)
      } else if let error = model.comparisonErrorText {
        Text(verbatim: error)
          .font(.subheadline)
          .foregroundStyle(.red)
          .fixedSize(horizontal: false, vertical: true)
      }
    }
    .padding(.top, 4)
  }

  private func dateButton(
    _ title: LocalizedStringKey,
    value: MonthDate,
    field: ComparisonField
  ) -> some View {
    Button {
      editing = field
    } label: {
      VStack(alignment: .leading, spacing: 2) {
        Text(title)
          .font(.caption)
          .foregroundStyle(BrandColor.secondaryText)
        Text(verbatim: bsLine(value))
          .font(.subheadline.weight(.semibold))
          .foregroundStyle(.primary)
          .fixedSize(horizontal: false, vertical: true)
      }
      .frame(maxWidth: .infinity, minHeight: 44, alignment: .leading)
      .padding(.horizontal, 10)
      .background(
        RoundedRectangle(cornerRadius: 12, style: .continuous)
          .fill(Color(uiColor: .tertiarySystemFill))
      )
      .contentShape(Rectangle())
    }
    .buttonStyle(.plain)
  }

  private var monthLength: Int {
    guard let date = try? NepaliDate(year: year, month: month, day: 1) else { return 30 }
    return date.monthLength
  }

  private var showsReset: Bool {
    direction != .bsToAd || bsInput.isEmpty == false || model.result != nil
      || model.comparison != nil || from != model.today || to != model.today
      || year != model.today.year || month != model.today.month || day != model.today.day
  }

  private func clampAndConvert() {
    if day > monthLength { day = monthLength }
    convertWheels()
  }

  private func convertWheels() {
    bsError = nil
    model.fromBs(year: year, month: month, day: day)
    bsInput = "\(year)-\(month)-\(day)"
  }

  private func convertText() {
    model.fromBs(bsInput)
    if model.error != nil || model.result == nil {
      bsError = AppText.string("Not a date", model.language)
      return
    }
    bsError = nil
    syncWheelsFromResult()
  }

  private func convertCurrentInput() {
    switch direction {
    case .bsToAd: convertWheels()
    case .adToBs:
      let parts = Calendar(identifier: .gregorian).dateComponents(
        [.year, .month, .day],
        from: adDate
      )
      if let year = parts.year, let month = parts.month, let day = parts.day {
        model.fromAd(year: year, month: month, day: day)
      }
    }
  }

  private func syncWheelsFromResult() {
    guard let date = model.result?.bs else { return }
    year = date.year
    month = date.month
    day = date.day
  }

  private func copy() {
    DayClipboard.write(model.resultBsDate ?? "")
    CopyConfirmation.announce(AppText.string("Date copied", model.language))
    copyCount += 1
  }

  private func adText(_ result: Conversion) -> String {
    let day = model.digits.format(result.adDay)
    let month = MonthNames.ad(result.adMonth, model.language)
    let year = model.digits.format(result.adYear)
    return "\(day) \(month) \(year)"
  }

  private func shareText(_ result: Conversion) -> String {
    "\(model.resultBsDate ?? "")\n\(adText(result))"
  }

  private func differenceText(_ comparison: Comparison) -> String {
    let years = part(comparison.years, "year", "years")
    let months = part(comparison.months, "month", "months")
    let days = part(comparison.days, "day", "days")
    return "\(years), \(months), \(days)"
  }

  private func totalDaysText(_ comparison: Comparison) -> String {
    let days = part(abs(comparison.totalDays), "day", "days")
    return "\(AppText.string("Total", model.language)) \(days)"
  }

  private func part(_ value: Int, _ one: String, _ many: String) -> String {
    let key = String.LocalizationValue(stringLiteral: value == 1 ? one : many)
    return "\(model.digits.format(value)) \(AppText.string(key, model.language))"
  }

  private func bsLine(_ date: MonthDate) -> String {
    "\(MonthNames.bs(date.month, model.language)) \(model.digits.format(date.day)), "
      + "\(model.digits.format(date.year))"
  }

  private func reset() {
    model.reset()
    direction = .bsToAd
    year = model.today.year
    month = model.today.month
    day = model.today.day
    bsInput = ""
    adDate = Self.date(for: model.today)
    from = model.today
    to = model.today
    bsError = nil
  }

  private static func date(for today: MonthDate) -> Date {
    guard
      let date = try? NepaliDate(
        year: today.year,
        month: today.month,
        day: today.day
      )
    else { return Date() }
    let gregorian = date.gregorian
    return DayEvent.startOfDay(
      year: gregorian.year,
      month: gregorian.month,
      day: gregorian.day,
      timeZone: .current
    ) ?? Date()
  }
}

private enum Direction: Hashable {
  case bsToAd
  case adToBs
}

private enum ComparisonField: String, Identifiable {
  case from
  case to

  var id: String { rawValue }
}

/// The wheel picker for one end of the comparison.
private struct BSDatePickerSheet: View {
  @State private var year: Int
  @State private var month: Int
  @State private var day: Int
  private let language: AppLanguage
  private let digits: Digits
  private let onPick: (MonthDate) -> Void
  @Environment(\.dismiss) private var dismiss

  init(
    initial: MonthDate,
    language: AppLanguage,
    digits: Digits,
    onPick: @escaping (MonthDate) -> Void
  ) {
    _year = State(initialValue: initial.year)
    _month = State(initialValue: initial.month)
    _day = State(initialValue: initial.day)
    self.language = language
    self.digits = digits
    self.onPick = onPick
  }

  var body: some View {
    NavigationStack {
      HStack(spacing: 0) {
        Picker("Year", selection: $year) {
          ForEach(1901...2199, id: \.self) { value in
            Text(digits.format(value)).tag(value)
          }
        }
        .pickerStyle(.wheel)
        .frame(maxWidth: .infinity)

        Picker("Month", selection: $month) {
          ForEach(1...12, id: \.self) { value in
            Text(MonthNames.bs(value, language)).tag(value)
          }
        }
        .pickerStyle(.wheel)
        .frame(maxWidth: .infinity)

        Picker("Day", selection: $day) {
          ForEach(1...monthLength, id: \.self) { value in
            Text(digits.format(value)).tag(value)
          }
        }
        .pickerStyle(.wheel)
        .frame(maxWidth: .infinity)
      }
      .navigationTitle("Choose a date")
      .navigationBarTitleDisplayMode(.inline)
      .toolbar {
        ToolbarItem(placement: .cancellationAction) {
          Button("Cancel") { dismiss() }
        }
        ToolbarItem(placement: .confirmationAction) {
          Button("Go") {
            onPick(MonthDate(year: year, month: month, day: day))
            dismiss()
          }
        }
      }
    }
    .presentationDetents([.medium, .large])
    .presentationDragIndicator(.visible)
    .onChange(of: year) { _, _ in clampDay() }
    .onChange(of: month) { _, _ in clampDay() }
  }

  private var monthLength: Int {
    (try? NepaliDate(year: year, month: month, day: 1))?.monthLength ?? 30
  }

  private func clampDay() {
    if day > monthLength { day = monthLength }
  }
}
