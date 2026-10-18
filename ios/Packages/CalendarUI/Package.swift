// swift-tools-version: 6.0
import PackageDescription

// CalendarUI holds the SwiftUI views shared by the app and the widget extension. Calendar
// behaviour stays in CalendarKit, which wraps nepdate-swift (ADR-0004).
//
// CalendarScreens holds the app-only screens (the month pager, header and picker). It is a
// separate product so the widget extension, which links only CalendarUI, does not carry them:
// Swift type metadata keeps every public view alive in a static library, so dead stripping
// cannot drop them from a binary that links the whole module.
let package = Package(
  name: "CalendarUI",
  platforms: [.iOS(.v18)],
  products: [
    .library(name: "CalendarUI", targets: ["CalendarUI"]),
    .library(name: "CalendarScreens", targets: ["CalendarScreens"]),
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
    .target(
      name: "CalendarScreens",
      dependencies: [
        "CalendarUI",
        .product(name: "CalendarKit", package: "CalendarKit"),
      ]
    ),
  ],
  swiftLanguageModes: [.v6]
)
