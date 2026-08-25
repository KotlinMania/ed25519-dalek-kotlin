// port-lint: tests context.rs
package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.Sha512
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ContextTest {
    @Test
    fun contextCorrectness() {
        val dummySeed = ByteArray(32) { (it + 1).toByte() }
        val signingKey = SigningKey.fromBytes(dummySeed)
        val verifyingKey = signingKey.verifyingKey()

        val contextStr = "Local Channel 3".encodeToByteArray()
        val hasher = Sha512()
        hasher.update("Stay tuned for more news at 7".encodeToByteArray())
        val prehashedMessage = hasher.finalize()

        // Signer
        val signingContext = signingKey.withContext(contextStr)
        val signature = signingContext.signDigest(prehashedMessage)

        // Verifier
        val verifyingContext = verifyingKey.withContext(contextStr)
        val verified = verifyingContext.verifyDigest(prehashedMessage, signature)

        assertTrue(verified)
    }

    @Test
    fun testContextCreationAndAccess() {
        val dummyKey = "test-key"
        val contextBytes = "Local Channel 3".encodeToByteArray()
        val context = Context.new(dummyKey, contextBytes)

        assertEquals("test-key", context.key())
        assertContentEquals(contextBytes, context.value())
    }

    @Test
    fun testContextMaxLength() {
        assertEquals(255, Context.MAX_LENGTH)
        val validBytes = ByteArray(255) { it.toByte() }
        val context = Context.new("key", validBytes)
        assertContentEquals(validBytes, context.value())

        val oversizedBytes = ByteArray(256) { it.toByte() }
        assertFailsWith<SignatureError> {
            Context.new("key", oversizedBytes)
        }
    }
}
