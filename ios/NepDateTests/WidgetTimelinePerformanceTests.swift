import CalendarKit
import Darwin
import Foundation
import XCTest

/// I2-05: building the 7-day month widget timeline stays small. The number is a regression signal;
/// the device peak with every widget placed is a manual check (`docs/benchmarks.md`).
final class WidgetTimelinePerformanceTests: XCTestCase {
  func testMemory() throws {
    let settings = Settings(language: .nepali, digits: .nepali, dateFollows: .nepal)
    let now = Date(timeIntervalSince1970: 1_791_699_300)
    let options = XCTMeasureOptions()
    options.iterationCount = 5

    let before = Self.residentBytes()
    measure(metrics: [XCTMemoryMetric()], options: options) {
      let entries = MonthWidgetTimeline.entries(
        now: now,
        settings: settings,
        deviceZone: CalendarDates.nepal
      )
      XCTAssertEqual(entries.count, 8)
    }
    let after = Self.residentBytes()
    print("MEMORY peak resident before \(before) bytes, after \(after) bytes, 8 month entries")
  }

  /// The peak resident size of the test process so far, in bytes.
  private static func residentBytes() -> UInt64 {
    var usage = rusage()
    guard getrusage(RUSAGE_SELF, &usage) == 0 else { return 0 }
    return UInt64(usage.ru_maxrss)
  }
}
