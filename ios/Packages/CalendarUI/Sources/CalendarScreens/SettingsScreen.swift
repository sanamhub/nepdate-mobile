import CalendarKit
import CalendarUI
import SwiftUI
import UIKit

/// Settings and About (MR-06, MR-07, MR-11, MR-12).
public struct SettingsScreen: View {
  @State private var model: SettingsViewModel
  private let info: AboutInfo
  private let links: AboutLinksState
  private let onChanged: (Settings) -> Void
  private let opener: SettingsURLOpener
  @Environment(\.dismiss) private var dismiss
  @Environment(\.colorScheme) private var colorScheme

  /// Creates the screen.
  /// - Parameters:
  ///   - model: The settings and their store.
  ///   - info: The About values.
  ///   - links: The injected subscription state.
  ///   - onChanged: Called after every setting change.
  ///   - opener: Opens the app settings URL.
  @MainActor
  public init(
    model: SettingsViewModel,
    info: AboutInfo,
    links: AboutLinksState = AboutLinksState(),
    onChanged: @escaping (Settings) -> Void = { _ in },
    opener: SettingsURLOpener = SettingsURLOpener()
  ) {
    _model = State(initialValue: model)
    self.info = info
    self.links = links
    self.onChanged = onChanged
    self.opener = opener
  }

  /// The screen's content.
  public var body: some View {
    NavigationStack {
      SettingsContent(
        model: $model,
        info: info,
        links: links,
        onChanged: onChanged,
        opener: opener
      )
      .navigationTitle("Settings")
      .navigationBarTitleDisplayMode(.inline)
      .toolbar {
        ToolbarItem(placement: .principal) {
          Text("Settings")
            .font(.headline)
            .foregroundStyle(.primary)
        }
        ToolbarItem(placement: .confirmationAction) {
          Button {
            dismiss()
          } label: {
            Text("Close")
              .font(.body)
          }
        }
      }
      .tint(BrandColor.today)
      .toolbarBackground(Color(uiColor: .systemBackground), for: .navigationBar)
      .toolbarBackground(.visible, for: .navigationBar)
      .toolbarColorScheme(colorScheme == .dark ? .dark : .light, for: .navigationBar)
    }
  }
}

/// The Settings list without its navigation, so snapshots can render it.
public struct SettingsContent: View {
  @Binding private var model: SettingsViewModel
  private let info: AboutInfo
  private let links: AboutLinksState
  private let onChanged: (Settings) -> Void
  private let opener: SettingsURLOpener

  /// Creates the content.
  /// - Parameters:
  ///   - model: The settings and their store.
  ///   - info: The About values.
  ///   - links: The injected subscription state.
  ///   - onChanged: Called after every setting change.
  ///   - opener: Opens the app settings URL.
  @MainActor
  public init(
    model: Binding<SettingsViewModel>,
    info: AboutInfo,
    links: AboutLinksState = AboutLinksState(),
    onChanged: @escaping (Settings) -> Void = { _ in },
    opener: SettingsURLOpener = SettingsURLOpener()
  ) {
    _model = model
    self.info = info
    self.links = links
    self.onChanged = onChanged
    self.opener = opener
  }

  /// The content.
  public var body: some View {
    List {
      Section {
        Button {
          opener.openAppSettings()
        } label: {
          HStack {
            Text("Language")
            Spacer(minLength: 8)
            Text(verbatim: languageName)
              .foregroundStyle(BrandColor.secondaryText)
            Image(systemName: "chevron.right")
              .font(.caption.weight(.semibold))
              .foregroundStyle(BrandColor.secondaryText)
          }
          .contentShape(Rectangle())
        }
        .accessibilityLabel(Text("Language"))
        .accessibilityValue(Text(verbatim: languageName))
        .accessibilityIdentifier("language_row")
        .buttonStyle(.plain)
        .foregroundStyle(.primary)
      }

      Section {
        sectionHeader("Digits")
        choice("Follow language", selected: model.settings.digits == .followLanguage) {
          model.setDigits(.followLanguage)
          onChanged(model.settings)
        }
        choice("Nepali (०१२)", selected: model.settings.digits == .nepali) {
          model.setDigits(.nepali)
          onChanged(model.settings)
        }
        choice("English (012)", selected: model.settings.digits == .english) {
          model.setDigits(.english)
          onChanged(model.settings)
        }
      }

      Section {
        sectionHeader("Date follows")
        choice("This device", selected: model.settings.dateFollows == .device) {
          model.setDateFollows(.device)
          onChanged(model.settings)
        }
        choice("Nepal time", selected: model.settings.dateFollows == .nepal) {
          model.setDateFollows(.nepal)
          onChanged(model.settings)
        }
      }

      Section {
        Toggle("Show tithi in the month grid", isOn: showTithi)
          .frame(minHeight: 44)
      }

      Section {
        NavigationLink {
          AboutScreen(
            content: AboutContent(
              info: info,
              language: model.settings.language.resolved,
              digits: resolvedDigits,
              settings: model.settings,
              links: links
            )
          )
        } label: {
          Text("About")
            .foregroundStyle(.primary)
        }
        .accessibilityIdentifier("about_link")
      }
    }
    .listStyle(.insetGrouped)
    .tint(BrandColor.today)
  }

  private var resolvedDigits: Digits {
    model.settings.digits.resolved(for: model.settings.language.resolved)
  }

  private var languageName: String {
    switch model.settings.language.resolved {
    case .english: return "English"
    case .nepali: return "नेपाली"
    }
  }

  private var showTithi: Binding<Bool> {
    Binding(
      get: { model.settings.showTithi },
      set: { value in
        model.setShowTithi(value)
        onChanged(model.settings)
      }
    )
  }

  private func choice(
    _ label: LocalizedStringKey,
    selected: Bool,
    action: @escaping () -> Void
  ) -> some View {
    Button(action: action) {
      HStack {
        Text(label)
        Spacer(minLength: 8)
        if selected {
          Image(systemName: "checkmark")
            .font(.body.weight(.semibold))
            .foregroundStyle(BrandColor.today)
        }
      }
      .frame(minHeight: 44)
      .contentShape(Rectangle())
    }
    .buttonStyle(.plain)
    .foregroundStyle(.primary)
    .accessibilityAddTraits(selected ? .isSelected : [])
  }

  private func sectionHeader(_ title: LocalizedStringKey) -> some View {
    Text(title)
      .font(.headline)
      .foregroundStyle(.primary)
      .frame(minHeight: 44)
      .accessibilityAddTraits(.isHeader)
  }
}

/// The About screen with its navigation title.
public struct AboutScreen: View {
  private let content: AboutContent
  @Environment(\.colorScheme) private var colorScheme

  /// Creates the screen.
  /// - Parameter content: The About values and links.
  public init(content: AboutContent) {
    self.content = content
  }

  /// The screen's content.
  public var body: some View {
    content
      .navigationTitle("About")
      .navigationBarTitleDisplayMode(.inline)
      .toolbar {
        ToolbarItem(placement: .principal) {
          Text("About")
            .font(.headline)
            .foregroundStyle(.primary)
        }
      }
      .toolbarBackground(Color(uiColor: .systemBackground), for: .navigationBar)
      .toolbarBackground(.visible, for: .navigationBar)
      .toolbarColorScheme(colorScheme == .dark ? .dark : .light, for: .navigationBar)
  }
}

/// The About list without its navigation.
public struct AboutContent: View {
  private let info: AboutInfo
  private let language: AppLanguage
  private let digits: Digits
  private let settings: Settings
  private let links: AboutLinksState
  @State private var copied: CopyConfirmationTimer
  @Environment(\.openURL) private var openURL

  /// Creates the content.
  /// - Parameters:
  ///   - info: The version and coverage values.
  ///   - language: The language of the values.
  ///   - digits: The resolved Digits setting.
  ///   - settings: The settings the diagnostic text describes.
  ///   - links: The injected subscription state.
  @MainActor
  public init(
    info: AboutInfo,
    language: AppLanguage,
    digits: Digits,
    settings: Settings,
    links: AboutLinksState = AboutLinksState()
  ) {
    self.info = info
    self.language = language
    self.digits = digits
    self.settings = settings
    self.links = links
    _copied = State(initialValue: CopyConfirmationTimer())
  }

  /// The content.
  public var body: some View {
    List {
      Section {
        sectionHeader("About")
        infoRow("App version", info.appVersion)
        infoRow("Library version", info.libraryVersion)
        infoRow("Holiday data", info.coverageText(language: language, digits: digits))
        infoRow("Data source", "C# NepDate via the hub (MIT)")
        infoRow(
          "Official sources",
          "Ministry of Home Affairs holiday notice; Nepal Panchanga Nirnayak Samiti"
        )
      }

      Section {
        link("Known data questions", AboutLinks.knownData)
        link("Report a wrong date", AboutLinks.reportDate)
        link("Licences", AboutLinks.licences)
        link("Privacy", AboutLinks.privacy)
        link("Source code", AboutLinks.sourceCode)
      }

      if links.showsSubscription {
        Section {
          Button(action: openSubscription) {
            Text("Subscribe to Nepal holidays")
              .frame(minHeight: 44)
          }
        }
      }

      Section {
        Button(action: copyDiagnostic) {
          Text("Copy diagnostic info")
            .frame(minHeight: 44)
        }
        if copied.isShowing {
          Text("Copied")
            .font(.footnote)
            .foregroundStyle(BrandColor.secondaryText)
            .accessibilityIdentifier("diagnostic_copied")
        }
      }
    }
    .listStyle(.insetGrouped)
    .tint(BrandColor.today)
    .navigationTitle("About")
    .navigationBarTitleDisplayMode(.inline)
  }

  private func infoRow(_ label: LocalizedStringKey, _ value: String) -> some View {
    VStack(alignment: .leading, spacing: 2) {
      Text(label)
        .font(.body)
        .foregroundStyle(.primary)
        .fixedSize(horizontal: false, vertical: true)
      Text(verbatim: value)
        .font(.subheadline)
        .foregroundStyle(BrandColor.secondaryText)
        .fixedSize(horizontal: false, vertical: true)
    }
  }

  private func link(_ label: LocalizedStringKey, _ url: String) -> some View {
    Group {
      if let destination = URL(string: url) {
        Link(label, destination: destination)
          .foregroundStyle(BrandColor.today)
      } else {
        Text(label)
      }
    }
    .frame(minHeight: 44)
  }

  private func sectionHeader(_ title: LocalizedStringKey) -> some View {
    Text(title)
      .font(.headline)
      .foregroundStyle(.primary)
      .frame(minHeight: 44)
      .accessibilityAddTraits(.isHeader)
  }

  private func openSubscription() {
    guard let url = URL(string: links.holidaysURL) else { return }
    openURL(url)
  }

  private func copyDiagnostic() {
    let labels = DiagnosticLabels(
      app: AppText.string("App", language),
      os: AppText.string("iOS", language),
      dateFollows: AppText.string("Date follows", language),
      digits: AppText.string("Digits", language),
      tithi: AppText.string("Show tithi", language),
      on: AppText.string("on", language),
      off: AppText.string("off", language),
      device: AppText.string("This device", language),
      nepal: AppText.string("Nepal time", language),
      followLanguage: AppText.string("Follow language", language),
      nepaliDigits: AppText.string("Nepali", language),
      englishDigits: AppText.string("English", language)
    )
    let text = DiagnosticText.make(
      appVersion: info.appVersion,
      osVersion: info.osVersion,
      settings: settings,
      labels: labels
    )
    DayClipboard.write(text)
    CopyConfirmation.announce(AppText.string("Copied", language))
    copied.show()
  }
}
