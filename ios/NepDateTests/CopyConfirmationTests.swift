@testable import CalendarScreens
import Testing

/// The day sheet's copy confirmation: a later copy gets its full interval.
@MainActor
@Suite("Copy confirmation")
struct CopyConfirmationTests {
  @Test("a second copy keeps the confirmation visible after the first interval ends")
  func secondCopyKeepsTheConfirmation() async throws {
    let gate = SleepGate()
    let confirmation = CopyConfirmationTimer(
      duration: .seconds(1),
      sleep: { _ in try await gate.sleep() }
    )

    confirmation.show()
    await gate.waitForSleepers(1)
    let first = try #require(confirmation.hideTask)

    confirmation.show()
    await gate.waitForSleepers(2)

    await gate.releaseNext()
    await first.value
    #expect(confirmation.isShowing)

    await gate.releaseNext()
    await confirmation.hideTask?.value
    #expect(!confirmation.isShowing)
  }
}

/// Holds each sleep until the test releases it, so the timer's cancellation is tested without
/// waiting for a real interval.
private actor SleepGate {
  private var continuations: [CheckedContinuation<Void, Error>] = []
  private var waiters: [CheckedContinuation<Void, Never>] = []

  func sleep() async throws {
    try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
      continuations.append(continuation)
      resumeWaiters()
    }
  }

  func waitForSleepers(_ count: Int) async {
    while continuations.count < count {
      await withCheckedContinuation { (waiter: CheckedContinuation<Void, Never>) in
        waiters.append(waiter)
      }
    }
  }

  func releaseNext() {
    guard !continuations.isEmpty else { return }
    continuations.removeFirst().resume()
  }

  private func resumeWaiters() {
    let current = waiters
    waiters.removeAll()
    for waiter in current {
      waiter.resume()
    }
  }
}
