package fr.vco.codingame.contest.code4life

import java.util.*
import kotlin.math.max

fun log(message: Any?) = System.err.println(message.toString())

const val MAX_MOLECULES = 10
const val MAX_SAMPLES = 3
const val MAX_MOLECULE = 5
val moleculeType = listOf("A", "B", "C", "D", "E")

enum class Module { DIAGNOSIS, MOLECULES, SAMPLES, LABORATORY, START_POS }

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
    fun contains(type: String, quantity: Int) = this[type] >= quantity
    fun filterPositive() = molecules.map { (m, q) -> m to max(0, q) }.toMolecules()
}

val emptyMolecules = moleculeType.map { it to 0 }.toMolecules()

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

    fun canProduce(sample: Sample): Boolean {
        return storage.contains(realCosts(sample))
    }

    fun need(sample: Sample): Molecules {
        return realCosts(sample) - storage
    }

    fun realCosts(sample: Sample): Molecules {
        return (sample.costs - expertise).filterPositive()
    }

    fun neededMolecule(samples: List<Sample>): Molecules {
        var expertiseSimulated = expertise
        var totalCost = emptyMolecules
        samples.forEach { s ->
            totalCost += (s.costs - expertiseSimulated).filterPositive()
            expertiseSimulated = expertiseSimulated.add(s.gain, 1)
        }
        return (totalCost - storage).filterPositive()
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

    override fun equals(other: Any?): Boolean {
        return if (other is Sample) this.id == other.id
        else false
    }

    override fun hashCode(): Int {
        return id
    }
}


class SampleGroup(private val samples: List<Sample>) {

    fun costFor(bot: Bot): Molecules {
        var expertise = bot.expertise
        var totalCost = emptyMolecules
        samples.forEach { s ->
            totalCost += (s.costs - expertise).filterPositive()
            expertise = expertise.add(s.gain, 1)
        }
        return (totalCost - bot.storage).filterPositive()
    }

    fun gain(): Molecules {
        return samples.fold(emptyMolecules) { acc, s -> acc.add(s.gain, 1) }
    }

    fun health() = samples.sumBy { it.health }
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

class MovingState(game: Game) : State(game) {
    override fun action() = MoveAction()
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

class DiagnosisState(game: Game) : State(game) {
    override fun action(): Action {
        /* TODO :
            - Keep Samples if several of them can be done in one time
            - check cloud for interesting samples
         */
        return if (mySamples.isEmpty()) GotoAction(Module.SAMPLES)
        else mySamples.firstOrNull { !it.isDiagnosed() }?.let(::SampleAction)
            ?: mySamples.firstOrNull { me.need(it).molecules.any { (_, m) -> m > 5 } }?.let(::SampleAction)
            ?: GotoAction(Module.MOLECULES)
    }
}

class MoleculesState(game: Game) : State(game) {
    override fun action(): Action {
        /*TODO :
           - Take molecules needed by opponent too in priority
           - Dont wait for molecule if opponent hold them
        */
//
//        val totalCost = mySamples.sortedBy { it.health.toFloat() / me.realCosts(it).sum() }
//            .fold(emptyMolecules) { totalCost, s ->
//                val newTotal = totalCost + me.realCosts(s)
//                val needed = (newTotal - me.storage).filterPositive()
//                if ((availableMolecules + me.storage).contains(newTotal) && (needed + me.storage).isValid()) newTotal
//                else totalCost
//            }
//
//        val needed = totalCost - me.storage
//        mySamples.forEach { log("${it.costs} - ${it.gain}") }
//        log("Storage : ${me.storage}")
//        log("cost: $totalCost")
//        log("need: $needed")
        log("availables : $availableMolecules")
        val needed = getBestSampleGroup(me,mySamples)?.costFor(me)
        log("new Need : $needed")
        val action = needed?.molecules?.keys?.firstOrNull { needed[it] > 0 }?.let { MoleculeAction(it) }


        return when {
            action != null -> action
            //needed.sum() == 0  -> GotoAction(Module.LABORATORY)
            else -> GotoAction(Module.LABORATORY)
        }

    }

    fun rentability(sample: Sample) = sample.health.toFloat() / me.realCosts(sample).sum()


    fun getBestSampleGroup(bot: Bot, samples: List<Sample>): SampleGroup? {
        return samples.getAllCombinations()
            .filter {
                val cost = it.costFor(bot)
                availableMolecules.contains(cost) && (bot.storage + cost).isValid()
            }
            .apply{
                log("--")
                forEach{ log("${it.costFor(bot)} - ${it.health()}") }

            }
            .maxWith(  compareBy({it.health()},{ -it.costFor(bot).sum()  }))

    }


}


fun List<Sample>.getAllCombinations(combination: List<Sample> = emptyList(), depth: Int = 3): List<SampleGroup> {
    val combinations = mutableListOf<SampleGroup>()
    if (combination.size >= depth) return combinations
    this.forEach {
        if (!combination.contains(it)) {
            val newCombi = combination + it
            combinations.add(SampleGroup(newCombi))
            combinations.addAll(getAllCombinations(newCombi, depth))
        }

    }
    return combinations
}


class LaboratoryState(game: Game) : State(game) {
    override fun action(): Action {
        return when {
            /*
             TODO : Goto the diagnosis module directly if there is interesting samples
             */
            mySamples.any { me.canProduce(it) } -> mySamples.firstOrNull { me.canProduce(it) }!!.let(::SampleAction)
            mySamples.size <= 1 -> GotoAction(Module.SAMPLES)
            else -> GotoAction(Module.MOLECULES)
//            mySamples.isEmpty() -> GotoAction(Module.SAMPLES)
//            mySamples.none { me.canProduce(it) } -> GotoAction(Module.MOLECULES)
//            else -> mySamples.firstOrNull { me.canProduce(it) }?.let(::SampleAction) ?: WaitAction()
        }
    }
}

class Game(val projects: List<Molecules>, input: Scanner) {

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
        if (me.isMoving()) MovingState(this)
        else when (me.module) {
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
        println(Game(projects, input).currentState().action())
    }
}