import CalendarKit
import CalendarUI
import Foundation
import SwiftUI
import UIKit

/// The Patro tab (MR-01, MR-14, MR-22, MR-23): a pager of month pages with the today card, the
/// grid and the month's holidays and events, a title menu for today and the year view, and the
/// previous, today and next buttons.
public struct MonthScreen: View {
  @State private var model: MonthViewModel
  @State private var scrolledMonth: MonthKey?
  @State private var window: [MonthKey]
  @State private var detail: MonthDate?
  @State private var isRecentring = false
  @State private var showsYear = false
  @Namespace private var yearZoom
  @Namespace private var detailZoom
  @Environment(\.accessibilityReduceMotion) private var reduceMotion
  private let settings: Settings
  private let today: MonthDate
  private let route: AppRoute?
  private let monthCommand: MonthCommand?
  private let onRouteHandled: () -> Void
  private let onMonthCommandHandled: () -> Void

  /// Creates the screen.
  /// - Parameters:
  ///   - model: The month to open on and the settings to draw it with.
  ///   - settings: The settings the screen follows while it is open.
  ///   - today: Today in the zone the settings choose.
  ///   - route: A place to open, from a widget tap; `nil` when there is none.
  ///   - monthCommand: A month change asked for by a keyboard command; `nil` when there is none.
  ///   - onRouteHandled: Called once the screen has opened `route`, so the caller can clear it.
  ///   - onMonthCommandHandled: Called once the screen has applied `monthCommand`.
  public init(
    model: MonthViewModel,
    settings: Settings,
    today: MonthDate,
    route: AppRoute? = nil,
    monthCommand: MonthCommand? = nil,
    onRouteHandled: @escaping () -> Void = {},
    onMonthCommandHandled: @escaping () -> Void = {}
  ) {
    _model = State(initialValue: model)
    _scrolledMonth = State(initialValue: model.month)
    _window = State(initialValue: Self.window(around: model.month))
    self.settings = settings
    self.today = today
    self.route = route
    self.monthCommand = monthCommand
    self.onRouteHandled = onRouteHandled
    self.onMonthCommandHandled = onMonthCommandHandled
  }

  /// The screen's content.
  public var body: some View {
    VStack(spacing: 0) {
      monthSpan
      pager
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .background(Color(uiColor: .systemBackground))
    .navigationTitle(model.monthTitle)
    .navigationBarTitleDisplayMode(.large)
    .toolbarTitleMenu {
      Button("Go to today") {
        go(to: todayMonth)
      }
      Button("Year view") {
        showsYear = true
      }
    }
    .toolbar {
      ToolbarItem(placement: .topBarLeading) {
        Button {
          showsYear = true
        } label: {
          Label("Year view", systemImage: "calendar")
        }
        .accessibilityLabel(Text("Year view"))
      }

      ToolbarItemGroup(placement: .topBarTrailing) {
        Button(action: { go(to: previousMonth) }) {
          Image(systemName: "chevron.left")
        }
        .disabled(!model.canGoPrevious)
        .accessibilityLabel(Text("Previous month"))

        if model.showsTodayButton {
          Button(action: { go(to: todayMonth) }) {
            Text("Today")
          }
          .accessibilityLabel(Text("Today"))
        }

        Button(action: { go(to: nextMonth) }) {
          Image(systemName: "chevron.right")
        }
        .disabled(!model.canGoNext)
        .accessibilityLabel(Text("Next month"))
      }
    }
    .navigationDestination(isPresented: $showsYear) {
      yearDestination
    }
    .onAppear { open(route) }
    .onChange(of: route) { _, newValue in open(newValue) }
    .onChange(of: monthCommand) { _, command in apply(command) }
    .onChange(of: settings) { _, newValue in apply(newValue, today: today) }
    .onChange(of: today) { _, newValue in apply(settings, today: newValue) }
    .modifier(
      DayDetailPresentation(
        date: $detail,
        today: today,
        language: model.language,
        digits: model.digits,
        zoom: detailZoom,
        reduceMotion: reduceMotion
      )
    )
  }

  private var monthSpan: some View {
    HStack(spacing: 8) {
      Text(verbatim: model.adSpan)
        .font(.subheadline)
        .foregroundStyle(BrandColor.secondaryText)
      Spacer(minLength: 0)
    }
    .padding(.horizontal, 16)
    .padding(.bottom, 2)
    .matchedTransitionSource(id: Self.yearZoomID, in: yearZoom)
  }

  private var pager: some View {
    ScrollViewReader { proxy in
      ScrollView(.horizontal) {
        LazyHStack(spacing: 0) {
          ForEach(window) { key in
            MonthPage(
              key: key,
              today: today,
              language: model.language,
              digits: model.digits,
              showTithi: model.showTithi,
              showsADDates: model.showsADDates,
              selected: model.selected,
              zoom: detailZoom,
              onSelect: select,
              onOpenDay: openDay
            )
            .accessibilityHidden(key != (scrolledMonth ?? model.month))
            .containerRelativeFrame([.horizontal, .vertical])
            .id(key)
          }
        }
        .scrollTargetLayout()
      }
      .scrollTargetBehavior(.paging)
      .scrollPosition(id: $scrolledMonth)
      .scrollIndicators(.hidden)
      .accessibilityIdentifier("month_pager")
      .onChange(of: scrolledMonth) { _, newValue in
        guard !isRecentring, let newValue, newValue != model.month else { return }
        model.go(to: newValue)
        if let index = window.firstIndex(of: newValue),
          index < 5 || index >= window.count - 5
        {
          recentre(on: newValue, proxy: proxy)
        }
      }
    }
  }

  private var yearDestination: some View {
    Group {
      if let year = YearViewModel(
        year: model.month.year,
        today: today,
        language: model.language,
        digits: model.digits
      ) {
        let screen = YearScreen(model: year, onPick: pickMonth)
        if reduceMotion {
          screen
        } else {
          screen.navigationTransition(.zoom(sourceID: Self.yearZoomID, in: yearZoom))
        }
      }
    }
  }

  private var previousMonth: MonthKey? {
    MonthKey(id: model.month.id - 1)
  }

  private var nextMonth: MonthKey? {
    MonthKey(id: model.month.id + 1)
  }

  private var todayMonth: MonthKey? {
    MonthKey(monthOf: model.today)
  }

  private func select(_ date: MonthDate) {
    model.select(date)
    detail = date
  }

  /// Opens a day, changing the shown month first when the day is in another month.
  private func openDay(_ date: MonthDate) {
    if let key = MonthKey(monthOf: date), key != model.month {
      go(to: key)
    }
    model.select(date)
    detail = date
  }

  /// Opens a widget's route: today's month, or a day's month with its detail sheet.
  private func open(_ route: AppRoute?) {
    guard let route else { return }
    switch route {
    case .today:
      detail = nil
      go(to: todayMonth)
    case .day(let date):
      openDay(date)
    case .converter:
      // The Converter is another tab, so the app never hands this route to the month screen.
      break
    }
    onRouteHandled()
  }

  /// Applies a keyboard command, then clears it.
  private func apply(_ command: MonthCommand?) {
    guard let command else { return }
    switch command {
    case .today: go(to: todayMonth)
    case .previous: go(to: previousMonth)
    case .next: go(to: nextMonth)
    }
    onMonthCommandHandled()
  }

  /// Rebuilds the shown month for new settings or a new today, keeping the shown month and the
  /// selected day. The pager is not rebuilt, so a settings change never loses the screen's state.
  private func apply(_ settings: Settings, today: MonthDate) {
    let language = settings.language.resolved
    let digits = settings.digits.resolved(for: language)
    guard
      let updated = MonthViewModel(
        today: today,
        language: language,
        digits: digits,
        showTithi: settings.showTithi,
        showsADDates: settings.showsADDates,
        month: model.month
      )
    else { return }
    let selection = model.selected
    model = updated
    if let selection { model.select(selection) }
  }

  private func pickMonth(_ key: MonthKey) {
    showsYear = false
    go(to: key)
  }

  private func go(to key: MonthKey?) {
    guard let key, key != model.month else { return }
    model.go(to: key)
    if !window.contains(key) {
      window = Self.window(around: key)
    }
    withAnimation(Motion.animation(.snappy, reduceMotion: reduceMotion)) {
      scrolledMonth = key
    }
  }

  /// Replaces the window around a month the user swiped to near its edge. Swapping the pages
  /// keeps the scroll offset, not the page, so the offset would then show another month; the
  /// scroll position is reported back while that happens. Paging changes are ignored until the
  /// pager is put back on the month.
  private func recentre(on key: MonthKey, proxy: ScrollViewProxy) {
    isRecentring = true
    window = Self.window(around: key)
    Task { @MainActor in
      proxy.scrollTo(key, anchor: .leading)
      scrolledMonth = key
      isRecentring = false
    }
  }

  /// The pages the pager holds. A window around the shown month keeps the number of views small;
  /// the year view can still reach every month and `go(to:)` recentres the window first.
  private static func window(around key: MonthKey) -> [MonthKey] {
    let radius = 24
    var start = key.id - radius
    var end = key.id + radius
    if start < 0 {
      end = min(MonthKey.count - 1, radius * 2)
      start = 0
    }
    if end >= MonthKey.count {
      start = max(0, MonthKey.count - 1 - radius * 2)
      end = MonthKey.count - 1
    }
    return (start...end).compactMap { MonthKey(id: $0) }
  }

  private static let yearZoomID = "year"
}

/// One page of the pager: the today card, the weekday row, the grid and the month's holidays and
/// events, or the list layout at accessibility sizes.
private struct MonthPage: View {
  let key: MonthKey
  let today: MonthDate
  let language: AppLanguage
  let digits: Digits
  let showTithi: Bool
  let showsADDates: Bool
  let selected: MonthDate?
  let zoom: Namespace.ID
  let onSelect: (MonthDate) -> Void
  let onOpenDay: (MonthDate) -> Void
  @Environment(\.dynamicTypeSize) private var dynamicTypeSize

  var body: some View {
    if let layout = MonthLayout(
      year: key.year,
      month: key.month,
      showTithi: showTithi,
      today: today
    ) {
      if dynamicTypeSize.isAccessibilitySize {
        MonthAccessibilityList(
          layout: layout,
          language: language,
          digits: digits,
          selected: selected,
          onSelect: onSelect
        )
      } else {
        ScrollView {
          VStack(spacing: 8) {
            if isCurrentMonth, let todayCell = todayCell(in: layout) {
              TodayCard(
                cell: todayCell,
                nextHoliday: PatroLookahead.nextHoliday(after: today),
                language: language,
                digits: digits,
                todayLabel: AppText.string("Today", language),
                publicHoliday: AppText.string("public holiday", language),
                onOpenDay: onOpenDay
              )
              .padding(.horizontal, 8)
            }
            MonthWeekdayRow(language: language)
              .padding(.horizontal, 8)
            MonthGrid(
              layout: layout,
              language: language,
              digits: digits,
              selected: selected,
              showTithi: showTithi,
              showsADDates: showsADDates,
              publicHoliday: AppText.string("public holiday", language),
              todayLabel: AppText.string("Today", language),
              zoomNamespace: zoom,
              onSelect: onSelect
            )
            .padding(.horizontal, 8)

            let days = layout.cells.filter { $0.isInMonth && ($0.isHoliday || $0.hasEvents) }
            if !days.isEmpty {
              MonthDayList(
                cells: days,
                language: language,
                digits: digits,
                publicHoliday: AppText.string("public holiday", language),
                onSelect: onSelect
              )
              .padding(.horizontal, 8)
            }
          }
          .padding(.top, 4)
          .padding(.bottom, 16)
        }
      }
    } else {
      Color.clear
    }
  }

  private var isCurrentMonth: Bool {
    MonthKey(monthOf: today) == key
  }

  private func todayCell(in layout: MonthLayout) -> MonthCell? {
    layout.cells.first { $0.isInMonth && $0.isToday }
  }
}

/// The content of the month screen without the pager and the navigation, for the snapshot tests.
public struct MonthScreenContent: View {
  private let model: MonthViewModel
  @Namespace private var detailZoom

  /// Creates the content.
  /// - Parameter model: The month to draw and the settings to draw it with.
  public init(model: MonthViewModel) {
    self.model = model
  }

  /// The content.
  public var body: some View {
    MonthPage(
      key: model.month,
      today: model.today,
      language: model.language,
      digits: model.digits,
      showTithi: model.showTithi,
      showsADDates: model.showsADDates,
      selected: model.selected,
      zoom: detailZoom,
      onSelect: { _ in },
      onOpenDay: { _ in }
    )
    .background(Color(uiColor: .systemBackground))
  }
}

/// Presents the day sheet as a sheet on a phone and as an inspector on a regular-width screen
/// (iPad), with the zoom transition from the tapped cell unless Reduce Motion is on (MR-20).
private struct DayDetailPresentation: ViewModifier {
  @Binding var date: MonthDate?
  let today: MonthDate
  let language: AppLanguage
  let digits: Digits
  let zoom: Namespace.ID
  let reduceMotion: Bool
  @Environment(\.horizontalSizeClass) private var horizontalSizeClass

  func body(content: Content) -> some View {
    if horizontalSizeClass == .regular {
      content.inspector(isPresented: presented) {
        destination
      }
    } else {
      content.sheet(isPresented: presented) {
        destination
      }
    }
  }

  private var presented: Binding<Bool> {
    Binding(
      get: { date != nil },
      set: { isPresented in
        if !isPresented { date = nil }
      }
    )
  }

  @ViewBuilder
  private var destination: some View {
    if let date,
      let model = DayDetailViewModel(date: date, language: language, digits: digits)
    {
      let sheet = DayDetailSheet(model: model, today: today)
      if reduceMotion {
        sheet
      } else {
        sheet.navigationTransition(.zoom(sourceID: date, in: zoom))
      }
    }
  }
}
