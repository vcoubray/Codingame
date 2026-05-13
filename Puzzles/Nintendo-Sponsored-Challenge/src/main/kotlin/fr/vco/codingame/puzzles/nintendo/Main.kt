@file:OptIn(ExperimentalUnsignedTypes::class)

package fr.vco.codingame.puzzles.nintendo

import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextUInt

fun main() {
    println(ODD_MASK.toString(16))
}

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

    fun edf(degree: Int): List<Polynomial> {
        if(this.deg() < degree || this == ONE) return emptyList()
        if (this.deg() == degree) return listOf(this)
        var gcd: Polynomial
        do {
            val r = random(deg())
            var t = r
            var s = r
            repeat(degree - 1) {
                s = s.square() % this
                t = (t + s) % this
            }
            gcd = t.gcd(this)
        } while (gcd.deg() == 0 || gcd.deg() == this.deg())
        return gcd.edf(degree) + (this / gcd).edf(degree)
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
            val irreductibles = p.ddf().map { (p2, k) -> p2.edf(k) }

            List(multiplier) { irreductibles }.flatten()
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

// C++ conversion
//
//
//#include <bit>
//#include <cstdint>
//#include <iostream>
//#include <sstream>
//#include <string>
//#include <vector>
//#include <algorithm>
//#include <random>
//#include <bitset>
//#include <iomanip>
//#include <stdexcept>
//#include <utility>
//
//using namespace std;
//
//
//vector<uint32_t> andMask(const vector<uint32_t>& v, uint32_t mask) {
//    vector<uint32_t> result(v.size());
//    for (size_t i = 0; i < v.size(); i++) result[i] = v[i] & mask;
//    return result;
//}
//
//vector<uint32_t> shr(const vector<uint32_t>& v, uint32_t shift) {
//    vector<uint32_t> result(v.size());
//    size_t wordShift = shift / 32;
//    size_t bitShift = shift % 32;
//
//    for (size_t i = 0; i < v.size() - wordShift; i++) {
//        size_t src = i + wordShift;
//        result[i] = v[src] >> bitShift;
//        if (bitShift > 0 && src +1 < v.size()) {
//            result[i] = result[i] | (v[src+1] << (32- bitShift));
//        }
//    }
//    return result;
//}
//
//void xorShiftedInto(vector<uint32_t>& dest, const vector<uint32_t>& src, const int shift) {
//    int wordShift = shift / 32;
//    int bitShift = shift % 32;
//    for(int i = 0; i < src.size(); i++) {
//        int di = i + wordShift;
//        if (di >= dest.size()) break;
//        dest[di] = dest[di] ^ (src[i] << bitShift);
//        if (bitShift > 0 && di + 1 < dest.size()) {
//            dest[di + 1] = dest[di + 1] ^ (src[i] >> (32 - bitShift));
//        }
//    }
//}
//
//uint32_t spreadLowHalfBits(const uint32_t& src) {
//    uint32_t result = src;
//    result = (result | (result << 8)) & 0x00FF00FFu;
//    result = (result | (result << 4)) & 0x0F0F0F0Fu;
//    result = (result | (result << 2)) & 0x33333333u;
//    result = (result | (result << 1)) & 0x55555555u;
//    return result;
//}
//
//pair<uint32_t, uint32_t> spreadBits(const uint32_t& src ) {
//    return { spreadLowHalfBits(src & 0x0000FFFFu) , spreadLowHalfBits(src >> 16)};
//}
//
//
//class Polynomial {
//    private:
//    vector<uint32_t> terms;
//
//    static vector<uint32_t> trim(vector<uint32_t> terms) {
//        size_t n = terms.size();
//        while(n > 0  && terms[n-1] == 0 ) n--;
//        if (n == terms.size()) {
//            return terms;
//        } else {
//            vector<uint32_t> trimed(n);
//            copy_n(terms.begin(), n, trimed.begin());
//            return trimed;
//        }
//    }
//
//    static int deg(const vector<uint32_t>& terms) {
//        for (int i = (int)terms.size() - 1; i >= 0; i--) {
//            if (terms[i] != 0) return i * 32 + (31 - countl_zero(terms[i]));
//        }
//        return -1;
//    }
//
//    static const uint32_t ODD_MASK = 0xAAAAAAAA;
//
//
//    public:
//
//    /* Constructors */
//    Polynomial(uint32_t term): terms{term} {}
//    Polynomial(vector<uint32_t> rawTerms) : terms(move(trim(rawTerms))) {}
//    Polynomial(string strTerms) {
//        stringstream ss(strTerms);
//        string term;
//        char del = ' ';
//        while (getline(ss, term, del)) {
//            terms.push_back(static_cast<uint32_t>(std::stoul(term, nullptr, 16)));
//        }
//        terms = trim(terms);
//    }
//
//    /* Static */
//    static const Polynomial ZERO;
//    static const Polynomial ONE;
//
//    static Polynomial x(size_t n){
//        vector<uint32_t> terms(n / 32 + 1);
//        terms[n/32] = 1 << (n % 32);
//        return Polynomial(terms);
//    }
//
//    static Polynomial random(int deg) {
//        if(deg < 0) return ZERO;
//
//        static std::mt19937 rng(std::random_device{}());
//        static std::uniform_int_distribution<uint32_t> dist;
//
//        int termSize = deg / 32 + 1;
//        vector<uint32_t> terms(termSize);
//        for (auto& t : terms) t = dist(rng);
//        terms[termSize - 1] = terms[termSize - 1] & ((1 << deg % 32) - 1);
//        return Polynomial(trim(terms));
//    }
//
//    /* methods */
//
//    bool isZero() const {
//        return terms.empty();
//    }
//
//    int deg() const {
//        return deg(terms);
//    }
//
//    bool operator==(const Polynomial& other) const {
//        return terms == other.terms;
//    }
//
//    bool operator!=(const Polynomial& other) const {
//        return terms != other.terms;
//    }
//
//    Polynomial operator+(const Polynomial& other) const {
//        int size = max(terms.size(), other.terms.size());
//        vector<uint32_t> r(size);
//        for (size_t i = 0; i < size; i++) {
//            r[i] = (i < terms.size()? terms[i]: 0) ^
//            (i < other.terms.size()? other.terms[i]: 0);
//        }
//        return Polynomial(r);
//    }
//
//    Polynomial operator*(const Polynomial& other) const {
//        if(isZero() || other.isZero()) return ZERO;
//        int resultDeg = deg() + other.deg() + 1;
//        vector<uint32_t> result = vector<uint32_t>((resultDeg + 31 ) / 32);
//
//        for (size_t i = 0; i < terms.size(); i++) {
//            uint32_t term = terms[i];
//            size_t bitIdx = i * 32;
//            while (term != 0) {
//                if ((term & 1) == 1) {
//                    xorShiftedInto(result, other.terms, bitIdx);
//                }
//                term = term >> 1;
//                bitIdx++;
//            }
//        }
//        return Polynomial(result);
//    }
//
//    pair<Polynomial, Polynomial> divmod(const Polynomial& other) const {
//        if(other.isZero()) throw("Divide by Zero");
//
//        int dDeg = other.deg();
//        int myDeg = deg();
//        if(myDeg < dDeg) return {ZERO, *this};
//
//        vector<uint32_t> rem(terms);
//        vector<uint32_t> quot = vector<uint32_t>((myDeg-dDeg) / 32 + 1);
//        int remDeg = myDeg;
//        while (remDeg >= dDeg) {
//            int shift = remDeg - dDeg;
//            quot[shift / 32] = quot[shift / 32] | (1 << (shift % 32));
//            xorShiftedInto(rem, other.terms, shift);
//            remDeg = deg(rem);
//        }
//        return {Polynomial(quot), Polynomial(rem)};
//    }
//
//
//    Polynomial operator/(const Polynomial& other) const {
//        return divmod(other).first;
//    }
//
//    Polynomial operator%(const Polynomial& other) const {
//        return divmod(other).second;
//    }
//
//    Polynomial square() const {
//        vector<uint32_t> square(terms.size() * 2);
//        for(size_t i = 0; i < terms.size(); i++) {
//            auto [lo, hi] = spreadBits(terms[i]);
//            square[i * 2] = lo;
//            square[i * 2 + 1] = hi;
//        }
//        return square;
//    }
//
//    Polynomial sqrt() const {
//        vector<uint32_t> sqrtTerms((terms.size()+1)/2, 0);
//        for (int i = 0; i < deg() / 2; i++) {
//            sqrtTerms[i / 32] |= terms[i/16] >> (((2 * i) % 32) & 1);
//        }
//        return Polynomial(sqrtTerms);
//    }
//
//    Polynomial gcd(const Polynomial& other) const {
//        Polynomial a = *this;
//        Polynomial b = other;
//
//        while (!b.isZero()) {
//            Polynomial r = a % b;
//            a = b;
//            b = r;
//        }
//        return a;
//    }
//
//    uint32_t evalAt0() const {
//        if(isZero()) {
//            return 0;
//        } else {
//            return terms.front() & 1;
//        }
//    }
//
//    uint32_t evalAt1() const {
//        uint32_t counter = 0;
//        for( uint32_t t: terms) {
//            counter += popcount(t);
//        }
//        return counter % 2;
//    }
//
//    Polynomial derivative() const {
//        return Polynomial(shr(andMask(terms, ODD_MASK), 1));
//    }
//
//    vector<pair<Polynomial, size_t>> squareFreeDecomposition() const {
//        vector<pair<Polynomial, size_t>> squareFrees;
//        Polynomial p = *this;
//
//        size_t multiplier = 1;
//
//        while (p != ONE) {
//            Polynomial pp = p.derivative();
//            if(pp.isZero()) {
//                p = p.sqrt();
//                multiplier *= 2;
//            } else {
//                size_t i = 1;
//                Polynomial c = p.gcd(pp);
//                Polynomial w = p / c;
//                while (w != ONE) {
//                    Polynomial y = w.gcd(c);
//                    Polynomial z = w / y;
//                    if (z != ONE) squareFrees.push_back({z, i * multiplier});
//                    w = y;
//                    c = c / y;
//                    i++;
//                }
//                p = c;
//            }
//        }
//        return squareFrees;
//    }
//
//    vector<pair<Polynomial, size_t>> ddf() const {
//        vector<pair<Polynomial, size_t>> result;
//        Polynomial x = Polynomial::x(1);
//        Polynomial p = *this;
//        Polynomial y = x;
//        size_t k = 1;
//
//        while (2 * k <= p.deg()) {
//            y = y.square() % p;
//            Polynomial g = (y + x).gcd(p);
//            if (g.deg() > 0) {
//                result.push_back({g, k});
//                p = p / g;
//                y = y % p;
//            }
//            k++;
//        }
//        if(p.deg() > 0) result.push_back({p, p.deg()});
//        return result;
//    }
//
//    vector<Polynomial> edf(int degree) const {
//        if (deg() < degree  || *this == ONE) return {};
//        if (deg() == degree) return {*this};
//
//        Polynomial gcd(0);
//        do {
//            Polynomial r = random(deg());
//            Polynomial t = r;
//            Polynomial s = r;
//            for (size_t i = 0; i < degree - 1; i++) {
//                s = s.square() % *this;
//                t = (t + s) % *this;
//            }
//            gcd = t.gcd(*this);
//
//        } while (gcd.deg() == 0 || gcd.deg() == deg());
//
//        vector<Polynomial> a = gcd.edf(degree);
//        vector<Polynomial> b = (*this/gcd).edf(degree);
//        vector<Polynomial> result;
//        result.insert(result.end(), a.begin(), a.end());
//        result.insert(result.end(), b.begin(), b.end());
//        return result;
//    }
//
//    vector<Polynomial> extractIrreductibleFactors() const {
//        vector<Polynomial> factors;
//        Polynomial p = *this;
//
//        size_t p0 = p.evalAt0();
//        size_t p1 = p.evalAt1();
//        while( p0 == 0 || p1 == 0) {
//            Polynomial div(1);
//
//            if(p0 == 0) {
//                div = x(1);
//            } else {
//                div = Polynomial(3); // x + 1
//            }
//            factors.push_back(div);
//            p = p / div;
//            p0 = p.evalAt0();
//            p1 = p.evalAt1();
//        }
//
//        for (const auto& [q, multiplier] : p.squareFreeDecomposition()) {
//            for (const auto& [s, k] : q.ddf()) {
//            for (Polynomial factor : s.edf(k)) {
//            for (int i = 0; i < multiplier; i++) {
//            factors.push_back(factor);
//        }
//        }
//        }
//        }
//
//        return factors;
//    }
//
//    string toBinary() const {
//        ostringstream oss;
//        for (size_t i = 0; i < terms.size(); i++) {
//            if (i > 0) oss << " ";
//            oss << bitset<32>(terms[i]).to_string();
//        }
//        return oss.str();
//    }
//
//    string toHex() const {
//        ostringstream oss;
//        for (size_t i = 0; i < terms.size(); i++) {
//            if (i > 0) oss << " ";
//            oss << std::hex << std::setw(8) << setfill('0') << terms[i];
//        }
//        return oss.str();
//    }
//};
//
//const Polynomial Polynomial::ZERO{std::vector<uint32_t>{}};
//const Polynomial Polynomial::ONE{std::vector<uint32_t>{1u}};
//
//
//string toString(const Polynomial& p1, const Polynomial& p2) {
//    ostringstream oss;
//    oss << p1.toHex() << " " << p2.toHex();
//    return oss.str();
//}
//
//vector<string> decode(const string& input, const size_t& size) {
//    vector<string> results;
//    Polynomial p = Polynomial(input);
//    vector<Polynomial> factors = p.extractIrreductibleFactors();
//
//    Polynomial f1 = factors.front();
//    Polynomial f2 = accumulate(
//            factors.begin() + 1, factors.end(),
//    Polynomial::ONE,
//    multiplies<Polynomial>()
//    );
//
//    for (size_t i = 1; i < factors.size(); i++) {
//        if (f1.deg() <= size && f2.deg() <= size) {
//            results.push_back(toString(f1, f2));
//            results.push_back(toString(f2, f1));
//        }
//        f1 = f1 * factors[i];
//        f2 = f2 * factors[i];
//    }
//
//    if(p.deg() <= size) {
//        results.push_back(toString(Polynomial::ONE.toHex(), p));
//        results.push_back(toString(p, Polynomial::ONE.toHex()));
//    }
//    sort(results.begin(), results.end());
//    return results;
//}
//
//
//int main()
//{
//    int size;
//    cin >> size; cin.ignore();
//    string line;
//    getline(cin, line);
//
//    for (const auto& result : decode(line, size)) {
//    cout << result << endl;
//}
//}
