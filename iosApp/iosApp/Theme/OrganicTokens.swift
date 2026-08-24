import SwiftUI
import Shared

/// The bridge between the toolkit-free `:design-system` module and SwiftUI - the exact
/// counterpart of `TokenBindings.kt` on the Android side. Everything iOS-specific about the
/// Organic design language lives here and in `OrganicFonts.swift`; the token module itself knows
/// nothing about SwiftUI.
///
/// One wrinkle worth understanding. Kotlin `value class` does not exist in Objective-C, so the
/// bridge **inlines** each token to its underlying primitive rather than boxing it:
///
///     ColorToken(Long)   ->  Int64      (packed AARRGGBB)
///     DpToken(Float)     ->  Float
///     SpToken(Float)     ->  Float
///     OpacityToken(Float)->  Float
///     DurationToken(Int) ->  Int32
///
/// That costs nothing at runtime, but it also means Swift loses the distinct types and the
/// members declared on them - `ColorToken.withAlpha(...)` is simply not available here, so the
/// equivalent is reimplemented below. Nullable tokens behave the OPPOSITE way: `SpToken?` boxes
/// back to an untyped `Any?`, which is why `lineHeight` needs unwrapping by hand.

extension Color {
    /// Rebuilds a colour from the packed sRGB `AARRGGBB` the token carries.
    init(argb: Int64) {
        let a = Double((argb >> 24) & 0xFF) / 255.0
        let r = Double((argb >> 16) & 0xFF) / 255.0
        let g = Double((argb >> 8) & 0xFF) / 255.0
        let b = Double(argb & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
    }

    /// The Swift twin of `ColorToken.withAlpha`, which the bridge did not carry across.
    /// Rounds to 8 bits exactly as the Kotlin version does, so both platforms land on the
    /// same pixel.
    func organicAlpha(_ fraction: Float) -> Color {
        opacity(Double((min(max(fraction, 0), 1) * 255 + 0.5).rounded(.down)) / 255.0)
    }
}

/// Durations arrive as milliseconds; SwiftUI animations want seconds.
extension Int32 {
    var seconds: Double { Double(self) / 1000.0 }
}

extension EasingToken {
    /// SwiftUI has no general cubic-bezier curve, so the two control points are mapped onto the
    /// nearest built-in timing curve. `.timingCurve` takes the same four numbers CSS does.
    var animation: Animation { .timingCurve(Double(a), Double(b), Double(c), Double(d)) }

    func animation(durationMillis: Int32) -> Animation {
        .timingCurve(Double(a), Double(b), Double(c), Double(d), duration: durationMillis.seconds)
    }
}

/// A `SpToken?` crosses the bridge boxed and untyped. This unwraps it back to a `CGFloat`.
func organicOptionalSp(_ boxed: Any?) -> CGFloat? {
    guard let value = boxed as? Float else { return nil }
    return CGFloat(value)
}
