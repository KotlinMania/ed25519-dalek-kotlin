// port-lint: tests ed25519-dalek/src/hazmat.rs
package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.Sha512
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class HazmatTest {
    private fun randomExpandedSecretKey(): ExpandedSecretKey {
        val bytes = Random.nextBytes(64)
        return ExpandedSecretKey.fromBytes(bytes)
    }

    @Test
    fun signVerifyNonspec() {
        val esk = randomExpandedSecretKey()
        val vk = VerifyingKey.fromExpandedSecretKey(esk)

        val msg = "Then one day, a piano fell on my head".encodeToByteArray()

        val sig = rawSign(esk, msg, vk)
        rawVerify(vk, msg, sig)
    }

    @Test
    fun signVerifyPrehashedNonspec() {
        val esk = randomExpandedSecretKey()
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
    fun signByupdate() {
        val esk = randomExpandedSecretKey()
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
