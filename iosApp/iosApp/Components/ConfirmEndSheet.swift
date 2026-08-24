import SwiftUI
import Shared

/// The "End early?" confirmation - a scrim over the countdown with a bottom-anchored card
/// offering "Keep focusing" (primary) or "End the session" (ghost text).
struct ConfirmEndSheet: View {
    let bodyText: String
    let onKeepFocusing: () -> Void
    let onEndSession: () -> Void

    var body: some View {
        ZStack(alignment: .bottom) {
            Organic.colors.lockedSurfaceColor
                .organicAlpha(Organic.opacity.scrim)
                .ignoresSafeArea()

            VStack(alignment: .leading, spacing: 8) {
                Text("End early?")
                    .organicText(Organic.type.heading3)
                    .foregroundStyle(Organic.colors.textColor)
                Text(bodyText)
                    .organicText(Organic.type.body)
                    .foregroundStyle(Organic.colors.neutral.color700)
                    .padding(.bottom, 4)

                PillButton(text: "Keep focusing", action: onKeepFocusing, fullWidth: true)
                PillButton(
                    text: "End the session",
                    action: onEndSession,
                    variant: .ghost,
                    fullWidth: true,
                    containerColor: .clear,
                    contentColor: Organic.colors.neutral.color700
                )
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 24)
            .padding(.vertical, 26)
            .background(
                Organic.colors.bgColor,
                in: RoundedRectangle(cornerRadius: CGFloat(Organic.radius.lg) * 1.15, style: .continuous)
            )
            .padding(18)
        }
    }
}
