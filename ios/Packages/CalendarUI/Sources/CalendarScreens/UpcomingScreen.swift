import CalendarKit
import CalendarUI
import SwiftUI

/// The Upcoming tab (MR-03): the next 20 public holidays and events, grouped by BS month.
public struct UpcomingScreen: View {
  private let model: UpcomingViewModel
  private let language: AppLanguage
  private let digits: Digits
  private let onSettings: (() -> Void)?
  @State private var selected: MonthDate?

  /// Creates the screen.
  /// - Parameters:
  ///   - model: The entries to show.
  ///   - language: The language of the names.
  ///   - digits: The resolved Digits setting.
  ///   - onSettings: Opens Settings.
  public init(
    model: UpcomingViewModel,
    language: AppLanguage,
    digits: Digits,
    onSettings: (() -> Void)? = nil
  ) {
    self.model = model
    self.language = language
    self.digits = digits
    self.onSettings = onSettings
  }

  /// The screen's content.
  public var body: some View {
    VStack(spacing: 0) {
      ScreenHeader(title: "Upcoming", onSettings: onSettings)

      if model.entries.isEmpty {
        Text("No upcoming holidays or events yet")
          .font(.body)
          .foregroundStyle(BrandColor.secondaryText)
          .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
          .padding(16)
      } else {
        List {
          ForEach(model.groups) { group in
            Section {
              ForEach(group.entries) { entry in
                Button {
                  selected = entry.date
                } label: {
                  UpcomingRow(entry: entry)
                    .accessibilityHidden(true)
                }
                .buttonStyle(.plain)
              }
            } header: {
              Text(verbatim: group.title)
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(BrandColor.secondaryText)
                .textCase(nil)
            }
          }

          if let footer = model.dataEndsText {
            Section {
              Text(verbatim: footer)
                .font(.footnote)
                .foregroundStyle(BrandColor.secondaryText)
                .fixedSize(horizontal: false, vertical: true)
            }
          }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
      }
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .background(Color(uiColor: .systemBackground))
    .sheet(isPresented: showsDetail) {
      if let selected,
        let detail = DayDetailViewModel(
          date: selected,
          language: language,
          digits: digits
        )
      {
        DayDetailSheet(model: detail)
      }
    }
  }

  private var showsDetail: Binding<Bool> {
    Binding(
      get: { selected != nil },
      set: { isPresented in
        if !isPresented { selected = nil }
      }
    )
  }
}

private struct UpcomingRow: View {
  let entry: UpcomingEntry

  var body: some View {
    VStack(alignment: .leading, spacing: 3) {
      Text(verbatim: entry.name)
        .font(.headline)
        .foregroundStyle(entry.isHoliday ? BrandColor.holiday : Color.primary)
        .fixedSize(horizontal: false, vertical: true)

      if entry.isHoliday {
        Rectangle()
          .fill(BrandColor.holiday)
          .frame(width: 24, height: 2)
          .accessibilityHidden(true)
      }

      Text(verbatim: entry.dateLine)
        .font(.subheadline)
        .foregroundStyle(BrandColor.secondaryText)
        .fixedSize(horizontal: false, vertical: true)

      Text(verbatim: entry.relativeText)
        .font(.subheadline.weight(.semibold))
        .foregroundStyle(BrandColor.secondaryText)
    }
    .frame(maxWidth: .infinity, alignment: .leading)
    .padding(.vertical, 6)
    .contentShape(Rectangle())
  }
}
