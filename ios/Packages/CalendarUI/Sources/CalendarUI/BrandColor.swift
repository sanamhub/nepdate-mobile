import CalendarKit
import SwiftUI
import UIKit

/// The two brand colours of the design system, as dynamic light and dark colours.
public enum BrandColor {
  /// The holiday and Saturday red: `#C8102E` in light, `#FF6B7A` in dark.
  public static let holiday = Color(
    uiColor: UIColor { traits in
      traits.userInterfaceStyle == .dark
        ? UIColor(red: 1.0, green: 0.4196, blue: 0.4784, alpha: 1.0)
        : UIColor(red: 0.7843, green: 0.0627, blue: 0.1804, alpha: 1.0)
    }
  )

  /// The holiday fill behind a holiday numeral, at the design system's alpha.
  public static let holidayFill = Color(
    uiColor: UIColor { traits in
      traits.userInterfaceStyle == .dark
        ? UIColor(red: 1.0, green: 0.4196, blue: 0.4784, alpha: 0.16)
        : UIColor(red: 0.7843, green: 0.0627, blue: 0.1804, alpha: 0.10)
    }
  )

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

  /// The today ring and "Today" button blue: `#003893` in light, `#8FB3FF` in dark.
  public static let today = Color(
    uiColor: UIColor { traits in
      traits.userInterfaceStyle == .dark
        ? UIColor(red: 0.5608, green: 0.7020, blue: 1.0, alpha: 1.0)
        : UIColor(red: 0.0, green: 0.2196, blue: 0.5765, alpha: 1.0)
    }
  )
}
