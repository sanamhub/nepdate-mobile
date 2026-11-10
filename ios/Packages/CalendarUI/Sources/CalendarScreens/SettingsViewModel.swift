import CalendarKit
import Foundation

/// The Settings screen's writable state (MR-06, MR-07).
public struct SettingsViewModel: Sendable {
  /// The current settings.
  public private(set) var settings: Settings

  private let store: SettingsStore

  /// Creates the model.
  /// - Parameters:
  ///   - settings: The settings to show.
  ///   - store: The store that receives each change.
  public init(settings: Settings, store: SettingsStore = SettingsStore()) {
    self.settings = settings
    self.store = store
  }

  /// Stores the Digits choice.
  /// - Parameter value: The new setting.
  public mutating func setDigits(_ value: DigitsSetting) {
    settings.digits = value
    store.setDigits(value)
  }

  /// Stores the Date follows choice.
  /// - Parameter value: The new setting.
  public mutating func setDateFollows(_ value: DateFollows) {
    settings.dateFollows = value
    store.setDateFollows(value)
  }

  /// Stores the Show tithi choice.
  /// - Parameter value: The new setting.
  public mutating func setShowTithi(_ value: Bool) {
    settings.showTithi = value
    store.setShowTithi(value)
  }

  /// Stores the Appearance choice (MR-16).
  /// - Parameter value: The new setting.
  public mutating func setAppearance(_ value: Appearance) {
    settings.appearance = value
    store.setAppearance(value)
  }

  /// Stores the Show AD dates choice (MR-17).
  /// - Parameter value: The new setting.
  public mutating func setShowsADDates(_ value: Bool) {
    settings.showsADDates = value
    store.setShowsADDates(value)
  }

  /// Stores the Haptic feedback choice (MR-19).
  /// - Parameter value: The new setting.
  public mutating func setHapticsEnabled(_ value: Bool) {
    settings.hapticsEnabled = value
    store.setHapticsEnabled(value)
  }

  /// Stores the Opens on choice (MR-18).
  /// - Parameter value: The new setting.
  public mutating func setOpensOn(_ value: OpensOn) {
    settings.opensOn = value
    store.setOpensOn(value)
  }

  /// Whether the first-launch "Nepal time" suggestion should show (MR-06 AC1).
  /// - Parameter deviceZone: The device's zone.
  /// - Returns: True only for an unhandled install, This device and a zone outside Nepal time.
  public func shouldSuggestNepalTime(deviceZone: TimeZone = .current) -> Bool {
    !settings.dateFollowsSuggestionHandled
      && settings.dateFollows == .device
      && deviceZone.secondsFromGMT() != CalendarDates.nepal.secondsFromGMT()
  }

  /// Accepts the Nepal time suggestion and records that it was shown.
  public mutating func acceptNepalTime() {
    setDateFollows(.nepal)
    setSuggestionHandled()
  }

  /// Dismisses the Nepal time suggestion and records that it was shown.
  public mutating func dismissNepalTime() {
    setSuggestionHandled()
  }

  private mutating func setSuggestionHandled() {
    settings.dateFollowsSuggestionHandled = true
    store.setDateFollowsSuggestionHandled(true)
  }
}
