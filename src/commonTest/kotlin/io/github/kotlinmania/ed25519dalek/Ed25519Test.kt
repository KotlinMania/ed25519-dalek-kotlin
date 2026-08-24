package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.CompressedEdwardsY
import io.github.kotlinmania.ed25519dalek.internal.Sha512
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Ed25519Test {
    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.replace(" ", "").replace("\n", "")
        val result = ByteArray(clean.length / 2)
        for (i in result.indices) {
            val index = i * 2
            val v = clean.substring(index, index + 2).toInt(16)
            result[i] = v.toByte()
        }
        return result
    }

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }

    @Test
    fun testRfc8032Section7_1Vector1() {
        val secBytes = hexToBytes("9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60")
        val expectedPubBytes = hexToBytes("d75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a")
        val msg = ByteArray(0)
        val expectedSig =
            hexToBytes(
                "e5564300c360ac729086e2cc806e828a84877f1eb8e5d974d873e065224901555fb8821590a33bacc61e39701cf9b46bd25bf5f0595bbe24655141438e7a100b",
            )

        val signingKey = SigningKey.fromBytes(secBytes)
        val verifyingKey = signingKey.verifyingKey()
        assertEquals(bytesToHex(expectedPubBytes), bytesToHex(verifyingKey.asBytes()))

        val signature = signingKey.sign(msg)
        assertEquals(bytesToHex(expectedSig), bytesToHex(signature.toBytes()))

        signingKey.verify(msg, signature)
        verifyingKey.verify(msg, signature)
        verifyingKey.verifyStrict(msg, signature)
    }

    @Test
    fun testRfc8032Section7_1Vector2() {
        val secBytes = hexToBytes("4ccd089b28ff96da9db6c346ec114e0f5b8a319f35aba624da8cf6ed4fb8a6fb")
        val expectedPubBytes = hexToBytes("3d4017c3e843895a92b70aa74d1b7ebc9c982ccf2ec4968cc0cd55f12af4660c")
        val msg = hexToBytes("72")
        val expectedSig =
            hexToBytes(
                "92a009a9f0d4cab8720e820b5f642540a2b27b5416503f8fb3762223ebdb69da085ac1e43e15996e458f3613d0f11d8c387b2eaeb4302aeeb00d291612bb0c00",
            )

        val signingKey = SigningKey.fromBytes(secBytes)
        val verifyingKey = signingKey.verifyingKey()
        assertEquals(bytesToHex(expectedPubBytes), bytesToHex(verifyingKey.asBytes()))

        val signature = signingKey.sign(msg)
        assertEquals(bytesToHex(expectedSig), bytesToHex(signature.toBytes()))

        signingKey.verify(msg, signature)
        verifyingKey.verify(msg, signature)
        verifyingKey.verifyStrict(msg, signature)
    }

    @Test
    fun testRfc8032Section7_3Ed25519phPrehash() {
        val secBytes = hexToBytes("833fe62409237b9d62ec77587520911e9a759cec1d19755b7da901b96dca3d42")
        val pubBytes = hexToBytes("ec172b93ad5e563bf4932c70e1245034c35467ef2efd4d64ebf819683467e2bf")
        val msgBytes = hexToBytes("616263")
        val expectedSigBytes =
            hexToBytes(
                "98a70222f0b8121aa9d30f813d683f809e462b469c7ff87639499bb94e6dae4131f85042463c2a355a2003d062adf5aaa10b8c61e636062aaad11c2a26083406",
            )

        val signingKey = SigningKey.fromBytes(secBytes)
        val verifyingKey = VerifyingKey.fromBytes(pubBytes)
        assertEquals(verifyingKey, signingKey.verifyingKey())

        val prehash = Sha512()
        prehash.update(msgBytes)
        val prehashedDigest = prehash.finalize()

        val sig = signingKey.signPrehashed(prehashedDigest, null)
        assertEquals(bytesToHex(expectedSigBytes), bytesToHex(sig.toBytes()))

        signingKey.verifyPrehashed(prehashedDigest, null, sig)
        verifyingKey.verifyPrehashed(prehashedDigest, null, sig)
        verifyingKey.verifyPrehashedStrict(prehashedDigest, null, sig)
    }

    @Test
    fun testSignVerifyBasic() {
        val good = "test message".encodeToByteArray()
        val bad = "wrong message".encodeToByteArray()

        val secretKey = ByteArray(32) { it.toByte() }
        val signingKey = SigningKey.fromBytes(secretKey)
        val verifyingKey = signingKey.verifyingKey()

        assertFalse(verifyingKey.isWeak())

        val goodSig = signingKey.sign(good)
        val badSig = signingKey.sign(bad)

        signingKey.verify(good, goodSig)
        verifyingKey.verify(good, goodSig)
        verifyingKey.verifyStrict(good, goodSig)

        assertFailsWith<SignatureError> { signingKey.verify(good, badSig) }
        assertFailsWith<SignatureError> { verifyingKey.verify(good, badSig) }
        assertFailsWith<SignatureError> { verifyingKey.verifyStrict(good, badSig) }
        assertFailsWith<SignatureError> { signingKey.verify(bad, goodSig) }
        assertFailsWith<SignatureError> { verifyingKey.verify(bad, goodSig) }
        assertFailsWith<SignatureError> { verifyingKey.verifyStrict(bad, goodSig) }
    }

    @Test
    fun testStreamVerifier() {
        val good = "test message".encodeToByteArray()
        val bad = "wrong message".encodeToByteArray()

        val secretKey = ByteArray(32) { (it * 3).toByte() }
        val signingKey = SigningKey.fromBytes(secretKey)
        val goodSig = signingKey.sign(good)
        val badSig = signingKey.sign(bad)

        val verifier1 = signingKey.verifyStream(goodSig)
        verifier1.update(good)
        verifier1.finalizeAndVerify()

        val verifier2 = signingKey.verifyStream(badSig)
        verifier2.update(good)
        assertFailsWith<SignatureError> { verifier2.finalizeAndVerify() }

        val verifier3 = signingKey.verifyStream(goodSig)
        verifier3.update("test ".encodeToByteArray())
        verifier3.update("message".encodeToByteArray())
        verifier3.finalizeAndVerify()

        val verifier4 = signingKey.verifyStream(goodSig)
        verifier4.update(bad)
        assertFailsWith<SignatureError> { verifier4.finalizeAndVerify() }
    }

    @Test
    fun testBatchVerification() {
        val messages =
            listOf(
                "Watch closely everyone, I'm going to show you how to kill a god.".encodeToByteArray(),
                "I'm not a cryptographer I just encrypt a lot.".encodeToByteArray(),
                "Still not a cryptographer.".encodeToByteArray(),
                "This is a test of the tsunami alert system. This is only a test.".encodeToByteArray(),
                "Molotov cocktails on me like accessories.".encodeToByteArray(),
                "Hey, I never cared about your bucks.".encodeToByteArray(),
                "Nope, we came to riot, here to incite, we don't want any of your stuff.".encodeToByteArray(),
            )

        val signingKeys = ArrayList<SigningKey>()
        val signatures = ArrayList<Signature>()
        val verifyingKeys = ArrayList<VerifyingKey>()

        for (i in messages.indices) {
            val sec = ByteArray(32) { (it + i * 17).toByte() }
            val sk = SigningKey.fromBytes(sec)
            signingKeys.add(sk)
            signatures.add(sk.sign(messages[i]))
            verifyingKeys.add(sk.verifyingKey())
        }

        verifyBatch(messages, signatures, verifyingKeys)
    }

    @Test
    fun testWeakKeyRepudiation() {
        // Torsion point of order 8
        val eightTorsion4 =
            byteArrayOf(
                236.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                255.toByte(),
                127.toByte(),
            )
        val compressed = CompressedEdwardsY(eightTorsion4)
        val pt = compressed.decompress()
        assertTrue(pt != null)
        assertTrue(pt.isSmallOrder())

        val vk = VerifyingKey.fromBytes(eightTorsion4)
        assertTrue(vk.isWeak())
    }

    @Test
    fun testKeypairSerialization() {
        val secBytes = hexToBytes("9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60")
        val signingKey = SigningKey.fromBytes(secBytes)
        val keypairBytes = signingKey.toKeypairBytes()
        assertEquals(64, keypairBytes.size)

        val restored = SigningKey.fromKeypairBytes(keypairBytes)
        assertEquals(signingKey, restored)
        assertEquals(signingKey.verifyingKey(), restored.verifyingKey())
    }
}
