import CalendarScreens
import Testing

/// I1-04: the copy action writes one plain-text item to the system pasteboard (MR-02 AC3).
///
/// This runs in the app process, where iOS allows the write and the read. The XCUITest runner's
/// own pasteboard read waits on the system paste prompt, so the UI test checks the action's
/// accessibility value instead.
@MainActor
@Suite("Day clipboard")
struct DayClipboardTests {
  @Test("MR-02 AC3: the pasteboard holds the BS date in Devanagari")
  func copyText() {
    DayClipboard.write("असोज ९, २०८३")

    #expect(DayClipboard.read() == "असोज ९, २०८३")
  }

  @Test("MR-02 AC3: the pasteboard holds an ASCII date too")
  func copyEnglishText() {
    DayClipboard.write("Asoj 9, 2083")

    #expect(DayClipboard.read() == "Asoj 9, 2083")
  }
}
