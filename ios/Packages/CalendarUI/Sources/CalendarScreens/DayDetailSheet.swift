import CalendarKit
import CalendarUI
import EventKit
import EventKitUI
import SwiftUI
import UIKit

/// The day detail sheet (MR-02): the BS and AD dates, the weekday, the tithi, a public holiday,
/// every event, and the Add to calendar and Copy date actions.
public struct DayDetailSheet: View {
  private let model: DayDetailViewModel
  @Environment(\.dismiss) private var dismiss
  @State private var detent: PresentationDetent = .large

  /// Creates the sheet.
  /// - Parameter model: The day's values.
  public init(model: DayDetailViewModel) {
    self.model = model
  }

  /// The sheet's content.
  public var body: some View {
    DayDetailContent(model: model) {
      dismiss()
    }
    .presentationDetents([.medium, .large], selection: $detent)
    .presentationDragIndicator(.visible)
  }
}

/// The content of the day sheet without its presentation. The snapshot tests render this.
public struct DayDetailContent: View {
  private let model: DayDetailViewModel
  private let onClose: () -> Void

  @Environment(\.locale) private var locale
  @State private var eventStore: EKEventStore?
  @State private var eventToEdit: EKEvent?
  @State private var showsCalendarEditor = false
  @State private var copiedText: String?
  @State private var showsCopied: Bool

  /// Creates the content.
  /// - Parameters:
  ///   - model: The day's values.
  ///   - onClose: The close button's action.
  ///   - showsCopiedInitially: Whether the copied confirmation starts visible, for snapshots.
  public init(
    model: DayDetailViewModel,
    onClose: @escaping () -> Void,
    showsCopiedInitially: Bool = false
  ) {
    self.model = model
    self.onClose = onClose
    _showsCopied = State(initialValue: showsCopiedInitially)
  }

  /// The sheet's content.
  public var body: some View {
    ScrollView {
      VStack(alignment: .leading, spacing: 16) {
        header
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
        if showsCopied {
          Text("Date copied")
            .font(.footnote)
            .foregroundStyle(BrandColor.secondaryText)
            .accessibilityIdentifier("day_detail_copied")
        }
      }
      .padding(.horizontal, 20)
      .padding(.bottom, 24)
    }
    .background(Color(uiColor: .systemBackground))
    .accessibilityIdentifier("day_detail")
    .sheet(isPresented: $showsCalendarEditor) {
      if let eventStore, let eventToEdit {
        EventEditorView(
          eventStore: eventStore,
          event: eventToEdit,
          isPresented: $showsCalendarEditor
        )
      }
    }
  }

  private var header: some View {
    HStack(alignment: .top, spacing: 8) {
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

    if model.isPublicHoliday {
      Text("Public holiday")
        .font(.subheadline.weight(.semibold))
        .foregroundStyle(BrandColor.holiday)
        .padding(.horizontal, 12)
        .padding(.vertical, 6)
        .background(BrandColor.holidayFill, in: Capsule())
        .fixedSize(horizontal: false, vertical: true)
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
      .tint(BrandColor.today)

      Button(action: copyDate) {
        Text("Copy date")
          .frame(maxWidth: .infinity, minHeight: 44)
      }
      .buttonStyle(.bordered)
      .tint(BrandColor.today)
      .accessibilityValue(Text(copiedText ?? ""))
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
    showsCopied = true
    UIAccessibility.post(
      notification: .announcement,
      argument: AppText.string("Date copied", model.language)
    )
    Task { @MainActor in
      try? await Task.sleep(for: .seconds(2))
      showsCopied = false
    }
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
