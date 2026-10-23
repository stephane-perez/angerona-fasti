package com.stephaneperez.angerona.fasti.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Container-format checks that run before the Keystore is ever touched (the Android
 * Keystore itself isn't available in JVM unit tests).
 */
class CryptoManagerContainerTest {
    private val header = byteArrayOf('F'.code.toByte(), 'S'.code.toByte(), 'T'.code.toByte(), 'I'.code.toByte(), 1)
    private val minContainer = 5 + 12 + 16

    @Test
    fun looksEncrypted_requiresMagicVersionAndMinimumSize() {
        assertTrue(CryptoManager.looksEncrypted(header + ByteArray(minContainer - 5)))
        assertFalse(CryptoManager.looksEncrypted(header + ByteArray(minContainer - 6)))
        assertFalse(CryptoManager.looksEncrypted(ByteArray(0)))
        assertFalse(CryptoManager.looksEncrypted("{\"events\":[]} plain JSON, not ours".toByteArray()))

        val wrongVersion = header.copyOf().also { it[4] = 2 }
        assertFalse(CryptoManager.looksEncrypted(wrongVersion + ByteArray(minContainer)))
    }

    @Test
    fun decrypt_rejectsForeignAndOversizedData_withoutThrowing() {
        assertNull(CryptoManager.decrypt(ByteArray(0)))
        assertNull(CryptoManager.decrypt(ByteArray(100) { 7 }))
        assertNull(CryptoManager.decrypt(header + ByteArray(CryptoManager.MAX_PLAINTEXT_BYTES + 64)))
    }

    @Test
    fun decrypt_neverThrowsWhenNoKeyIsAvailable() {
        // Well-formed container, but no Keystore here: must come back as null, not crash.
        assertNull(CryptoManager.decrypt(header + ByteArray(minContainer)))
    }
}
