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

  /// The today ring and "Today" button blue: `#003893` in light, `#8FB3FF` in dark.
  public static let today = Color(
    uiColor: UIColor { traits in
      traits.userInterfaceStyle == .dark
        ? UIColor(red: 0.5608, green: 0.7020, blue: 1.0, alpha: 1.0)
        : UIColor(red: 0.0, green: 0.2196, blue: 0.5765, alpha: 1.0)
    }
  )
}
