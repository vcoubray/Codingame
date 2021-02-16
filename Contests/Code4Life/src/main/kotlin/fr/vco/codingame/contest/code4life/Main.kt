package fr.vco.codingame.contest.code4life

import java.util.*
import kotlin.math.max
import kotlin.math.min

fun log(message: String) = System.err.println(message)

val moleculeType = listOf("A", "B", "C", "D", "E")
const val MAX_MOLECULES = 10
const val MAX_SAMPLES = 3

enum class Module { DIAGNOSIS, MOLECULES, SAMPLES, LABORATORY, START_POS }

fun Int.toMoleculeType() = moleculeType[this]

class Bot(
    val module: Module,
    val eta: Int,
    val score: Int,
    val storage: List<Int>,
    val expertise : List<Int>
)
 {
    fun hasEnough(cost: List<Int>): Boolean {
        return storage.zip(expertise).map{(a,b) -> a+b}
            .zip(cost).all { (a, b) -> a >= b }
    }
    fun need(cost: List<Int>):List<Int> {
        return storage.zip(expertise).map{(a,b) -> a+b}
            .zip(cost).map { (a, b) -> max(b - a, 0) }
    }

     fun isMoving() = eta > 0


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
                eta,
                score,
                listOf(storageA, storageB, storageC, storageD, storageE),
                listOf(expertiseA, expertiseB, expertiseC, expertiseD, expertiseE)
            )
        }

        val availableA = input.nextInt()
        val availableB = input.nextInt()
        val availableC = input.nextInt()
        val availableD = input.nextInt()
        val availableE = input.nextInt()
        val availablesMolecules = listOf(availableA,availableB,availableC,availableD,availableE)

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
        val mySamples = samples.filter{ it.owner == 0}
        val mySample = samples.firstOrNull { it.owner == 0 }
        val availableSamples = samples.filter { it.owner == -1 }.sortedByDescending { it.rentability() }

        log(mySample?.toString() ?: "")


        val action = when {
            me.isMoving() -> "WAIT"
            me.module == Module.START_POS -> "GOTO ${Module.SAMPLES}"
            me.module == Module.SAMPLES -> {
                val rank = when {
                    me.expertise.sum() > 10 -> 3
                    me.expertise.sum() > 5 -> 2
                    else -> 1
                }
                if(mySamples.size < MAX_SAMPLES ) "CONNECT $rank"
                else "GOTO ${Module.DIAGNOSIS}"
            }
            me.module == Module.DIAGNOSIS -> {
                if(mySamples.isEmpty()) "GOTO ${Module.SAMPLES}"
                else mySamples.firstOrNull{!it.isDiagnosed()}?.let{"CONNECT ${it.id}"}
                    ?:mySamples.firstOrNull{me.need(it.costs).any{m -> m > 5}}?.let{"CONNECT ${it.id}"}
                    ?:"GOTO ${Module.MOLECULES}"
            }
            me.module == Module.MOLECULES -> {
                when {
                    mySamples.any{me.hasEnough(it.costs)} -> "GOTO ${Module.LABORATORY}"
                    else -> mySamples.firstOrNull()
                        ?.let{me.need(it.costs)}
                        ?.apply{log(this.toString())}
                        ?.zip(availablesMolecules)?.mapIndexed { i, (a,b) -> i.toMoleculeType() to (a > 0 && b > 0) }
                        ?.apply{log(this.toString())}
                        ?.firstOrNull{it.second}?.let{"CONNECT ${it.first}"}
                        ?:"WAIT"
                }
            }
            me.module == Module.LABORATORY -> {
                when {
                    mySamples.isEmpty() -> "GOTO ${Module.SAMPLES}"
                    mySamples.none{me.hasEnough(it.costs)} -> "GOTO ${Module.MOLECULES}"
                    else ->  mySamples.firstOrNull{me.hasEnough(it.costs)}?.let{"CONNECT ${it.id}"}?:"WAIT"
                }
            }
            else -> "WAIT"
        }

        println(action)
    }
}