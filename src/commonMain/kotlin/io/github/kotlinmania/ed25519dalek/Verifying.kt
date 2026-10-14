// port-lint: source verifying.rs
package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.CompressedEdwardsY
import io.github.kotlinmania.ed25519dalek.internal.EdwardsPoint
import io.github.kotlinmania.ed25519dalek.internal.MontgomeryPoint
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

/**
 * An ed25519 public key.
 */
class VerifyingKey internal constructor(
    internal val compressed: CompressedEdwardsY,
    internal val point: EdwardsPoint,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeBytes(toBytes())

    companion object : Deserialize<VerifyingKey> {
        override fun <D> deserialize(deserializer: D): SerdeResult<VerifyingKey>
            where D : Deserializer =
            deserializer.deserializeBytes(VerifyingKeyVisitor)

        /**
         * Construct a [VerifyingKey] from a 32-byte public key representation.
         */
        fun fromBytes(bytes: ByteArray): VerifyingKey {
            if (bytes.size != PUBLIC_KEY_LENGTH) {
                throw SignatureError.from(InternalError.BytesLength("VerifyingKey", PUBLIC_KEY_LENGTH))
            }
            val compressed = CompressedEdwardsY(bytes.copyOf())
            val point =
                compressed.decompress()
                    ?: throw SignatureError.from(InternalError.PointDecompression)
            return VerifyingKey(compressed, point)
        }

        internal fun fromEdwardsPoint(point: EdwardsPoint): VerifyingKey {
            val compressed = point.compress()
            return VerifyingKey(compressed, point)
        }

        fun fromExpandedSecretKey(esk: ExpandedSecretKey): VerifyingKey {
            val point = EdwardsPoint.BASEPOINT.mul(esk.scalar)
            return fromEdwardsPoint(point)
        }

        fun fromSigningKey(signingKey: SigningKey): VerifyingKey = signingKey.verifyingKey()

        /** Construct a [VerifyingKey] from SPKI public key DER bytes. */
        fun fromPublicKeyDer(der: ByteArray): VerifyingKey {
            val marker = byteArrayOf(0x06, 0x03, 0x2b, 0x65, 0x70, 0x03, 0x21, 0x00)
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
            val pub = der.copyOfRange(idx, idx + 32)
            return fromBytes(pub)
        }
    }

    /** Convert this public key to SPKI public key DER bytes. */
    fun toPublicKeyDer(): ByteArray {
        val prefix =
            byteArrayOf(
                0x30,
                0x2a,
                0x30,
                0x05,
                0x06,
                0x03,
                0x2b,
                0x65,
                0x70,
                0x03,
                0x21,
                0x00,
            )
        return prefix + asBytes()
    }

    /** Returns the signature algorithm identifier for PKCS#8 / SPKI. */
    fun signatureAlgorithmIdentifier(): AlgorithmIdentifier =
        AlgorithmIdentifier(oid = "1.3.101.112", parameters = null)

    /** Create a verifying context for prehashed verification. */
    fun withContext(contextValue: ByteArray): Context<VerifyingKey> =
        Context.new(this, contextValue)

    /** Convert this public key to a byte array. */
    fun toBytes(): ByteArray = compressed.asBytes()

    /** View this public key as a byte array. */
    fun asBytes(): ByteArray = compressed.asBytes()

    /** Returns whether this is a weak public key (has low order). */
    fun isWeak(): Boolean = point.isSmallOrder()

    /**
     * The ordinary non-batched Ed25519 verification check, rejecting non-canonical R values.
     */
    fun rawVerify(message: ByteArray, signature: Signature) {
        val internalSig = signature.toInternal()
        val expectedR = RCompute.compute(this, internalSig, null, message)
        if (expectedR != internalSig.R) {
            throw SignatureError.from(InternalError.Verify)
        }
    }

    /**
     * The prehashed non-batched Ed25519 verification check, rejecting non-canonical R values.
     */
    fun rawVerifyPrehashed(
        prehashedMessage: ByteArray,
        context: ByteArray? = null,
        signature: Signature,
    ) {
        val internalSig = signature.toInternal()
        val ctx = context ?: ByteArray(0)
        if (ctx.size > Context.MAX_LENGTH) {
            throw SignatureError.from(InternalError.PrehashedContextLength)
        }
        val expectedR = RCompute.compute(this, internalSig, ctx, prehashedMessage)
        if (expectedR != internalSig.R) {
            throw SignatureError.from(InternalError.Verify)
        }
    }

    /**
     * Verify a signature on a message with this public key.
     */
    fun verify(message: ByteArray, signature: Signature) {
        rawVerify(message, signature)
    }

    /**
     * Verify a signature on a prehashed message with an optional context string.
     */
    fun verifyPrehashed(
        prehashedMessage: ByteArray,
        context: ByteArray? = null,
        signature: Signature,
    ) {
        rawVerifyPrehashed(prehashedMessage, context, signature)
    }

    /**
     * Equivalent to [verifyPrehashed] with context set to null.
     */
    fun verifyDigest(prehashedMessage: ByteArray, signature: Signature) {
        verifyPrehashed(prehashedMessage, null, signature)
    }

    /**
     * Strictly verify a signature on a message, checking for small-order torsion components.
     */
    fun verifyStrict(message: ByteArray, signature: Signature) {
        val internalSig = signature.toInternal()
        val sigR =
            internalSig.R.decompress()
                ?: throw SignatureError.from(InternalError.Verify)

        if (sigR.isSmallOrder() || point.isSmallOrder()) {
            throw SignatureError.from(InternalError.Verify)
        }

        val expectedR = RCompute.compute(this, internalSig, null, message)
        if (expectedR != internalSig.R) {
            throw SignatureError.from(InternalError.Verify)
        }
    }

    /**
     * Strictly verify a signature on a prehashed message.
     */
    fun verifyPrehashedStrict(
        prehashedMessage: ByteArray,
        context: ByteArray?,
        signature: Signature,
    ) {
        val internalSig = signature.toInternal()
        val ctx = context ?: ByteArray(0)
        if (ctx.size > Context.MAX_LENGTH) {
            throw SignatureError.from(InternalError.PrehashedContextLength)
        }

        val sigR =
            internalSig.R.decompress()
                ?: throw SignatureError.from(InternalError.Verify)

        if (sigR.isSmallOrder() || point.isSmallOrder()) {
            throw SignatureError.from(InternalError.Verify)
        }

        val expectedR = RCompute.compute(this, internalSig, ctx, prehashedMessage)
        if (expectedR != internalSig.R) {
            throw SignatureError.from(InternalError.Verify)
        }
    }

    /** Constructs stream verifier with candidate signature. */
    fun verifyStream(signature: Signature): StreamVerifier {
        val internalSig = signature.toInternal()
        return StreamVerifier(this, internalSig)
    }

    /** Convert this verifying key into Montgomery form for X25519 key exchange. */
    internal fun toMontgomery(): MontgomeryPoint = point.toMontgomery()

    /** Return this verifying key in Edwards form. */
    internal fun toEdwards(): EdwardsPoint = point

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VerifyingKey) return false
        return compressed == other.compressed
    }

    override fun hashCode(): Int = compressed.hashCode()

    override fun toString(): String = "VerifyingKey($compressed)"
}

/**
 * Helper for verification computing the expected R component.
 */
internal class RCompute(
    private val key: VerifyingKey,
    private val signature: InternalSignature,
    private val hasher: Sha512,
) {
    companion object {
        fun compute(
            key: VerifyingKey,
            signature: InternalSignature,
            prehashCtx: ByteArray?,
            message: ByteArray,
        ): CompressedEdwardsY {
            val c = new(key, signature, prehashCtx)
            c.update(message)
            return c.finish()
        }

        fun new(
            key: VerifyingKey,
            signature: InternalSignature,
            prehashCtx: ByteArray?,
        ): RCompute {
            val hasher = Sha512()
            if (prehashCtx != null) {
                hasher.update(buildDomainPrefix(1, prehashCtx))
            }
            hasher.update(signature.R.asBytes())
            hasher.update(key.asBytes())
            return RCompute(key, signature, hasher)
        }
    }

    fun update(message: ByteArray) {
        hasher.update(message)
    }

    fun finish(): CompressedEdwardsY {
        val kDigest = hasher.finalize()
        val k = Scalar.fromHash(kDigest)
        val minusA = key.point.negate()
        return EdwardsPoint.vartimeDoubleScalarMulBasepoint(k, minusA, signature.s).compress()
    }
}

private object VerifyingKeyVisitor : Visitor<VerifyingKey> {
    override fun expecting(): String = "An ed25519 verifying (public) key"

    override fun visitBytes(v: ByteArray): SerdeResult<VerifyingKey> =
        try {
            SerdeResult.success(VerifyingKey.fromBytes(v))
        } catch (e: Exception) {
            SerdeResult.failure(SerdeError.custom(e.message ?: "invalid verifying key"))
        }

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<VerifyingKey> {
        val bytes = ByteArray(PUBLIC_KEY_LENGTH)
        for (i in 0 until PUBLIC_KEY_LENGTH) {
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
                SerdeError.invalidLength(PUBLIC_KEY_LENGTH + remaining, Expected { "expected 32 bytes" }),
            )
        }

        return try {
            SerdeResult.success(VerifyingKey.fromBytes(bytes))
        } catch (e: Exception) {
            SerdeResult.failure(SerdeError.custom(e.message ?: "invalid verifying key"))
        }
    }
}
