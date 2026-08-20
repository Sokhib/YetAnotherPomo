package com.tora.yetanotherpomo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.tora.organic.tokens.FontFamilyToken
import com.tora.yetanotherpomo.R

// Caprasimo (display) over Figtree (body) - bundled as static .ttf so the Locked/Blocked
// overlay renders correctly with zero network or Play Services dependency.
val CaprasimoFamily = FontFamily(
    Font(R.font.caprasimo_regular, FontWeight.Normal),
)

val FigtreeFamily = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_medium, FontWeight.Medium),
    Font(R.font.figtree_semibold, FontWeight.SemiBold),
)

/**
 * Binds the design system's font *roles* to the font files this app ships.
 *
 * The token module names families by role and canonical family name only - a font binary is
 * always platform-shaped (`res/font` here, a classpath `.ttf` on desktop, `@font-face` on the
 * web), so resolving one is deliberately the consumer's job.
 */
@Immutable
class OrganicFonts(
    val display: FontFamily = CaprasimoFamily,
    val body: FontFamily = FigtreeFamily,
) {
    fun familyFor(token: FontFamilyToken): FontFamily = when (token) {
        FontFamilyToken.Display -> display
        FontFamilyToken.Body -> body
    }
}

val DefaultOrganicFonts = OrganicFonts()

val LocalOrganicFonts = staticCompositionLocalOf { DefaultOrganicFonts }
