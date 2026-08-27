// port-lint: source ed25519-dalek/src/signature.rs
package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.CompressedEdwardsY
import io.github.kotlinmania.ed25519dalek.internal.Scalar

/**
 * An ed25519 signature.
 *
 * These signatures, unlike the ed25519 signature reference implementation, are
 * "detached"—that is, they do not include a copy of the message which has been signed.
 */
class Signature(
    private val bytes: ByteArray,
) {
    init {
        if (bytes.size != SIGNATURE_LENGTH) {
            throw SignatureError.from(InternalError.BytesLength("Signature", SIGNATURE_LENGTH))
        }
    }

    companion object {
        /** Construct a [Signature] from a slice of 64 bytes. */
        fun fromBytes(bytes: ByteArray): Signature {
            if (bytes.size != SIGNATURE_LENGTH) {
                throw SignatureError.from(InternalError.BytesLength("Signature", SIGNATURE_LENGTH))
            }
            // Validate internal format (scalar < l)
            InternalSignature.fromBytes(bytes)
            return Signature(bytes.copyOf())
        }

        /** Construct a [Signature] from 32-byte R and 32-byte S components. */
        fun fromComponents(r: ByteArray, s: ByteArray): Signature {
            if (r.size != 32 || s.size != 32) {
                throw SignatureError.from(InternalError.BytesLength("Signature component", 32))
            }
            val bytes = ByteArray(SIGNATURE_LENGTH)
            r.copyInto(bytes, 0, 0, 32)
            s.copyInto(bytes, 32, 0, 32)
            return fromBytes(bytes)
        }

        internal fun fromInternal(internal: InternalSignature): Signature {
            val bytes = ByteArray(SIGNATURE_LENGTH)
            internal.R.asBytes().copyInto(bytes, 0, 0, 32)
            internal.s.asBytes().copyInto(bytes, 32, 0, 32)
            return Signature(bytes)
        }
    }

    /** Convert this signature to a byte array. */
    fun toBytes(): ByteArray = bytes.copyOf()

    /** Return the R component of this signature. */
    fun rBytes(): ByteArray = bytes.copyOfRange(0, 32)

    /** Return the S component of this signature. */
    fun sBytes(): ByteArray = bytes.copyOfRange(32, 64)

    /** Split the signature into (R, S) components. */
    fun toComponents(): Pair<ByteArray, ByteArray> = Pair(rBytes(), sBytes())

    /** Convert this signature to a byte array (alias for [toBytes]). */
    fun toVec(): ByteArray = toBytes()

    internal fun toInternal(): InternalSignature = InternalSignature.fromBytes(bytes)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Signature) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String =
        "Signature(" + bytes.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') } + ")"
}

/**
 * Internal signature representation holding pre-parsed curve elements.
 */
internal class InternalSignature(
    val R: CompressedEdwardsY,
    val s: Scalar,
) {
    companion object {
        fun checkScalar(bytes: ByteArray): Scalar {
            if (bytes.size != 32) {
                throw SignatureError.from(InternalError.BytesLength("Scalar", 32))
            }
            return Scalar.fromCanonicalBytes(bytes)
                ?: throw SignatureError.from(InternalError.ScalarFormat)
        }

        fun fromBytes(bytes: ByteArray): InternalSignature {
            if (bytes.size != SIGNATURE_LENGTH) {
                throw SignatureError.from(InternalError.BytesLength("Signature", SIGNATURE_LENGTH))
            }
            val rBytes = bytes.copyOfRange(0, 32)
            val sBytes = bytes.copyOfRange(32, 64)
            val s = checkScalar(sBytes)
            return InternalSignature(CompressedEdwardsY(rBytes), s)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InternalSignature) return false
        return R == other.R && s == other.s
    }

    override fun hashCode(): Int = 31 * R.hashCode() + s.hashCode()

    override fun toString(): String = "Signature( R: $R, s: $s )"
}
