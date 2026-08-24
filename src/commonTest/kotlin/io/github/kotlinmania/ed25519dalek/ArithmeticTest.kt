package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.EdwardsPoint
import io.github.kotlinmania.ed25519dalek.internal.FieldElement
import io.github.kotlinmania.ed25519dalek.internal.Scalar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ArithmeticTest {
    @Test
    fun testFieldElementBasic() {
        val zero = FieldElement.ZERO
        val one = FieldElement.ONE

        assertEquals(one, zero.add(one))
        assertEquals(zero, one.sub(one))
        assertEquals(one, one.mul(one))
        assertEquals(one, one.square())
        assertEquals(one, one.invert())
    }

    @Test
    fun testFieldElementInversion() {
        val bytes = ByteArray(32)
        bytes[0] = 42
        val fe = FieldElement.fromBytes(bytes)
        val inv = fe.invert()
        val prod = fe.mul(inv)
        assertEquals(FieldElement.ONE, prod)
    }

    @Test
    fun testScalarArithmetic() {
        val zero = Scalar.ZERO
        val one = Scalar.ONE

        assertEquals(one, zero.add(one))
        assertEquals(zero, one.sub(one))
        assertEquals(one, one.mul(one))

        val s2 = one.add(one)
        val s3 = s2.add(one)
        val s6 = s2.mul(s3)

        val s5 = s6.sub(one)
        assertEquals(s2.add(s3), s5)
    }

    @Test
    fun testBasepointDecompression() {
        val bp = EdwardsPoint.BASEPOINT
        assertNotNull(bp)
        assertFalse(bp.isIdentity())

        val compressed = bp.compress()
        val decompressed = compressed.decompress()
        assertNotNull(decompressed)
        assertEquals(bp, decompressed)
    }

    @Test
    fun testPointAdditionAndScalarMul() {
        val bp = EdwardsPoint.BASEPOINT
        val bp2 = bp.add(bp)
        val bpDouble = bp.double()
        assertEquals(bp2, bpDouble)

        val s2 = Scalar.from(2)
        val bpMul2 = bp.mul(s2)
        assertEquals(bp2, bpMul2)

        val s0 = Scalar.ZERO
        val identity = bp.mul(s0)
        assertTrue(identity.isIdentity())
    }
}
