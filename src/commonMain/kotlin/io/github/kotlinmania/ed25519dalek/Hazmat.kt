// port-lint: source hazmat.rs
package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.EdwardsPoint
import io.github.kotlinmania.ed25519dalek.internal.Scalar
import io.github.kotlinmania.ed25519dalek.internal.Sha512

/**
 * Low-level interfaces to ed25519 functions.
 *
 * Warning: Hazmat
 *
 * These primitives are easy-to-misuse low-level interfaces.
 */

/**
 * Contains the secret scalar and domain separator used for generating signatures.
 */
class ExpandedSecretKey internal constructor(
    internal val scalar: Scalar,
    val hashPrefix: ByteArray,
) {
    init {
        require(hashPrefix.size == 32) { "hashPrefix must be 32 bytes" }
    }

    /** View the 32-byte secret scalar. */
    fun scalarBytes(): ByteArray = scalar.toByteArray()

    companion object {
        private fun clampInteger(bytes: ByteArray): ByteArray {
            val copy = bytes.copyOf()
            copy[0] = (copy[0].toInt() and 248).toByte()
            copy[31] = (copy[31].toInt() and 127).toByte()
            copy[31] = (copy[31].toInt() or 64).toByte()
            return copy
        }

        fun fromBytes(bytes: ByteArray): ExpandedSecretKey {
            if (bytes.size != EXPANDED_SECRET_KEY_LENGTH) {
                throw SignatureError.from(
                    InternalError.BytesLength("ExpandedSecretKey", EXPANDED_SECRET_KEY_LENGTH),
                )
            }
            val scalarBytes = bytes.copyOfRange(0, 32)
            val hashPrefix = bytes.copyOfRange(32, 64)
            val clamped = clampInteger(scalarBytes)
            val scalar = Scalar.fromBits(clamped)
            return ExpandedSecretKey(scalar, hashPrefix)
        }

        fun fromSlice(bytes: ByteArray): ExpandedSecretKey = fromBytes(bytes)

        /** Generate a random [ExpandedSecretKey]. */
        fun random(): ExpandedSecretKey {
            val bytes = kotlin.random.Random.nextBytes(EXPANDED_SECRET_KEY_LENGTH)
            return fromBytes(bytes)
        }
    }

    fun toBytes(): ByteArray {
        val out = ByteArray(EXPANDED_SECRET_KEY_LENGTH)
        scalar.toByteArray().copyInto(out, 0, 0, 32)
        hashPrefix.copyInto(out, 32, 0, 32)
        return out
    }

    internal fun rawSign(message: ByteArray, verifyingKey: VerifyingKey): Signature {
        val hasherR = Sha512()
        hasherR.update(hashPrefix)
        hasherR.update(message)
        val rDigest = hasherR.finalize()
        val rScalar = Scalar.fromHash(rDigest)

        val R = EdwardsPoint.BASEPOINT.mul(rScalar).compress()

        val hasherK = Sha512()
        hasherK.update(R.asBytes())
        hasherK.update(verifyingKey.asBytes())
        hasherK.update(message)
        val kDigest = hasherK.finalize()
        val kScalar = Scalar.fromHash(kDigest)

        val s = rScalar.add(kScalar.mul(scalar))

        return Signature.fromComponents(R.asBytes(), s.toByteArray())
    }

    internal fun rawSignPrehashed(
        prehashedMessage: ByteArray,
        verifyingKey: VerifyingKey,
        context: ByteArray?,
    ): Signature {
        val ctx = context ?: ByteArray(0)
        if (ctx.size > Context.MAX_LENGTH) {
            throw SignatureError.from(InternalError.PrehashedContextLength)
        }

        // Domain separator for Ed25519ph: "SigEd25519 no Ed25519 collisions" || 1 || len(ctx) || ctx
        val domPrefix = buildDomainPrefix(1, ctx)

        val hasherR = Sha512()
        hasherR.update(domPrefix)
        hasherR.update(hashPrefix)
        hasherR.update(prehashedMessage)
        val rDigest = hasherR.finalize()
        val rScalar = Scalar.fromHash(rDigest)

        val R = EdwardsPoint.BASEPOINT.mul(rScalar).compress()

        val hasherK = Sha512()
        hasherK.update(domPrefix)
        hasherK.update(R.asBytes())
        hasherK.update(verifyingKey.asBytes())
        hasherK.update(prehashedMessage)
        val kDigest = hasherK.finalize()
        val kScalar = Scalar.fromHash(kDigest)

        val s = rScalar.add(kScalar.mul(scalar))

        return Signature.fromComponents(R.asBytes(), s.toByteArray())
    }

    internal fun rawSignByUpdate(
        msgUpdate: (Sha512) -> Unit,
        verifyingKey: VerifyingKey,
    ): Signature {
        val hasherR = Sha512()
        hasherR.update(hashPrefix)
        msgUpdate(hasherR)
        val rDigest = hasherR.finalize()
        val rScalar = Scalar.fromHash(rDigest)

        val R = EdwardsPoint.BASEPOINT.mul(rScalar).compress()

        val hasherK = Sha512()
        hasherK.update(R.asBytes())
        hasherK.update(verifyingKey.asBytes())
        msgUpdate(hasherK)
        val kDigest = hasherK.finalize()
        val kScalar = Scalar.fromHash(kDigest)

        val s = rScalar.add(kScalar.mul(scalar))

        return Signature.fromComponents(R.asBytes(), s.toByteArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExpandedSecretKey) return false
        return scalar == other.scalar && hashPrefix.contentEquals(other.hashPrefix)
    }

    override fun hashCode(): Int = 31 * scalar.hashCode() + hashPrefix.contentHashCode()

    override fun toString(): String = "ExpandedSecretKey(...)"
}

internal fun buildDomainPrefix(flag: Byte, context: ByteArray): ByteArray {
    val prefixString = "SigEd25519 no Ed25519 collisions".encodeToByteArray()
    val out = ByteArray(prefixString.size + 2 + context.size)
    prefixString.copyInto(out, 0)
    out[prefixString.size] = flag
    out[prefixString.size + 1] = context.size.toByte()
    context.copyInto(out, prefixString.size + 2)
    return out
}

/** Compute an ordinary Ed25519 signature over the given message. */
fun rawSign(
    esk: ExpandedSecretKey,
    message: ByteArray,
    verifyingKey: VerifyingKey,
): Signature = esk.rawSign(message, verifyingKey)

/** Compute a signature over the given prehashed message. */
fun rawSignPrehashed(
    esk: ExpandedSecretKey,
    prehashedMessage: ByteArray,
    verifyingKey: VerifyingKey,
    context: ByteArray? = null,
): Signature = esk.rawSignPrehashed(prehashedMessage, verifyingKey, context)

/** Compute an ordinary Ed25519 signature with incremental message chunks. */
internal fun rawSignByUpdate(
    esk: ExpandedSecretKey,
    msgUpdate: (Sha512) -> Unit,
    verifyingKey: VerifyingKey,
): Signature = esk.rawSignByUpdate(msgUpdate, verifyingKey)

/** Ordinary non-batched Ed25519 verification check. */
fun rawVerify(
    vk: VerifyingKey,
    message: ByteArray,
    signature: Signature,
): Unit = vk.verify(message, signature)

/** Prehashed Ed25519 verification check. */
fun rawVerifyPrehashed(
    vk: VerifyingKey,
    prehashedMessage: ByteArray,
    context: ByteArray? = null,
    signature: Signature,
): Unit = vk.verifyPrehashed(prehashedMessage, context, signature)
