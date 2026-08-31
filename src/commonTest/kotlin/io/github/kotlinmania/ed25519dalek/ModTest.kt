// port-lint: tests ed25519-dalek/src/lib.rs
package io.github.kotlinmania.ed25519dalek

import kotlin.test.Test
import kotlin.test.assertEquals

class ModTest {
    @Test
    fun testKeyAlgorithmConstant() {
        assertEquals("Ed25519", ED25519_KEY_ALGORITHM)
    }
}
