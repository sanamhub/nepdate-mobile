import CalendarKit
import CalendarScreens
import Foundation
import SwiftUI
import Testing
import UIKit

/// The pager's named VoiceOver actions (MR-01 AC5), read from the accessibility tree of the real
/// screen rather than from a copy of its rules. A month with both neighbours offers Previous month
/// and Next month, the first month offers only Next month and the last only Previous month, so the
/// rotor never offers a month the picker range does not have.
///
/// The screen is hosted in a window and its accessibility elements are walked, because the custom
/// actions are not part of the UIKit view tree on their own: `UIHostingController` builds them for
/// the accessibility clients.
@MainActor
@Suite("Month pager accessibility actions")
struct MonthPagerAccessibilityTests {
  private static let today = MonthDate(year: 2083, month: 6, day: 20)

  @Test("a month with both neighbours offers both actions")
  func bothNeighbours() throws {
    let names = try Self.actionNames(month: MonthKey(year: 2199, month: 11))
    #expect(names == ["Next month", "Previous month"])
  }

  @Test("the first month offers only Next month")
  func startOfRange() throws {
    #expect(try Self.actionNames(month: MonthKey(year: 1901, month: 1)) == ["Next month"])
  }

  @Test("the last month offers only Previous month")
  func endOfRange() throws {
    #expect(try Self.actionNames(month: MonthKey(year: 2199, month: 12)) == ["Previous month"])
  }

  /// The names of the custom actions in the hosted screen's accessibility tree, sorted.
  private static func actionNames(month: MonthKey?) throws -> [String] {
    let model = try #require(
      MonthViewModel(
        today: today,
        language: .english,
        digits: .english,
        showTithi: false,
        month: month
      )
    )
    let controller = UIHostingController(
      rootView: NavigationStack {
        MonthScreen(model: model, settings: Settings(), today: today)
      }
    )
    let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 402, height: 874))
    controller.view.frame = window.bounds
    window.rootViewController = controller
    window.makeKeyAndVisible()
    window.layoutIfNeeded()
    // SwiftUI builds the month's pages and their accessibility elements over the next run loop
    // turn, so the walk below waits one out.
    RunLoop.main.run(until: Date(timeIntervalSinceNow: 0.5))

    let tree = nodes(in: controller.view)
    var names: Set<String> = []
    for node in tree {
      for action in node.accessibilityCustomActions ?? [] {
        print(
          "PAGER action=\(action.name) on=\(type(of: node)) "
            + "identifier=\(identifier(of: node) ?? "") label=\(node.accessibilityLabel ?? "")"
        )
        names.insert(action.name)
      }
    }
    let identified = tree.compactMap { node -> String? in
      guard let identifier = identifier(of: node), !identifier.isEmpty else { return nil }
      return "\(type(of: node)) \(identifier)"
    }
    print("PAGER nodes \(identified)")
    print("PAGER actions \(names.sorted())")
    return names.sorted()
  }

  /// Every object in the view's UIKit tree and its accessibility elements, parents before children.
  private static func nodes(in root: UIView) -> [NSObject] {
    var result: [NSObject] = []
    var seen: Set<ObjectIdentifier> = []

    func add(_ object: NSObject) -> Bool {
      seen.insert(ObjectIdentifier(object)).inserted
    }

    func visit(_ view: UIView, _ depth: Int) {
      guard depth < 60, add(view) else { return }
      result.append(view)
      for subview in view.subviews {
        visit(subview, depth + 1)
      }
      for element in accessibilityElements(of: view) where add(element) {
        result.append(element)
      }
    }

    visit(root, 0)
    return result
  }

  /// The elements a view holds. A container reports them as an array or through the count methods;
  /// the accessibility tree uses the second when the first is empty.
  private static func accessibilityElements(of view: UIView) -> [NSObject] {
    if let elements = view.accessibilityElements {
      return elements.compactMap { $0 as? NSObject }
    }
    let count = view.accessibilityElementCount()
    guard count > 0 else { return [] }
    return (0..<count).compactMap { view.accessibilityElement(at: $0) as? NSObject }
  }

  private static func identifier(of node: NSObject) -> String? {
    (node as? UIAccessibilityIdentification)?.accessibilityIdentifier
  }
}
