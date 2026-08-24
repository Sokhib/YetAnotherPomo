import SwiftUI
import Shared

/// Convenience accessors so screens read `Organic.colors.accent` / `Organic.spacing.space4`,
/// mirroring the `Organic` object the Compose side exposes.
///
/// Compose publishes the materialised tokens through CompositionLocals because it needs them to
/// be swappable per subtree. SwiftUI has @Environment for that, but nothing here ever swaps the
/// design system at runtime, so these are plain statics - simpler, and it keeps the call sites
/// identical to Android's.
enum Organic {
    static let system: DesignSystem = OrganicDesignSystemKt.OrganicDesignSystem

    static let colors = system.colors
    static let type = system.typography
    static let spacing = system.spacing
    static let radius = system.radius
    static let elevation = system.elevation
    static let stroke = system.stroke
    static let size = system.size
    static let opacity = system.opacity
    static let motion = system.motion
}

/// Colour roles, already converted. Reading `Organic.colors.accent` gives the raw Int64 the
/// bridge inlined; these give a SwiftUI `Color`.
extension ColorTokens {
    var bgColor: Color { Color(argb: bg) }
    var surfaceColor: Color { Color(argb: surface) }
    var textColor: Color { Color(argb: text) }
    var accentColor: Color { Color(argb: accent) }
    var accent2Color: Color { Color(argb: accent2) }
    var dividerColor: Color { Color(argb: divider) }
    var lockedSurfaceColor: Color { Color(argb: lockedSurface) }
    var lockedOnSurfaceColor: Color { Color(argb: lockedOnSurface) }
}

extension ColorRampTokens {
    var color100: Color { Color(argb: c100) }
    var color200: Color { Color(argb: c200) }
    var color300: Color { Color(argb: c300) }
    var color400: Color { Color(argb: c400) }
    var color500: Color { Color(argb: c500) }
    var color600: Color { Color(argb: c600) }
    var color700: Color { Color(argb: c700) }
    var color800: Color { Color(argb: c800) }
    var color900: Color { Color(argb: c900) }
}
