<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" width="96" alt="">

# Focus Lock

**A focus timer that actually enforces itself.**

![Android 24+](https://img.shields.io/badge/Android-7.0%2B-C67139?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2-C67139?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-EBDDC5?style=flat-square&logo=jetpackcompose&logoColor=201E1D)

</div>

<br>

You set how long you want to concentrate and choose the handful of apps you're allowed to keep —
everything else simply won't open until the time is up. Reach for the app you're trying to avoid
and you get a calm full-screen reminder of how much time is left instead of its feed. Home and
Settings always stay reachable, so you're never locked out of your own phone.

No account, no setup, no internet. Nothing leaves the device.

<br>

## Screens

<div align="center">

| Home | Settings | Locked |
|:---:|:---:|:---:|
| <img src="docs/screenshots/home.jpg" width="240" alt="Home screen with a circular duration dial set to 15 minutes"> | <img src="docs/screenshots/settings.jpg" width="240" alt="Settings screen showing defaults and lock toggles"> | <img src="docs/screenshots/locked.jpg" width="240" alt="Locked screen counting down from 14:59"> |
| Dial in a duration, glance at what's&nbsp;still allowed, begin. | Your defaults, and how the lock behaves. | Takes over for the whole session. |

</div>

<br>

## What it does

- **Dial in any session** from 5 to 60 minutes, or tap a `15` / `25` / `50` preset.
- **Pick your allowlist** from every app on the phone. Everything else goes quiet for the session.
- **Blocks on sight** — open something off the list and a full-screen card appears instead, showing
  the time remaining and a shortcut back to the apps you *are* allowed.
- **One press to back out.** Press Back once and you leave the blocked app entirely — it closes
  rather than lingering in Recents with a preview of what you were avoiding.
- **Hard to quit by accident.** Ending the whole session early takes a deliberate press-and-hold
  plus a confirmation.
- **Never locks you out.** Your launcher, Settings and keyboard always keep working.
- **Keeps time honestly.** The countdown survives leaving the app, locking the screen, or the app
  being closed entirely.

<br>

## Stack

| | |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| State | `ViewModel` + `StateFlow`, Coroutines & Flow |
| Persistence | DataStore Preferences |
| DI | Manual — no Hilt, no KSP |
| Blocking | `AccessibilityService` + accessibility overlay |
| Build | AGP 9.3.1, Gradle 9.5, version catalog |
| SDK | min 24 · target 37 · Java 11 |

<br>

## Getting started

```bash
./gradlew :app:installDebug
```

Then enable the service once under **Settings → Accessibility → Focus Lock**, or tap **Grant
access** on the in-app banner, which takes you straight there.

<br>

## Project layout

```
app/
├── di/            manual singletons, built once at startup
├── domain/        models and repository interfaces
├── data/          storage, installed-app lookup
├── service/       the blocker and its full-screen overlay
└── ui/            screens, components, theme

design-system/     the "Organic" design language as plain Kotlin
                   — colour, type, spacing, motion tokens with
                     no Android or Compose dependency
```

Two modules. `:design-system` holds the design language on its own, with no dependency on Android
or Compose, so the visual language can be swapped or reused without touching a screen.

<br>

## License

Not licensed yet — default copyright applies, which makes this source-available rather than open
source. A `LICENSE` file may follow.

**Fonts.** Bundles **Figtree** (© 2022 The Figtree Project Authors) and **Caprasimo**
(© 2023 The Caprasimo Project Authors), both under the SIL Open Font License 1.1. Full texts in
[`licenses/`](licenses), summary in [`NOTICE`](NOTICE).
