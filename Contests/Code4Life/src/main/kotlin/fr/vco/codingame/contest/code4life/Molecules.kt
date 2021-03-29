package fr.vco.codingame.contest.code4life

import java.util.*
import kotlin.math.max

const val MAX_MOLECULES = 10
const val MAX_SAMPLES = 3
const val MAX_MOLECULE = 5
val moleculeType = listOf("A", "B", "C", "D", "E")

fun List<Pair<String, Int>>.toMolecules() = Molecules(this.toMap())
fun Map<String, Int>.toMolecules() = Molecules(this)

data class Molecules(val molecules: Map<String, Int>) {
    constructor(input: Scanner) : this(List(5) { input.nextInt() }.zip(moleculeType).map { (a, b) -> b to a }.toMap())

    operator fun get(key: String): Int = molecules[key]!!
    operator fun plus(other: Molecules) = moleculeType.map { m -> m to this[m] + other[m] }.toMolecules()
    operator fun minus(other: Molecules) = moleculeType.map { m -> m to this[m] - other[m] }.toMolecules()
    fun add(type: String, quantity: Int) =
        molecules.toMutableMap().also { it[type] = it[type]!! + quantity }.toMolecules()

    fun sum() = molecules.values.sum()
    fun commonTypes(other: Molecules): Collection<String> = moleculeType.filter { m -> this[m] > 0 && other[m] > 0 }
    fun isValid() = sum() <= MAX_MOLECULES && molecules.all { (_, q) -> q in 0..MAX_MOLECULE }
    fun contains(other: Molecules) = moleculeType.all { m -> contains(m, other[m]) }
    fun contains(type: String, quantity: Int = 1) = this[type] >= quantity
    fun filterPositive() = molecules.map { (m, q) -> m to max(0, q) }.toMolecules()
}

val emptyMolecules = moleculeType.map { it to 0 }.toMolecules()

