package com.stephaneperez.angerona.fasti.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts and decrypts the calendar's JSON content with an AES-256-GCM key held in the
 * Android Keystore.
 *
 * This is the same design used by Angerona (see that project's DEVELOPMENT.md for the
 * full audit history that shaped it), applied here from the start rather than
 * discovered through several rounds of review:
 * - `getExistingKey()` is split from `getOrCreateKey()` so a failed decrypt never mints
 *   a key as a side effect.
 * - The format header is authenticated via GCM's AAD, not just the ciphertext.
 * - A container-size guard lives inside `decrypt()` itself, not only in callers.
 * - `looksEncrypted()` checks the real minimum container size (header + IV + tag), not
 *   just header + IV.
 * - Decrypted bytes are strictly validated as UTF-8 (a lenient decode would silently
 *   replace invalid sequences with `�` instead of signaling corruption).
 * - Key access is synchronized.
 *
 * The key is generated once (lazily, on first save), is non-exportable, and scoped to
 * this app's UID: another app cannot access it, and this file cannot be decrypted on
 * another device without the corresponding key. It is not recoverable across an app
 * uninstall or a factory reset: a fresh install always gets a fresh key.
 */
object CryptoManager {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "angerona_fasti_calendar_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_TAG_LENGTH_BYTES = GCM_TAG_LENGTH_BITS / 8
    private const val IV_LENGTH_BYTES = 12

    /**
     * Upper bound on the calendar's JSON payload. Generous relative to a single note
     * (this holds every event, not one document) — comfortably thousands of events —
     * while still refusing an unbounded file rather than risking an OOM.
     */
    const val MAX_PLAINTEXT_BYTES = 5 * 1024 * 1024 // 5 MB

    // On-disk container: "FSTI" magic + 1-byte format version + 12-byte GCM IV +
    // ciphertext (the 16-byte GCM auth tag is appended to the ciphertext by the cipher).
    private val MAGIC = byteArrayOf('F'.code.toByte(), 'S'.code.toByte(), 'T'.code.toByte(), 'I'.code.toByte())
    private const val FORMAT_VERSION: Byte = 1
    private const val HEADER_LENGTH = 4 + 1
    private val HEADER = MAGIC + byteArrayOf(FORMAT_VERSION)

    private const val MAX_CONTAINER_BYTES = HEADER_LENGTH + IV_LENGTH_BYTES + MAX_PLAINTEXT_BYTES + GCM_TAG_LENGTH_BYTES
    private const val MIN_CONTAINER_BYTES = HEADER_LENGTH + IV_LENGTH_BYTES + GCM_TAG_LENGTH_BYTES

    private val keyLock = Any()

    /** Only used by [encrypt] — decryption must never have the side effect of minting a key. */
    private fun getOrCreateKey(): SecretKey = synchronized(keyLock) {
        getExistingKeyLocked() ?: run {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    private fun getExistingKey(): SecretKey? = synchronized(keyLock) { getExistingKeyLocked() }

    private fun getExistingKeyLocked(): SecretKey? {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        return keyStore.getKey(KEY_ALIAS, null) as? SecretKey
    }

    /** Encrypts [plainText] (UTF-8) into the on-disk container format described above. */
    fun encrypt(plainText: String): ByteArray {
        val plainBytes = plainText.toByteArray(Charsets.UTF_8)
        require(plainBytes.size <= MAX_PLAINTEXT_BYTES) { "Text exceeds MAX_PLAINTEXT_BYTES" }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        cipher.updateAAD(HEADER)
        val iv = cipher.iv
        require(iv.size == IV_LENGTH_BYTES) { "Unexpected GCM IV length: ${iv.size}" }
        val ciphertext = cipher.doFinal(plainBytes)
        return HEADER + iv + ciphertext
    }

    /** True if [data] starts with our magic header, version, and a plausible minimum length. */
    fun looksEncrypted(data: ByteArray): Boolean {
        if (data.size < MIN_CONTAINER_BYTES) return false
        for (i in MAGIC.indices) if (data[i] != MAGIC[i]) return false
        return data[MAGIC.size] == FORMAT_VERSION
    }

    /**
     * Attempts to decrypt [data] written by [encrypt]. Returns null if [data] doesn't
     * look like one of ours, is larger than this class could ever have produced, or if
     * decryption fails for any reason (key lost, corrupted data, tampering). Never
     * throws. Only ever reads an existing Keystore key — never creates one as a side
     * effect of a failed open.
     */
    fun decrypt(data: ByteArray): String? {
        if (!looksEncrypted(data)) return null
        if (data.size > MAX_CONTAINER_BYTES) return null

        return runCatching {
            val key = getExistingKey() ?: return@runCatching null

            val iv = data.copyOfRange(HEADER_LENGTH, HEADER_LENGTH + IV_LENGTH_BYTES)
            val ciphertext = data.copyOfRange(HEADER_LENGTH + IV_LENGTH_BYTES, data.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            cipher.updateAAD(HEADER)
            val plainBytes = cipher.doFinal(ciphertext)
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(plainBytes))
                .toString()
        }.getOrNull()
    }
}
