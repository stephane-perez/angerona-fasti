# Angerona Fasti (Android)

A native Kotlin + Jetpack Compose calendar app — local-only, month-grid view, every
event encrypted on disk. Sister app to [Angerona](https://github.com/stephane-perez/angerona)
(an encrypted notepad); same visual language, same philosophy, same encryption
approach, adapted for a single calendar instead of many notes.

> **AI authorship disclosure.** Every line of code, configuration, and documentation in
> this repository was designed and written by an AI (Claude, Anthropic), reusing and
> hardening the architecture built for Angerona. A human reviewed the result and
> directed the design decisions (storage format, feature scope, naming).

## Requirements
- JDK 17
- Android SDK 36 (minSdk 26)
- Android Studio, or just the CI — see "Building without Android Studio" below.

## Getting started
Open this folder in Android Studio and hit Run — self-contained Gradle project
(Gradle 8.13, AGP 8.13.0, Kotlin 1.9.24). No accounts, no network access.

## Building without Android Studio
Every push to `main` builds a debug APK via GitHub Actions
(`.github/workflows/build.yml`) — same pattern as Angerona:
- **Every push/PR**: builds `angerona-fasti.apk`, uploaded as a workflow artifact.
- **Pushing a tag starting with `v`**: also publishes a public GitHub Release with the
  APK attached.

## What's in v1
- **Month grid** — the only view for now; no week/day/agenda view yet.
- **Events**: title, optional start/end time (`HH:mm`, plain text entry — no wheel
  picker), optional description. Tap a day to see its events, tap an event to edit it,
  the trash icon to delete (with confirmation).
- **No recurrence.** No reminders/notifications. Both explicitly out of scope for v1 —
  a deliberate scope decision, not an oversight.
- **Encrypted, single file**: the whole calendar is one JSON document, encrypted with
  AES-256-GCM via the Android Keystore, stored in the app's private storage (not
  Storage Access Framework — there's nothing for the person to pick a location for,
  unlike Angerona's many user-named note files). See "File encryption" below.
- English, French, Spanish — follows the phone's system language.

## File encryption

**The calendar file is encrypted on disk** — there's no way to save it in plain text.
Only this app, on this specific device, can read it back, using an AES-256-GCM key
held in the Android Keystore. Same guarantees and limits as Angerona:

- ✅ Unreadable by any other app, or if copied elsewhere.
- ✅ Tampering with the file is detected (GCM authentication covers the format header
  too), not silently accepted.
- ⚠️ **The key survives neither an app uninstall, nor a factory reset, nor a device
  change.** If that happens, the calendar becomes permanently unreadable — no recovery
  password. Not a portable encrypted backup, only a "this device, this app"
  protection.
- ⚠️ Not password-protected: opening is automatic while the phone is unlocked and the
  app installed.
- ⚠️ The file is capped at 5 MB (plenty for thousands of events) to avoid a crash on a
  huge/corrupted file.

Unlike Angerona, this app can guarantee a genuinely **atomic write**: because storage
is a private, app-owned file rather than a Storage Access Framework document, a save
writes to a temp file first and only replaces the real file via an atomic rename — a
failed write can never leave a corrupted or truncated calendar behind. See
`DEVELOPMENT.md` for the encryption implementation details, and Angerona's own
`DEVELOPMENT.md` for the multi-round security review that shaped this design
originally.

## License
WTFPL — see [`LICENSE`](LICENSE).
