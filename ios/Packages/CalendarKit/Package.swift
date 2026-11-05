// swift-tools-version: 6.0
import PackageDescription

// nepdate-swift is pinned to an exact release (ADR-0003 §1, ios/README.md).
let package = Package(
  name: "CalendarKit",
  platforms: [.iOS(.v18)],
  products: [
    .library(name: "CalendarKit", targets: ["CalendarKit"]),
  ],
  dependencies: [
    .package(
      url: "https://github.com/sanamhub/nepdate-swift.git",
      exact: "1.0.0-rc.1"
    ),
  ],
  targets: [
    .target(
      name: "CalendarKit",
      dependencies: [
        .product(name: "NepDate", package: "nepdate-swift"),
        .product(name: "NepDatePatro", package: "nepdate-swift"),
      ]
    ),
  ],
  swiftLanguageModes: [.v6]
)
