// port-lint: tests signature.rs
package io.github.kotlinmania.ed25519dalek

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SignatureTest {
    @Test
    fun testSignatureComponents() {
        val r = ByteArray(32) { (it + 1).toByte() }
        val s = ByteArray(32) { 0 } // scalar 0 is valid (< l)

        val sig = Signature.fromComponents(r, s)
        assertEquals(64, sig.toBytes().size)
        assertEquals(r.toList(), sig.rBytes().toList())
        assertEquals(s.toList(), sig.sBytes().toList())

        val (compR, compS) = sig.toComponents()
        assertEquals(r.toList(), compR.toList())
        assertEquals(s.toList(), compS.toList())
    }

    @Test
    fun testInvalidSignatureLength() {
        assertFailsWith<SignatureError> {
            Signature.fromBytes(ByteArray(63))
        }
        assertFailsWith<SignatureError> {
            Signature.fromBytes(ByteArray(65))
        }
    }

    @Test
    fun testFromSliceAndToVec() {
        val bytes = ByteArray(64)
        val sig1 = Signature.fromBytes(bytes)
        val sig2 = Signature.fromSlice(bytes)
        assertEquals(sig1, sig2)
        assertEquals(sig1.hashCode(), sig2.hashCode())
        assertTrue(sig1.toBytes().contentEquals(sig2.toVec()))
        assertTrue(sig1.toString().startsWith("Signature("))
    }
}
