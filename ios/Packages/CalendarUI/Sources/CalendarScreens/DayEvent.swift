import CalendarKit
import EventKit
import Foundation

/// Builds the all-day event handed to the system event editor (MR-02 AC2).
///
/// The builder is separate from the editor so a unit test can inspect the event without presenting
/// the editor or asking for calendar access. It never reads a calendar, a default calendar or an
/// authorization status.
public enum DayEvent {
  /// Builds the event for a day detail sheet.
  ///
  /// The title is the first event or the holiday name, so a holiday with a named event keeps that
  /// name and its scope. The event is all-day, starts at local midnight of the AD date and has no
  /// alarm.
  /// - Parameters:
  ///   - model: The day's values.
  ///   - publicHoliday: The localised words for a public holiday.
  ///   - store: The event store the editor uses.
  ///   - calendar: The calendar that gives the local midnight.
  /// - Returns: The event, or `nil` when the AD date cannot be made.
  public static func event(
    for model: DayDetailViewModel,
    publicHoliday: String,
    store: EKEventStore,
    calendar: Calendar = .current
  ) -> EKEvent? {
    let gregorian = model.gregorianDate
    var components = DateComponents()
    components.calendar = calendar
    components.timeZone = calendar.timeZone
    components.year = gregorian.year
    components.month = gregorian.month
    components.day = gregorian.day
    guard let midnight = calendar.date(from: components) else { return nil }
    let start = calendar.startOfDay(for: midnight)

    let event = EKEvent(eventStore: store)
    event.title = model.calendarTitle(publicHoliday: publicHoliday)
    event.isAllDay = true
    event.startDate = start
    event.endDate = calendar.date(byAdding: .day, value: 1, to: start) ?? start
    event.alarms = nil
    return event
  }
}
