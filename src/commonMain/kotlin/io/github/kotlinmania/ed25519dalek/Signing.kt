// port-lint: source signing.rs
package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.Scalar
import io.github.kotlinmania.ed25519dalek.verifying.StreamVerifier
import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.Expected
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.U8Deserialize
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import io.github.kotlinmania.sha2.Sha512
import io.github.kotlinmania.zeroize.Zeroize
import io.github.kotlinmania.zeroize.ZeroizeOnDrop
import io.github.kotlinmania.zeroize.zeroize
import kotlin.random.Random

/** Type alias for secret key bytes matching upstream `SecretKey`. */
typealias SecretKey = ByteArray

/**
 * Representation of an algorithm identifier in PKCS#8.
 */
data class AlgorithmIdentifier(
    val oid: String,
    val parameters: String? = null,
)

/**
 * An ed25519 signing key used to produce signatures.
 */
class SigningKey internal constructor(
    internal val secretKey: ByteArray,
    internal val verifyingKey: VerifyingKey,
) : Zeroize,
    ZeroizeOnDrop,
    AutoCloseable,
    Serialize {
    init {
        require(secretKey.size == SECRET_KEY_LENGTH)
    }

    override fun zeroize() {
        secretKey.zeroize()
    }

    override fun close() {
        zeroize()
    }

    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeBytes(secretKey)

    companion object : Deserialize<SigningKey> {
        override fun <D> deserialize(deserializer: D): SerdeResult<SigningKey>
            where D : Deserializer =
            deserializer.deserializeBytes(SigningKeyVisitor)

        /** Generate a fresh random [SigningKey]. */
        fun generate(): SigningKey {
            val bytes = Random.nextBytes(SECRET_KEY_LENGTH)
            return fromBytes(bytes)
        }

        /** Construct a [SigningKey] from a 32-byte secret key seed. */
        fun fromBytes(secretKey: ByteArray): SigningKey {
            if (secretKey.size != SECRET_KEY_LENGTH) {
                throw SignatureError.from(InternalError.BytesLength("SecretKey", SECRET_KEY_LENGTH))
            }
            val esk = expandedSecretKeyFromSecret(secretKey)
            val verifyingKey = VerifyingKey.fromExpandedSecretKey(esk)
            return SigningKey(secretKey.copyOf(), verifyingKey)
        }

        /** Construct a [SigningKey] from a 64-byte keypair (secret key + verifying key). */
        fun fromKeypairBytes(bytes: ByteArray): SigningKey {
            if (bytes.size != KEYPAIR_LENGTH) {
                throw SignatureError.from(InternalError.BytesLength("Keypair", KEYPAIR_LENGTH))
            }
            val secretBytes = bytes.copyOfRange(0, SECRET_KEY_LENGTH)
            val publicBytes = bytes.copyOfRange(SECRET_KEY_LENGTH, KEYPAIR_LENGTH)

            val signingKey = fromBytes(secretBytes)
            val expectedVerifyingKey = VerifyingKey.fromBytes(publicBytes)

            if (signingKey.verifyingKey != expectedVerifyingKey) {
                throw SignatureError.from(InternalError.MismatchedKeypair)
            }

            return signingKey
        }

        /** Construct a [SigningKey] from PKCS#8 DER bytes. */
        fun fromPkcs8Der(der: ByteArray): SigningKey {
            val marker = byteArrayOf(0x06, 0x03, 0x2b, 0x65, 0x70, 0x04, 0x22, 0x04, 0x20)
            var idx = -1
            for (i in 0..(der.size - marker.size - 32)) {
                var matches = true
                for (j in marker.indices) {
                    if (der[i + j] != marker[j]) {
                        matches = false
                        break
                    }
                }
                if (matches) {
                    idx = i + marker.size
                    break
                }
            }
            if (idx < 0) {
                throw SignatureError.from(InternalError.Verify)
            }
            val sec = der.copyOfRange(idx, idx + 32)
            val signingKey = fromBytes(sec)

            val pkMarker = byteArrayOf(0x81.toByte(), 0x21, 0x00)
            for (i in (idx + 32)..(der.size - pkMarker.size - 32)) {
                var matches = true
                for (j in pkMarker.indices) {
                    if (der[i + j] != pkMarker[j]) {
                        matches = false
                        break
                    }
                }
                if (matches) {
                    val pub = der.copyOfRange(i + pkMarker.size, i + pkMarker.size + 32)
                    if (!signingKey.verifyingKey().asBytes().contentEquals(pub)) {
                        throw SignatureError.from(InternalError.Verify)
                    }
                    break
                }
            }
            return signingKey
        }

        private fun expandedSecretKeyFromSecret(secretKey: ByteArray): ExpandedSecretKey {
            val hasher = Sha512()
            hasher.update(secretKey)
            val hash = hasher.finalize()
            return ExpandedSecretKey.fromBytes(hash)
        }
    }

    /** Convert this signing key to PKCS#8 DER encoded bytes. */
    fun toPkcs8Der(): ByteArray {
        val prefix =
            byteArrayOf(
                0x30,
                0x2e,
                0x02,
                0x01,
                0x00,
                0x30,
                0x05,
                0x06,
                0x03,
                0x2b,
                0x65,
                0x70,
                0x04,
                0x22,
                0x04,
                0x20,
            )
        return prefix + secretKey
    }

    /** Returns the signature algorithm identifier for PKCS#8. */
    fun signatureAlgorithmIdentifier(): AlgorithmIdentifier =
        AlgorithmIdentifier(oid = "1.3.101.112", parameters = null)

    /** Create a context for prehashed signing. */
    fun withContext(contextValue: ByteArray): Context<SigningKey> =
        Context.new(this, contextValue)

    /** Convert this signing key to a 32-byte secret key. */
    fun toBytes(): ByteArray = secretKey.copyOf()

    /** View this signing key as a 32-byte secret key. */
    fun asBytes(): ByteArray = secretKey.copyOf()

    /** Convert this signing key to a 64-byte keypair (secret key + public key). */
    fun toKeypairBytes(): ByteArray {
        val out = ByteArray(KEYPAIR_LENGTH)
        secretKey.copyInto(out, 0, 0, SECRET_KEY_LENGTH)
        verifyingKey.asBytes().copyInto(out, SECRET_KEY_LENGTH, 0, PUBLIC_KEY_LENGTH)
        return out
    }

    /** Get the [VerifyingKey] corresponding to this signing key. */
    fun verifyingKey(): VerifyingKey = verifyingKey

    /** Sign a message with this signing key. */
    fun sign(message: ByteArray): Signature {
        val esk = expandedSecretKeyFromSecret(secretKey)
        return esk.rawSign(message, verifyingKey)
    }

    /**
     * Sign a prehashed message with this signing key using the Ed25519ph algorithm.
     */
    fun signPrehashed(prehashedMessage: ByteArray, context: ByteArray? = null): Signature {
        val esk = expandedSecretKeyFromSecret(secretKey)
        return esk.rawSignPrehashed(prehashedMessage, verifyingKey, context)
    }

    /** Verify a signature on a message with this key's public key. */
    fun verify(message: ByteArray, signature: Signature) {
        verifyingKey.verify(message, signature)
    }

    /** Verify a signature on a prehashed message. */
    fun verifyPrehashed(
        prehashedMessage: ByteArray,
        context: ByteArray? = null,
        signature: Signature,
    ) {
        verifyingKey.verifyPrehashed(prehashedMessage, context, signature)
    }

    /** Strictly verify a signature on a message. */
    fun verifyStrict(message: ByteArray, signature: Signature) {
        verifyingKey.verifyStrict(message, signature)
    }

    /** Constructs stream verifier with candidate signature. */
    fun verifyStream(signature: Signature): StreamVerifier =
        verifyingKey.verifyStream(signature)

    /**
     * Convert this signing key into a byte representation of an unreduced, unclamped Curve25519 scalar.
     */
    fun toScalarBytes(): ByteArray {
        val hasher = Sha512()
        hasher.update(secretKey)
        val hash = hasher.finalize()
        return hash.copyOfRange(0, 32)
    }

    /**
     * Convert this signing key into a Curve25519 scalar (clamped and reduced).
     */
    internal fun toScalar(): Scalar {
        val esk = expandedSecretKeyFromSecret(secretKey)
        return esk.scalar
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SigningKey) return false
        return secretKey.contentEquals(other.secretKey)
    }

    override fun hashCode(): Int = secretKey.contentHashCode()

    override fun toString(): String = "SigningKey(verifyingKey=$verifyingKey)"
}

private object SigningKeyVisitor : Visitor<SigningKey> {
    override fun expecting(): String = "An ed25519 signing (private) key"

    override fun visitBytes(v: ByteArray): SerdeResult<SigningKey> =
        try {
            SerdeResult.success(SigningKey.fromBytes(v))
        } catch (e: Exception) {
            SerdeResult.failure(SerdeError.custom(e.message ?: "invalid signing key"))
        }

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<SigningKey> {
        val bytes = ByteArray(SECRET_KEY_LENGTH)
        for (i in 0 until SECRET_KEY_LENGTH) {
            val element =
                access.nextElement(U8Deserialize).getOrNull()
                    ?: return SerdeResult.failure(SerdeError.invalidLength(i, Expected { "expected 32 bytes" }))
            bytes[i] = element.toByte()
        }

        var remaining = 0
        while (access.nextElement(U8Deserialize).getOrNull() != null) {
            remaining++
        }

        if (remaining > 0) {
            return SerdeResult.failure(
                SerdeError.invalidLength(SECRET_KEY_LENGTH + remaining, Expected { "expected 32 bytes" }),
            )
        }

        return try {
            SerdeResult.success(SigningKey.fromBytes(bytes))
        } catch (e: Exception) {
            SerdeResult.failure(SerdeError.custom(e.message ?: "invalid signing key"))
        }
    }
}
