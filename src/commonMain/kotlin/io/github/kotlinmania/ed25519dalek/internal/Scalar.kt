package io.github.kotlinmania.ed25519dalek.internal

/**
 * An element of the scalar field GF(l) where
 * l = 2^252 + 27742317777372353535851937790883648493
 *   = 0x1000000000000000000000000000000014def9dea2f79cd65812631a5cf5d3ed
 */
internal class Scalar private constructor(
    // 8 32-bit words, little-endian, canonical representation in [0, l)
    internal val w: IntArray,
) {
    init {
        require(w.size == 8)
    }

    companion object {
        // l in 32-bit little-endian words
        // ed d3 f5 5c 1a 63 12 58 d6 9c f7 a2 de f9 de 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 10
        internal val L_WORDS =
            intArrayOf(
                0x5cf5d3ed.toInt(),
                0x5812631a.toInt(),
                0xa2f79cd6.toInt(),
                0x14def9de.toInt(),
                0x00000000,
                0x00000000,
                0x00000000,
                0x10000000,
            )

        val ZERO = Scalar(IntArray(8))
        val ONE = Scalar(intArrayOf(1, 0, 0, 0, 0, 0, 0, 0))

        fun fromCanonicalBytes(bytes: ByteArray): Scalar? {
            if (bytes.size != 32) return null
            val words = bytesToWords(bytes)
            if (isGreaterThanOrEqualL(words)) return null
            return Scalar(words)
        }

        fun fromBits(bytes: ByteArray): Scalar {
            require(bytes.size == 32)
            val copy = bytes.copyOf()
            copy[31] = (copy[31].toInt() and 0x7F).toByte()
            val words = bytesToWords(copy)
            // Reduce mod l if >= l
            return reduce256(words)
        }

        fun fromHash(digest: ByteArray): Scalar {
            require(digest.size == 64)
            return fromBytesModOrderWide(digest)
        }

        fun from(v: Long): Scalar {
            val words = IntArray(8)
            words[0] = v.toInt()
            words[1] = (v ushr 32).toInt()
            return reduce256(words)
        }

        fun fromBytesModOrderWide(bytes: ByteArray): Scalar {
            require(bytes.size == 64)
            val w16 = IntArray(16)
            for (i in 0 until 16) {
                w16[i] = load32(bytes, i * 4)
            }
            return reduce512(w16)
        }

        private fun load32(bytes: ByteArray, offset: Int): Int {
            val b0 = bytes[offset].toInt() and 0xFF
            val b1 = bytes[offset + 1].toInt() and 0xFF
            val b2 = bytes[offset + 2].toInt() and 0xFF
            val b3 = bytes[offset + 3].toInt() and 0xFF
            return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
        }

        private fun bytesToWords(bytes: ByteArray): IntArray {
            val words = IntArray(8)
            for (i in 0 until 8) {
                words[i] = load32(bytes, i * 4)
            }
            return words
        }

        private fun isGreaterThanOrEqualL(w: IntArray): Boolean {
            for (i in 7 downTo 0) {
                val a = w[i].toLong() and 0xFFFFFFFFL
                val b = L_WORDS[i].toLong() and 0xFFFFFFFFL
                if (a > b) return true
                if (a < b) return false
            }
            return true
        }

        private fun subL(w: IntArray): Boolean {
            var borrow = 0L
            val res = IntArray(8)
            for (i in 0 until 8) {
                val diff = (w[i].toLong() and 0xFFFFFFFFL) - (L_WORDS[i].toLong() and 0xFFFFFFFFL) - borrow
                res[i] = diff.toInt()
                borrow = if (diff < 0) 1L else 0L
            }
            if (borrow == 0L) {
                res.copyInto(w)
                return true
            }
            return false
        }

        private fun addL(w: IntArray) {
            var carry = 0L
            for (i in 0 until 8) {
                val sum = (w[i].toLong() and 0xFFFFFFFFL) + (L_WORDS[i].toLong() and 0xFFFFFFFFL) + carry
                w[i] = sum.toInt()
                carry = sum ushr 32
            }
        }

        private fun reduce256(w: IntArray): Scalar {
            val cur = w.copyOf()
            while (isGreaterThanOrEqualL(cur)) {
                subL(cur)
            }
            return Scalar(cur)
        }

        // Reduces a 512-bit integer (16 32-bit words) modulo l
        private fun reduce512(w: IntArray): Scalar {
            // Barrett-like reduction or shift-and-subtract / division
            // Since 512-bit division is fast with BigInteger / binary division:
            var rem = w.copyOf() // 16 words
            // In little-endian, bit length is at most 512
            for (bit in 511 downTo 252) {
                val wordIdx = bit / 32
                val bitIdx = bit % 32
                if ((rem[wordIdx] and (1 shl bitIdx)) != 0) {
                    // Subtract l shifted by (bit - 252)
                    subtractShiftedL(rem, bit - 252)
                }
            }
            val res = IntArray(8)
            rem.copyInto(res, 0, 0, 8)
            while (isGreaterThanOrEqualL(res)) {
                subL(res)
            }
            return Scalar(res)
        }

        private fun subtractShiftedL(rem: IntArray, shift: Int): Boolean {
            val shiftWords = shift / 32
            val shiftBits = shift % 32

            // Build shifted L (9 words)
            val shifted = IntArray(16)
            var carry = 0L
            for (i in 0 until 8) {
                val lw = (L_WORDS[i].toLong() and 0xFFFFFFFFL) shl shiftBits
                val total = lw or carry
                shifted[i + shiftWords] = total.toInt()
                carry = total ushr 32
            }
            if (shiftWords + 8 < 16) {
                shifted[shiftWords + 8] = carry.toInt()
            }

            var borrow = 0L
            for (i in shiftWords until 16) {
                val diff = (rem[i].toLong() and 0xFFFFFFFFL) - (shifted[i].toLong() and 0xFFFFFFFFL) - borrow
                rem[i] = diff.toInt()
                borrow = if (diff < 0) 1L else 0L
            }
            return borrow == 0L
        }
    }

    fun add(other: Scalar): Scalar {
        val res = IntArray(8)
        var carry = 0L
        for (i in 0 until 8) {
            val sum = (w[i].toLong() and 0xFFFFFFFFL) + (other.w[i].toLong() and 0xFFFFFFFFL) + carry
            res[i] = sum.toInt()
            carry = sum ushr 32
        }
        if (carry > 0 || isGreaterThanOrEqualL(res)) {
            subL(res)
        }
        return Scalar(res)
    }

    fun sub(other: Scalar): Scalar {
        val res = IntArray(8)
        var borrow = 0L
        for (i in 0 until 8) {
            val diff = (w[i].toLong() and 0xFFFFFFFFL) - (other.w[i].toLong() and 0xFFFFFFFFL) - borrow
            res[i] = diff.toInt()
            borrow = if (diff < 0) 1L else 0L
        }
        if (borrow > 0) {
            addL(res)
        }
        return Scalar(res)
    }

    fun negate(): Scalar {
        if (this == ZERO) return ZERO
        val res = IntArray(8)
        var borrow = 0L
        for (i in 0 until 8) {
            val diff = (L_WORDS[i].toLong() and 0xFFFFFFFFL) - (w[i].toLong() and 0xFFFFFFFFL) - borrow
            res[i] = diff.toInt()
            borrow = if (diff < 0) 1L else 0L
        }
        return Scalar(res)
    }

    fun mul(other: Scalar): Scalar {
        val prod = IntArray(16)
        for (i in 0 until 8) {
            var carry = 0L
            val a = w[i].toLong() and 0xFFFFFFFFL
            for (j in 0 until 8) {
                val b = other.w[j].toLong() and 0xFFFFFFFFL
                val cur = (prod[i + j].toLong() and 0xFFFFFFFFL) + a * b + carry
                prod[i + j] = cur.toInt()
                carry = cur ushr 32
            }
            prod[i + 8] = carry.toInt()
        }
        return reduce512(prod)
    }

    fun toByteArray(): ByteArray {
        val out = ByteArray(32)
        for (i in 0 until 8) {
            val v = w[i]
            out[i * 4] = v.toByte()
            out[i * 4 + 1] = (v shr 8).toByte()
            out[i * 4 + 2] = (v shr 16).toByte()
            out[i * 4 + 3] = (v shr 24).toByte()
        }
        return out
    }

    fun asBytes(): ByteArray = toByteArray()

    fun getBit(i: Int): Int {
        val wordIdx = i / 32
        val bitIdx = i % 32
        return (w[wordIdx] ushr bitIdx) and 1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Scalar) return false
        return w.contentEquals(other.w)
    }

    override fun hashCode(): Int = w.contentHashCode()

    override fun toString(): String =
        toByteArray().joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
}
