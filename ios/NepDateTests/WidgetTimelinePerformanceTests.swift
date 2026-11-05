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
    print("MEMORY resident before \(before) bytes, after \(after) bytes, 8 month entries")
  }

  private static func residentBytes() -> UInt64 {
    var info = mach_task_basic_info()
    var count = mach_msg_type_number_t(
      MemoryLayout<mach_task_basic_info>.size / MemoryLayout<natural_t>.size
    )
    let result = withUnsafeMutablePointer(to: &info) { pointer in
      pointer.withMemoryRebound(to: integer_t.self, capacity: Int(count)) { raw in
        task_info(mach_task_self(), task_flavor_t(MACH_TASK_BASIC_INFO), raw, &count)
      }
    }
    return result == KERN_SUCCESS ? UInt64(info.resident_size) : 0
  }
}
