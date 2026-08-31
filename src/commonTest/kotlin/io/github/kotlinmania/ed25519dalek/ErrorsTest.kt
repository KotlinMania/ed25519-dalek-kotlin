// port-lint: tests ed25519-dalek/src/errors.rs
package io.github.kotlinmania.ed25519dalek

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ErrorsTest {
    @Test
    fun testInternalErrorDisplayStrings() {
        assertEquals("Cannot decompress Edwards point", InternalError.PointDecompression.toString())
        assertEquals("Cannot use scalar with high-bit set", InternalError.ScalarFormat.toString())
        assertEquals(
            "SecretKey must be 32 bytes in length",
            InternalError.BytesLength("SecretKey", 32).toString(),
        )
        assertEquals("Verification equation was not satisfied", InternalError.Verify.toString())
        assertEquals(
            "Arrays must be the same length: sigs has length 2, msgs has length 3, pks has length 2.",
            InternalError.ArrayLength("sigs", 2, "msgs", 3, "pks", 2).toString(),
        )
        assertEquals(
            "An ed25519ph signature can only take up to 255 octets of context",
            InternalError.PrehashedContextLength.toString(),
        )
        assertEquals("Mismatched Keypair detected", InternalError.MismatchedKeypair.toString())
    }

    @Test
    fun testSignatureErrorFromInternalError() {
        val err = SignatureError.from(InternalError.ScalarFormat)
        assertNotNull(err.message)
        assertTrue(err.message!!.contains("Cannot use scalar with high-bit set"))
        assertEquals(InternalError.ScalarFormat, err.internal)
    }
}
