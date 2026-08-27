// port-lint: source verifying.rs
package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.CompressedEdwardsY
import io.github.kotlinmania.ed25519dalek.internal.EdwardsPoint
import io.github.kotlinmania.ed25519dalek.internal.MontgomeryPoint
import io.github.kotlinmania.ed25519dalek.internal.Scalar
import io.github.kotlinmania.ed25519dalek.internal.Sha512
import io.github.kotlinmania.ed25519dalek.verifying.StreamVerifier

/**
 * An ed25519 public key.
 */
class VerifyingKey internal constructor(
    internal val compressed: CompressedEdwardsY,
    internal val point: EdwardsPoint,
) {
    companion object {
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
        val prefix = byteArrayOf(
            0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x03, 0x21, 0x00,
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
     * Verify a signature on a message with this public key.
     */
    fun verify(message: ByteArray, signature: Signature) {
        val internalSig = signature.toInternal()
        val expectedR = RCompute.compute(this, internalSig, null, message)
        if (expectedR != internalSig.R) {
            throw SignatureError.from(InternalError.Verify)
        }
    }

    /**
     * Verify a signature on a prehashed message with an optional context string.
     */
    fun verifyPrehashed(
        prehashedMessage: ByteArray,
        context: ByteArray?,
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
