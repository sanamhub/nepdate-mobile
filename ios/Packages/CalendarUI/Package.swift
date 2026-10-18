// swift-tools-version: 6.0
import PackageDescription

// CalendarUI holds the SwiftUI views shared by the app and the widget extension. Calendar
// behaviour stays in CalendarKit, which wraps nepdate-swift (ADR-0004).
let package = Package(
  name: "CalendarUI",
  platforms: [.iOS(.v18)],
  products: [
    .library(name: "CalendarUI", targets: ["CalendarUI"]),
  ],
  dependencies: [
    .package(path: "../CalendarKit"),
  ],
  targets: [
    .target(
      name: "CalendarUI",
      dependencies: [
        .product(name: "CalendarKit", package: "CalendarKit"),
      ]
    ),
  ],
  swiftLanguageModes: [.v6]
)
