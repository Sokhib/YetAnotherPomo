import SwiftUI
import Shared

/// The doc's "Hold to end" control: an outlined pill whose fill sweeps left-to-right over
/// `holdDurationMs` while pressed, firing `onHoldComplete` at 100%; releasing early cancels and
/// eases the fill back to zero. Ports `startHold`/`holdUp` from `HoldToConfirmButton.kt`.
struct HoldToConfirmButton: View {
    let text: String
    let holdDurationMs: Int32
    let onHoldComplete: () -> Void
    var minHeight: CGFloat = 56
    var borderColor: Color = Organic.colors.lockedOnSurfaceColor.organicAlpha(Organic.opacity.border)
    var fillColor: Color = Organic.colors.accentRamp.color800
    var contentColor: Color = Organic.colors.lockedOnSurfaceColor.organicAlpha(Organic.opacity.strong)

    @State private var progress: CGFloat = 0
    @State private var holdTask: Task<Void, Never>?

    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .leading) {
                Rectangle()
                    .fill(fillColor)
                    .frame(width: geometry.size.width * progress)
                Text(text)
                    .font(.custom(OrganicFonts.display, fixedSize: 17))
                    .foregroundStyle(contentColor)
                    .frame(maxWidth: .infinity, alignment: .center)
            }
        }
        .frame(height: minHeight)
        .clipShape(Capsule())
        .overlay { Capsule().strokeBorder(borderColor, lineWidth: CGFloat(Organic.stroke.hairline)) }
        .contentShape(Capsule())
        .gesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in if holdTask == nil { startHold() } }
                .onEnded { _ in cancelHold() }
        )
    }

    private func startHold() {
        progress = 0
        // `motion.linear` is the token's easing curve: the fill must track real elapsed time, so
        // a hold that looks 80% done really is 80% done.
        withAnimation(Organic.motion.linear.animation(durationMillis: holdDurationMs)) {
            progress = 1
        }
        holdTask = Task {
            try? await Task.sleep(nanoseconds: UInt64(holdDurationMs) * 1_000_000)
            guard !Task.isCancelled else { return }
            UIImpactFeedbackGenerator(style: .heavy).impactOccurred()
            onHoldComplete()
        }
    }

    private func cancelHold() {
        holdTask?.cancel()
        holdTask = nil
        withAnimation(.easeOut(duration: Organic.motion.quick.seconds)) { progress = 0 }
    }
}
