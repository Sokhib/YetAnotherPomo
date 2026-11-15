# Night lock — discussion notes (2026-11-15)

## Goal

Block social media apps on my Android phone from **22:00 until 07:00**, in a way that even I
can't undo in the moment.

## Options considered

### 1. Device Owner mode — strongest, ruled out

Make the app the device owner once with
`adb shell dpm set-device-owner <package>/.AdminReceiver`. Android itself then enforces the
block, and the app can't be deactivated or uninstalled from Settings.

- Block: `DevicePolicyManager.setPackagesSuspended(admin, apps, true)` at 22:00, `false` at
  07:00 (icon greys out, "app paused", notifications hidden).
- Close the loopholes: `setAutoTimeRequired` / `DISALLOW_CONFIG_DATE_TIME` (clock change),
  `setUninstallBlocked` on the social apps (reinstalling would clear the suspension),
  `DISALLOW_DEBUGGING_FEATURES` + `DISALLOW_INSTALL_UNKNOWN_SOURCES` (my own signed update
  undoing it), `DISALLOW_FACTORY_RESET`, `DISALLOW_SAFE_BOOT`, and either suspend browsers or
  set a blocking Private DNS + `DISALLOW_CONFIG_PRIVATE_DNS`.
- Only exit left: factory reset from recovery mode (the hardware buttons), which wipes the phone.
- During development, mark the app `android:testOnly="true"` so
  `adb shell dpm remove-active-admin` can still remove it.

**Ruled out:** `dpm set-device-owner` refuses while any account is on the phone, and I don't
want to remove my Google account and add it back.

### 2. Self-protecting Accessibility Service — chosen

How the strict modes in AppBlock and Stay Focused work.

- An `AccessibilityService` listens for `TYPE_WINDOW_STATE_CHANGED` and blocks the social apps
  during the window.
- It protects itself: during the window it also blocks the Settings pages used to escape —
  Accessibility, this app's App info (Force stop / Uninstall), Device admin apps, Developer
  options, Date & time.
- Registered as a plain **Device Admin**, so it can't be uninstalled without first being
  deactivated, and that page is blocked too.
- Optional: read the browser URL bar to block the social media websites.

**Weak spots:** safe mode (third-party apps don't run, so it can be deactivated and uninstalled
there), adb from the Mac, phone makers that kill background apps (exempt it from battery
optimisation), and Android 13+ sideloaded apps, which need "Allow restricted settings" tapped
once. It stops impulse, not a planned bypass, and impulse is the real problem at bedtime.

### 3. Family Link — not possible

It only supervises child and teen accounts, so it can't supervise my adult account.
Digital Wellbeing's Bedtime mode is too easy to turn off.

## Where YetAnotherPomo stands

It already has the core of option 2: `FocusAccessibilityService` works out which app is in front
and covers blocked apps with a full-screen overlay. But it is built so I can always get out:

| Needed for the night lock | YetAnotherPomo today |
|---|---|
| A fixed 22:00–07:00 schedule | Only a 5–60 min session started by hand |
| Block social apps | Blocks everything except an allowlist |
| Even I can't stop it | The overlay's hold-to-end button ends the session |
| Settings blocked during the lock | Settings always allowed on purpose (`SystemPackagePolicy.kt`) |
| Can't be uninstalled | Not a device admin |
| Survives a reboot | Session timed on `elapsedRealtime`, so a reboot ends it (`FocusSession.kt`) |
| Blocks websites | `canRetrieveWindowContent="false"`, so it can't read the URL bar |

## What to build

1. **Night mode:** a 22:00–07:00 schedule on the wall clock with a blocklist of social apps, and
   no end button while it's active. Re-check the state on boot and on time and timezone changes.
2. **Self-protection:** during the lock, stop treating Settings as always allowed and block the
   escape pages listed in option 2. Telling one Settings page from another needs
   `canRetrieveWindowContent="true"`, because many phones show every sub-page under a single
   `SubSettings` activity.
3. **Device admin:** add a `DeviceAdminReceiver`, so the app can't be uninstalled without first
   being deactivated.
4. **Optional:** read the browser URL bar to block the websites.
5. **No edits to the lock during the window:** changes take effect the next day.

The detection code and the overlay can stay as they are.

## Decision (2026-11-15)

Added to YetAnotherPomo as an opt-in **Night lock**, on `main`. The README's "Never locks you out"
now names Night lock as the one opt-in exception. Websites are not blocked: the social apps are
the target, and browsers can go on the blocklist if needed.

## How it was built

| Ask | Where |
|---|---|
| 22:00–07:00 wall-clock schedule, blocklist, no end button | `domain/model/NightLock.kt`, `NightTime.kt`; `service/NightOverlayContent.kt` |
| Re-check on boot, update, time and time-zone change | `receiver/NightLockSystemReceiver.kt`; the service also re-checks on `TIME_TICK`, unlock and the same clock broadcasts |
| A clock change can't end a running lock | `Deadline`: the end must pass on the wall clock *and* `elapsedRealtime` (same boot, via `BOOT_COUNT`). Only a network-time correction re-derives it |
| Survives a reboot | The running lock is persisted with its end; after a reboot the wall clock alone decides |
| Settings escape pages blocked during the lock | `service/NightGuard.kt` + `EscapePageMatcher.kt`: activity name, then window title, then visible text (this app's name, or rows like "USB debugging"), using Settings' own localized strings. Content is read only for Settings-like packages, only during the lock |
| Device admin | `receiver/NightLockAdminReceiver.kt`, no policies. Activating it stays possible at night; deactivating doesn't |
| No edits during the window | Stricter edits apply at once; looser ones wait in `pending` until tonight's lock ends, and both configs are enforced until then |
| Battery optimisation weak spot | "Battery" row asks for the exemption |

Known gaps, by design: safe mode, `adb`, factory reset, and an Accessibility shortcut assigned to
Focus Lock beforehand.
