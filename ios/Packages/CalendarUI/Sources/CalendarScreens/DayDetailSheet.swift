import CalendarKit
import CalendarUI
import EventKit
import EventKitUI
import SwiftUI
import UIKit

/// The day detail sheet (MR-02, MR-20): the header tile, the relative and holiday chips, the BS
/// and AD dates, the weekday, the tithi, a public holiday, every event, and the Add to calendar,
/// Share and Copy date actions, with previous and next day in the toolbar.
public struct DayDetailSheet: View {
  @State private var model: DayDetailViewModel
  @State private var detent: PresentationDetent = .large
  @State private var step = 0
  private let today: MonthDate?
  @Environment(\.dismiss) private var dismiss
  @Environment(\.accessibilityReduceMotion) private var reduceMotion

  /// Creates the sheet.
  /// - Parameters:
  ///   - model: The day's values.
  ///   - today: The app's today, for the relative chip; `nil` hides that chip.
  public init(model: DayDetailViewModel, today: MonthDate? = nil) {
    _model = State(initialValue: model)
    self.today = today
  }

  /// The sheet's content.
  public var body: some View {
    NavigationStack {
      DayDetailContent(
        model: model,
        today: today,
        onPrevious: { move(-1) },
        onNext: { move(1) },
        onClose: { dismiss() }
      )
      .navigationBarTitleDisplayMode(.inline)
      .toolbar {
        ToolbarItemGroup(placement: .topBarLeading) {
          Button(action: { move(-1) }) {
            Image(systemName: "chevron.left")
          }
          .disabled(!model.canGoPrevious)
          .accessibilityLabel(Text("Previous day"))

          Button(action: { move(1) }) {
            Image(systemName: "chevron.right")
          }
          .disabled(!model.canGoNext)
          .accessibilityLabel(Text("Next day"))
        }
      }
    }
    .presentationDetents([.medium, .large], selection: $detent)
    .presentationDragIndicator(.visible)
    .hapticFeedback(.selection, trigger: step)
  }

  private func move(_ offset: Int) {
    guard let next = model.day(offset: offset) else { return }
    withAnimation(Motion.animation(.snappy, reduceMotion: reduceMotion)) {
      model = next
      step += 1
    }
  }
}

/// The content of the day sheet without its presentation. The snapshot tests render this.
public struct DayDetailContent: View {
  private let model: DayDetailViewModel
  private let today: MonthDate?
  private let onPrevious: () -> Void
  private let onNext: () -> Void
  private let onClose: () -> Void

  @Environment(\.locale) private var locale
  @Environment(\.accessibilityReduceMotion) private var reduceMotion
  @State private var eventStore: EKEventStore?
  @State private var eventToEdit: EKEvent?
  @State private var showsCalendarEditor = false
  @State private var copiedText: String?
  @State private var copied: CopyConfirmationTimer
  @State private var copyCount = 0

  /// Creates the content.
  /// - Parameters:
  ///   - model: The day's values.
  ///   - today: The app's today, for the relative chip; `nil` hides that chip.
  ///   - onPrevious: Shows the day before.
  ///   - onNext: Shows the day after.
  ///   - onClose: The close button's action.
  ///   - showsCopiedInitially: Whether the copied confirmation starts visible, for snapshots.
  @MainActor
  public init(
    model: DayDetailViewModel,
    today: MonthDate? = nil,
    onPrevious: @escaping () -> Void = {},
    onNext: @escaping () -> Void = {},
    onClose: @escaping () -> Void,
    showsCopiedInitially: Bool = false
  ) {
    self.model = model
    self.today = today
    self.onPrevious = onPrevious
    self.onNext = onNext
    self.onClose = onClose
    _copied = State(initialValue: CopyConfirmationTimer(isShowing: showsCopiedInitially))
  }

  /// The sheet's content.
  public var body: some View {
    ScrollView {
      VStack(alignment: .leading, spacing: 16) {
        header
        if model.isPublicHoliday || today != nil {
          chips
        }
        Text(verbatim: model.weekdayAndDate)
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)
          .fixedSize(horizontal: false, vertical: true)

        if model.hasData {
          facts
        } else {
          Text("No holiday or tithi data for this date yet")
            .font(.body)
            .fixedSize(horizontal: false, vertical: true)
            .padding(.top, 4)
        }

        actions
        if copied.isShowing {
          Text("Date copied")
            .font(.footnote)
            .foregroundStyle(BrandColor.secondaryText)
            .accessibilityIdentifier("day_detail_copied")
        }
      }
      .padding(.horizontal, 20)
      .padding(.top, 8)
      .padding(.bottom, 24)
    }
    .background(Color(uiColor: .systemBackground))
    .accessibilityIdentifier("day_detail")
    .accessibilityAdjustableAction { direction in
      switch direction {
      case .increment: onNext()
      case .decrement: onPrevious()
      default: break
      }
    }
    .sheet(isPresented: $showsCalendarEditor) {
      if let eventStore, let eventToEdit {
        EventEditorView(
          eventStore: eventStore,
          event: eventToEdit,
          isPresented: $showsCalendarEditor
        )
      }
    }
    .hapticFeedback(.success, trigger: copyCount)
  }

  private var header: some View {
    ZStack(alignment: .topLeading) {
      HStack(alignment: .top, spacing: 12) {
        tile
        VStack(alignment: .leading, spacing: 2) {
          Text(verbatim: model.bsDate)
            .font(.largeTitle)
            .fontWeight(.bold)
            .fixedSize(horizontal: false, vertical: true)
            .accessibilityAddTraits(.isHeader)

          if model.language == .english {
            Text(verbatim: model.devanagariDate)
              .font(.title3)
              .foregroundStyle(BrandColor.secondaryText)
              .fixedSize(horizontal: false, vertical: true)
          }
        }
        .frame(maxWidth: .infinity, alignment: .leading)

        Button(action: onClose) {
          Image(systemName: "xmark")
            .font(.body.weight(.semibold))
            .frame(width: 44, height: 44)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text("Close"))
      }
      .id(model.date)
      .transition(
        Motion.transition(
          .asymmetric(
            insertion: .move(edge: .bottom).combined(with: .opacity),
            removal: .move(edge: .top).combined(with: .opacity)
          ),
          reduceMotion: reduceMotion
        )
      )
    }
  }

  private var tile: some View {
    ZStack {
      tileBackground
      Text(verbatim: model.digits.format(model.date.day))
        .font(.title.weight(.bold))
        .foregroundStyle(tileInk)
        .minimumScaleFactor(0.6)
    }
    .frame(width: 72, height: 72)
    .accessibilityHidden(true)
  }

  @ViewBuilder
  private var tileBackground: some View {
    if model.date == today {
      SunnyShape().fill(BrandColor.today)
    } else if model.isPublicHoliday {
      CookieShape().fill(BrandColor.holidayContainer)
    } else {
      RoundedRectangle(cornerRadius: 20, style: .continuous)
        .fill(Color(uiColor: .tertiarySystemFill))
    }
  }

  private var tileInk: Color {
    if model.date == today { return BrandColor.onToday }
    if model.isPublicHoliday { return BrandColor.onHolidayContainer }
    return .primary
  }

  private var chips: some View {
    FlowLayout(horizontalSpacing: 8, verticalSpacing: 8) {
      if model.isPublicHoliday {
        HStack(spacing: 6) {
          SoftBurstShape()
            .fill(BrandColor.holiday)
            .frame(width: 12, height: 12)
          Text("Public holiday")
            .font(.subheadline.weight(.medium))
        }
        .foregroundStyle(BrandColor.onHolidayContainer)
        .padding(.horizontal, 12)
        .padding(.vertical, 6)
        .background(BrandColor.holidayContainer, in: Capsule())
      }
      if let today {
        Text(verbatim: relativeText(model.relativeDay(from: today)))
          .font(.subheadline.weight(.medium))
          .foregroundStyle(.primary)
          .padding(.horizontal, 12)
          .padding(.vertical, 6)
          .overlay {
            Capsule()
              .stroke(Color(uiColor: .separator), lineWidth: 1)
          }
      }
    }
    .padding(.horizontal, 4)
  }

  private func relativeText(_ relative: RelativeDay) -> String {
    switch relative {
    case .today:
      return AppText.string("Today", model.language)
    case .tomorrow:
      return AppText.string("Tomorrow", model.language)
    case .yesterday:
      return AppText.string("Yesterday", model.language)
    case .inDays(let days):
      return String(
        format: AppText.string("in %@ days", model.language),
        model.digits.format(days)
      )
    case .daysAgo(let days):
      return String(
        format: AppText.string("%@ days ago", model.language),
        model.digits.format(days)
      )
    }
  }

  @ViewBuilder
  private var facts: some View {
    if let tithi = model.tithi {
      VStack(alignment: .leading, spacing: 3) {
        Text("Tithi")
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)
        Text(verbatim: tithi)
          .font(.body)
          .fixedSize(horizontal: false, vertical: true)
      }
    }

    if !model.events.isEmpty {
      VStack(alignment: .leading, spacing: 6) {
        Text("Events")
          .font(.subheadline)
          .foregroundStyle(BrandColor.secondaryText)
        ForEach(Array(model.events.enumerated()), id: \.offset) { event in
          Text(verbatim: event.element)
            .font(.body)
            .fixedSize(horizontal: false, vertical: true)
        }
      }
    }
  }

  private var actions: some View {
    VStack(spacing: 12) {
      Button(action: addToCalendar) {
        Text("Add to calendar")
          .frame(maxWidth: .infinity, minHeight: 44)
      }
      .buttonStyle(.borderedProminent)
      .tint(BrandColor.accent)
      .foregroundStyle(BrandColor.onAccent)

      HStack(spacing: 12) {
        ShareLink(item: model.shareText) {
          Label("Share", systemImage: "square.and.arrow.up")
            .frame(maxWidth: .infinity, minHeight: 44)
        }
        .buttonStyle(.bordered)
        .tint(BrandColor.accent)

        Button(action: copyDate) {
          Label("Copy date", systemImage: "doc.on.doc")
            .frame(maxWidth: .infinity, minHeight: 44)
        }
        .buttonStyle(.bordered)
        .tint(BrandColor.accent)
        .accessibilityValue(Text(copiedText ?? ""))
      }
    }
    .padding(.top, 8)
  }

  private var publicHoliday: String {
    String(localized: "Public holiday", locale: locale)
  }

  private func addToCalendar() {
    let store = eventStore ?? EKEventStore()
    eventStore = store
    guard
      let event = DayEvent.event(
        for: model,
        publicHoliday: publicHoliday,
        store: store
      )
    else { return }
    eventToEdit = event
    showsCalendarEditor = true
  }

  private func copyDate() {
    DayClipboard.write(model.copyText)
    copiedText = model.copyText
    CopyConfirmation.announce(AppText.string("Date copied", model.language))
    copied.show()
    copyCount += 1
  }
}

/// Presents the system event editor for the prepared event. It needs no calendar permission: the
/// editor asks for one only when it has to, and the app never reads the store.
private struct EventEditorView: UIViewControllerRepresentable {
  let eventStore: EKEventStore
  let event: EKEvent
  @Binding var isPresented: Bool

  func makeCoordinator() -> Coordinator {
    Coordinator(isPresented: $isPresented)
  }

  func makeUIViewController(context: Context) -> EKEventEditViewController {
    let controller = EKEventEditViewController()
    controller.eventStore = eventStore
    controller.event = event
    controller.editViewDelegate = context.coordinator
    return controller
  }

  func updateUIViewController(_ controller: EKEventEditViewController, context: Context) {}

  /// EventKit calls the delegate on the main thread. The class stays nonisolated because
  /// `EKEventEditViewDelegate` is not annotated with the main actor.
  final class Coordinator: NSObject, EKEventEditViewDelegate {
    private let isPresented: Binding<Bool>

    init(isPresented: Binding<Bool>) {
      self.isPresented = isPresented
    }

    func eventEditViewController(
      _ controller: EKEventEditViewController,
      didCompleteWith action: EKEventEditViewAction
    ) {
      isPresented.wrappedValue = false
    }
  }
}
