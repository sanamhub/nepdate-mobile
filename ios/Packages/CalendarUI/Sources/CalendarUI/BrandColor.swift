import CalendarKit
import SwiftUI
import UIKit

/// The brand colours of the design system, as colour sets in the CalendarUI package's own asset
/// catalogue (ADR-0015).
///
/// Every role has a light, a dark and a high-contrast value, so the palette can be read on Windows
/// in a diff and does not depend on code to change an appearance. The values are the shipped
/// marigold palette: holiday is sindoor red, today is marigold on a filled Sunny mark. Surfaces are
/// the system's own colours, never these.
///
/// `BrandColorTests` measures every pair that carries text at 4.5:1 in all four appearances.
public enum BrandColor {
  /// The holiday and Saturday red.
  public static let holiday = Color("Holiday", bundle: .module)

  /// The holiday red behind a holiday numeral, already at the design system's alpha (10 % light,
  /// 16 % dark).
  public static let holidayFill = Color("HolidayFill", bundle: .module)

  /// The stronger holiday tile behind a holiday date in lists, sheets and widgets.
  public static let holidayContainer = Color("HolidayContainer", bundle: .module)

  /// Text and numerals drawn on `holidayContainer`.
  public static let onHolidayContainer = Color("OnHolidayContainer", bundle: .module)

  /// The marigold of the Sunny mark, which is the "today" marker everywhere.
  public static let today = Color("Today", bundle: .module)

  /// The ink drawn on the Sunny mark.
  public static let onToday = Color("OnToday", bundle: .module)

  /// The app accent, from the Material primary.
  public static let accent = Color("AccentColor", bundle: .main)

  /// The label on a control filled with ccent, from the Material on-primary: white in light,
  /// dark brown in dark, where the accent is a light marigold. A prominent button sets it, because
  /// the system's white label is unreadable on the dark accent.
  public static let onAccent = Color("OnAccent", bundle: .module)

  /// Secondary text (AD numerals, dimmed days, tithi and event lines). The system secondary label
  /// is below 4.5:1 in light mode, so the month screen uses this pair. Measured (WCAG relative
  /// luminance): light 7.55:1 on the background, 6.36:1 on `holidayFill`, 6.25:1 on `selection`;
  /// dark 12.41:1, 10.48:1 and 8.87:1. Light was 0.40 white (5.74:1, 4.83:1 and 4.75:1), which the
  /// system audit flagged on two caption-sized AD numerals.
  public static let secondaryText = Color(
    uiColor: UIColor { traits in
      traits.userInterfaceStyle == .dark
        ? UIColor(white: 0.78, alpha: 1.0)
        : UIColor(white: 0.33, alpha: 1.0)
    }
  )

  /// The fill behind the selected day (MR-01 AC4).
  public static let selection = Color(uiColor: .secondarySystemFill)
}
