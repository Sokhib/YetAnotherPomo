import SwiftUI
import Shared

/// A labelled switch row for Settings ("Full-screen lock", "Long-press to break out", ...).
struct SwitchRow: View {
    let label: String
    let note: String
    let isOn: Bool
    let onToggle: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .organicText(Organic.type.label)
                    .foregroundStyle(Organic.colors.textColor)
                Text(note)
                    .organicText(Organic.type.bodySmall)
                    .foregroundStyle(Organic.colors.neutral.color600)
            }
            Spacer(minLength: 12)
            OrganicSwitch(isOn: isOn) { _ in onToggle() }
        }
        .frame(maxWidth: .infinity, minHeight: CGFloat(Organic.size.minTouchTarget), alignment: .leading)
        .padding(.vertical, 13)
        .padding(.horizontal, 8)
        .contentShape(Rectangle())
        .onTapGesture(perform: onToggle)
    }
}
