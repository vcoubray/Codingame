package fr.vco.codingame.contest.code4life

import java.util.*

fun log(message: String) = System.err.println(message)

enum class Module { DIAGNOSIS, MOLECULES, SAMPLES, LABORATORY, START_POS }

val moleculeType = listOf("A", "B", "C", "D", "E")

fun Int.toMoleculeType() = moleculeType[this]

class Bot(
    val module: Module,
    val score: Int,
    val storage: List<Int>
) {
    fun hasEnough(cost: List<Int>): Boolean {
        return storage.zip(cost).all { (a, b) -> a >= b }
    }
}

data class Sample(
    val id: Int,
    val owner: Int,
    val rank: Int,
    val health: Int,
    val costs: List<Int>
) {
    fun rentability() = health.toFloat() / costs.sum()
    fun isDiagnosed() = health != -1
}

fun main() {
    val input = Scanner(System.`in`)
    val projectCount = input.nextInt()
    for (i in 0 until projectCount) {
        val a = input.nextInt()
        val b = input.nextInt()
        val c = input.nextInt()
        val d = input.nextInt()
        val e = input.nextInt()
    }

    // game loop
    while (true) {

        val bots = List(2) {
            val module = input.next()
            val eta = input.nextInt()
            val score = input.nextInt()
            val storageA = input.nextInt()
            val storageB = input.nextInt()
            val storageC = input.nextInt()
            val storageD = input.nextInt()
            val storageE = input.nextInt()
            val expertiseA = input.nextInt()
            val expertiseB = input.nextInt()
            val expertiseC = input.nextInt()
            val expertiseD = input.nextInt()
            val expertiseE = input.nextInt()
            Bot(
                Module.valueOf(module),
                score,
                listOf(storageA, storageB, storageC, storageD, storageE)
            )
        }

        val availableA = input.nextInt()
        val availableB = input.nextInt()
        val availableC = input.nextInt()
        val availableD = input.nextInt()
        val availableE = input.nextInt()
        val sampleCount = input.nextInt()

        val samples = List(sampleCount) {
            val sampleId = input.nextInt()
            val carriedBy = input.nextInt()
            val rank = input.nextInt()
            val expertiseGain = input.next()
            val health = input.nextInt()
            val costA = input.nextInt()
            val costB = input.nextInt()
            val costC = input.nextInt()
            val costD = input.nextInt()
            val costE = input.nextInt()
            Sample(
                sampleId,
                carriedBy,
                rank,
                health,
                listOf(costA, costB, costC, costD, costE)
            )
        }


        val me = bots.first()
        val mySample = samples.firstOrNull { it.owner == 0 }
        val availableSamples = samples.filter { it.owner == -1 }.sortedByDescending { it.rentability() }

        log(mySample?.toString() ?: "")
        val action = when {
            mySample == null && me.module != Module.SAMPLES -> "GOTO ${Module.SAMPLES}"
            mySample == null -> "CONNECT 2"
            !mySample.isDiagnosed() && me.module != Module.DIAGNOSIS -> "GOTO ${Module.DIAGNOSIS}"
            !mySample.isDiagnosed() -> "CONNECT ${mySample.id}"
            me.hasEnough(mySample.costs) && me.module != Module.LABORATORY -> "GOTO ${Module.LABORATORY}"
            me.hasEnough(mySample.costs) -> "CONNECT ${mySample.id}"
            me.module != Module.MOLECULES -> "GOTO ${Module.MOLECULES}"
            else -> "CONNECT ${me.storage.zip(mySample.costs).indexOfFirst { (a, b) -> a < b }.toMoleculeType()}"
        }

        println(action)
    }
}