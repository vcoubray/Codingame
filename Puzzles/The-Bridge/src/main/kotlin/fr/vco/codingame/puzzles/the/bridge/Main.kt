package fr.vco.codingame.puzzles.the.bridge

import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

const val LANE_COUNT = 4
const val HOLE = '0'

enum class Action {
    SLOW, DOWN, UP, JUMP, SPEED
}

class State(
    var x: Int,
    var speed: Int,
    val bikes: IntArray,
    var bikesCount: Int,
    val actions: MutableList<Action> = mutableListOf()
) {
    private var maxLane = bikes.maxOrNull()!!
    private var minLane = bikes.minOrNull()!!
    fun copy() = State(x, speed, bikes.copyOf(bikesCount), bikesCount, actions.toMutableList())

    fun play(action: Action) = apply {
        when (action) {
            Action.SPEED -> speed += 1
            Action.UP -> changeLane(-1)
            Action.DOWN -> changeLane(1)
            Action.SLOW -> speed -= 1
            Action.JUMP -> {} // Do nothing
        }
        x += speed
        actions.add(action)
    }

    private fun changeLane(shift: Int) {
        for (i in 0 until bikesCount) {
            bikes[i] += shift
        }
        minLane += shift
        maxLane += shift
    }

    fun getValidActions() = Action.values().filter(::isValidAction)

    private fun isValidAction(action: Action) = when {
        action == Action.DOWN && maxLane == LANE_COUNT - 1 -> false
        action == Action.UP && minLane == 0 -> false
        action == Action.SLOW && speed <= 1 -> false
        action == Action.JUMP && speed == 0 -> false
        else -> true
    }
}

fun String.toBitSet(): BitSet {
    return this.foldIndexed(BitSet(this.length)) { i, bitSet, a -> bitSet.apply { if (a == HOLE) set(i) } }
}

class Game(
    val initialBikeCount: Int,
    private val minBikes: Int,
    private val lanes: List<BitSet>
) {

    private val endRoad = lanes.first().size()

    constructor(input: Scanner) : this(
        input.nextInt(),
        input.nextInt(),
        List(LANE_COUNT) { input.next().toBitSet() }
    )

    private fun isValidState(state: State) = state.bikesCount >= minBikes
    private fun isVictoryState(state: State) = state.x > endRoad

    private fun play(state: State, action: Action): State {

        val stateFinal = state.copy()
        stateFinal.play(action)
        val start = state.x
        val end = min(stateFinal.x, endRoad - 1)

        for (i in stateFinal.bikesCount - 1 downTo 0) {

            val initialLane = state.bikes[i]
            val finalLane = stateFinal.bikes[i]
            val roadToCheck = when {
                action == Action.JUMP -> lanes[finalLane].get(end, end + 1)
                initialLane != finalLane -> lanes[initialLane].get(start, max(start, end))
                    .apply { or(lanes[finalLane].get(start, end + 1)) }
                else -> lanes[finalLane].get(start, end + 1)
            }
            if (roadToCheck.cardinality() != 0) {
                stateFinal.bikes[i] = stateFinal.bikes[stateFinal.bikesCount - 1]
                stateFinal.bikesCount--
            }
        }
        return stateFinal
    }

    fun dfs(state: State, timeout: Int): List<Action> {
        val start = System.currentTimeMillis()
        val toVisit = ArrayDeque<State>().apply { add(state) }

        var bestState: State? = null
        while (toVisit.isNotEmpty() && System.currentTimeMillis() - start < timeout) {
            val current = toVisit.removeFirst()
            if (isVictoryState(current)) {
                if (current.bikesCount > (bestState?.bikesCount ?: 0)) {
                    bestState = current
                    if (current.bikesCount == initialBikeCount) break
                }
                continue
            }

            val children = current.getValidActions().map { play(current, it) }
            children.filter(::isValidState).forEach(toVisit::addFirst)
        }
        return bestState?.actions ?: emptyList()
    }
}

fun main() {
    val input = Scanner(System.`in`)
    val game = Game(input)

    while (true) {
        measureTimeMillis {
            val state = readState(input, game)
            val actions = game.dfs(state, 140)

            println(actions.first())
        }.let { System.err.println("Solution found in ${it}ms") }
    }
}

data class Bike(val x: Int, val y: Int, val isAlive: Boolean) {
    constructor(input: Scanner) : this(input.nextInt(), input.nextInt(), input.nextInt() == 1)
}

fun readState(input: Scanner, game: Game): State {
    val speed = input.nextInt()
    val bikes = List(game.initialBikeCount) { Bike(input) }.filter { it.isAlive }
    return State(bikes.first().x, speed, bikes.map { it.y }.toIntArray(), bikes.size)
}
