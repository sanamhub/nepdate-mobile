import CalendarKit
import CalendarUI
import SwiftUI
import UIKit

/// The first-run screen (MR-15): today's BS day, the six promises, the Appearance choice, the
/// Language row and the button that opens the patro.
///
/// The screen is shown once, from a full-screen cover. It takes the day as a value, so a test and
/// a snapshot pin Asoj 25, 2083 and never read the clock.
public struct FirstRunScreen: View {
  @State private var appearance: Appearance
  @ScaledMetric(relativeTo: .largeTitle) private var markSize: CGFloat = 88

  private let settings: Settings
  private let today: MonthDate
  private let onFinish: (Settings) -> Void
  private let opener: SettingsURLOpener
  @Environment(\.dynamicTypeSize) private var dynamicTypeSize

  /// Creates the screen.
  /// - Parameters:
  ///   - settings: The settings to start from.
  ///   - today: The day drawn on the Sunny mark.
  ///   - onFinish: Called with the settings to save when the screen closes.
  ///   - opener: Opens the system per-app Language page.
  @MainActor
  public init(
    settings: Settings,
    today: MonthDate,
    onFinish: @escaping (Settings) -> Void,
    opener: SettingsURLOpener = SettingsURLOpener()
  ) {
    self.settings = settings
    self.today = today
    self.onFinish = onFinish
    self.opener = opener
    _appearance = State(initialValue: settings.appearance)
  }

  /// The screen's content.
  public var body: some View {
    ScrollView {
      VStack(spacing: dynamicTypeSize.isAccessibilitySize ? 20 : 14) {
        header
        promises
        appearanceChoice
        languageRow
        openButton
      }
      .padding(.horizontal, 20)
      .padding(.vertical, 16)
      .frame(maxWidth: .infinity)
    }
    .background(Color(uiColor: .systemBackground))
    .preferredColorScheme(appearance.colorScheme)
    .accessibilityIdentifier("first_run")
  }

  private var header: some View {
    VStack(spacing: 10) {
      SunnyShape()
        .fill(BrandColor.today)
        .frame(width: markSize, height: markSize)
        .overlay {
          Text(verbatim: digits.format(today.day))
            .font(BrandType.numerals(.largeTitle).weight(.bold))
            .foregroundStyle(BrandColor.onToday)
            .minimumScaleFactor(0.6)
        }
        .accessibilityHidden(true)

      Text("A light, fast patro of your own")
        .font(.title2.weight(.bold))
        .multilineTextAlignment(.center)
        .fixedSize(horizontal: false, vertical: true)
        .accessibilityAddTraits(.isHeader)
    }
  }

  private var promises: some View {
    let promises = AppPromises.all
    return LazyVGrid(columns: columns, alignment: .leading, spacing: 10) {
      ForEach(promises.indices, id: \.self) { index in
        HStack(alignment: .firstTextBaseline, spacing: 8) {
          Image(systemName: promises[index].symbol)
            .font(.body)
            .foregroundStyle(BrandColor.accent)
            .frame(width: 24, alignment: .center)
            .accessibilityHidden(true)
          Text(promises[index].title)
            .font(.subheadline)
            .fixedSize(horizontal: false, vertical: true)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
      }
    }
    .accessibilityElement(children: .contain)
  }

  private var columns: [GridItem] {
    let count = dynamicTypeSize.isAccessibilitySize ? 1 : 2
    return Array(repeating: GridItem(.flexible(), alignment: .top), count: count)
  }

  private var appearanceChoice: some View {
    VStack(alignment: .leading, spacing: 6) {
      Text("Appearance")
        .font(.subheadline)
        .foregroundStyle(BrandColor.secondaryText)
      Picker("Appearance", selection: $appearance) {
        Text("System").tag(Appearance.system)
        Text("Light").tag(Appearance.light)
        Text("Dark").tag(Appearance.dark)
      }
      .pickerStyle(.segmented)
      .accessibilityIdentifier("first_run_appearance")
    }
    .frame(maxWidth: .infinity, alignment: .leading)
  }

  private var languageRow: some View {
    Button {
      opener.openAppSettings()
    } label: {
      HStack(spacing: 8) {
        Text("Language")
        Spacer(minLength: 8)
        Text(verbatim: languageName)
          .foregroundStyle(BrandColor.secondaryText)
        Image(systemName: "chevron.right")
          .font(.caption.weight(.semibold))
          .foregroundStyle(BrandColor.secondaryText)
      }
      .frame(minHeight: 44)
      .contentShape(Rectangle())
      .accessibilityHidden(true)
    }
    .buttonStyle(.plain)
    .foregroundStyle(.primary)
    .accessibilityLabel(Text("Language"))
    .accessibilityValue(Text(verbatim: languageName))
    .accessibilityIdentifier("first_run_language")
  }

  private var openButton: some View {
    Button {
      var updated = settings
      updated.appearance = appearance
      onFinish(updated)
    } label: {
      Text("Open the patro")
        .frame(maxWidth: .infinity, minHeight: 50)
    }
    .buttonStyle(.borderedProminent)
    .tint(BrandColor.accent)
    .foregroundStyle(BrandColor.onAccent)
    .accessibilityIdentifier("first_run_open")
  }

  private var language: AppLanguage {
    settings.language.resolved
  }

  private var digits: Digits {
    settings.digits.resolved(for: language)
  }

  private var languageName: String {
    switch language {
    case .english: return "English"
    case .nepali: return "नेपाली"
    }
  }

}
