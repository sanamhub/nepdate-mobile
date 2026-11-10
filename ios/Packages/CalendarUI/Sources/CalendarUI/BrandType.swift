import SwiftUI

/// The type roles of the design system (ADR-0015): system text styles only, rounded numerals with
/// tabular figures so columns of dates line up.
///
/// Three rules come with the roles and are not enforced by the compiler, so they are written here:
/// no fixed heights on a text view, no `lineLimit(1)` with `minimumScaleFactor` on Devanagari (a
/// conjunct that does not fit wraps instead of shrinking), and a widget keeps a fixed text size
/// only where WidgetKit itself requires one, scaling with `@ScaledMetric` where it can.
public enum BrandType {
  /// A numeral role: the system size for [style], rounded, with monospaced digits.
  public static func numerals(_ style: Font.TextStyle) -> Font {
    .system(style, design: .rounded).monospacedDigit()
  }

  /// The BS numeral of a month cell.
  public static let cellNumeral = numerals(.title2)

  /// The BS month and year above the grid.
  public static let monthTitle = numerals(.largeTitle).weight(.semibold)

  /// A sheet or section heading.
  public static let heading = Font.system(.headline)

  /// Body copy: lists, the day sheet, About.
  public static let body = Font.system(.body)

  /// A caption: the AD date under a BS date, the relative day.
  public static let caption = Font.system(.caption2)
}
