import UIKit
import UniformTypeIdentifiers

/// The system pasteboard write and read used by "Copy date" (MR-02 AC3).
///
/// The read is only for the unit test: it proves the write lands as one plain-text item in the
/// app's own process. The UI test reads the action's accessibility value instead, because the
/// XCUITest runner's own pasteboard read waits on the system paste prompt.
public enum DayClipboard {
  /// Writes the text as one plain-text pasteboard item.
  /// - Parameter text: The BS date in the app language.
  public static func write(_ text: String) {
    UIPasteboard.general.setItems(
      [[UTType.plainText.identifier: text]],
      options: [:]
    )
  }

  /// The pasteboard's plain text, if any.
  /// - Returns: The current string, or `nil`.
  public static func read() -> String? {
    UIPasteboard.general.string
  }
}
