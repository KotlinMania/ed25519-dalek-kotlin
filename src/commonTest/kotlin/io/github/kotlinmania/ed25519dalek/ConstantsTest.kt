// port-lint: tests ed25519-dalek/src/constants.rs
package io.github.kotlinmania.ed25519dalek

import kotlin.test.Test
import kotlin.test.assertEquals

class ConstantsTest {
    @Test
    fun testConstantsLengths() {
        assertEquals(64, SIGNATURE_LENGTH)
        assertEquals(32, SECRET_KEY_LENGTH)
        assertEquals(32, PUBLIC_KEY_LENGTH)
        assertEquals(64, KEYPAIR_LENGTH)
        assertEquals(64, EXPANDED_SECRET_KEY_LENGTH)
    }
}
