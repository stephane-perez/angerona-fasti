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
- **Reminders**: any event with a start time can get a system notification — 15 min,
  30 min, 1 hour, 2 hours, or 1 day before. See "Reminders" below.
- **No recurrence.** Explicitly out of scope for v1 — a deliberate scope decision, not
  an oversight.
- **Encrypted, single file**: the whole calendar is one JSON document, encrypted with
  AES-256-GCM via the Android Keystore, stored in the app's private storage (not
  Storage Access Framework — there's nothing for the person to pick a location for,
  unlike Angerona's many user-named note files). See "File encryption" below.
- English, French, Spanish — follows the phone's system language.

## Reminders
Pick a lead time (15 min / 30 min / 1 hour / 2 hours / 1 day before) in an event's
edit dialog, next to its start time — it needs one, since a reminder counts back from
a clock time. At the chosen moment, Android shows a normal system notification; tapping
it opens the app on that event's day.

A few things worth knowing:
- **The notification is deliberately generic** — just "Reminder / You have an upcoming
  event", never the event's title, time or description. Anyone glancing at the lock
  screen or notification shade learns nothing about your calendar. Tapping it opens
  the app on the right day, but only once you're actually past your own lock screen.
- Reminders use `AlarmManager`, so they still fire while the app is closed, and they're
  automatically re-armed after a reboot.
- Requires the notification permission (Android 13+), asked for once on first launch;
  if declined, everything else still works, reminders just won't show. Exact-time
  alarms need a system permission too (`SCHEDULE_EXACT_ALARM`) — if that's been
  revoked in system settings, the app falls back to an inexact alarm (fires within a
  few minutes of the chosen time) rather than silently dropping the reminder.

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
