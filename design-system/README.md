# `:design-system`

The Organic design language, expressed as data.

This is a plain **Kotlin/JVM** module: no Android Gradle plugin, no `android {}` block, no
`res/`, and **zero dependencies**. It can be consumed by an Android app, a desktop app, a
screenshot tool, or a codegen script that emits CSS custom properties.

## What's in here

| Token group  | Type                | Covers                                                          |
|--------------|---------------------|-----------------------------------------------------------------|
| `colors`     | `ColorTokens`       | Semantic roles (`bg`, `surface`, `accent`, …) + three 100–900 ramps |
| `typography` | `TypographyTokens`  | Nine text styles over two font *roles* (Display / Body)         |
| `spacing`    | `SpacingTokens`     | The 4.4dp-based scale, `space1`–`space8`                        |
| `radius`     | `RadiusTokens`      | `sm` / `md` / `lg` / `pill`                                     |
| `elevation`  | `ElevationTokens`   | Shadow depth, `sm` / `md` / `lg`                                |
| `stroke`     | `StrokeTokens`      | `hairline` / `thin` / `ring`                                    |
| `size`       | `SizeTokens`        | Touch targets, screen gutters, icon sizes                       |
| `opacity`    | `OpacityTokens`     | The named alpha scale, `faint` → `opaque`                       |
| `motion`     | `MotionTokens`      | Durations in ms + cubic-bezier curves                           |

All of it hangs off one aggregate:

```kotlin
val OrganicDesignSystem: DesignSystem
```

## Primitives

Tokens are `value class` wrappers over JVM primitives, so nothing here depends on a UI toolkit:

- `ColorToken(0xFFF5EAD8)` — packed sRGB ARGB, with `withAlpha(...)` that rounds to 8 bits the
  same way Compose's `Color.copy(alpha = ...)` does.
- `DpToken(8f)` / `SpToken(15f)` — the number only; resolving against a screen density is the
  consumer's job.
- `OpacityToken`, `DurationToken`, `EasingToken`.

## Fonts

Fonts are named by **role and family name**, never by file:

```kotlin
enum class FontFamilyToken(val familyName: String) { Display("Caprasimo"), Body("Figtree") }
```

A font binary is always platform-shaped — an Android `res/font` entry here, a classpath `.ttf` on
desktop, a `@font-face` rule on the web — so loading one is deliberately out of scope. Consumers
can check `typography.requirements` to see exactly which family/weight combinations the scale
needs, and fail loudly if one is missing.

## Consuming it

Write one binding layer that maps each token to a platform type, then read everything through it.
The Android/Compose binding for this repo lives in
`app/src/main/java/com/tora/yetanotherpomo/ui/theme/` — `TokenBindings.kt` does the conversions,
`OrganicFonts.kt` resolves the font roles, and `OrganicTheme.kt` materialises the whole set once
and publishes it via `CompositionLocal`s. Screens then read `Organic.colors.accent`,
`Organic.spacing.space4`, `Organic.opacity.muted`, and so on.

## Swapping the design language

`OrganicTheme` takes a `DesignSystem` parameter:

```kotlin
OrganicTheme(designSystem = MyOtherDesignSystem) { … }
```

Every call site keeps reading `Organic.*` unchanged. To replace the language wholesale, supply a
different `DesignSystem` instance — `OrganicDesignSystem.kt` is the only file with concrete values
in it.

## Testing

Because the module is toolkit-free, its tokens are testable on the plain JVM with no Robolectric
and no instrumentation:

```
./gradlew :design-system:test
```
