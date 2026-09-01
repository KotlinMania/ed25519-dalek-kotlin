// port-lint: source errors.rs
package io.github.kotlinmania.ed25519dalek

/**
 * Errors which may occur when parsing keys and/or signatures to or from wire formats.
 */

/**
 * Internal errors. Most application-level developers will likely not
 * need to pay any attention to these.
 */
sealed class InternalError {
    data object PointDecompression : InternalError() {
        override fun toString(): String = "Cannot decompress Edwards point"
    }

    data object ScalarFormat : InternalError() {
        override fun toString(): String = "Cannot use scalar with high-bit set"
    }

    /**
     * An error in the length of bytes handed to a constructor.
     *
     * To use this, pass a string specifying the [name] of the type which is
     * returning the error, and the [length] in bytes which its constructor
     * expects.
     */
    data class BytesLength(
        val name: String,
        val length: Int,
    ) : InternalError() {
        override fun toString(): String = "$name must be $length bytes in length"
    }

    /** The verification equation wasn't satisfied */
    data object Verify : InternalError() {
        override fun toString(): String = "Verification equation was not satisfied"
    }

    /**
     * Two arrays did not match in size, making the called signature
     * verification method impossible.
     */
    data class ArrayLength(
        val nameA: String,
        val lengthA: Int,
        val nameB: String,
        val lengthB: Int,
        val nameC: String,
        val lengthC: Int,
    ) : InternalError() {
        override fun toString(): String =
            "Arrays must be the same length: $nameA has length $lengthA, " +
                "$nameB has length $lengthB, $nameC has length $lengthC."
    }

    /** An ed25519ph signature can only take up to 255 octets of context. */
    data object PrehashedContextLength : InternalError() {
        override fun toString(): String =
            "An ed25519ph signature can only take up to 255 octets of context"
    }

    /** A mismatched (public, secret) key pair. */
    data object MismatchedKeypair : InternalError() {
        override fun toString(): String = "Mismatched Keypair detected"
    }
}

/**
 * Errors which may occur while processing signatures and keypairs.
 *
 * This error may arise due to:
 *
 * * Being given bytes with a length different to what was expected.
 * * A problem decompressing `r`, a curve point, in the `Signature`, or the
 *   curve point for a `PublicKey`.
 * * A problem with the format of `s`, a scalar, in the `Signature`. This
 *   is only raised if the high-bit of the scalar was set. (Scalars must
 *   only be constructed from 255-bit integers.)
 * * Failure of a signature to satisfy the verification equation.
 */
class SignatureError(
    message: String? = null,
    val internal: InternalError? = null,
    cause: Throwable? = null,
) : Exception(message ?: internal?.toString() ?: "Signature error", cause) {
    companion object {
        fun from(internal: InternalError): SignatureError =
            SignatureError(internal.toString(), internal)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignatureError) return false
        return message == other.message && internal == other.internal
    }

    override fun hashCode(): Int {
        var result = message?.hashCode() ?: 0
        result = 31 * result + (internal?.hashCode() ?: 0)
        return result
    }
}
