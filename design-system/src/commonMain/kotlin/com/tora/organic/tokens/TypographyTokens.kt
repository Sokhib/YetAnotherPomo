package com.tora.organic.tokens

/**
 * A typeface referenced by role and canonical family name rather than by a font file, because a
 * font binary is always platform-shaped (an Android `res/font` entry, a desktop `.ttf` on the
 * classpath, a `@font-face` rule). Consumers map each role onto whatever their platform loads.
 */
enum class FontFamilyToken(val familyName: String) {
    /** Caprasimo — used for headings, dial digits and button labels. */
    Display("Caprasimo"),

    /** Figtree — used for body copy, labels and everything else. */
    Body("Figtree"),
}

/** The weights the Organic type scale actually calls for, as CSS/OpenType numeric weights. */
enum class FontWeightToken(val weight: Int) {
    Normal(400),
    Medium(500),
    SemiBold(600),
}

data class TextStyleToken(
    val family: FontFamilyToken,
    val fontSize: SpToken,
    val weight: FontWeightToken = FontWeightToken.Normal,
    val lineHeight: SpToken? = null,
    val letterSpacing: SpToken? = null,
)

/** Which weights of a family a consumer must be able to supply for the scale to render right. */
data class FontFamilyRequirement(
    val family: FontFamilyToken,
    val weights: Set<FontWeightToken>,
)

data class TypographyTokens(
    val heading1: TextStyleToken,
    val heading2: TextStyleToken,
    val heading3: TextStyleToken,
    val body: TextStyleToken,
    val bodySmall: TextStyleToken,
    val label: TextStyleToken,
    val labelSmall: TextStyleToken,
    val dialDigits: TextStyleToken,
    val clockDigits: TextStyleToken,
) {
    val styles: List<TextStyleToken>
        get() = listOf(
            heading1, heading2, heading3, body, bodySmall,
            label, labelSmall, dialDigits, clockDigits,
        )

    /**
     * The font files a consumer has to provide, derived from the scale itself. A binding layer
     * can assert against this so a missing weight fails loudly instead of silently synthesising.
     */
    val requirements: List<FontFamilyRequirement>
        get() = styles.groupBy { it.family }
            .map { (family, styles) -> FontFamilyRequirement(family, styles.map { it.weight }.toSet()) }
}
