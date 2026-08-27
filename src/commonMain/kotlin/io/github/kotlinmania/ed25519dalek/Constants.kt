// port-lint: source ed25519-dalek/src/constants.rs
package io.github.kotlinmania.ed25519dalek

/**
 * Common constants such as buffer sizes for keypairs and signatures.
 */

/** The length of an ed25519 `Signature`, in bytes. */
const val SIGNATURE_LENGTH: Int = 64

/** The length of an ed25519 `SecretKey`, in bytes. */
const val SECRET_KEY_LENGTH: Int = 32

/** The length of an ed25519 `PublicKey`, in bytes. */
const val PUBLIC_KEY_LENGTH: Int = 32

/** The length of an ed25519 `Keypair`, in bytes. */
const val KEYPAIR_LENGTH: Int = SECRET_KEY_LENGTH + PUBLIC_KEY_LENGTH

/** The length of the "key" portion of an "expanded" ed25519 secret key, in bytes. */
const val EXPANDED_SECRET_KEY_KEY_LENGTH: Int = 32

/** The length of the "nonce" portion of an "expanded" ed25519 secret key, in bytes. */
const val EXPANDED_SECRET_KEY_NONCE_LENGTH: Int = 32

/** The length of an "expanded" ed25519 key, `ExpandedSecretKey`, in bytes. */
const val EXPANDED_SECRET_KEY_LENGTH: Int =
    EXPANDED_SECRET_KEY_KEY_LENGTH + EXPANDED_SECRET_KEY_NONCE_LENGTH
