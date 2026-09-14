package com.example

import com.example.data.security.SecurityCryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SecurityCryptoManagerTest {

    @Test
    fun testPasswordHashingFormatAndUniqueness() {
        val rawPassword = "TestSecretPassword#786"
        val hash1 = SecurityCryptoManager.hashPassword(rawPassword)
        val hash2 = SecurityCryptoManager.hashPassword(rawPassword)

        // Must start with salted prefix
        assertTrue(hash1.startsWith("sha256:salt16:v1:"))
        assertTrue(hash2.startsWith("sha256:salt16:v1:"))

        // Salts must be unique (randomized) so hashes for the same password differ
        assertNotEquals(hash1, hash2)

        // Raw password must not appear in plaintext anywhere in the hash
        assertFalse(hash1.contains(rawPassword))
    }

    @Test
    fun testPasswordVerificationSuccess() {
        val rawPassword = "SuperSecurePassword123!"
        val hash = SecurityCryptoManager.hashPassword(rawPassword)

        assertTrue(SecurityCryptoManager.verifyPassword(rawPassword, hash))
    }

    @Test
    fun testPasswordVerificationFailureOnWrongInput() {
        val rawPassword = "SuperSecurePassword123!"
        val hash = SecurityCryptoManager.hashPassword(rawPassword)

        assertFalse(SecurityCryptoManager.verifyPassword("WrongPassword!", hash))
        assertFalse(SecurityCryptoManager.verifyPassword("", hash))
    }

    @Test
    fun testLegacyPlaintextBackwardCompatibility() {
        // Legacy demo passwords before hashing
        val legacyStored = "AdminPass@786"
        assertTrue(SecurityCryptoManager.verifyPassword("AdminPass@786", legacyStored))
        assertFalse(SecurityCryptoManager.verifyPassword("BadPassword", legacyStored))
    }

    @Test
    fun testEncryptionAndDecryptionRoundtrip() {
        val secretToken = "AlnoorAdminPasscode#7860"
        val encrypted = SecurityCryptoManager.encrypt(secretToken)

        // Must not be plaintext
        assertNotEquals(secretToken, encrypted)
        assertTrue(encrypted.startsWith("enc:gcm256:v1:"))

        // Decryption roundtrip must match original exactly
        val decrypted = SecurityCryptoManager.decrypt(encrypted)
        assertEquals(secretToken, decrypted)
    }

    @Test
    fun testLegacyUnencryptedDecryptionCompatibility() {
        val legacyPlainPin = "7860"
        val decrypted = SecurityCryptoManager.decrypt(legacyPlainPin)
        assertEquals("7860", decrypted)
    }
}
