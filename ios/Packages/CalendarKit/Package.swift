// swift-tools-version: 6.0
import PackageDescription

// nepdate-swift has no release tag yet, and the apps must not create one. The dependency is
// therefore pinned by revision: the main commit of 2026-10-18. Switch to an exact version pin
// when 0.1.0 is released (ADR-0003 §1, ios/README.md).
let package = Package(
  name: "CalendarKit",
  platforms: [.iOS(.v18)],
  products: [
    .library(name: "CalendarKit", targets: ["CalendarKit"]),
  ],
  dependencies: [
    .package(
      url: "https://github.com/sanamhub/nepdate-swift.git",
      revision: "708cd3626ecb22abbb90a3f1d1adc05e778db6d5"
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
