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


interface Action


class WaitAction : Action {
    override fun toString() = "WAIT"
}

class MoveAction : Action {
    override fun toString() = "Moving"
}

class GotoAction(val module: Module) : Action {
    override fun toString() = "GOTO $module"
}

class SearchAction(val rank: Int) : Action {
    override fun toString() = "CONNECT $rank"
}

class SampleAction(val sample: Sample) : Action {
    override fun toString() = "CONNECT ${sample.id}"
}

class MoleculeAction(val molecule: String) : Action {
    override fun toString() = "CONNECT $molecule"
}


abstract class State(game: Game) {

    val availableMolecules: Molecules = game.availableMolecules
    val mySamples: List<Sample> = game.mySamples
    val oppSamples: List<Sample> = game.oppSamples
    val cloudSamples: List<Sample> = game.cloudSamples
    val me: Bot = game.me
    val opp: Bot = game.opp

    abstract fun action(): Action
}

class StartState(game: Game) : State(game) {
    override fun action() = GotoAction(Module.SAMPLES)
}

class SamplesState(game: Game) : State(game) {
    override fun action(): Action {
        /* TODO : Optimize rank selection */
        val rank = when {
            me.expertise.sum() > 10 -> 3
            me.expertise.sum() > 5 -> 2
            else -> 1
        }
        return if (mySamples.size < MAX_SAMPLES) SearchAction(rank)
        else GotoAction(Module.DIAGNOSIS)
    }
}

class MoleculesState(game: Game) : State(game) {
    override fun action(): Action {
        return when {
            /*TODO :
                - Prepare most rentable Sample in priority
                - Take molecules needed by opponent too in priority
                - Prepare several Sample in one time, if possible
                - Dont wait for molecule if opponent hold them
             */
            mySamples.any { me.hasEnough(it.costs) } -> GotoAction(Module.LABORATORY)
            else -> mySamples.firstOrNull()
                ?.let { me.need(it.costs) }
                ?.apply { log(this.toString()) }
                ?.union(availableMolecules)
                ?.apply { log(this.toString()) }
                ?.firstOrNull()?.let(::MoleculeAction)
                ?: WaitAction()
        }
    }
}

class DiagnosisState(game: Game) : State(game) {
    override fun action(): Action {
        /* TODO :
            - Keep Samples if several of them can be done in one time
            - check cloud for interesting samples
         */
        return if (mySamples.isEmpty()) GotoAction(Module.SAMPLES)
        else mySamples.firstOrNull { !it.isDiagnosed() }?.let(::SampleAction)
            ?: mySamples.firstOrNull { me.need(it.costs).molecules.any { (_, m) -> m > 5 } }?.let(::SampleAction)
            ?: GotoAction(Module.MOLECULES)
    }
}

class LaboratoryState(game: Game) : State(game) {
    override fun action(): Action {
        return when {
            /*
             TODO : Goto the diagnosis module directly if there is interesting samples
             */
            mySamples.isEmpty() -> GotoAction(Module.SAMPLES)
            mySamples.none { me.hasEnough(it.costs) } -> GotoAction(Module.MOLECULES)
            else -> mySamples.firstOrNull { me.hasEnough(it.costs) }?.let (::SampleAction) ?: WaitAction()
        }
    }
}

class Game( val projects : List<Molecules>, input: Scanner) {

    val availableMolecules: Molecules
    val mySamples: List<Sample>
    val oppSamples: List<Sample>
    val cloudSamples: List<Sample>
    val me: Bot
    val opp: Bot

    init {
        val bots = List(2) { Bot(input) }
        me = bots.first()
        opp = bots.last()
        availableMolecules = Molecules(input)

        val sampleCount = input.nextInt()
        val samples = List(sampleCount) { Sample(input) }
        mySamples = samples.filter { it.owner == 0 }
        oppSamples = samples.filter { it.owner == 1 }
        cloudSamples = samples.filter { it.owner == -1 }
    }

    fun currentState(): State =
        when (me.module) {
            Module.SAMPLES -> SamplesState(this)
            Module.DIAGNOSIS -> DiagnosisState(this)
            Module.LABORATORY -> LaboratoryState(this)
            Module.MOLECULES -> MoleculesState(this)
            Module.START_POS -> StartState(this)
        }
}


fun main() {
    val input = Scanner(System.`in`)
    val projectCount = input.nextInt()
    val projects = List(projectCount) { Molecules(input) }

    // game loop
    while (true) {
        println(Game(projects,input).currentState().action())
    }
}