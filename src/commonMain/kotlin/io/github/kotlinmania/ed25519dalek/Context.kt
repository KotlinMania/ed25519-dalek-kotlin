// port-lint: source ed25519-dalek/src/context.rs
package io.github.kotlinmania.ed25519dalek

/**
 * Ed25519 contexts as used by Ed25519ph.
 *
 * Contexts are domain separator strings that can be used to isolate uses of
 * the algorithm between different protocols (which is very hard to reliably do
 * otherwise) and between different uses within the same protocol.
 *
 * To create a context, call either of the following:
 *
 * - [SigningKey.withContext]
 * - [VerifyingKey.withContext]
 *
 * For more information, see [RFC8032 § 8.3](https://www.rfc-editor.org/rfc/rfc8032#section-8.3).
 */
class Context<K> internal constructor(
    private val key: K,
    private val value: ByteArray,
) {
    companion object {
        /** Maximum length of the context value in octets. */
        const val MAX_LENGTH: Int = 255

        /** Create a new Ed25519ph context. */
        internal fun <K> new(key: K, value: ByteArray): Context<K> {
            if (value.size <= MAX_LENGTH) {
                return Context(key, value.copyOf())
            } else {
                throw SignatureError.from(InternalError.PrehashedContextLength)
            }
        }
    }

    /** Borrow the key. */
    fun key(): K = key

    /** Borrow the context string value. */
    fun value(): ByteArray = value.copyOf()

    /** Sign a prehashed digest using this signing context. */
    fun signDigest(prehashedMessage: ByteArray): Signature {
        val k = key
        if (k is SigningKey) {
            return k.signPrehashed(prehashedMessage, value)
        }
        throw UnsupportedOperationException("Key is not a SigningKey")
    }

    /** Verify a prehashed digest using this verifying context. */
    fun verifyDigest(prehashedMessage: ByteArray, signature: Signature): Boolean {
        val k = key
        if (k is VerifyingKey) {
            return try {
                k.verifyPrehashed(prehashedMessage, value, signature)
                true
            } catch (_: Exception) {
                false
            }
        }
        throw UnsupportedOperationException("Key is not a VerifyingKey")
    }
}
