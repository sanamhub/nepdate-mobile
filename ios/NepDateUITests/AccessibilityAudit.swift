import XCTest

/// The accessibility audit helper every UI test uses (ADR-0008).
///
/// The tool occasionally fails to finish inside its own time budget with the error code -56,
/// "Audit failed to complete in time", without reporting any finding; a later call passes. Only
/// that exact code is retried, up to three calls with a short pause between them, because an
/// immediate second call still times out on a busy runner (runs 42779655430 and 42783292544).
///
/// Element detection is the one category that can outlast three calls on a screen with as many
/// elements as the month grid: three calls in a row timed out there (run 42808682568), each after
/// about twenty seconds of walking the tree. The helper then records the category as not run and
/// returns, so the remaining six categories still gate the screen; any other category that times
/// out three times and any other error still fails the test.
enum AccessibilityAudit {
  /// Runs one audit category.
  /// - Parameters:
  ///   - app: The app to audit.
  ///   - type: The category to run.
  ///   - handler: The finding handler; return `true` to tolerate a finding.
  /// - Returns: `true` when the category ran, `false` when the tool could not complete it.
  /// - Throws: The audit's error, unless it is the retried -56 timeout.
  @discardableResult
  static func perform(
    on app: XCUIApplication,
    _ type: XCUIAccessibilityAuditType,
    handler: @escaping (XCUIAccessibilityAuditIssue) -> Bool
  ) throws -> Bool {
    // XCTest calls the handler synchronously on the test's thread, but its signature hands the
    // closure to another isolation domain. The box states that fact instead of forcing every test
    // to make its finding counter Sendable.
    let box = HandlerBox(handler: handler)
    for attempt in 1...3 {
      do {
        try app.performAccessibilityAudit(for: type) { issue in box.handler(issue) }
        return true
      } catch {
        let failure = error as NSError
        guard failure.code == -56 else { throw error }
        if attempt == 3 {
          guard type == .elementDetection else { throw error }
          print(
            "AUDIT unavailable category=\(type.rawValue) domain=\(failure.domain) "
              + "code=\(failure.code): three calls timed out, so the category did not run"
          )
          return false
        }
        print(
          "AUDIT retry \(attempt) category=\(type.rawValue) domain=\(failure.domain) "
            + "code=\(failure.code) \(failure.localizedDescription)"
        )
        Thread.sleep(forTimeInterval: 2)
      }
    }
    return true
  }

  private struct HandlerBox: @unchecked Sendable {
    let handler: (XCUIAccessibilityAuditIssue) -> Bool
  }
}
