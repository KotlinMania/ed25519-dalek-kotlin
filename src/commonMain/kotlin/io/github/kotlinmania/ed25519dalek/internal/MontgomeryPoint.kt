package io.github.kotlinmania.ed25519dalek.internal

/**
 * A Montgomery curve point represented by its 32-byte U-coordinate.
 */
internal class MontgomeryPoint(
    private val bytes: ByteArray,
) {
    init {
        require(bytes.size == 32)
    }

    fun asBytes(): ByteArray = bytes.copyOf()

    fun toByteArray(): ByteArray = bytes.copyOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MontgomeryPoint) return false
        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun toString(): String =
        "MontgomeryPoint(" + bytes.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') } + ")"
}
