// port-lint: source verifying/stream.rs
package io.github.kotlinmania.ed25519dalek.verifying

import io.github.kotlinmania.ed25519dalek.InternalError
import io.github.kotlinmania.ed25519dalek.InternalSignature
import io.github.kotlinmania.ed25519dalek.RCompute
import io.github.kotlinmania.ed25519dalek.SignatureError
import io.github.kotlinmania.ed25519dalek.VerifyingKey
import io.github.kotlinmania.ed25519dalek.internal.CompressedEdwardsY

/**
 * An incremental stream verifier for ed25519.
 */
class StreamVerifier internal constructor(
    private val cr: RCompute,
    private val sigR: CompressedEdwardsY,
) {
    internal constructor(publicKey: VerifyingKey, signature: InternalSignature) : this(
        RCompute.new(publicKey, signature, null),
        signature.R,
    )

    /** Digest a message chunk. */
    fun update(chunk: ByteArray) {
        cr.update(chunk)
    }

    /** Finalize verifier and check against candidate signature. */
    fun finalizeAndVerify() {
        val expectedR = cr.finish()
        if (expectedR != sigR) {
            throw SignatureError.from(InternalError.Verify)
        }
    }
}
