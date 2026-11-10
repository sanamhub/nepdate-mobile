import CalendarUI
import Foundation
import SwiftUI
import Testing
import UIKit

/// The brand pairs that carry text, measured in light, dark and both high-contrast appearances.
///
/// The colours come from the asset catalog, so this is the gate that keeps a palette edit from
/// shipping an unreadable pair (MR-10 AC3): change a colour set, and a failing ratio names the role
/// and the appearance (ADR-0015).
@Suite("Brand colours")
struct BrandColorTests {
  private struct Appearance {
    let name: String
    let style: UIUserInterfaceStyle
    let contrast: UIAccessibilityContrast
  }

  private static let appearances = [
    Appearance(name: "light", style: .light, contrast: .normal),
    Appearance(name: "dark", style: .dark, contrast: .normal),
    Appearance(name: "light, high contrast", style: .light, contrast: .high),
    Appearance(name: "dark, high contrast", style: .dark, contrast: .high),
  ]

  /// WCAG 2.2 asks for 4.5:1 on text below 18 pt, which is where every pair here is used.
  private static let floor = 4.5

  @Test("holiday ink on the app background is legible")
  func holidayOnBackground() {
    for appearance in Self.appearances {
      let traits = Self.traits(appearance)
      let background = UIColor.systemBackground.resolvedColor(with: traits)
      expectAtLeast(
        "holiday on the background",
        BrandColor.holiday,
        background,
        traits,
        in: appearance.name
      )
    }
  }

  @Test("text on the holiday tile is legible")
  func onHolidayContainer() {
    for appearance in Self.appearances {
      let traits = Self.traits(appearance)
      let fill = UIColor(BrandColor.holidayContainer).resolvedColor(with: traits)
      expectAtLeast(
        "onHolidayContainer on holidayContainer",
        BrandColor.onHolidayContainer,
        fill,
        traits,
        in: appearance.name
      )
    }
  }

  @Test("the numeral on the Sunny mark is legible")
  func onToday() {
    for appearance in Self.appearances {
      let traits = Self.traits(appearance)
      let fill = UIColor(BrandColor.today).resolvedColor(with: traits)
      expectAtLeast("onToday on today", BrandColor.onToday, fill, traits, in: appearance.name)
    }
  }

  @Test("the accent reads on the app background")
  func accentOnBackground() {
    for appearance in Self.appearances {
      let traits = Self.traits(appearance)
      let background = UIColor.systemBackground.resolvedColor(with: traits)
      expectAtLeast(
        "accent on the background",
        BrandColor.accent,
        background,
        traits,
        in: appearance.name
      )
    }
  }

  @Test("the label on a prominent accent button is legible")
  func onAccent() {
    for appearance in Self.appearances {
      let traits = Self.traits(appearance)
      let fill = UIColor(BrandColor.accent).resolvedColor(with: traits)
      expectAtLeast("onAccent on accent", BrandColor.onAccent, fill, traits, in: appearance.name)
    }
  }

  private static func traits(_ appearance: Appearance) -> UITraitCollection {
    UITraitCollection(traitsFrom: [
      UITraitCollection(userInterfaceStyle: appearance.style),
      UITraitCollection(accessibilityContrast: appearance.contrast),
    ])
  }

  private func expectAtLeast(
    _ pair: String,
    _ foreground: Color,
    _ background: UIColor,
    _ traits: UITraitCollection,
    in appearance: String
  ) {
    let ink = UIColor(foreground).resolvedColor(with: traits)
    let ratio = Self.contrast(ink, background)
    #expect(
      ratio >= Self.floor,
      "\(pair) is \(String(format: "%.2f", ratio)):1 in \(appearance), below the floor"
    )
  }

  /// The WCAG relative-luminance ratio of two opaque colours.
  private static func contrast(_ first: UIColor, _ second: UIColor) -> Double {
    let a = luminance(first)
    let b = luminance(second)
    let lighter = max(a, b)
    let darker = min(a, b)
    return (lighter + 0.05) / (darker + 0.05)
  }

  private static func luminance(_ color: UIColor) -> Double {
    var red: CGFloat = 0
    var green: CGFloat = 0
    var blue: CGFloat = 0
    var alpha: CGFloat = 0
    guard color.getRed(&red, green: &green, blue: &blue, alpha: &alpha) else { return 0 }
    func channel(_ value: CGFloat) -> Double {
      let value = Double(value)
      return value <= 0.03928 ? value / 12.92 : pow((value + 0.055) / 1.055, 2.4)
    }
    return 0.2126 * channel(red) + 0.7152 * channel(green) + 0.0722 * channel(blue)
  }
}
