package fr.vco.codingame.contest.code4life

import java.util.*
import kotlin.math.max

fun log(message: String) = System.err.println(message)

const val MAX_MOLECULES = 10
const val MAX_SAMPLES = 3
val moleculeType = listOf("A", "B", "C", "D", "E")

enum class Module { DIAGNOSIS, MOLECULES, SAMPLES, LABORATORY, START_POS }

fun List<Pair<String, Int>>.toMolecules() = Molecules(this.toMap())

data class Molecules(val molecules: Map<String, Int>) {
    constructor(input: Scanner) : this(List(5) { input.nextInt() }.zip(moleculeType).map { (a, b) -> b to a }.toMap())

    operator fun get(key: String): Int = molecules[key]!!
    operator fun plus(other: Molecules) = molecules.map { (a, b) -> a to b + other[a] }.toMolecules()
    operator fun minus(other: Molecules) = molecules.map { (a, b) -> a to b - other[a] }.toMolecules()
    fun sum() = molecules.values.sum()
    fun union(other: Molecules): Collection<String> = molecules.filter { (a, b) -> b > 0 && other[a] > 0 }.keys
}

data class Bot(
    val module: Module,
    val eta: Int,
    val score: Int,
    val storage: Molecules,
    val expertise: Molecules
) {
    constructor(input: Scanner) : this(
        module = Module.valueOf(input.next()),
        eta = input.nextInt(),
        score = input.nextInt(),
        storage = Molecules(input),
        expertise = Molecules(input)
    )

    val usable: Molecules = storage + expertise

    fun hasEnough(costs: Molecules): Boolean {
        return (usable - costs).molecules.all { (_, b) -> b >= 0 }
    }

    fun need(costs: Molecules): Molecules {
        return (costs - usable).molecules.map { (a, b) -> a to max(b, 0) }.toMolecules()
    }

    fun isMoving() = eta > 0

}

data class Sample(
    val id: Int,
    val owner: Int,
    val rank: Int,
    val gain: String,
    val health: Int,
    val costs: Molecules
) {

    constructor(input: Scanner) : this(
        id = input.nextInt(),
        owner = input.nextInt(),
        rank = input.nextInt(),
        gain = input.next(),
        health = input.nextInt(),
        costs = Molecules(input)
    )

    fun rentability() = health.toFloat() / costs.sum()
    fun isDiagnosed() = health != -1
}

fun main() {
    val input = Scanner(System.`in`)
    val projectCount = input.nextInt()
    val projects = List(projectCount) { Molecules(input)}

    // game loop
    while (true) {
        val bots = List(2) { Bot(input) }
        val availableMolecules = Molecules(input)
        val sampleCount = input.nextInt()
        val samples = List(sampleCount) { Sample(input) }

        val (me,opp) = bots

        val mySamples = samples.filter { it.owner == 0 }
        val oppSamples = samples.filter { it.owner == 1 }
        val cloudSamples = samples.filter { it.owner == -1 }

        val action = when {
            me.isMoving() -> "WAIT"
            me.module == Module.START_POS -> "GOTO ${Module.SAMPLES}"
            me.module == Module.SAMPLES -> {
                val rank = when {
                    me.expertise.sum() > 10 -> 3
                    me.expertise.sum() > 5 -> 2
                    else -> 1
                }
                if (mySamples.size < MAX_SAMPLES) "CONNECT $rank"
                else "GOTO ${Module.DIAGNOSIS}"
            }
            me.module == Module.DIAGNOSIS -> {
                if (mySamples.isEmpty()) "GOTO ${Module.SAMPLES}"
                else mySamples.firstOrNull { !it.isDiagnosed() }?.let { "CONNECT ${it.id}" }
                    ?: mySamples.firstOrNull { me.need(it.costs).molecules.any { (_, m) -> m > 5 } }?.let { "CONNECT ${it.id}" }
                    ?: "GOTO ${Module.MOLECULES}"
            }
            me.module == Module.MOLECULES -> {
                when {
                    mySamples.any { me.hasEnough(it.costs) } -> "GOTO ${Module.LABORATORY}"
                    else -> mySamples.firstOrNull()
                        ?.let { me.need(it.costs) }
                        ?.apply { log(this.toString()) }
                        ?.union(availableMolecules)
                        ?.apply { log(this.toString()) }
                        ?.firstOrNull()?.let { "CONNECT $it" }
                        ?: "WAIT"
                }
            }
            me.module == Module.LABORATORY -> {
                when {
                    mySamples.isEmpty() -> "GOTO ${Module.SAMPLES}"
                    mySamples.none { me.hasEnough(it.costs) } -> "GOTO ${Module.MOLECULES}"
                    else -> mySamples.firstOrNull { me.hasEnough(it.costs) }?.let { "CONNECT ${it.id}" } ?: "WAIT"
                }
            }
            else -> "WAIT"
        }
        log(action)
        println(action)
    }
}