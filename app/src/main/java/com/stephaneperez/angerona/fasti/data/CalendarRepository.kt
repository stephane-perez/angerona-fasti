package com.stephaneperez.angerona.fasti.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

/**
 * Persists the whole calendar as a single encrypted file, `calendar.fsti`, in the app's
 * private storage (`context.filesDir`) — no Storage Access Framework, no file picker:
 * there's exactly one file, always in the same place, and the person never chooses a
 * name or location for it. This is a deliberate simplification versus Angerona (which
 * uses SAF, since it manages many user-chosen files) and buys a real correctness
 * property Angerona could never fully guarantee: a true atomic write. Because this is
 * a normal filesystem path rather than a SAF document, a write goes to a temp file
 * first and is only made visible via `File.renameTo` (atomic on the same filesystem),
 * so a write that fails partway never corrupts or truncates the previous good file.
 */
class CalendarRepository(context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val calendarFile = File(context.filesDir, "calendar.fsti")
    private val tempFile = File(context.filesDir, "calendar.fsti.tmp")

    /**
     * Loads the calendar. Returns an empty [CalendarData] if no file exists yet (first
     * launch), if the file is larger than [CryptoManager.MAX_PLAINTEXT_BYTES]-derived
     * bounds, or if it can't be decrypted (see [LoadResult]).
     */
    fun load(): LoadResult {
        if (!calendarFile.exists()) return LoadResult.Success(CalendarData())

        val bytes = runCatching { calendarFile.readBytes() }.getOrNull()
            ?: return LoadResult.ReadFailed

        if (bytes.size > CryptoManager.MAX_PLAINTEXT_BYTES + 64) {
            // A little slack over the plaintext bound for header/IV/tag overhead —
            // this file is entirely ours, so there's no "legacy plain format" case to
            // worry about the way Angerona has to for user-picked .txt files.
            return LoadResult.TooLarge
        }

        if (!CryptoManager.looksEncrypted(bytes)) return LoadResult.NotEncrypted

        val decrypted = CryptoManager.decrypt(bytes) ?: return LoadResult.DecryptFailed

        val data = runCatching { json.decodeFromString<CalendarData>(decrypted) }.getOrNull()
            ?: return LoadResult.CorruptJson

        return LoadResult.Success(data)
    }

    /**
     * Encrypts and writes [data], atomically. Returns true on success. On failure, the
     * previous file (if any) is left completely untouched — see the class doc comment.
     */
    fun save(data: CalendarData): Boolean {
        val jsonText = json.encodeToString(data)
        if (jsonText.toByteArray(Charsets.UTF_8).size > CryptoManager.MAX_PLAINTEXT_BYTES) {
            return false
        }

        val encrypted = CryptoManager.encrypt(jsonText)

        return runCatching {
            tempFile.writeBytes(encrypted)
            if (!tempFile.renameTo(calendarFile)) {
                // renameTo can fail across filesystems/some edge cases; fall back to a
                // copy-then-delete, still leaving the original untouched until the new
                // content is fully written.
                tempFile.copyTo(calendarFile, overwrite = true)
                tempFile.delete()
            }
            true
        }.getOrElse { false }
    }
}

/** Distinguishes *why* a load didn't produce data, without ever throwing. */
sealed interface LoadResult {
    data class Success(val data: CalendarData) : LoadResult
    data object ReadFailed : LoadResult
    data object TooLarge : LoadResult
    data object NotEncrypted : LoadResult
    data object DecryptFailed : LoadResult
    data object CorruptJson : LoadResult
}
