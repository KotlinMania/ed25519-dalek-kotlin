package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.Sha512
import kotlin.test.Test
import kotlin.test.assertEquals

class HazmatTest {
    @Test
    fun testSignVerifyRaw() {
        val rawBytes = ByteArray(64) { (it * 7).toByte() }
        val esk = ExpandedSecretKey.fromBytes(rawBytes)
        val vk = VerifyingKey.fromExpandedSecretKey(esk)

        val msg = "Then one day, a piano fell on my head".encodeToByteArray()

        val sig = rawSign(esk, msg, vk)
        rawVerify(vk, msg, sig)
    }

    @Test
    fun testSignVerifyPrehashedRaw() {
        val rawBytes = ByteArray(64) { (it * 13).toByte() }
        val esk = ExpandedSecretKey.fromBytes(rawBytes)
        val vk = VerifyingKey.fromExpandedSecretKey(esk)

        val msg = "And then I got trampled by a herd of buffalo".encodeToByteArray()
        val h = Sha512()
        h.update(msg)
        val digest = h.finalize()

        val ctx = "consequences".encodeToByteArray()

        val sig = rawSignPrehashed(esk, digest, vk, ctx)
        rawVerifyPrehashed(vk, digest, ctx, sig)
    }

    @Test
    fun testSignByUpdate() {
        val rawBytes = ByteArray(64) { (it * 19).toByte() }
        val esk = ExpandedSecretKey.fromBytes(rawBytes)
        val vk = VerifyingKey.fromExpandedSecretKey(esk)

        val msg = "realistic".encodeToByteArray()
        val goodSig = rawSign(esk, msg, vk)

        val sig1 =
            rawSignByUpdate(
                esk,
                { h -> h.update(msg) },
                vk,
            )
        assertEquals(goodSig, sig1)

        val sig2 =
            rawSignByUpdate(
                esk,
                { h ->
                    h.update(msg.copyOfRange(0, 1))
                    h.update(msg.copyOfRange(1, msg.size))
                },
                vk,
            )
        assertEquals(goodSig, sig2)
    }
}
