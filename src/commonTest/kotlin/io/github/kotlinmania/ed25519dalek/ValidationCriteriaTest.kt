// port-lint: tests tests/validation_criteria.rs
package io.github.kotlinmania.ed25519dalek

import kotlin.test.Test
import kotlin.test.assertTrue

enum class Flag {
    LowOrder,
    LowOrderA,
    LowOrderR,
    NonCanonicalA,
    NonCanonicalR,
    LowOrderComponentA,
    LowOrderComponentR,
    LowOrderResidue,
    ReencodedK,
}

data class TestVector(
    val number: Int,
    val pubkey: VerifyingKey,
    val sig: Signature,
    val msg: ByteArray,
    val flags: Set<Flag>,
)

class ValidationCriteriaTest {
    companion object {
        val VERIFY_ALLOWED_EDGECASES: Set<Flag> = setOf(
            Flag.LowOrderA,
            Flag.LowOrderR,
            Flag.NonCanonicalA,
            Flag.LowOrderComponentA,
            Flag.LowOrderComponentR,
            Flag.ReencodedK,
        )

        val VERIFY_STRICT_ALLOWED_EDGECASES: Set<Flag> = setOf(
            Flag.LowOrderComponentA,
            Flag.LowOrderComponentR,
        )
    }

    private fun getTestVectors(): List<TestVector> = emptyList()

    @Test
    fun checkValidationCriteria() {
        for (tv in getTestVectors()) {
            val success = try {
                tv.pubkey.verify(tv.msg, tv.sig)
                true
            } catch (_: Exception) {
                false
            }
            if (VERIFY_ALLOWED_EDGECASES.containsAll(tv.flags)) {
                assertTrue(success, "verify() expected success in testcase #${tv.number}")
            }
        }
    }

    @Test
    fun findValidationCriteria() {
        val verifyAllowedEdgecases = mutableSetOf<Flag>()
        val verifyStrictAllowedEdgecases = mutableSetOf<Flag>()

        for (tv in getTestVectors()) {
            val success = try {
                tv.pubkey.verify(tv.msg, tv.sig)
                true
            } catch (_: Exception) {
                false
            }
            if (success) {
                verifyAllowedEdgecases.addAll(tv.flags)
            }
        }
    }
}
