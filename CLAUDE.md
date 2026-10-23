# Angerona Fasti — project rules

Local-only, encrypted calendar for Android (Kotlin + Jetpack Compose), month-grid view,
with event reminders. Sister app of Angerona Nota (encrypted notepad). Personal-use app:
keep security measures proportionate, don't over-engineer. The person you work with
speaks French; talk to them in French, but all repo docs and code comments stay in
English.

## Hard constraints (do not break)
- **Never change `applicationId` / package (`com.stephaneperez.angerona.fasti`).**
  The encryption key lives in the Android Keystore, tied to the app identity. Changing it
  orphans the calendar file permanently. Only the display name (`app_name`) may change.
- **Never change the debug signing key** (`app/debug.keystore`): a signature change
  forces an uninstall, which destroys the Keystore key and the calendar with it.
- `release` build type must never reference the debug keystore.
- `android:allowBackup="false"` must stay.
- **Notifications must stay content-free**: no event title, time or description, only
  the generic "Reminder / You have an upcoming event" text (privacy on the lock screen).
  Only the event id and date travel in intent extras, to route the tap.

## Architecture
- Whole calendar = one JSON document (`CalendarData`), encrypted into
  `calendar.fsti` in app-private storage (`filesDir`). No SAF.
- Writes are atomic: temp file, then `File.renameTo`.
- `CryptoManager`: AES-256-GCM, Keystore key, header as GCM AAD, 5 MB plaintext cap,
  size bounds enforced inside the class, strict UTF-8 decoding.
- Reminders: `CalendarEvent.reminderMinutesBefore` (nullable, needs a start time).
  `ReminderScheduler` is the only code touching `AlarmManager` (exact alarm if
  `canScheduleExactAlarms()`, inexact fallback). `ReminderReceiver` shows the
  notification; `BootReceiver` reschedules everything after reboot (`goAsync` + thread);
  `FastiViewModel` also reschedules on cold start.
- The time field auto-inserts ":" (numeric keyboard has no colon key).
- No recurrence (deliberately out of scope).

## Conventions
- **Always update `README.md` when changing source code.** README stays in English and
  short; technical detail goes in `DEVELOPMENT.md`.
- Strings: English (`values/`), French (`values-fr/`), Spanish (`values-es/`). Every new
  string needs all three. **Apostrophes in `strings.xml` must be escaped as `\'`**, or
  the CI build breaks.
- Signing config: use `signingConfigs.getByName("debug")`, never `create("debug")`.
- Don't import `androidx.compose.foundation.layout.weight` explicitly.
- Edge-to-edge: content uses `windowInsetsPadding(WindowInsets.safeDrawing)`.
- Version comes from git tags (`vX.Y.Z`); CI passes `-PversionNameOverride` /
  `-PversionCodeOverride` (run number). Never hardcode versions.
- CI: `.github/workflows/build.yml`, default branch `main`. APK is published as
  `angerona-fasti.apk`. Work on feature branches (e.g. `feature/...`), merge via PR; a
  `v*` tag triggers the GitHub Release.
- License: WTFPL. F-Droid metadata lives in `fastlane/metadata/android/`.
- Build needs JDK 17, compileSdk/targetSdk 36, AGP 8.13.0, Gradle 8.13.
