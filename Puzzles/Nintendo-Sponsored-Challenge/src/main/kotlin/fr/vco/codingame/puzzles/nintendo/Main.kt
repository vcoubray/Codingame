@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.vco.codingame.puzzles.nintendo

import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextUInt


fun decode(input: String, size: Int): List<String> {
    val polynomial = input.toPolynomial()
    val factors = polynomial.getIrreductibleFactors()

    val results = mutableListOf<Pair<Polynomial, Polynomial>>()
    var f1 = factors.first()
    var f2 = factors.drop(1).reduce(Polynomial::times)

    factors.drop(1).forEach {
        if (f1.deg() <= size && f2.deg() <= size) {
            results.add(Pair(f1, f2))
            results.add(Pair(f2, f1))
        }
        f1 *= it
        f2 /= it
    }

    if (polynomial.deg() <= size) {
        results.add(Pair(polynomial, Polynomial.ONE))
        results.add(Pair(Polynomial.ONE, polynomial))
    }

    return results.map { it.first.toString(16) + " " + it.second.toString(16) }.sorted()
}

fun String.toPolynomial(): Polynomial {
    return Polynomial(this.split(" ").map { it.toUInt(16) }.toUIntArray())
}

val ODD_MASK = (0..16).fold(0u) { a, b -> a or (1u shl (2 * b + 1)) }

class Polynomial(rawTerms: UIntArray) {

    val terms: UIntArray = rawTerms.trim()

    constructor(term: UInt) : this(uintArrayOf(term))

    companion object {
        val ZERO = Polynomial(UIntArray(0))
        val ONE = Polynomial(1u)

        fun x(n: Int): Polynomial {
            val arr = UIntArray(n / 32 + 1)
            arr[n / 32] = 1u shl (n % 32)
            return Polynomial(arr)
        }

        fun random(deg: Int): Polynomial {
            val termSize = deg / 32 + 1
            val terms = UIntArray(termSize) {
                Random.nextUInt()
            }
            terms[termSize - 1] = terms[termSize - 1] and ((1u shl deg%32) - 1u)
            return Polynomial(terms.trim())
        }
    }

    fun evalAt0() = if (this.isZero()) 0u else terms[0] and 1u
    fun evalAt1() = terms.sumOf { it.countOneBits() }.toUInt() % 2u

    fun isZero(): Boolean = terms.isEmpty()

    fun deg(): Int {
        if (terms.isEmpty()) return -1
        val last = terms.last()
        return (terms.size - 1) * 32 + (31 - last.countLeadingZeroBits())
    }

    fun derivative(): Polynomial = Polynomial((terms and ODD_MASK).shr(1))

    operator fun plus(other: Polynomial): Polynomial {
        val n = max(terms.size, other.terms.size)
        val r = UIntArray(n) { i ->
            (if (i < terms.size) terms[i] else 0u) xor
                    (if (i < other.terms.size) other.terms[i] else 0u)
        }
        return Polynomial(r)
    }

    operator fun times(other: Polynomial): Polynomial {
        if (isZero() || other.isZero()) return ZERO
        val resultBits = deg() + other.deg() + 1
        val result = UIntArray((resultBits + 31) / 32)
        for (i in terms.indices) {
            var term = terms[i]
            var bitIdx = i * 32
            while (term != 0u) {
                if ((term and 1u) == 1u) {
                    xorShiftedInto(result, other.terms, bitIdx)
                }
                term = term shr 1
                bitIdx++
            }
        }
        return Polynomial(result)
    }

    fun divmod(other: Polynomial): Pair<Polynomial, Polynomial> {
        require(!other.isZero()) { "Divide by Zero" }
        val dDeg = other.deg()
        val myDeg = deg()
        if (myDeg < dDeg) return ZERO to this

        val rem = terms.copyOf()
        val quot = UIntArray((myDeg - dDeg) / 32 + 1)
        var remDeg = myDeg
        while (remDeg >= dDeg) {
            val shift = remDeg - dDeg
            quot[shift / 32] = quot[shift / 32] or (1u shl (shift % 32))
            xorShiftedInto(rem, other.terms, shift)
            remDeg = degOf(rem)
        }
        return Polynomial(quot) to Polynomial(rem)
    }

    operator fun div(other: Polynomial): Polynomial = divmod(other).first
    operator fun rem(other: Polynomial): Polynomial = divmod(other).second

    fun square(): Polynomial {
        val squareTerms = UIntArray(terms.size * 2)
        terms.forEachIndexed { i, term ->
            val (lo, hi) = term.spreadBits()
            squareTerms[2 * i] = lo
            squareTerms[2 * i + 1] = hi
        }
        return Polynomial(squareTerms)
    }

    fun sqrt(): Polynomial {

        val sqrtTerms = UIntArray((terms.size + 1) / 2) { 0u }

        for (i in 0..deg() / 2) {
            sqrtTerms[i / 32] = sqrtTerms[i / 32] or (((terms[i / 16] shr ((2 * i) % 32)) and 1u) shl (i % 32))
        }
        return Polynomial(sqrtTerms)
    }

    fun squarefreeDecomposition(): List<Pair<Polynomial, Int>> {
        val squareFrees = mutableListOf<Pair<Polynomial, Int>>()
        var p = this

        var multiplier = 1

        while (p != ONE) {
            val pp = p.derivative()
            if (pp.isZero()) {
                p = p.sqrt()
                multiplier *= 2
            } else {
                var i = 1
                var c = p.gcd(pp)
                var w = p / c
                while (w != ONE) {
                    val y = w.gcd(c)
                    val z = w / y
                    if (z != ONE) squareFrees.add(z to i * multiplier)
                    w = y
                    c /= y
                    i++
                }
                p = c
            }
        }
        return squareFrees
    }

    fun gcd(other: Polynomial): Polynomial {
        var a = this
        var b = other
        while (!b.isZero()) {
            val r = a % b
            a = b
            b = r
        }
        return a
    }

    // Distinct-Degree Factorization : suppose `this` square-free.
    // Return the list of (g, k) where g is the product of every irreducible factor of degree k.
    fun ddf(): List<Pair<Polynomial, Int>> {
        val result = mutableListOf<Pair<Polynomial, Int>>()
        val x = x(1)
        var p = this
        var y = x
        var k = 1
        while (2 * k <= p.deg()) {
            y = y.square() % p
            val g = (y + x).gcd(p)
            if (g.deg() > 0) {
                result.add(g to k)
                p /= g
                y %= p
            }
            k++
        }
        if (p.deg() > 0) result.add(p to p.deg())
        return result
    }

    fun edf(degre: Int): List<Polynomial> {
        if(this.deg() < degre || this == ONE) return emptyList()
        if (this.deg() == degre) return listOf(this)
        var gcd: Polynomial
        do {
            val r = random(deg())
            var t = r
            var s = r
            repeat(degre - 1) {
                s = s.square() % this
                t = (t + s) % this
            }
            gcd = t.gcd(this)
        } while (gcd.deg() == 0 || gcd.deg() == this.deg())
        return gcd.edf(degre) + (this / gcd).edf(degre)
    }

    fun getIrreductibleFactors(): List<Polynomial> {

        if (evalAt0() == 0u) {
            val div = Polynomial(2u) // x
            return this.div(div).getIrreductibleFactors() + div
        }
        if (evalAt1() == 0u) {
            val div = Polynomial(3u) // x + 1
            return this.div(div).getIrreductibleFactors() + div
        }

        val a = squarefreeDecomposition().flatMap { (p, multiplier) ->
            val irredeuctibles = p.ddf().map { (p2, k) -> p2.edf(k) }

            List(multiplier) { irredeuctibles }.flatten()
        }

        return a.flatten()
    }

    override fun equals(other: Any?): Boolean {
        return other is Polynomial && this.terms.contentEquals(other.terms)
    }

    override fun hashCode(): Int {
        return terms.contentHashCode()
    }

    fun toString(radix: Int): String {
        val width = UInt.MAX_VALUE.toString(radix).length
        if (terms.isEmpty()) return "0".padStart(width, '0')
        return terms.joinToString(" ") { it.toString(radix).padStart(width, '0') }
    }

    override fun toString(): String = "${toString(16)} -> ${toString(2)}"

}

infix fun UIntArray.and(mask: UInt): UIntArray {
    return this.map { it and mask }.toUIntArray()
}

infix fun UIntArray.shr(n: Int): UIntArray {
    val result = UIntArray(size)
    val wordShift = n / 32
    val bitShift = n % 32

    for (i in 0 until size - wordShift) {
        val src = i + wordShift
        result[i] = this[src] shr bitShift
        if (bitShift > 0 && src + 1 < size) {
            result[i] = result[i] or (this[src + 1] shl (32 - bitShift))
        }
    }
    return result
}

private fun xorShiftedInto(dest: UIntArray, src: UIntArray, shiftBits: Int) {
    val wordShift = shiftBits / 32
    val bitShift = shiftBits % 32
    for (i in src.indices) {
        val di = i + wordShift
        if (di >= dest.size) break
        dest[di] = dest[di] xor (src[i] shl bitShift)
        if (bitShift > 0 && di + 1 < dest.size) {
            dest[di + 1] = dest[di + 1] xor (src[i] shr (32 - bitShift))
        }
    }
}

private fun degOf(a: UIntArray): Int {
    for (i in a.size - 1 downTo 0) {
        if (a[i] != 0u) return i * 32 + (31 - a[i].countLeadingZeroBits())
    }
    return -1
}

private fun UIntArray.trim(): UIntArray {
    var n = size
    while (n > 0 && this[n - 1] == 0u) n--
    return if (n == size) this else copyOf(n)
}

private fun UInt.spreadBits(): Pair<UInt, UInt> {
    fun spread(bits: UInt): UInt {
        var result = bits
        result = result or (result shl 8) and 0x00FF00FFu
        result = result or (result shl 4) and 0x0F0F0F0Fu
        result = result or (result shl 2) and 0x33333333u
        result = result or (result shl 1) and 0x55555555u
        return result
    }
    return spread(this and 0x0000FFFFu) to spread(this shr 16)
}

