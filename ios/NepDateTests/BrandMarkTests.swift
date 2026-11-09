import CalendarUI
import Foundation
import SwiftUI
import Testing
import UIKit

/// The four brand marks are drawn from the same polygons as Android, so the tests here are about
/// the geometry rather than about the look: a mark has to fill the rect it is given and stay inside
/// it. The look is reviewed from the `brand-marks-*` screenshots in the artefact.
@Suite("Brand marks")
struct BrandMarkTests {
  private static let size: CGFloat = 100
  private static let rect = CGRect(x: 0, y: 0, width: size, height: size)
  private static let inset: CGFloat = 12

  @Test("every mark fills its rect and stays inside it")
  func fillsItsRect() {
    for mark in Self.marks() {
      let path = mark.shape.path(in: Self.rect)
      let box = path.boundingRect
      #expect(!path.isEmpty, "\(mark.name) draws nothing")
      #expect(
        box.width >= Self.size - Self.inset && box.height >= Self.size - Self.inset,
        "\(mark.name) leaves too much of its rect empty: \(box)"
      )
      #expect(
        box.minX >= -0.5 && box.minY >= -0.5
          && box.maxX <= Self.size + 0.5 && box.maxY <= Self.size + 0.5,
        "\(mark.name) draws outside its rect: \(box)"
      )
    }
  }

  @Test("every mark is rounded rather than a plain polygon")
  func isRounded() {
    for mark in Self.marks() {
      var curves = 0
      mark.shape.path(in: Self.rect).cgPath.applyWithBlock { element in
        if element.pointee.type == .addCurve {
          curves += 1
        }
      }
      #expect(curves >= 4, "\(mark.name) has \(curves) rounded corners")
    }
  }

  @Test("a zero rect draws nothing instead of NaN")
  func emptyRect() {
    for mark in Self.marks() {
      let path = mark.shape.path(in: .zero)
      #expect(path.boundingRect.width.isFinite && path.boundingRect.height.isFinite)
    }
  }

  private static func marks() -> [(name: String, shape: any Shape)] {
    [
      ("Sunny", SunnyShape()),
      ("SoftBurst", SoftBurstShape()),
      ("Diamond", DiamondShape()),
      ("Cookie", CookieShape()),
    ]
  }
}
