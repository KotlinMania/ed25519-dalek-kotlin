package io.github.kotlinmania.ed25519dalek.internal

/**
 * Pure Kotlin implementation of the SHA-512 cryptographic hash algorithm (FIPS 180-4).
 */
internal class Sha512 {
    private val state = LongArray(8)
    private val buffer = ByteArray(128)
    private var bufferLength = 0
    private var totalBytes = 0L

    init {
        reset()
    }

    fun reset() {
        state[0] = 0x6a09e667f3bcc908uL.toLong()
        state[1] = 0xbb67ae8584caa73buL.toLong()
        state[2] = 0x3c6ef372fe94f82buL.toLong()
        state[3] = 0xa54ff53a5f1d36f1uL.toLong()
        state[4] = 0x510e527fade682d1uL.toLong()
        state[5] = 0x9b05688c2b3e6c1fuL.toLong()
        state[6] = 0x1f83d9abfb41bd6buL.toLong()
        state[7] = 0x5be0cd19137e2179uL.toLong()
        bufferLength = 0
        totalBytes = 0L
    }

    fun update(input: ByteArray, offset: Int = 0, length: Int = input.size) {
        if (length == 0) return
        var pos = offset
        var len = length
        totalBytes += len.toLong()

        if (bufferLength > 0) {
            val toCopy = minOf(len, 128 - bufferLength)
            input.copyInto(buffer, bufferLength, pos, pos + toCopy)
            bufferLength += toCopy
            pos += toCopy
            len -= toCopy
            if (bufferLength == 128) {
                processBlock(buffer, 0)
                bufferLength = 0
            }
        }

        while (len >= 128) {
            processBlock(input, pos)
            pos += 128
            len -= 128
        }

        if (len > 0) {
            input.copyInto(buffer, 0, pos, pos + len)
            bufferLength = len
        }
    }

    fun chainUpdate(input: ByteArray): Sha512 {
        update(input)
        return this
    }

    fun finalize(): ByteArray {
        val out = ByteArray(64)
        finalize(out, 0)
        return out
    }

    fun finalize(out: ByteArray, outOffset: Int = 0) {
        val totalBits = totalBytes * 8L
        // Append 0x80 byte
        buffer[bufferLength++] = 0x80.toByte()

        if (bufferLength > 112) {
            buffer.fill(0, bufferLength, 128)
            processBlock(buffer, 0)
            bufferLength = 0
        }

        buffer.fill(0, bufferLength, 120)
        // SHA-512 uses a 128-bit bit length; we support up to 64-bit length (high 64 bits = 0)
        for (i in 0 until 8) {
            buffer[120 + i] = ((totalBits ushr ((7 - i) * 8)) and 0xffL).toByte()
        }
        processBlock(buffer, 0)

        for (i in 0 until 8) {
            val v = state[i]
            for (j in 0 until 8) {
                out[outOffset + i * 8 + j] = ((v ushr ((7 - j) * 8)) and 0xffL).toByte()
            }
        }
    }

    fun clone(): Sha512 {
        val copy = Sha512()
        state.copyInto(copy.state)
        buffer.copyInto(copy.buffer)
        copy.bufferLength = bufferLength
        copy.totalBytes = totalBytes
        return copy
    }

    private fun processBlock(data: ByteArray, offset: Int) {
        val w = LongArray(80)
        for (i in 0 until 16) {
            val o = offset + i * 8
            w[i] = ((data[o].toLong() and 0xffL) shl 56) or
                ((data[o + 1].toLong() and 0xffL) shl 48) or
                ((data[o + 2].toLong() and 0xffL) shl 40) or
                ((data[o + 3].toLong() and 0xffL) shl 32) or
                ((data[o + 4].toLong() and 0xffL) shl 24) or
                ((data[o + 5].toLong() and 0xffL) shl 16) or
                ((data[o + 6].toLong() and 0xffL) shl 8) or
                (data[o + 7].toLong() and 0xffL)
        }

        for (i in 16 until 80) {
            val s0 = gamma0(w[i - 15])
            val s1 = gamma1(w[i - 2])
            w[i] = w[i - 16] + s0 + w[i - 7] + s1
        }

        var a = state[0]
        var b = state[1]
        var c = state[2]
        var d = state[3]
        var e = state[4]
        var f = state[5]
        var g = state[6]
        var h = state[7]

        for (i in 0 until 80) {
            val s1 = sigma1(e)
            val ch = (e and f) xor (e.inv() and g)
            val temp1 = h + s1 + ch + K[i] + w[i]
            val s0 = sigma0(a)
            val maj = (a and b) xor (a and c) xor (b and c)
            val temp2 = s0 + maj

            h = g
            g = f
            f = e
            e = d + temp1
            d = c
            c = b
            b = a
            a = temp1 + temp2
        }

        state[0] += a
        state[1] += b
        state[2] += c
        state[3] += d
        state[4] += e
        state[5] += f
        state[6] += g
        state[7] += h
    }

    companion object {
        fun digest(data: ByteArray): ByteArray {
            val sha = Sha512()
            sha.update(data)
            return sha.finalize()
        }

        private fun sigma0(x: Long): Long = x.rotateRight(28) xor x.rotateRight(34) xor x.rotateRight(39)

        private fun sigma1(x: Long): Long = x.rotateRight(14) xor x.rotateRight(18) xor x.rotateRight(41)

        private fun gamma0(x: Long): Long = x.rotateRight(1) xor x.rotateRight(8) xor (x ushr 7)

        private fun gamma1(x: Long): Long = x.rotateRight(19) xor x.rotateRight(61) xor (x ushr 6)

        private val K =
            longArrayOf(
                0x428a2f98d728ae22uL.toLong(),
                0x7137449123ef65cduL.toLong(),
                0xb5c0fbcfec4d3b2fuL.toLong(),
                0xe9b5dba58189dbbcuL.toLong(),
                0x3956c25bf348b538uL.toLong(),
                0x59f111f1b605d019uL.toLong(),
                0x923f82a4af194f9buL.toLong(),
                0xab1c5ed5da6d8118uL.toLong(),
                0xd807aa98a3030242uL.toLong(),
                0x12835b0145706fbeuL.toLong(),
                0x243185be4ee4b28cuL.toLong(),
                0x550c7dc3d5ffb4e2uL.toLong(),
                0x72be5d74f27b896fuL.toLong(),
                0x80deb1fe3b1696b1uL.toLong(),
                0x9bdc06a725c71235uL.toLong(),
                0xc19bf174cf692694uL.toLong(),
                0xe49b69c19ef14ad2uL.toLong(),
                0xefbe4786384f25e3uL.toLong(),
                0x0fc19dc68b8cd5b5uL.toLong(),
                0x240ca1cc77ac9c65uL.toLong(),
                0x2de92c6f592b0275uL.toLong(),
                0x4a7484aa6ea6e483uL.toLong(),
                0x5cb0a9dcbd41fbd4uL.toLong(),
                0x76f988da831153b5uL.toLong(),
                0x983e5152ee66dfabuL.toLong(),
                0xa831c66d2db43210uL.toLong(),
                0xb00327c898fb213fuL.toLong(),
                0xbf597fc7beef0ee4uL.toLong(),
                0xc6e00bf33da88fc2uL.toLong(),
                0xd5a79147930aa725uL.toLong(),
                0x06ca6351e003826fuL.toLong(),
                0x142929670a0e6e70uL.toLong(),
                0x27b70a8546d22ffcuL.toLong(),
                0x2e1b21385c26c926uL.toLong(),
                0x4d2c6dfc5ac42aeduL.toLong(),
                0x53380d139d95b3dfuL.toLong(),
                0x650a73548baf63deuL.toLong(),
                0x766a0abb3c77b2a8uL.toLong(),
                0x81c2c92e47edaee6uL.toLong(),
                0x92722c851482353buL.toLong(),
                0xa2bfe8a14cf10364uL.toLong(),
                0xa81a664bbc423001uL.toLong(),
                0xc24b8b70d0f89791uL.toLong(),
                0xc76c51a30654be30uL.toLong(),
                0xd192e819d6ef5218uL.toLong(),
                0xd69906245565a910uL.toLong(),
                0xf40e35855771202auL.toLong(),
                0x106aa07032bbd1b8uL.toLong(),
                0x19a4c116b8d2d0c8uL.toLong(),
                0x1e376c085141ab53uL.toLong(),
                0x2748774cdf8eeb99uL.toLong(),
                0x34b0bcb5e19b48a8uL.toLong(),
                0x391c0cb3c5c95a63uL.toLong(),
                0x4ed8aa4ae3418acbuL.toLong(),
                0x5b9cca4f7763e373uL.toLong(),
                0x682e6ff3d6b2b8a3uL.toLong(),
                0x748f82ee5defb2fcuL.toLong(),
                0x78a5636f43172f60uL.toLong(),
                0x84c87814a1f0ab72uL.toLong(),
                0x8cc702081a6439ecuL.toLong(),
                0x90befffa23631e28uL.toLong(),
                0xa4506cebde82bde9uL.toLong(),
                0xbef9a3f7b2c67915uL.toLong(),
                0xc67178f2e372532buL.toLong(),
                0xca273eceea26619cuL.toLong(),
                0xd186b8c721c0c207uL.toLong(),
                0xeada7dd6cde0eb1euL.toLong(),
                0xf57d4f7fee6ed178uL.toLong(),
                0x06f067aa72176fbauL.toLong(),
                0x0a637dc5a2c898a6uL.toLong(),
                0x113f9804bef90daeuL.toLong(),
                0x1b710b35131c471buL.toLong(),
                0x28db77f523047d84uL.toLong(),
                0x32caab7b40c72493uL.toLong(),
                0x3c9ebe0a15c9bebcuL.toLong(),
                0x431d67c49c100d4cuL.toLong(),
                0x4cc5d4becb3e42b6uL.toLong(),
                0x597f299cfc657e2auL.toLong(),
                0x5fcb6fab3ad6faecuL.toLong(),
                0x6c44198c4a475817uL.toLong(),
            )
    }
}
