// port-lint: source ed25519-dalek/src/batch.rs
package io.github.kotlinmania.ed25519dalek

import io.github.kotlinmania.ed25519dalek.internal.EdwardsPoint
import io.github.kotlinmania.ed25519dalek.internal.Scalar
import io.github.kotlinmania.ed25519dalek.internal.Sha512

/**
 * Batch signature verification.
 */

/**
 * Verify a batch of [signatures] on [messages] with their respective [verifyingKeys].
 *
 * @throws SignatureError if any signature is invalid or if array lengths do not match.
 */
fun verifyBatch(
    messages: List<ByteArray>,
    signatures: List<Signature>,
    verifyingKeys: List<VerifyingKey>,
) {
    if (signatures.size != messages.size || signatures.size != verifyingKeys.size) {
        throw SignatureError.from(
            InternalError.ArrayLength(
                nameA = "signatures",
                lengthA = signatures.size,
                nameB = "messages",
                lengthB = messages.size,
                nameC = "verifying_keys",
                lengthC = verifyingKeys.size,
            ),
        )
    }

    if (signatures.isEmpty()) {
        return
    }

    val hrams = ArrayList<ByteArray>(signatures.size)
    for (i in signatures.indices) {
        val h = Sha512()
        h.update(signatures[i].rBytes())
        h.update(verifyingKeys[i].asBytes())
        h.update(messages[i])
        hrams.add(h.finalize())
    }

    val transcript = Sha512()
    transcript.update("ed25519 batch verification".encodeToByteArray())
    for (hram in hrams) {
        transcript.update(hram)
    }
    for (sig in signatures) {
        transcript.update(sig.sBytes())
    }
    val transcriptHash = transcript.finalize()

    val zs = ArrayList<Scalar>(signatures.size)
    for (i in signatures.indices) {
        val zh = Sha512()
        zh.update(transcriptHash)
        zh.update(byteArrayOf(i.toByte(), (i shr 8).toByte(), (i shr 16).toByte(), (i shr 24).toByte()))
        val zDigest = zh.finalize()
        val z64 = ByteArray(64)
        zDigest.copyInto(z64, 0, 0, 16) // 128-bit random scalar
        zs.add(Scalar.fromBytesModOrderWide(z64))
    }

    val internalSigs = signatures.map { it.toInternal() }
    val hramScalars = hrams.map { Scalar.fromBytesModOrderWide(it) }

    var bCoeff = Scalar.ZERO
    for (i in signatures.indices) {
        val sz = zs[i].mul(internalSigs[i].s)
        bCoeff = bCoeff.add(sz)
    }

    val zhrams = ArrayList<Scalar>(signatures.size)
    for (i in signatures.indices) {
        zhrams.add(hramScalars[i].mul(zs[i]))
    }

    val scalars = ArrayList<Scalar>()
    val points = ArrayList<EdwardsPoint?>()

    // -bCoefficient * BASEPOINT
    scalars.add(bCoeff.negate())
    points.add(EdwardsPoint.BASEPOINT)

    // + ∑ z[i] R[i]
    for (i in signatures.indices) {
        scalars.add(zs[i])
        points.add(internalSigs[i].R.decompress())
    }

    // + ∑ (z[i] H(R||A||M)[i]) A[i]
    for (i in signatures.indices) {
        scalars.add(zhrams[i])
        points.add(verifyingKeys[i].point)
    }

    val result =
        EdwardsPoint.optionalMultiscalarMul(scalars, points)
            ?: throw SignatureError.from(InternalError.Verify)

    if (!result.isIdentity()) {
        throw SignatureError.from(InternalError.Verify)
    }
}
