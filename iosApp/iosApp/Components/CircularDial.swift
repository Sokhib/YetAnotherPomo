import SwiftUI
import Shared

/// The Home screen's drag-to-set-minutes dial: an accent progress ring over a neutral track,
/// 12 tick marks, a knob riding the outer edge, and the minutes readout in the centre.
///
/// The angle maths is ported from `CircularDial.kt` unchanged - degrees measured clockwise from
/// 12 o'clock, `m = round(deg / 360 * max)` clamped to `[5, max]`.
///
/// Animation differs in mechanism, not in effect. Compose drives one `animateFloatAsState` and
/// reads it while drawing; SwiftUI cannot interpolate a value a `Canvas` closure reads, so the
/// ring is an animatable `Shape`, the knob animates through `rotationEffect`, and the digits use
/// a small `Animatable` modifier. All three read the same `fraction`, so they stay in step.
struct CircularDial: View {
    let minutes: Int
    let maxMinutes: Int
    let onMinutesChange: (Int) -> Void
    var size: CGFloat = 272

    private var ring: CGFloat { CGFloat(Organic.stroke.ring) }
    private var fraction: Double { min(max(Double(minutes) / Double(maxMinutes), 0), 1) }

    /// Ported 1:1 from `minutesAt(offset)`.
    private func minutes(at point: CGPoint) -> Int {
        let dx = point.x - size / 2
        let dy = point.y - size / 2
        var degreesFromTop = atan2(dx, -dy) * 180 / .pi
        if degreesFromTop < 0 { degreesFromTop += 360 }
        let m = Int((degreesFromTop / 360 * Double(maxMinutes)).rounded())
        return min(max(m, 5), maxMinutes)
    }

    var body: some View {
        ZStack {
            Circle()
                .strokeBorder(Organic.colors.neutral.color300, lineWidth: ring)

            DialArc(fraction: fraction)
                .stroke(Organic.colors.accentColor, lineWidth: ring)
                .padding(ring / 2)

            ticks

            // Inner disc covering the ring's inside, leaving only the outer band visible.
            Circle()
                .fill(Organic.colors.bgColor)
                .frame(width: size - ring * 2, height: size - ring * 2)

            knob

            VStack(spacing: 2) {
                Text(verbatim: "\(displayMinutes)")
                    .organicText(Organic.type.dialDigits)
                    .foregroundStyle(Organic.colors.textColor)
                    .modifier(AnimatableMinutes(value: Double(minutes), lowerBound: 5, upperBound: maxMinutes))
                Text("MINUTES")
                    .organicText(Organic.type.labelSmall)
                    .foregroundStyle(Organic.colors.neutral.color600)
            }
        }
        .frame(width: size, height: size)
        .contentShape(Circle())
        // minimumDistance 0 makes a plain tap set the value immediately, which is what the two
        // Compose detectors (detectTapGestures + detectDragGestures) achieve together.
        .gesture(
            DragGesture(minimumDistance: 0)
                .onChanged { onMinutesChange(minutes(at: $0.location)) }
        )
        .animation(.spring(response: 0.35, dampingFraction: 1), value: minutes)
    }

    private var displayMinutes: Int { min(max(minutes, 5), maxMinutes) }

    /// 12 evenly spaced marks, matching the doc's static `rotate(i*30deg)` ring.
    private var ticks: some View {
        Canvas { context, canvasSize in
            let tickColor = Organic.colors.textColor.organicAlpha(Organic.opacity.tick)
            for i in 0..<12 {
                var path = Path()
                path.move(to: CGPoint(x: canvasSize.width / 2, y: 4.5))
                path.addLine(to: CGPoint(x: canvasSize.width / 2, y: 13.5))
                let rotated = path.applying(
                    CGAffineTransform(translationX: canvasSize.width / 2, y: canvasSize.height / 2)
                        .rotated(by: Double(i) * 30 * .pi / 180)
                        .translatedBy(x: -canvasSize.width / 2, y: -canvasSize.height / 2)
                )
                context.stroke(
                    rotated,
                    with: .color(tickColor),
                    style: StrokeStyle(lineWidth: CGFloat(Organic.stroke.thin), lineCap: .round)
                )
            }
        }
        .frame(width: size, height: size)
    }

    private var knob: some View {
        ZStack {
            Circle().fill(Organic.colors.bgColor)
                .frame(width: CGFloat(Organic.size.iconMd), height: CGFloat(Organic.size.iconMd))
                .shadow(radius: 4, y: 2)
            Circle().fill(Organic.colors.accentColor)
                .frame(width: CGFloat(Organic.size.iconSm), height: CGFloat(Organic.size.iconSm))
        }
        // Park the knob at 12 o'clock, then rotate the whole frame - rotationEffect animates,
        // where an offset computed from cos/sin would not.
        .offset(y: -size / 2)
        .rotationEffect(.degrees(fraction * 360))
        .frame(width: size, height: size)
    }
}

/// The progress sweep. `animatableData` is what lets SwiftUI interpolate the arc rather than
/// snapping it - the direct counterpart of Compose reading an `animateFloatAsState` while drawing.
private struct DialArc: Shape {
    var fraction: Double

    var animatableData: Double {
        get { fraction }
        set { fraction = newValue }
    }

    func path(in rect: CGRect) -> Path {
        var path = Path()
        guard fraction > 0 else { return path }
        path.addArc(
            center: CGPoint(x: rect.midX, y: rect.midY),
            radius: min(rect.width, rect.height) / 2,
            startAngle: .degrees(-90),
            endAngle: .degrees(-90 + 360 * fraction),
            clockwise: false
        )
        return path
    }
}

/// Animates the digits themselves, so a preset tap counts up to the new value instead of jumping.
private struct AnimatableMinutes: AnimatableModifier {
    var value: Double
    let lowerBound: Int
    let upperBound: Int

    var animatableData: Double {
        get { value }
        set { value = newValue }
    }

    func body(content: Content) -> some View {
        Text(verbatim: "\(min(max(Int(value.rounded()), lowerBound), upperBound))")
            .organicText(Organic.type.dialDigits)
            .foregroundStyle(Organic.colors.textColor)
    }
}
