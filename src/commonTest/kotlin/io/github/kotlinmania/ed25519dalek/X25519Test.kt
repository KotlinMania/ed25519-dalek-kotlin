// port-lint: tests ../tests/x25519.rs
package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.Sha512
import kotlin.test.Test
import kotlin.test.assertEquals

class X25519Test {
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
    fun ed25519ToX25519Dh() {
        val edSecretKeyA = hexToBytes("9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60")
        val edSecretKeyB = hexToBytes("4ccd089b28ff96da9db6c346ec114e0f5b8a319f35aba624da8cf6ed4fb8a6fb")

        val edSigningKeyA = SigningKey.fromBytes(edSecretKeyA)
        val edSigningKeyB = SigningKey.fromBytes(edSecretKeyB)

        val scalarBytesA = edSigningKeyA.toScalarBytes()
        val scalarBytesB = edSigningKeyB.toScalarBytes()

        val hasherA = Sha512()
        hasherA.update(edSecretKeyA)
        val hashA = hasherA.finalize().copyOfRange(0, 32)
        assertEquals(bytesToHex(hashA), bytesToHex(scalarBytesA))

        val hasherB = Sha512()
        hasherB.update(edSecretKeyB)
        val hashB = hasherB.finalize().copyOfRange(0, 32)
        assertEquals(bytesToHex(hashB), bytesToHex(scalarBytesB))

        val expectedXPublicA = "d85e07ec22b0ad881537c2f44d662d1a143cf830c57aca4305d85c7a90f6b62e"
        val expectedXPublicB = "25c704c594b88afc00a76b69d1ed2b984d7e22550f3ed0802d04fbcd07d38d47"

        val montyA = edSigningKeyA.verifyingKey().toMontgomery()
        val montyB = edSigningKeyB.verifyingKey().toMontgomery()

        assertEquals(expectedXPublicA, bytesToHex(montyA.asBytes()))
        assertEquals(expectedXPublicB, bytesToHex(montyB.asBytes()))
    }
}
