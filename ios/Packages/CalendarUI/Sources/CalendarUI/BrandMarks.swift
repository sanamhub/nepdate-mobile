import SwiftUI

/// One vertex of a brand mark in its own coordinate space, plus the rounding of its corner.
struct MarkVertex {
  let point: CGPoint
  /// The corner rounding in the same unit as Compose's `CornerRounding.radius`: 0 leaves a sharp
  /// corner and 1 rounds it all the way to the middle of the shorter side.
  let rounding: CGFloat
}

/// A Material 3 Expressive polygon, ported from `androidx.graphics.shapes`.
///
/// Android draws its marks with `RoundedPolygon`: a polygon whose corners are cut back by a
/// rounding distance and joined with a cubic. This is that construction, small enough to read. The
/// vertices come in the library's own units; drawing maps the polygon's bounds onto the whole rect,
/// which is what Android does when it scales the path by the view size.
///
/// The second value of Compose's `CornerRounding` (the smoothing) is not ported: the four brand
/// marks either leave it at zero or use it in a way that is invisible at the sizes they are drawn.
struct MarkPolygon {
  let vertices: [MarkVertex]

  /// Compose's `RoundedPolygon.star`: 2 × [points] vertices, alternating the outer and the inner
  /// radius, the first point at the top.
  static func star(
    points: Int,
    innerRadius: CGFloat,
    rounding: CGFloat
  ) -> MarkPolygon {
    let count = points * 2
    let vertices = (0..<count).map { index -> MarkVertex in
      let radius: CGFloat = index.isMultiple(of: 2) ? 1 : innerRadius
      let angle = -CGFloat.pi / 2 + CGFloat(index) * .pi / CGFloat(points)
      return MarkVertex(
        point: CGPoint(x: 0.5 + radius * cos(angle), y: 0.5 + radius * sin(angle)),
        rounding: rounding
      )
    }
    return MarkPolygon(vertices: vertices)
  }

  /// Compose's `customPolygon(points, reps:)`: the base points repeated around the centre.
  static func repeated(
    _ base: [MarkVertex],
    reps: Int,
    centre: CGPoint = CGPoint(x: 0.5, y: 0.5)
  ) -> MarkPolygon {
    let vertices = (0..<(base.count * reps)).map { index -> MarkVertex in
      let source = base[index % base.count]
      let angle = CGFloat(index / base.count) * 2 * .pi / CGFloat(reps)
      let offset = CGPoint(x: source.point.x - centre.x, y: source.point.y - centre.y)
      return MarkVertex(
        point: CGPoint(
          x: centre.x + offset.x * cos(angle) - offset.y * sin(angle),
          y: centre.y + offset.x * sin(angle) + offset.y * cos(angle)
        ),
        rounding: source.rounding
      )
    }
    return MarkPolygon(vertices: vertices)
  }

  /// The outline of the rounded polygon, mapped onto [rect].
  func path(in rect: CGRect) -> Path {
    var path = Path()
    let points = vertices.map(\.point)
    guard points.count >= 3 else { return path }

    let bounds = points.reduce(CGRect.null) { box, point in
      box.union(CGRect(origin: point, size: .zero))
    }
    guard bounds.width > 0, bounds.height > 0 else { return path }
    let scaleX = rect.width / bounds.width
    let scaleY = rect.height / bounds.height
    func place(_ point: CGPoint) -> CGPoint {
      CGPoint(
        x: rect.minX + (point.x - bounds.minX) * scaleX,
        y: rect.minY + (point.y - bounds.minY) * scaleY
      )
    }

    // Each corner is cut back along both of its sides by a distance that depends on the rounding
    // and on the shorter of the two sides, then joined by a cubic that turns the corner.
    let cuts: [(inner: CGPoint, outer: CGPoint)] = points.indices.map { index in
      let point = points[index]
      let previous = points[(index + points.count - 1) % points.count]
      let next = points[(index + 1) % points.count]
      let short = min(distance(point, previous), distance(point, next))
      let cut = vertices[index].rounding * 0.5 * short
      return (
        inner: point.moved(towards: previous, by: cut),
        outer: point.moved(towards: next, by: cut)
      )
    }

    path.move(to: place(cuts[0].inner))
    for index in points.indices {
      let point = points[index]
      let cut = cuts[index]
      let inner = place(cut.inner)
      let outer = place(cut.outer)
      let corner = place(point)
      path.addCurve(
        to: outer,
        control1: inner.moved(towards: corner, by: distance(inner, corner) * Self.circleHandle),
        control2: outer.moved(towards: corner, by: distance(outer, corner) * Self.circleHandle)
      )
      path.addLine(to: place(cuts[(index + 1) % points.count].inner))
    }
    path.closeSubpath()
    return path
  }

  /// The handle of a cubic that draws a quarter circle, as a share of the distance to the corner.
  private static let circleHandle: CGFloat = 0.5523
}

private func distance(_ first: CGPoint, _ second: CGPoint) -> CGFloat {
  hypot(second.x - first.x, second.y - first.y)
}

private extension CGPoint {
  func moved(towards other: CGPoint, by length: CGFloat) -> CGPoint {
    let lengthToOther = distance(self, other)
    guard lengthToOther > 0 else { return self }
    return CGPoint(
      x: x + (other.x - x) / lengthToOther * length,
      y: y + (other.y - y) / lengthToOther * length
    )
  }
}

/// The Sunny shape: the marigold "today" mark, an eight-point star with a soft rounding.
public struct SunnyShape: Shape {
  /// Creates the Sunny mark.
  public init() {}

  /// The Sunny mark, mapped onto `rect`.
  public func path(in rect: CGRect) -> Path {
    MarkPolygon.star(points: 8, innerRadius: 0.8, rounding: 0.15).path(in: rect)
  }
}

/// The SoftBurst shape: the next-holiday mark on the today card.
public struct SoftBurstShape: Shape {
  /// Creates the SoftBurst mark.
  public init() {}

  /// The SoftBurst mark, mapped onto `rect`.
  public func path(in rect: CGRect) -> Path {
    MarkPolygon.repeated(
      [
        MarkVertex(point: CGPoint(x: 0.193, y: 0.277), rounding: 0.053),
        MarkVertex(point: CGPoint(x: 0.176, y: 0.055), rounding: 0.053),
      ],
      reps: 10
    ).path(in: rect)
  }
}

/// The Diamond shape: the small event marker under a day's numeral.
public struct DiamondShape: Shape {
  /// Creates the Diamond mark.
  public init() {}

  /// The Diamond mark, mapped onto `rect`.
  public func path(in rect: CGRect) -> Path {
    MarkPolygon.repeated(
      [
        MarkVertex(point: CGPoint(x: 0.500, y: 1.096), rounding: 0.151),
        MarkVertex(point: CGPoint(x: 0.040, y: 0.500), rounding: 0.159),
      ],
      reps: 2
    ).path(in: rect)
  }
}

/// The Cookie shape: the nine-sided tile of the day sheet's header.
public struct CookieShape: Shape {
  /// Creates the Cookie mark.
  public init() {}

  /// The Cookie mark, mapped onto `rect`.
  public func path(in rect: CGRect) -> Path {
    MarkPolygon.star(points: 9, innerRadius: 0.8, rounding: 0.5).path(in: rect)
  }
}
