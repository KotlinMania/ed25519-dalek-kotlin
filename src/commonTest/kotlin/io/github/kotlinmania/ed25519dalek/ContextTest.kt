// port-lint: tests context.rs
package io.github.kotlinmania.ed25519dalek

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ContextTest {
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
