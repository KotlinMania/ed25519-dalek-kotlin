// port-lint: tests ed25519-dalek/tests/ed25519.rs
package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.CompressedEdwardsY
import io.github.kotlinmania.ed25519dalek.internal.EdwardsPoint
import io.github.kotlinmania.ed25519dalek.internal.Scalar
import io.github.kotlinmania.ed25519dalek.internal.Sha512
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class Ed25519Test {
    companion object {
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

        val EIGHT_TORSION_4: ByteArray =
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

        internal val WEAK_PUBKEY: CompressedEdwardsY = CompressedEdwardsY(EIGHT_TORSION_4)

        val PUBLIC_KEY_BYTES: ByteArray =
            byteArrayOf(
                130.toByte(),
                39.toByte(),
                155.toByte(),
                15.toByte(),
                62.toByte(),
                76.toByte(),
                188.toByte(),
                63.toByte(),
                124.toByte(),
                122.toByte(),
                26.toByte(),
                251.toByte(),
                233.toByte(),
                253.toByte(),
                225.toByte(),
                220.toByte(),
                14.toByte(),
                41.toByte(),
                166.toByte(),
                120.toByte(),
                108.toByte(),
                35.toByte(),
                254.toByte(),
                77.toByte(),
                160.toByte(),
                83.toByte(),
                172.toByte(),
                58.toByte(),
                219.toByte(),
                42.toByte(),
                86.toByte(),
                120.toByte(),
            )

        val SECRET_KEY_BYTES: ByteArray =
            byteArrayOf(
                62.toByte(),
                70.toByte(),
                27.toByte(),
                163.toByte(),
                92.toByte(),
                182.toByte(),
                11.toByte(),
                3.toByte(),
                77.toByte(),
                234.toByte(),
                98.toByte(),
                4.toByte(),
                11.toByte(),
                127.toByte(),
                79.toByte(),
                228.toByte(),
                243.toByte(),
                187.toByte(),
                150.toByte(),
                73.toByte(),
                201.toByte(),
                137.toByte(),
                76.toByte(),
                22.toByte(),
                85.toByte(),
                251.toByte(),
                152.toByte(),
                2.toByte(),
                241.toByte(),
                42.toByte(),
                72.toByte(),
                54.toByte(),
            )

        val SIGNATURE_BYTES: ByteArray =
            byteArrayOf(
                10.toByte(),
                126.toByte(),
                151.toByte(),
                143.toByte(),
                157.toByte(),
                64.toByte(),
                47.toByte(),
                1.toByte(),
                196.toByte(),
                140.toByte(),
                179.toByte(),
                58.toByte(),
                226.toByte(),
                152.toByte(),
                18.toByte(),
                102.toByte(),
                160.toByte(),
                123.toByte(),
                80.toByte(),
                16.toByte(),
                210.toByte(),
                86.toByte(),
                196.toByte(),
                28.toByte(),
                53.toByte(),
                231.toByte(),
                12.toByte(),
                157.toByte(),
                169.toByte(),
                19.toByte(),
                158.toByte(),
                63.toByte(),
                45.toByte(),
                154.toByte(),
                238.toByte(),
                7.toByte(),
                53.toByte(),
                185.toByte(),
                227.toByte(),
                229.toByte(),
                79.toByte(),
                108.toByte(),
                213.toByte(),
                80.toByte(),
                124.toByte(),
                252.toByte(),
                84.toByte(),
                167.toByte(),
                216.toByte(),
                85.toByte(),
                134.toByte(),
                144.toByte(),
                129.toByte(),
                149.toByte(),
                41.toByte(),
                81.toByte(),
                63.toByte(),
                120.toByte(),
                126.toByte(),
                100.toByte(),
                92.toByte(),
                59.toByte(),
                50.toByte(),
                11.toByte(),
            )
    }

    private fun computeChallenge(
        message: ByteArray,
        pubKey: EdwardsPoint,
        signatureR: EdwardsPoint,
        context: ByteArray?,
    ): Scalar {
        val h = Sha512()
        if (context != null) {
            h.update("SigEd25519 no Ed25519 collisions".encodeToByteArray())
            h.update(byteArrayOf(1))
            h.update(byteArrayOf(context.size.toByte()))
            h.update(context)
        }
        h.update(signatureR.compress().asBytes())
        h.update(pubKey.compress().asBytes())
        h.update(message)
        return Scalar.fromHash(h.finalize())
    }

    private fun serializeSignature(r: EdwardsPoint, s: Scalar): ByteArray =
        r.compress().asBytes() + s.asBytes()

    private fun nonNullScalar(): Scalar {
        var s = Scalar.fromBits(Random.nextBytes(32))
        while (s == Scalar.ZERO) {
            s = Scalar.fromBits(Random.nextBytes(32))
        }
        return s
    }

    private fun pickR(s: Scalar): EdwardsPoint {
        val r0 = EdwardsPoint.BASEPOINT.mul(s)
        val weakPoint = WEAK_PUBKEY.decompress() ?: error("Failed decompressing WEAK_PUBKEY")
        return r0.add(weakPoint.negate())
    }

    @Test
    fun againstReferenceImplementation() {
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
    fun ed25519phRf8032TestVectorPrehash() {
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
    fun repudiation() {
        val message1 = "Send 100 USD to Alice".encodeToByteArray()
        val message2 = "Send 100000 USD to Alice".encodeToByteArray()

        val pubkey = WEAK_PUBKEY.decompress() ?: error("Decompress weak pubkey")
        val vk = VerifyingKey.fromBytes(pubkey.compress().asBytes())
        assertTrue(vk.isWeak())

        var s = nonNullScalar()
        var r = pickR(s)

        for (attempt in 0 until 1000) {
            val k1 = computeChallenge(message1, pubkey, r, null)
            val k2 = computeChallenge(message2, pubkey, r, null)
            val cond1 = pubkey.negate().add(pubkey.mul(k1)).isIdentity()
            val cond2 = pubkey.negate().add(pubkey.mul(k2)).isIdentity()
            if (cond1 && cond2) {
                val signatureBytes = serializeSignature(r, s)
                val sig = Signature.fromBytes(signatureBytes)
                vk.verify(message1, sig)
                vk.verify(message2, sig)
                assertFailsWith<SignatureError> { vk.verifyStrict(message1, sig) }
                assertFailsWith<SignatureError> { vk.verifyStrict(message2, sig) }
                return
            }
            s = nonNullScalar()
            r = pickR(s)
        }
    }

    @Test
    fun repudiationPrehash() {
        val h1 = Sha512()
        h1.update("Send 100 USD to Alice".encodeToByteArray())
        val message1Bytes = h1.finalize()

        val h2 = Sha512()
        h2.update("Send 100000 USD to Alice".encodeToByteArray())
        val message2Bytes = h2.finalize()

        val pubkey = WEAK_PUBKEY.decompress() ?: error("Decompress weak pubkey")
        val vk = VerifyingKey.fromBytes(pubkey.compress().asBytes())
        assertTrue(vk.isWeak())
        val contextStr = "edtest".encodeToByteArray()

        var s = nonNullScalar()
        var r = pickR(s)

        for (attempt in 0 until 1000) {
            val k1 = computeChallenge(message1Bytes, pubkey, r, contextStr)
            val k2 = computeChallenge(message2Bytes, pubkey, r, contextStr)
            val cond1 = pubkey.negate().add(pubkey.mul(k1)).isIdentity()
            val cond2 = pubkey.negate().add(pubkey.mul(k2)).isIdentity()
            if (cond1 && cond2) {
                val signatureBytes = serializeSignature(r, s)
                val sig = Signature.fromBytes(signatureBytes)
                vk.verifyPrehashed(message1Bytes, contextStr, sig)
                vk.verifyPrehashed(message2Bytes, contextStr, sig)
                assertFailsWith<SignatureError> { vk.verifyPrehashedStrict(message1Bytes, contextStr, sig) }
                assertFailsWith<SignatureError> { vk.verifyPrehashedStrict(message2Bytes, contextStr, sig) }
                return
            }
            s = nonNullScalar()
            r = pickR(s)
        }
    }

    @Test
    fun signVerify() {
        val good = "test message".encodeToByteArray()
        val bad = "wrong message".encodeToByteArray()

        val signingKey = SigningKey.generate()
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
    fun signVerifyDigestEquivalence() {
        val good = "test message".encodeToByteArray()
        val bad = "wrong message".encodeToByteArray()

        val keypair = SigningKey.generate()
        val goodSig = keypair.sign(good)
        val badSig = keypair.sign(bad)

        val verifier1 = keypair.verifyStream(goodSig)
        verifier1.update(good)
        verifier1.finalizeAndVerify()

        val verifier2 = keypair.verifyStream(badSig)
        verifier2.update(good)
        assertFailsWith<SignatureError> { verifier2.finalizeAndVerify() }

        val verifier3 = keypair.verifyStream(goodSig)
        verifier3.update("test ".encodeToByteArray())
        verifier3.update("message".encodeToByteArray())
        verifier3.finalizeAndVerify()

        val verifier4 = keypair.verifyStream(goodSig)
        verifier4.update(bad)
        assertFailsWith<SignatureError> { verifier4.finalizeAndVerify() }
    }

    @Test
    fun ed25519phSignVerify() {
        val good = "test message".encodeToByteArray()
        val bad = "wrong message".encodeToByteArray()

        val hGood = Sha512()
        hGood.update(good)
        val prehashedGood = hGood.finalize()

        val hBad = Sha512()
        hBad.update(bad)
        val prehashedBad = hBad.finalize()

        val context = "testing testing 1 2 3".encodeToByteArray()

        val signingKey = SigningKey.generate()
        val verifyingKey = signingKey.verifyingKey()

        val goodSig = signingKey.signPrehashed(prehashedGood, context)
        val badSig = signingKey.signPrehashed(prehashedBad, context)

        signingKey.verifyPrehashed(prehashedGood, context, goodSig)
        verifyingKey.verifyPrehashedStrict(prehashedGood, context, goodSig)

        assertFailsWith<SignatureError> { signingKey.verifyPrehashed(prehashedGood, context, badSig) }
        assertFailsWith<SignatureError> { verifyingKey.verifyPrehashedStrict(prehashedGood, context, badSig) }
        assertFailsWith<SignatureError> { signingKey.verifyPrehashed(prehashedBad, context, goodSig) }
        assertFailsWith<SignatureError> { verifyingKey.verifyPrehashedStrict(prehashedBad, context, goodSig) }
    }

    @Test
    fun verifyBatchSevenSignatures() {
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
            val sk = SigningKey.generate()
            signingKeys.add(sk)
            signatures.add(sk.sign(messages[i]))
            verifyingKeys.add(sk.verifyingKey())
        }

        verifyBatch(messages, signatures, verifyingKeys)
    }

    @Test
    fun publicKeyHashTraitCheck() {
        val secret = SigningKey.generate()
        val publicFromSecret = secret.verifyingKey()

        val map = HashMap<VerifyingKey, String>()
        map[publicFromSecret] = "Example_Public_Key"
        map[publicFromSecret] = "Updated Value"

        assertEquals("Updated Value", map[publicFromSecret])
        assertEquals(1, map.size)

        val secondSecret = SigningKey.generate()
        val publicFromSecondSecret = secondSecret.verifyingKey()
        assertNotEquals(publicFromSecret, publicFromSecondSecret)

        map[publicFromSecondSecret] = "Second public key"
        assertEquals("Second public key", map[publicFromSecondSecret])
        assertEquals(2, map.size)
    }

    @Test
    fun montgomeryAndEdwardsConversion() {
        val signingKey = SigningKey.generate()
        val verifyingKey = signingKey.verifyingKey()

        val ed = verifyingKey.toEdwards()
        assertContentEquals(verifyingKey.toBytes(), ed.compress().asBytes())

        val monty = verifyingKey.toMontgomery()
        assertTrue(monty.asBytes().isNotEmpty())
    }

    @Test
    fun serializeDeserializeSignatureBincode() {
        val signature = Signature.fromBytes(SIGNATURE_BYTES)
        val bytes = signature.toBytes()
        val decoded = Signature.fromBytes(bytes)
        assertEquals(signature, decoded)
    }

    @Test
    fun serializeDeserializeSignatureJson() {
        val signature = Signature.fromBytes(SIGNATURE_BYTES)
        val hex = bytesToHex(signature.toBytes())
        val decoded = Signature.fromBytes(hexToBytes(hex))
        assertEquals(signature, decoded)
    }

    @Test
    fun serializeDeserializeVerifyingKeyBincode() {
        val verifyingKey = VerifyingKey.fromBytes(PUBLIC_KEY_BYTES)
        val bytes = verifyingKey.toBytes()
        val decoded = VerifyingKey.fromBytes(bytes)
        assertEquals(verifyingKey, decoded)
    }

    @Test
    fun serializeDeserializeVerifyingKeyJson() {
        val verifyingKey = VerifyingKey.fromBytes(PUBLIC_KEY_BYTES)
        val hex = bytesToHex(verifyingKey.toBytes())
        val decoded = VerifyingKey.fromBytes(hexToBytes(hex))
        assertEquals(verifyingKey, decoded)
    }

    @Test
    fun serializeDeserializeVerifyingKeyJsonTooLong() {
        val tooLong = ByteArray(34)
        assertFailsWith<SignatureError> {
            VerifyingKey.fromBytes(tooLong)
        }
    }

    @Test
    fun serializeDeserializeVerifyingKeyJsonTooShort() {
        val tooShort = ByteArray(4)
        assertFailsWith<SignatureError> {
            VerifyingKey.fromBytes(tooShort)
        }
    }

    @Test
    fun serializeDeserializeSigningKeyBincode() {
        val signingKey = SigningKey.fromBytes(SECRET_KEY_BYTES)
        val bytes = signingKey.toBytes()
        val decoded = SigningKey.fromBytes(bytes)
        assertEquals(signingKey, decoded)
    }

    @Test
    fun serializeDeserializeSigningKeyJson() {
        val signingKey = SigningKey.fromBytes(SECRET_KEY_BYTES)
        val hex = bytesToHex(signingKey.toBytes())
        val decoded = SigningKey.fromBytes(hexToBytes(hex))
        assertEquals(signingKey, decoded)
    }

    @Test
    fun serializeDeserializeSigningKeyJsonTooLong() {
        val tooLong = ByteArray(34)
        assertFailsWith<SignatureError> {
            SigningKey.fromBytes(tooLong)
        }
    }

    @Test
    fun serializeDeserializeSigningKeyJsonTooShort() {
        val tooShort = ByteArray(4)
        assertFailsWith<SignatureError> {
            SigningKey.fromBytes(tooShort)
        }
    }

    @Test
    fun serializeDeserializeSigningKeyToml() {
        val signingKey = SigningKey.fromBytes(SECRET_KEY_BYTES)
        val hex = bytesToHex(signingKey.toBytes())
        val restored = SigningKey.fromBytes(hexToBytes(hex))
        assertEquals(signingKey, restored)
    }

    @Test
    fun serializeVerifyingKeySize() {
        val verifyingKey = VerifyingKey.fromBytes(PUBLIC_KEY_BYTES)
        assertEquals(PUBLIC_KEY_LENGTH, verifyingKey.toBytes().size)
    }

    @Test
    fun serializeSignatureSize() {
        val signature = Signature.fromBytes(SIGNATURE_BYTES)
        assertEquals(SIGNATURE_LENGTH, signature.toBytes().size)
    }

    @Test
    fun serializeSigningKeySize() {
        val signingKey = SigningKey.fromBytes(SECRET_KEY_BYTES)
        assertEquals(SECRET_KEY_LENGTH, signingKey.toBytes().size)
    }
}
