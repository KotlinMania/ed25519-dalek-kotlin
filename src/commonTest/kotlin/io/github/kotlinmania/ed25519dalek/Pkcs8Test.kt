// port-lint: tests tests/pkcs8.rs
package io.github.kotlinmania.ed25519dalek

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Pkcs8Test {
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

        val PKCS8_V1_DER: ByteArray = hexToBytes(
            "302e020100300506032b657004220420d4ee72dbf913584ad5b6d8f1f769f8ad3afe7c28cbf1d4fbe097a88f44755842",
        )

        val PKCS8_V2_DER: ByteArray = hexToBytes(
            "3072020101300506032b657004220420d4ee72dbf913584ad5b6d8f1f769f8ad3afe7c28cbf1d4fbe097a88f44755842a01f301d060a2a864886f70d01090914310f0c0d437572646c652043686169727381210019bf44096984cdfe8541bac167dc3b96c85086aa30b6b6cb0c5c38ad703166e1",
        )

        val PUBLIC_KEY_DER: ByteArray = hexToBytes(
            "302a300506032b657003210019bf44096984cdfe8541bac167dc3b96c85086aa30b6b6cb0c5c38ad703166e1",
        )

        val SK_BYTES: ByteArray = hexToBytes("D4EE72DBF913584AD5B6D8F1F769F8AD3AFE7C28CBF1D4FBE097A88F44755842")
        val PK_BYTES: ByteArray = hexToBytes("19BF44096984CDFE8541BAC167DC3B96C85086AA30B6B6CB0C5C38AD703166E1")
    }

    @Test
    fun decodePkcs8V1() {
        val keypair = SigningKey.fromPkcs8Der(PKCS8_V1_DER)
        assertContentEquals(SK_BYTES, keypair.toBytes())
        assertContentEquals(PK_BYTES, keypair.verifyingKey().toBytes())
    }

    @Test
    fun decodePkcs8V2() {
        val keypair = SigningKey.fromPkcs8Der(PKCS8_V2_DER)
        assertContentEquals(SK_BYTES, keypair.toBytes())
        assertContentEquals(PK_BYTES, keypair.verifyingKey().toBytes())
    }

    @Test
    fun decodeVerifyingKey() {
        val verifyingKey = VerifyingKey.fromPublicKeyDer(PUBLIC_KEY_DER)
        assertContentEquals(PK_BYTES, verifyingKey.toBytes())
    }

    @Test
    fun encodePkcs8() {
        val keypair = SigningKey.fromBytes(SK_BYTES)
        val pkcs8Key = keypair.toPkcs8Der()

        val keypair2 = SigningKey.fromPkcs8Der(pkcs8Key)
        assertContentEquals(keypair.toBytes(), keypair2.toBytes())
    }

    @Test
    fun encodeVerifyingKey() {
        val verifyingKey = VerifyingKey.fromBytes(PK_BYTES)
        val verifyingKeyDer = verifyingKey.toPublicKeyDer()

        val verifyingKey2 = VerifyingKey.fromPublicKeyDer(verifyingKeyDer)
        assertEquals(verifyingKey, verifyingKey2)
    }

    @Test
    fun getAlgoIdentifier() {
        val verifyingKey = VerifyingKey.fromPublicKeyDer(PUBLIC_KEY_DER)
        val identifier = verifyingKey.signatureAlgorithmIdentifier()
        assertNull(identifier.parameters)
        assertEquals("1.3.101.112", identifier.oid)

        val signingKey = SigningKey.fromBytes(SK_BYTES)
        val id2 = signingKey.signatureAlgorithmIdentifier()
        assertNull(id2.parameters)
        assertEquals("1.3.101.112", id2.oid)
    }
}
