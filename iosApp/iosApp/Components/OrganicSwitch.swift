import SwiftUI
import Shared

/// The design's 50x30 pill switch (`.knob` translateX(20px) when on), matching the Settings rows.
struct OrganicSwitch: View {
    let isOn: Bool
    let onToggle: (Bool) -> Void

    var body: some View {
        let track = isOn ? Organic.colors.accentColor : Organic.colors.neutral.color300

        ZStack(alignment: .leading) {
            Capsule().fill(track)
            Circle()
                .fill(Organic.colors.bgColor)
                .frame(width: 24, height: 24)
                .shadow(radius: 2, y: 1)
                .offset(x: isOn ? 23 : 3)
        }
        .frame(width: 50, height: 30)
        .animation(.easeInOut(duration: Organic.motion.standard.seconds), value: isOn)
        .contentShape(Rectangle())
        .onTapGesture { onToggle(!isOn) }
        // `.isToggle` needs iOS 17; the deployment target here is 16.
        .accessibilityAddTraits(.isButton)
        .accessibilityValue(isOn ? "On" : "Off")
    }
}
