import SnapshotTesting
import SwiftUI
import Testing
import UIKit

/// A throwaway test that proves `SnapshotTesting` links into the hosted test bundle. It records a
/// UIKit snapshot of a `UIHostingController`, which is the strategy the month screen snapshots will
/// use, rather than the library's SwiftUI `Snapshotting where Value: View` overload.
@MainActor
@Suite("Snapshot link")
struct SnapshotLinkTests {
  @Test("a hosting controller snapshots")
  func hostingController() {
    let view = Text("Asoj 2083")
      .frame(width: 390, height: 300)
      .background(Color.white)
    let controller = UIHostingController(rootView: view)
    assertSnapshot(
      of: controller,
      as: .image(
        size: CGSize(width: 390, height: 300),
        traits: UITraitCollection(userInterfaceStyle: .light)
      ),
      record: .all
    )
  }
}
