// port-lint: tests zeroize
package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.Scalar
import io.github.kotlinmania.zeroize.Zeroize
import io.github.kotlinmania.zeroize.ZeroizeOnDrop
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse

class ZeroizeTest {
    @Test
    fun testSigningKeyZeroize() {
        val seed = ByteArray(32) { (it + 1).toByte() }
        val signingKey = SigningKey.fromBytes(seed)

        val asZeroize: Zeroize = signingKey
        val asZeroizeOnDrop: ZeroizeOnDrop = signingKey
        val asAutoCloseable: AutoCloseable = signingKey
        asZeroize.hashCode()
        asZeroizeOnDrop.hashCode()
        asAutoCloseable.hashCode()

        // Before zeroize, bytes match the seed
        assertContentEquals(seed, signingKey.asBytes())

        signingKey.zeroize()

        // After zeroize, bytes must be all zeros
        val expectedZeros = ByteArray(32)
        assertContentEquals(expectedZeros, signingKey.asBytes())
        assertContentEquals(expectedZeros, signingKey.toBytes())
    }

    @Test
    fun testSigningKeyAutoCloseable() {
        val seed = ByteArray(32) { (it + 42).toByte() }
        val key = SigningKey.fromBytes(seed)

        key.use { k ->
            assertFalse(k.asBytes().all { it == 0.toByte() })
        }

        // Closed via .use {}, should now be zeroized
        val expectedZeros = ByteArray(32)
        assertContentEquals(expectedZeros, key.asBytes())
    }

    @Test
    fun testExpandedSecretKeyZeroize() {
        val bytes = ByteArray(64) { (it + 7).toByte() }
        val esk = ExpandedSecretKey.fromBytes(bytes)

        val asZeroize: Zeroize = esk
        val asZeroizeOnDrop: ZeroizeOnDrop = esk
        val asAutoCloseable: AutoCloseable = esk
        asZeroize.hashCode()
        asZeroizeOnDrop.hashCode()
        asAutoCloseable.hashCode()

        assertFalse(esk.hashPrefix.all { it == 0.toByte() })
        assertFalse(esk.scalarBytes().all { it == 0.toByte() })

        esk.zeroize()

        val expectedZeros32 = ByteArray(32)
        assertContentEquals(expectedZeros32, esk.hashPrefix)
        assertContentEquals(expectedZeros32, esk.scalarBytes())
    }

    @Test
    fun testExpandedSecretKeyAutoCloseable() {
        val bytes = ByteArray(64) { (it + 13).toByte() }
        val esk = ExpandedSecretKey.fromBytes(bytes)

        esk.use { e ->
            assertFalse(e.hashPrefix.all { it == 0.toByte() })
        }

        val expectedZeros32 = ByteArray(32)
        assertContentEquals(expectedZeros32, esk.hashPrefix)
        assertContentEquals(expectedZeros32, esk.scalarBytes())
    }

    @Test
    fun testScalarZeroize() {
        val scalar = Scalar.from(0x123456789ABCDEFL)
        assertFalse(scalar.toByteArray().all { it == 0.toByte() })

        scalar.zeroize()

        val expectedZeros = ByteArray(32)
        assertContentEquals(expectedZeros, scalar.toByteArray())
    }
}
