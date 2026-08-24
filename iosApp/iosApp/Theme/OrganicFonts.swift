import SwiftUI
import Shared

/// Binds the design system's font *roles* to the font files this app ships.
///
/// The token module names families by role and canonical family name only - a font binary is
/// always platform-shaped (`res/font` on Android, a bundled `.ttf` registered through
/// `UIAppFonts` here) - so resolving one is deliberately the consumer's job. Same contract as
/// `OrganicFonts.kt`.
///
/// The names below are the PostScript names read out of the TTF name tables, not guesses:
/// `Font.custom` matches on PostScript name, and Figtree ships each weight as its own face.
struct OrganicFonts {
    static let display = "Caprasimo-Regular"
    static let bodyRegular = "Figtree-Regular"
    static let bodyMedium = "Figtree-Medium"
    static let bodySemiBold = "Figtree-SemiBold"

    /// Figtree is bundled as three separate static faces rather than a variable font, so the
    /// weight selects a file rather than an axis value.
    static func name(family: FontFamilyToken, weight: FontWeightToken) -> String {
        switch family {
        case .display:
            return display
        default:
            switch weight {
            case .semibold: return bodySemiBold
            case .medium: return bodyMedium
            default: return bodyRegular
            }
        }
    }
}

extension TextStyleToken {
    /// The counterpart of `TextStyleToken.toTextStyle(fonts)`.
    var font: Font {
        .custom(OrganicFonts.name(family: family, weight: weight), fixedSize: CGFloat(fontSize))
    }

    /// SwiftUI expresses line height as spacing BETWEEN lines, whereas the token (like CSS and
    /// Compose) gives total line height. The difference is the font size.
    var lineSpacing: CGFloat {
        guard let lh = organicOptionalSp(lineHeight) else { return 0 }
        return max(0, lh - CGFloat(fontSize))
    }

    var tracking: CGFloat { organicOptionalSp(letterSpacing) ?? 0 }
}

extension View {
    /// Applies a token's font, line spacing and letter spacing in one call, so screens read
    /// `.organicText(Organic.type.heading1)` the way Compose screens read `Organic.type.heading1`.
    func organicText(_ token: TextStyleToken) -> some View {
        font(token.font)
            .lineSpacing(token.lineSpacing)
            .tracking(token.tracking)
    }
}
