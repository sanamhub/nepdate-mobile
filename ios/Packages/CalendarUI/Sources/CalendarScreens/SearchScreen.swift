import CalendarKit
import CalendarUI
import Foundation
import SwiftUI

/// The Search tab (MR-13): recent searches and the next three festivals on landing, festival names
/// in English and Nepali across the coverage, and a typed date that reads as BS, AD or both.
public struct SearchScreen: View {
  @State private var model: SearchViewModel
  @State private var query = ""
  private let today: MonthDate
  private let language: AppLanguage
  private let digits: Digits
  private let recentStore: RecentSearchStore
  private let onOpenDay: (MonthDate) -> Void

  /// Creates the screen.
  /// - Parameters:
  ///   - today: The first day the landing list starts from.
  ///   - language: The language of the names.
  ///   - digits: The resolved Digits setting.
  ///   - recentStore: The store for recent searches.
  ///   - initialQuery: The query to start with; empty by default.
  ///   - onOpenDay: Opens a day in the Patro tab.
  public init(
    today: MonthDate,
    language: AppLanguage,
    digits: Digits,
    recentStore: RecentSearchStore = RecentSearchStore(),
    initialQuery: String = "",
    onOpenDay: @escaping (MonthDate) -> Void
  ) {
    self.today = today
    self.language = language
    self.digits = digits
    self.recentStore = recentStore
    self.onOpenDay = onOpenDay
    _query = State(initialValue: initialQuery)
    _model = State(
      initialValue: SearchViewModel(
        today: today,
        language: language,
        digits: digits,
        query: initialQuery,
        recentStore: recentStore
      )
    )
  }

  /// The screen's content.
  public var body: some View {
    List {
      if model.isLanding {
        landing
      } else {
        results
      }
    }
    .listStyle(.plain)
    .overlay {
      if model.showsEmptyState {
        ContentUnavailableView.search(text: model.query)
      }
    }
    .navigationTitle("Search")
    .searchable(text: $query, prompt: Text("Search festivals and dates"))
    .onSubmit(of: .search) {
      if let preferred = model.jump.preferred {
        onOpenDay(preferred.date)
      }
    }
    .onChange(of: query) { _, _ in rebuild() }
  }

  @ViewBuilder
  private var landing: some View {
    if !model.recent.isEmpty {
      Section {
        ForEach(model.recent, id: \.self) { value in
          Button {
            query = value
          } label: {
            HStack(spacing: 8) {
              Image(systemName: "clock.arrow.circlepath")
                .foregroundStyle(BrandColor.secondaryText)
              Text(verbatim: value)
                .foregroundStyle(.primary)
              Spacer(minLength: 0)
            }
            .frame(minHeight: 44)
            .contentShape(Rectangle())
          }
          .buttonStyle(.plain)
          .swipeActions(edge: .trailing) {
            Button(role: .destructive) {
              recentStore.remove(value)
              rebuild()
            } label: {
              Text("Delete")
            }
          }
        }
      } header: {
        HStack {
          Text("Recent searches")
          Spacer(minLength: 4)
          Button {
            recentStore.clear()
            rebuild()
          } label: {
            Text("Clear")
          }
          .buttonStyle(.plain)
          .foregroundStyle(BrandColor.accent)
        }
        .textCase(nil)
      }
    }

    Section {
      ForEach(model.nextFestivals) { day in
        festivalRow(day)
      }
    } header: {
      Text("Next festivals")
        .textCase(nil)
    }
  }

  @ViewBuilder
  private var results: some View {
    if !model.jump.readings.isEmpty {
      Section {
        ForEach(model.jump.readings) { reading in
          Button {
            onOpenDay(reading.date)
          } label: {
            HStack(spacing: 8) {
              Image(systemName: "calendar")
                .foregroundStyle(BrandColor.accent)
              Text(verbatim: jumpTitle(reading))
                .foregroundStyle(.primary)
              Spacer(minLength: 0)
              Image(systemName: "arrow.right")
                .font(.caption.weight(.semibold))
                .foregroundStyle(BrandColor.secondaryText)
            }
            .frame(minHeight: 44)
            .contentShape(Rectangle())
          }
          .buttonStyle(.plain)
          .accessibilityIdentifier(
            reading.kind == .bs ? "jump_bs" : "jump_ad"
          )
        }
      }
    } else if !model.query.isEmpty {
      Section {
        Text("Type a date like 2083-06-20 or 2026-10-11")
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)
          .fixedSize(horizontal: false, vertical: true)
      }
    }

    ForEach(model.groups) { group in
      Section {
        ForEach(group.days) { day in
          festivalRow(day)
        }
      } header: {
        Text(verbatim: group.title)
          .textCase(nil)
      }
    }
  }

  private func festivalRow(_ day: FestivalDay) -> some View {
    Button {
      recentStore.add(model.query)
      onOpenDay(day.date)
    } label: {
      VStack(alignment: .leading, spacing: 3) {
        Text(verbatim: day.title(language, publicHoliday: publicHoliday))
          .font(.headline)
          .foregroundStyle(day.isHoliday ? BrandColor.holiday : .primary)
          .fixedSize(horizontal: false, vertical: true)
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

  private func rebuild() {
    model = SearchViewModel(
      today: today,
      language: language,
      digits: digits,
      query: query,
      recentStore: recentStore
    )
  }

  private var publicHoliday: String {
    AppText.string("public holiday", language)
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

  private func jumpTitle(_ reading: JumpReading) -> String {
    switch reading.kind {
    case .bs:
      let prefix = AppText.string("Go to BS", language)
      return "\(prefix) \(MonthNames.bs(reading.month, language)) "
        + "\(digits.format(reading.day)), \(digits.format(reading.year))"
    case .ad:
      let prefix = AppText.string("Go to AD", language)
      return "\(prefix) \(digits.format(reading.day)) "
        + "\(MonthNames.ad(reading.month, language)) \(digits.format(reading.year))"
    }
  }
}
