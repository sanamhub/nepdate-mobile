import SwiftUI

/// Lays views out left to right and wraps to a new line when the width runs out.
///
/// SwiftUI has no flow container of its own, and the today card and the festival chips need the
/// same wrapping: a row of chips as wide as the card, with a consistent gap between them.
public struct FlowLayout: Layout {
  /// The gap between two views on one line.
  public var horizontalSpacing: CGFloat

  /// The gap between two lines.
  public var verticalSpacing: CGFloat

  /// Creates the layout.
  /// - Parameters:
  ///   - horizontalSpacing: The gap between two views; 8 by default.
  ///   - verticalSpacing: The gap between two lines; 8 by default.
  public init(horizontalSpacing: CGFloat = 8, verticalSpacing: CGFloat = 8) {
    self.horizontalSpacing = horizontalSpacing
    self.verticalSpacing = verticalSpacing
  }

  /// The size a flow of views needs inside a proposal.
  /// - Parameters:
  ///   - proposal: The space offered by the parent.
  ///   - subviews: The views to lay out.
  ///   - cache: The layout cache; unused.
  /// - Returns: The width offered, or the widest line, and the height of every line.
  public func sizeThatFits(
    proposal: ProposedViewSize,
    subviews: Subviews,
    cache: inout ()
  ) -> CGSize {
    let width = proposal.width ?? .infinity
    let lines = lines(for: subviews, width: width)
    guard !lines.isEmpty else { return .zero }
    let usedWidth = lines.map(\.width).max() ?? 0
    let height =
      lines.map(\.height).reduce(0, +)
      + CGFloat(max(0, lines.count - 1)) * verticalSpacing
    return CGSize(width: proposal.width ?? usedWidth, height: height)
  }

  /// Places the views in a rectangle, wrapping where the width runs out.
  /// - Parameters:
  ///   - bounds: The rectangle to fill.
  ///   - proposal: The space offered by the parent.
  ///   - subviews: The views to place.
  ///   - cache: The layout cache; unused.
  public func placeSubviews(
    in bounds: CGRect,
    proposal: ProposedViewSize,
    subviews: Subviews,
    cache: inout ()
  ) {
    var y = bounds.minY
    for line in lines(for: subviews, width: bounds.width) {
      var x = bounds.minX
      for item in line.items {
        subviews[item.index].place(
          at: CGPoint(x: x, y: y),
          anchor: .topLeading,
          proposal: ProposedViewSize(width: item.size.width, height: item.size.height)
        )
        x += item.size.width + horizontalSpacing
      }
      y += line.height + verticalSpacing
    }
  }

  private struct Item {
    let index: Int
    let size: CGSize
  }

  private struct Line {
    var items: [Item] = []
    var width: CGFloat = 0
    var height: CGFloat = 0
  }

  private func lines(for subviews: Subviews, width: CGFloat) -> [Line] {
    var lines: [Line] = []
    var current = Line()
    for index in subviews.indices {
      let size = subviews[index].sizeThatFits(.unspecified)
      let needed =
        current.items.isEmpty ? size.width : current.width + horizontalSpacing + size.width
      if needed > width, !current.items.isEmpty {
        lines.append(current)
        current = Line()
      }
      current.width =
        current.items.isEmpty ? size.width : current.width + horizontalSpacing + size.width
      current.height = max(current.height, size.height)
      current.items.append(Item(index: index, size: size))
    }
    if !current.items.isEmpty { lines.append(current) }
    return lines
  }
}
