import SwiftUI

/// One of the six promises the app makes (MR-15 AC1): a symbol and a short label.
struct AppPromise {
  /// The SF Symbol.
  let symbol: String

  /// The label.
  let title: LocalizedStringKey
}

/// The six promises, shown on the first-run screen and in About.
enum AppPromises {
  /// The promises, in display order.
  static var all: [AppPromise] {
    [
      AppPromise(symbol: "wifi.slash", title: "Works offline"),
      AppPromise(symbol: "nosign", title: "No ads"),
      AppPromise(symbol: "lock.shield.fill", title: "Collects no data"),
      AppPromise(symbol: "chevron.left.forwardslash.chevron.right", title: "Free and open source"),
      AppPromise(symbol: "bolt.fill", title: "Fast and light"),
      AppPromise(symbol: "character.bubble.fill", title: "Nepali and English"),
    ]
  }
}
