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

## Scope decisions for v1
- **No recurrence.** Each event is a single, independent entry. Adding recurrence
  later means extending `CalendarEvent` with a recurrence rule and deciding how
  editing/deleting one instance of a recurring series should behave — real design
  work, deliberately deferred rather than half-built now.
- **No reminders/notifications.** Would need a scheduling mechanism
  (`AlarmManager`/`WorkManager`) and a notification permission (Android 13+) — a
  meaningfully bigger surface than v1's scope.
- **Time entry is a plain `HH:mm` text field**, not a wheel/clock picker. Deliberately
  light for v1; revisit if manual entry proves annoying in practice.
- **Month grid is the only view.** Week/day/agenda views were considered and set
  aside for this pass (see the conversation that scoped this project) — the month
  grid was the one explicitly chosen for v1.
