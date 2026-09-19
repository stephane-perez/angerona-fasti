# Development notes

## Versioning
Same mechanism as Angerona: `versionName`/`versionCode` come from the CI-passed
`-PversionNameOverride`/`-PversionCodeOverride` when present (our GitHub Actions
workflow always supplies them), falling back to git (`git describe --tags` /
`git rev-list --count HEAD`) for any other build environment (F-Droid's build server,
in particular), and to `0.0.0-dev` / `1` with no git history at all. See
`app/build.gradle.kts`.

## Fixed debug signing key
`app/debug.keystore` is committed (password `android`, alias `androiddebugkey` — AGP's
own defaults, nothing secret here, it's debug-only) so every CI build shares one
signature and installs as a normal update rather than forcing an uninstall each time.
`release` has no signingConfig at all today; a real release needs its own private,
never-committed keystore. Full reasoning in Angerona's `DEVELOPMENT.md` — identical
mechanism, not repeated here.

## Data model and storage
`data/CalendarModels.kt` — `CalendarEvent` (id, title, date as ISO-8601, optional
`HH:mm` start/end, description) and `CalendarData` (a `formatVersion` int plus the
event list), both `@Serializable` via kotlinx.serialization.

`data/CalendarRepository.kt` loads/saves the whole calendar as one encrypted file,
`calendar.fsti`, under `context.filesDir`. No SAF, no file picker — this is
deliberately simpler than Angerona, which manages many user-named documents; here
there is exactly one file the person never names or locates themselves. Saving writes
to `calendar.fsti.tmp` first and only makes the new content visible via
`File.renameTo` (atomic on the same filesystem, with a copy-then-delete fallback for
the rare case it isn't) — a real atomic write, which Angerona's SAF-based storage can
never fully guarantee (see its `DEVELOPMENT.md`, round 4's "non-atomic writes" entry).

`data/CryptoManager.kt` mirrors Angerona's, with every hardening decision Angerona
only reached after several audit rounds already baked in from the start:
`getExistingKey()`/`getOrCreateKey()` split, GCM AAD over the format header, a
container-size guard inside `decrypt()` itself, a real `MIN_CONTAINER_BYTES` check in
`looksEncrypted()`, strict UTF-8 decoding, synchronized key access. The size limit is
5 MB (`MAX_PLAINTEXT_BYTES`), much larger than Angerona's 100 KB per note, since this
one file holds every event rather than a single document.

## Reminders (notifications)
`CalendarEvent.reminderMinutesBefore` (`Int?`, minutes before `startTime`, null = no
reminder) is the only addition to the data model — old calendar files without the
field deserialize fine (kotlinx.serialization's `ignoreUnknownKeys` + the field's
default `null`), so no format migration was needed.

The moving pieces, all under `data/` (scheduling helpers) and `receiver/`:
- `notification/NotificationHelper.kt` — owns the notification channel (created once,
  idempotently) and builds/shows the actual notification. Checks `POST_NOTIFICATIONS`
  itself before calling `notify()`, so a revoked permission just means no notification
  rather than a `SecurityException`. Its content is fixed and generic ("Reminder" / "You
  have an upcoming event") — it never receives or displays the event's title, time or
  description; only the event id and date travel through the `Intent` extras, and only
  to route a tap, not to render anything.
- `notification/ReminderScheduler.kt` — the only place that touches `AlarmManager`.
  `schedule(event)` always cancels any previous alarm for that event id first, then
  computes the trigger time from `date` + `startTime` − `reminderMinutesBefore` and
  arms a new one (skipped if that time is already in the past). Prefers
  `setExactAndAllowWhileIdle`, but checks `AlarmManager.canScheduleExactAlarms()`
  first (API 31+) and falls back to a plain inexact `set()` if that permission's been
  denied or revoked in system settings — a reminder should still eventually fire
  rather than silently not exist. `cancel(eventId)` and `rescheduleAll(events)` round
  out the API; matching an alarm to an event is by `PendingIntent` request code
  (`eventId.hashCode()`) plus target component, which Android compares without
  looking at intent extras — so cancel doesn't need the event's other fields.
- `receiver/ReminderReceiver.kt` — the `BroadcastReceiver` AlarmManager fires into;
  just hands off to `NotificationHelper.show()`.
- `receiver/BootReceiver.kt` — listens for `BOOT_COMPLETED` (AlarmManager forgets
  every alarm on reboot) and reschedules all of them. Uses `goAsync()` plus a
  background thread since it needs to decrypt the calendar file, which is I/O the
  main thread shouldn't block on. This is safe precisely because the Keystore key
  isn't gated behind user authentication (see `CryptoManager` above) — it's available
  as soon as the app's process can run, same as any other cold start.
- `FastiViewModel` re-arms every event's reminder on every cold start too (not just
  after a reboot) — cheap, and keeps alarms consistent even after a force-stop, which
  also silently drops `AlarmManager` state without a `BOOT_COMPLETED` broadcast.
- `MainActivity` requests `POST_NOTIFICATIONS` once on first launch (Android 13+, no
  rationale UI — a no-op if declined) and reads the tapped notification's date extra to
  jump `FastiScreen` to that event's day, via `launchMode="singleTask"` +
  `onNewIntent`.

**Privacy note**: the notification is deliberately content-free (see
`NotificationHelper` above) precisely to avoid the alternative — an OS-level
notification that shows the event's title in cleartext on the lock screen for anyone
nearby to read. The trade-off is a less informative notification (you know *something*
is coming up, not what); that was judged the right default for a calendar whose whole
point is that its contents are encrypted. The calendar file on disk is unaffected
either way; this is purely about what the notification itself displays.

## Scope decisions for v1
- **No recurrence.** Each event is a single, independent entry. Adding recurrence
  later means extending `CalendarEvent` with a recurrence rule and deciding how
  editing/deleting one instance of a recurring series should behave — real design
  work, deliberately deferred rather than half-built now.
- **Time entry is a plain `HH:mm` text field**, not a wheel/clock picker. Deliberately
  light for v1; revisit if manual entry proves annoying in practice.
- **Month grid is the only view.** Week/day/agenda views were considered and set
  aside for this pass (see the conversation that scoped this project) — the month
  grid was the one explicitly chosen for v1.
