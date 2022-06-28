package fr.vco.codingame.puzzles.the.bridge

import java.util.*
import kotlin.math.min
import kotlin.system.measureTimeMillis

const val LANE_COUNT = 4
const val HOLE = '0'

enum class Action {
    SLOW, JUMP, DOWN, UP, SPEED
}

class State(
    var x: Int,
    var speed: Int,
    val bikes: IntArray,
    var bikesCount: Int,
    val actions: MutableList<Action> = mutableListOf()
) {
    //    var action : Action? = null
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
//        this.action = action
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
        else -> true
    }
}

class Game(
    val bikesCount: Int,
    private val minBikes: Int,
    private val lanes: List<String>
) {

    private val endRoad = lanes.first().length

    constructor(input: Scanner) : this(
        input.nextInt(),
        input.nextInt(),
        List<String>(LANE_COUNT) { input.next() }
    )

    private fun isValidState(state: State) = state.bikesCount >= minBikes
    private fun isVictoryState(state: State) = state.x > endRoad

    private fun play(state: State, action: Action): State {

        val stateFinal = state.copy()
        stateFinal.play(action)
        val start = state.x
        val end = min(stateFinal.x, endRoad - 1)
        for (i in stateFinal.bikesCount - 1 downTo 0) {
            val roadToCheck = when {
                action == Action.JUMP -> lanes[stateFinal.bikes[i]][end].toString()
                state.bikes[i] != stateFinal.bikes[i] -> lanes[state.bikes[i]].substring(start, end) +
                    lanes[stateFinal.bikes[i]].substring(start, end + 1)
                else -> lanes[stateFinal.bikes[i]].substring(start, end + 1)
            }
            if (roadToCheck.contains(HOLE)) {
                stateFinal.bikes[i] = stateFinal.bikes[stateFinal.bikesCount - 1]
                stateFinal.bikesCount--
            }
        }
        return stateFinal
    }

    fun dfs(state: State): List<Action> {
        val toVisit = ArrayDeque<State>().apply { add(state) }
        val actions = mutableListOf<Action>()
        while (toVisit.isNotEmpty()) {
            val current = toVisit.removeFirst()
            if (isVictoryState(current)) return current.actions
//            if (isVictoryState(current)) return actions

            val children = current.getValidActions().map { play(current, it) }
//            if(children.isNotEmpty()) current.action?.let(actions::add)
//            else actions.removeLast()
            children.filter(::isValidState).forEach(toVisit::addFirst)
        }
        return actions
    }
}

fun main() {
    val input = Scanner(System.`in`)
    val game = Game(input)

    while (true) {
        measureTimeMillis {
            val state = readState(input, game)
            val actions = game.dfs(state)

            println(actions.first())
        }.let { System.err.println("Solution found in ${it}ms") }
    }
}

data class Bike(val x: Int, val y: Int, val isAlive: Boolean) {
    constructor(input: Scanner) : this(input.nextInt(), input.nextInt(), input.nextInt() == 1)
}

fun readState(input: Scanner, game: Game): State {
    val speed = input.nextInt()
    val bikes = List(game.bikesCount) { Bike(input) }.filter { it.isAlive }
    return State(bikes.first().x, speed, bikes.map { it.y }.toIntArray(), bikes.size)
}
