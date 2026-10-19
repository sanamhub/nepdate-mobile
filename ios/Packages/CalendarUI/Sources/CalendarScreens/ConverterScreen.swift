import CalendarKit
import CalendarUI
import NepDate
import SwiftUI

/// The Converter tab (MR-04): BS to AD, AD to BS, and the difference between two BS dates.
public struct ConverterScreen: View {
  @State private var model: ConverterViewModel
  @State private var bsInput: String
  @State private var compareInput: String
  @State private var adDate: Date

  private let onSettings: (() -> Void)?

  /// Creates the screen.
  /// - Parameters:
  ///   - model: The initial converter state.
  ///   - bsInput: The initial BS field.
  ///   - compareInput: The initial comparison field.
  ///   - onSettings: Opens Settings.
  public init(
    model: ConverterViewModel,
    bsInput: String = "",
    compareInput: String = "",
    onSettings: (() -> Void)? = nil
  ) {
    _model = State(initialValue: model)
    _bsInput = State(initialValue: bsInput)
    _compareInput = State(initialValue: compareInput)
    _adDate = State(initialValue: Self.date(for: model.today))
    self.onSettings = onSettings
  }

  /// The screen's content.
  public var body: some View {
    VStack(spacing: 0) {
      ScreenHeader(title: "Converter", onSettings: onSettings)

      ScrollView {
        VStack(alignment: .leading, spacing: 12) {
          TextField("BS date", text: $bsInput)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .textFieldStyle(.roundedBorder)
            .accessibilityIdentifier("converter_bs_input")

          Button(action: convert) {
            Text("Convert")
              .frame(maxWidth: .infinity, minHeight: 44)
          }
          .buttonStyle(.borderedProminent)
          .tint(BrandColor.today)

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
            }
            .accessibilityIdentifier("converter_ad_picker")

          if let error = model.errorText {
            Text(verbatim: error)
              .font(.subheadline)
              .foregroundStyle(.red)
              .fixedSize(horizontal: false, vertical: true)
              .accessibilityIdentifier("converter_error")
          }

          if let result = model.resultBsDate {
            Divider()
            Text("Result")
              .font(.subheadline)
              .foregroundStyle(BrandColor.secondaryText)
            Text(verbatim: result)
              .font(.title2.weight(.bold))
              .fixedSize(horizontal: false, vertical: true)
              .accessibilityIdentifier("converter_result")
            if let weekday = model.resultWeekdayAndDate {
              Text(verbatim: weekday)
                .font(.body)
                .fixedSize(horizontal: false, vertical: true)
            }
            if let distance = model.daysFromTodayText {
              Text(verbatim: distance)
                .font(.subheadline)
                .foregroundStyle(BrandColor.secondaryText)
            }
          }

          Divider()
          TextField("Compare with", text: $compareInput)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .textFieldStyle(.roundedBorder)
            .accessibilityIdentifier("converter_compare_input")

          Button(action: compare) {
            Text("Compare")
              .frame(maxWidth: .infinity, minHeight: 44)
          }
          .buttonStyle(.bordered)
          .tint(BrandColor.today)

          if let error = model.comparisonErrorText {
            Text(verbatim: error)
              .font(.subheadline)
              .foregroundStyle(.red)
              .fixedSize(horizontal: false, vertical: true)
              .accessibilityIdentifier("converter_comparison_error")
          }

          if let range = model.comparisonRange {
            Text(verbatim: range)
              .font(.subheadline)
              .foregroundStyle(BrandColor.secondaryText)
              .fixedSize(horizontal: false, vertical: true)
              .accessibilityIdentifier("converter_comparison")
          }
          if let difference = model.differenceText {
            Text(verbatim: difference)
              .font(.body)
              .fixedSize(horizontal: false, vertical: true)
          }
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 24)
      }
      .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .background(Color(uiColor: .systemBackground))
  }

  private func convert() {
    model.fromBs(bsInput)
  }

  private func compare() {
    model.compareWithBs(compareInput)
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
