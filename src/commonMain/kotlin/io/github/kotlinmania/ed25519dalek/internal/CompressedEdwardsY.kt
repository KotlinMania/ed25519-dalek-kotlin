package io.github.kotlinmania.ed25519dalek.internal

/**
 * An Edwards point compressed to 32 bytes (the Y-coordinate plus a sign bit for X).
 */
internal class CompressedEdwardsY(
    private val bytes: ByteArray,
) {
    init {
        require(bytes.size == 32)
    }

    fun asBytes(): ByteArray = bytes.copyOf()

    fun toByteArray(): ByteArray = bytes.copyOf()

    fun decompress(): EdwardsPoint? {
        val copy = bytes.copyOf()
        val signBit = (copy[31].toInt() and 0x80) != 0
        copy[31] = (copy[31].toInt() and 0x7F).toByte()

        val y = FieldElement.fromBytes(copy)
        // Check if y is non-canonical (i.e. y >= p)
        val yBytes = y.toByteArray()
        if (!yBytes.contentEquals(copy)) {
            return null
        }

        // u = y^2 - 1
        val y2 = y.square()
        val u = y2.sub(FieldElement.ONE)
        // v = d * y^2 + 1
        val v = FieldElement.D.mul(y2).add(FieldElement.ONE)

        val (isSquare, xCandidate) = FieldElement.sqrtRatioM1(u, v)
        if (!isSquare) return null

        var x = xCandidate
        if (x.isNegative() != signBit) {
            x = x.negate()
        }

        return EdwardsPoint(x, y, FieldElement.ONE, x.mul(y))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompressedEdwardsY) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String =
        "CompressedEdwardsY(" + bytes.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') } + ")"
}
