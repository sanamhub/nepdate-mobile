import CalendarKit
import Foundation
import NepDatePatro

/// The links About shows. The app opens them in the system browser; it never fetches them itself.
public enum AboutLinks {
  /// The known-data questions copy inside this repository.
  public static let knownData =
    "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/known-data-questions.md"

  /// The wrong-date issue form.
  public static let reportDate =
    "https://github.com/sanamhub/nepdate-mobile/issues/new?template=wrong-date.yml"

  /// The licences page.
  public static let licences =
    "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/legal/README.md"

  /// The privacy statement.
  public static let privacy =
    "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/privacy-policy.md"

  /// The source repository.
  public static let sourceCode = "https://github.com/sanamhub/nepdate-mobile"

  /// Empty until the hub publishes `holidays.ics` (ADR-0003).
  public static let holidaysICSURL = ""

  /// The five links the requirement names, in display order.
  public static let all = [knownData, reportDate, licences, privacy, sourceCode]

  /// Only repositories whose pages are public today.
  public static let allowedRepositories = ["sanamhub/nepdate-mobile"]

  /// Checks that a link points at a public repository page.
  /// - Parameter url: The link to check.
  /// - Returns: Why it may not appear, or `nil` when it is allowed.
  public static func problem(with url: String) -> String? {
    guard let parsed = URL(string: url), parsed.scheme == "https" else {
      return "not an https URL: \(url)"
    }
    guard parsed.host == "github.com" else {
      return "not github.com: \(url)"
    }
    if let port = parsed.port, port != 443 {
      return "unexpected port: \(url)"
    }
    let segments = parsed.path.split(separator: "/").map(String.init)
    guard segments.count >= 2 else { return "no repository: \(url)" }
    let repository = "\(segments[0])/\(segments[1])"
    guard allowedRepositories.contains(repository) else {
      return "repository is not on the public allow-list: \(repository)"
    }
    guard !segments.contains("..") else { return "path traversal: \(url)" }
    return nil
  }
}

/// The About screen's data.
public struct AboutInfo: Sendable, Hashable {
  /// The app version and build, for example `0.1.0 (1)`.
  public let appVersion: String

  /// The `NepDateLibraryVersion` Info.plist value.
  public let libraryVersion: String

  /// The iOS version.
  public let osVersion: String

  /// The first day of holiday data.
  public let coverageStart: MonthDate

  /// The last day of holiday data.
  public let coverageEnd: MonthDate

  /// Creates the About values.
  /// - Parameters:
  ///   - appVersion: The app version and build.
  ///   - libraryVersion: The Info.plist library version.
  ///   - osVersion: The iOS version.
  public init(appVersion: String, libraryVersion: String, osVersion: String) {
    let coverage = Patro.coverage
    self.appVersion = appVersion
    self.libraryVersion = libraryVersion
    self.osVersion = osVersion
    self.coverageStart = MonthDate(
      year: coverage.lowerBound.year,
      month: coverage.lowerBound.month,
      day: coverage.lowerBound.day
    )
    self.coverageEnd = MonthDate(
      year: coverage.upperBound.year,
      month: coverage.upperBound.month,
      day: coverage.upperBound.day
    )
  }

  /// The coverage span, for example `Baisakh 1, 2077 – Mangsir 20, 2084`.
  /// - Parameters:
  ///   - language: The language of the month names.
  ///   - digits: The resolved Digits setting.
  /// - Returns: The two long BS dates.
  public func coverageText(language: AppLanguage, digits: Digits) -> String {
    let start =
      "\(MonthNames.bs(coverageStart.month, language)) "
      + "\(digits.format(coverageStart.day)), \(digits.format(coverageStart.year))"
    let end =
      "\(MonthNames.bs(coverageEnd.month, language)) "
      + "\(digits.format(coverageEnd.day)), \(digits.format(coverageEnd.year))"
    return AppText.string("\(start) – \(end)", language)
  }
}

/// The webcal row and its injected URL.
public struct AboutLinksState: Sendable, Hashable {
  /// The injected subscription URL. Empty hides the row.
  public let holidaysURL: String

  /// Creates the state.
  /// - Parameter holidaysURL: The URL to inject; empty by default.
  public init(holidaysURL: String = AboutLinks.holidaysICSURL) {
    self.holidaysURL = holidaysURL
  }

  /// Whether the Subscribe row shows.
  public var showsSubscription: Bool {
    !holidaysURL.isEmpty
  }
}

/// The localised labels of the diagnostic text.
public struct DiagnosticLabels: Sendable, Hashable {
  /// The app label.
  public let app: String
  /// The iOS label.
  public let os: String
  /// The Date follows label.
  public let dateFollows: String
  /// The Digits label.
  public let digits: String
  /// The Show tithi label.
  public let tithi: String
  /// The on value.
  public let on: String
  /// The off value.
  public let off: String
  /// The This device value.
  public let device: String
  /// The Nepal time value.
  public let nepal: String
  /// The Follow language value.
  public let followLanguage: String
  /// The Nepali digits value.
  public let nepaliDigits: String
  /// The English digits value.
  public let englishDigits: String

  /// Creates the labels.
  /// - Parameters:
  ///   - app: The app label.
  ///   - os: The iOS label.
  ///   - dateFollows: The Date follows label.
  ///   - digits: The Digits label.
  ///   - tithi: The Show tithi label.
  ///   - on: The on value.
  ///   - off: The off value.
  ///   - device: The This device value.
  ///   - nepal: The Nepal time value.
  ///   - followLanguage: The Follow language value.
  ///   - nepaliDigits: The Nepali digits value.
  ///   - englishDigits: The English digits value.
  public init(
    app: String,
    os: String,
    dateFollows: String,
    digits: String,
    tithi: String,
    on: String,
    off: String,
    device: String,
    nepal: String,
    followLanguage: String,
    nepaliDigits: String,
    englishDigits: String
  ) {
    self.app = app
    self.os = os
    self.dateFollows = dateFollows
    self.digits = digits
    self.tithi = tithi
    self.on = on
    self.off = off
    self.device = device
    self.nepal = nepal
    self.followLanguage = followLanguage
    self.nepaliDigits = nepaliDigits
    self.englishDigits = englishDigits
  }
}

/// The text behind "Copy diagnostic info" (ADR-0007 §5): the app, iOS and the three settings.
public enum DiagnosticText {
  /// Builds the text. It contains no user, device, account or location data.
  /// - Parameters:
  ///   - appVersion: The app version and build.
  ///   - osVersion: The iOS version.
  ///   - settings: The chosen settings.
  ///   - labels: The localised labels.
  /// - Returns: The text to copy.
  public static func make(
    appVersion: String,
    osVersion: String,
    settings: Settings,
    labels: DiagnosticLabels
  ) -> String {
    let follows = settings.dateFollows == .nepal ? labels.nepal : labels.device
    let digits: String
    switch settings.digits {
    case .followLanguage: digits = labels.followLanguage
    case .nepali: digits = labels.nepaliDigits
    case .english: digits = labels.englishDigits
    }
    return [
      "\(labels.app): \(appVersion)",
      "\(labels.os): \(osVersion)",
      "\(labels.dateFollows): \(follows)",
      "\(labels.digits): \(digits)",
      "\(labels.tithi): \(settings.showTithi ? labels.on : labels.off)",
    ].joined(separator: "\n")
  }
}
