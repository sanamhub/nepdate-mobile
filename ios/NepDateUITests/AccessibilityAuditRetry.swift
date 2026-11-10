import Foundation

/// Runs one accessibility audit pass, and runs it once more only when XCTest reports that the audit
/// did not complete in time (com.apple.xcode.xctest.accessibilityAudit, code -56). That error comes
/// from a slow simulator, not from the app: the same commit passes on a re-run. Any other error, and
/// every finding, still fails the first time. [reset] undoes what the timed-out pass counted.
@MainActor
func retryingAuditTimeout(reset: () -> Void, _ body: () throws -> Void) throws {
  do {
    try body()
  } catch let error as NSError
    where error.domain == auditErrorDomain && error.code == auditTimeoutCode
  {
    print("AUDIT timed out, running it once more: \(error.localizedDescription)")
    reset()
    try body()
  }
}

private let auditErrorDomain = "com.apple.xcode.xctest.accessibilityAudit"
private let auditTimeoutCode = -56
