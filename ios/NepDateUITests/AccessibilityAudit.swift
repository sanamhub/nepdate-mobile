import XCTest

/// The accessibility audit helper every UI test uses (ADR-0008).
///
/// The tool occasionally fails to finish inside its own time budget with the error code -56,
/// "Audit failed to complete in time", without reporting any finding; a second call passes. Only
/// that exact code is retried. Any other error and any finding still fails on the first call.
enum AccessibilityAudit {
  /// Runs one audit category.
  /// - Parameters:
  ///   - app: The app to audit.
  ///   - type: The category to run.
  ///   - handler: The finding handler; return `true` to tolerate a finding.
  /// - Throws: The audit's error, unless it is the retried -56 timeout.
  static func perform(
    on app: XCUIApplication,
    _ type: XCUIAccessibilityAuditType,
    handler: @escaping (XCUIAccessibilityAuditIssue) -> Bool
  ) throws {
    do {
      try app.performAccessibilityAudit(for: type, handler)
    } catch {
      let failure = error as NSError
      guard failure.code == -56 else { throw error }
      print(
        "AUDIT retry category=\(type.rawValue) domain=\(failure.domain) code=\(failure.code) "
          + "\(failure.localizedDescription)"
      )
      try app.performAccessibilityAudit(for: type, handler)
    }
  }
}
