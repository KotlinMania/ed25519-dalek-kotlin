package io.github.kotlinmania.ed25519dalek.internal

/**
 * A point on the Edwards form of Curve25519 in extended coordinates (X : Y : Z : T)
 * where x = X / Z, y = Y / Z, and x * y = T / Z.
 */
internal class EdwardsPoint internal constructor(
    val X: FieldElement,
    val Y: FieldElement,
    val Z: FieldElement,
    val T: FieldElement,
) {
    companion object {
        val IDENTITY =
            EdwardsPoint(
                FieldElement.ZERO,
                FieldElement.ONE,
                FieldElement.ONE,
                FieldElement.ZERO,
            )

        // Basepoint B = (x, 4/5)
        val BASEPOINT: EdwardsPoint by lazy {
            val yBytes =
                byteArrayOf(
                    0x58.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                    0x66.toByte(),
                )
            CompressedEdwardsY(yBytes).decompress()
                ?: error("Failed to decompress basepoint")
        }

        fun vartimeDoubleScalarMulBasepoint(
            a: Scalar,
            A: EdwardsPoint,
            b: Scalar,
        ): EdwardsPoint {
            var res = IDENTITY
            for (i in 255 downTo 0) {
                res = res.double()
                val bitA = a.getBit(i)
                val bitB = b.getBit(i)
                if (bitA == 1 && bitB == 1) {
                    res = res.add(A).add(BASEPOINT)
                } else if (bitA == 1) {
                    res = res.add(A)
                } else if (bitB == 1) {
                    res = res.add(BASEPOINT)
                }
            }
            return res
        }

        fun optionalMultiscalarMul(
            scalars: Iterable<Scalar>,
            points: Iterable<EdwardsPoint?>,
        ): EdwardsPoint? {
            val scalarList = scalars.toList()
            val pointList = points.toList()
            if (scalarList.size != pointList.size) return null

            var res = IDENTITY
            for (i in 255 downTo 0) {
                res = res.double()
                for (j in scalarList.indices) {
                    val pt = pointList[j] ?: return null
                    if (scalarList[j].getBit(i) == 1) {
                        res = res.add(pt)
                    }
                }
            }
            return res
        }
    }

    fun add(other: EdwardsPoint): EdwardsPoint {
        // Extended coordinate addition
        val a = (Y.sub(X)).mul(other.Y.sub(other.X))
        val b = (Y.add(X)).mul(other.Y.add(other.X))
        val c = T.mul(FieldElement.TWO_D).mul(other.T)
        val d = Z.mul(FieldElement(2, 0, 0, 0, 0, 0, 0, 0, 0, 0)).mul(other.Z)
        val e = b.sub(a)
        val f = d.sub(c)
        val g = d.add(c)
        val h = b.add(a)

        val x3 = e.mul(f)
        val y3 = g.mul(h)
        val t3 = e.mul(h)
        val z3 = f.mul(g)
        return EdwardsPoint(x3, y3, z3, t3)
    }

    fun double(): EdwardsPoint {
        val a = X.square()
        val b = Y.square()
        val c = Z.square().mul(FieldElement(2, 0, 0, 0, 0, 0, 0, 0, 0, 0))
        val d = a.negate()
        val e = (X.add(Y)).square().sub(a).sub(b)
        val g = d.add(b)
        val f = g.sub(c)
        val h = d.sub(b)

        val x3 = e.mul(f)
        val y3 = g.mul(h)
        val t3 = e.mul(h)
        val z3 = f.mul(g)
        return EdwardsPoint(x3, y3, z3, t3)
    }

    fun negate(): EdwardsPoint = EdwardsPoint(X.negate(), Y, Z, T.negate())

    fun sub(other: EdwardsPoint): EdwardsPoint = this.add(other.negate())

    fun mul(scalar: Scalar): EdwardsPoint {
        var res = IDENTITY
        for (i in 255 downTo 0) {
            res = res.double()
            if (scalar.getBit(i) == 1) {
                res = res.add(this)
            }
        }
        return res
    }

    fun isIdentity(): Boolean {
        // Point is identity if X == 0 and Y == Z
        val xz = X.mul(FieldElement.ONE)
        val yz = Y.sub(Z)
        return xz == FieldElement.ZERO && yz == FieldElement.ZERO
    }

    fun isSmallOrder(): Boolean {
        // Check if 8 * P == IDENTITY
        val p2 = this.double()
        val p4 = p2.double()
        val p8 = p4.double()
        return p8.isIdentity()
    }

    fun compress(): CompressedEdwardsY {
        val zInv = Z.invert()
        val x = X.mul(zInv)
        val y = Y.mul(zInv)
        val bytes = y.toByteArray()
        if (x.isNegative()) {
            bytes[31] = (bytes[31].toInt() or 0x80).toByte()
        }
        return CompressedEdwardsY(bytes)
    }

    fun toMontgomery(): MontgomeryPoint {
        // u = (Z + Y) / (Z - Y)
        val num = Z.add(Y)
        val den = Z.sub(Y)
        val u =
            if (den == FieldElement.ZERO) {
                FieldElement.ZERO
            } else {
                num.mul(den.invert())
            }
        return MontgomeryPoint(u.toByteArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EdwardsPoint) return false
        // X1 * Z2 == X2 * Z1 and Y1 * Z2 == Y2 * Z1
        val x1z2 = this.X.mul(other.Z)
        val x2z1 = other.X.mul(this.Z)
        val y1z2 = this.Y.mul(other.Z)
        val y2z1 = other.Y.mul(this.Z)
        return x1z2 == x2z1 && y1z2 == y2z1
    }

    override fun hashCode(): Int = compress().hashCode()

    override fun toString(): String = "EdwardsPoint(${compress()})"
}
