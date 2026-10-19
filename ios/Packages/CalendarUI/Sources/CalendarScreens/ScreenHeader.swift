import SwiftUI

/// A tab screen's title and Settings button. The month screen has its own header.
struct ScreenHeader: View {
  let title: LocalizedStringKey
  let onSettings: (() -> Void)?

  var body: some View {
    HStack(alignment: .center, spacing: 8) {
      Text(title)
        .font(.largeTitle)
        .fontWeight(.bold)
        .fixedSize(horizontal: false, vertical: true)
        .accessibilityAddTraits(.isHeader)

      Spacer(minLength: 4)

      if let onSettings {
        Button(action: onSettings) {
          Image(systemName: "gearshape")
            .font(.body.weight(.semibold))
            .frame(width: 44, height: 44)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text("Settings"))
      }
    }
    .padding(.horizontal, 16)
    .padding(.top, 8)
    .padding(.bottom, 4)
  }
}
