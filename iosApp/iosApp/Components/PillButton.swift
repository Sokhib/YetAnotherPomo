import SwiftUI
import Shared

enum PillButtonVariant { case primary, secondary, ghost }

/// The design's `.btn` pill (border-radius: 999px), matching Home's "Begin" button and the
/// confirm-sheet actions. The colour overrides let callers reproduce the doc's per-state
/// combinations (selected vs unselected duration presets) without a combinatorial explosion of
/// variants - same contract as `PillButton.kt`.
struct PillButton: View {
    let text: String
    let action: () -> Void
    var variant: PillButtonVariant = .primary
    var fullWidth: Bool = false
    var containerColor: Color? = nil
    var contentColor: Color? = nil
    var borderColor: Color? = nil
    var enabled: Bool = true

    @State private var pressed = false

    private var resolvedContainer: Color {
        containerColor ?? (variant == .primary ? Organic.colors.accentColor : .clear)
    }
    private var resolvedContent: Color {
        contentColor ?? (variant == .primary ? Organic.colors.bgColor : Organic.colors.neutral.color700)
    }
    private var resolvedBorder: Color? {
        borderColor ?? (variant == .secondary ? Organic.colors.dividerColor : nil)
    }
    private var pressedContainer: Color {
        guard pressed else { return resolvedContainer }
        return variant == .primary ? Organic.colors.accentRamp.color700 : Organic.colors.neutral.color200
    }

    var body: some View {
        let shape = Capsule()

        Text(text)
            // The Compose original overrides the label token with the display face at 17sp.
            .font(.custom(OrganicFonts.display, fixedSize: 17))
            .foregroundStyle(resolvedContent.organicAlpha(
                enabled ? Organic.opacity.opaque : Organic.opacity.muted
            ))
            .frame(maxWidth: fullWidth ? .infinity : nil)
            .padding(.vertical, 19)
            .padding(.horizontal, 24)
            .background(pressedContainer, in: shape)
            .overlay {
                if let border = resolvedBorder {
                    shape.strokeBorder(border, lineWidth: CGFloat(Organic.stroke.hairline))
                }
            }
            .contentShape(shape)
            .onTapGesture { if enabled { action() } }
            // SwiftUI has no MutableInteractionSource; a zero-distance drag is the idiomatic way
            // to observe the pressed state without swallowing the tap.
            .simultaneousGesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { _ in if enabled { pressed = true } }
                    .onEnded { _ in pressed = false }
            )
            .animation(.easeOut(duration: Organic.motion.quick.seconds), value: pressed)
    }
}
