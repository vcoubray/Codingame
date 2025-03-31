package fr.vco.codingame.puzzles.voxcodei.episode2

import kotlin.math.pow
import kotlin.math.roundToLong

val NODE_INDEX_MASKS = List(64) {
    2.0.pow(it).roundToLong()
}

fun initNodeArray(size: Int) = 2.0.pow(size).roundToLong() - 1
fun Long.remove(index: Int) = if(this[index]) this - NODE_INDEX_MASKS[index] else this
operator fun Long.get(i: Int): Boolean = (this.shr(i) and 1L) == 1L

const val BOMB_RANGE = 3


data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
    operator fun minus(other: Position) = Position(x - other.x, y - other.y)
    operator fun times(times: Int) = Position(x * times, y * times)
    operator fun unaryMinus() = Position(-x, -y)
    override fun toString() = "$x $y"
}

class GameSimulator(
    val width: Int,
    val height: Int,
    val bombs: Int,
    val turns: Int,
    val nodeCount: Int,
    val ranges: List<List<Int>>,
    val rounds: List<Round>
) {

    fun getFirstState() = State(
        turns,
        bombs,
        initNodeArray(nodeCount),
        initNodeArray(nodeCount)
    )

    fun indexToPosition(index: Int) = Position(index % width, index / width)
}

class Round(
    val nodes: List<Int>, // Position of all the nodes for a rounds
    val nodesInRange: Map<Int, List<Int>> // Associate a cell index with the nodes in range
)



class State(
    val turn: Int,
    val bombCount: Int,
    val nodes: Long,
    val notTargetedNodes: Long,
    val bomb1: Int = -1,
    val bomb2: Int = -1,
    val bomb3: Int = -1,
    val actions: List<Int> = emptyList()
) {

    companion object {
        val FREE_CELLS = MutableList(20*20) { 0 }
        var FREE_CELLS_COUNTER = 0
    }


    val hash = computeHashCode()

    fun isFinal(): Boolean {
        return turn <= 3 ||
                notTargetedNodes == 0L ||
                bombCount == 0
    }

    fun isWin(): Boolean = notTargetedNodes == 0L



    fun getPossibleActions(simulator: GameSimulator): List<Int> {
        // System.err.println("turn: $turn")
        // System.err.println("bombCount: $bombCount")
        // System.err.println("bomb1: $bomb1")
        // System.err.println("bomb2: $bomb2")
        // System.err.println("bomb3: $bomb3")
        // System.err.println("nodes: ${nodes.toString(2)}")
        // System.err.println("notTargetedNodes: ${notTargetedNodes.toString(2)}")

        if (bombCount == 0) return listOf(-1)
        FREE_CELLS_COUNTER++
        simulator.rounds[turn].nodes.forEach { FREE_CELLS[it] = FREE_CELLS_COUNTER }
        simulator.ranges.getOrNull(bomb1)?.forEach { FREE_CELLS[it] = FREE_CELLS_COUNTER }
        simulator.ranges.getOrNull(bomb2)?.forEach { FREE_CELLS[it] = FREE_CELLS_COUNTER }
        simulator.ranges.getOrNull(bomb3)?.forEach { FREE_CELLS[it] = FREE_CELLS_COUNTER }

        // System.err.println(freeCells.indices.filter{!freeCells[it]})
        val targets = simulator.rounds[turn - 3].nodesInRange
            .filter { (cell, _) -> FREE_CELLS[cell] < FREE_CELLS_COUNTER }
            .mapNotNull { (cell, nodes) ->
                val targetCount = nodes.count { notTargetedNodes[it] }
                if (targetCount > 0) {
                    cell to targetCount
                } else {
                    null
                }
            }

        // simulator.rounds[turn - 3].nodesInRange
        //     .mapIndexedNotNull{i, nodes -> if(nodes.isNotEmpty()) i to nodes else null}
        //     .filterIndexed { i, _ -> freeCells[i] }
        //     .forEach{(i, nodes) ->
        //     System.err.println("${simulator.indexToPosition(i)} -> ${nodes}")
        //     }

        // System.err.println("targets: $targets")
        return targets.sortedByDescending { (_, targetCount) -> targetCount }.map { it.first } + -1
    }

    fun play(action: Int, simulator: GameSimulator): State {

        return State(
            turn - 1,
            if (action != -1) bombCount - 1 else bombCount,
            removeNode(nodes, simulator.rounds[turn].nodesInRange[bomb1]),
            removeNode(notTargetedNodes, simulator.rounds[turn - 3].nodesInRange[action]),
            bomb2,
            bomb3,
            action,
            actions + action
        )
    }

    private fun removeNode(nodes: Long, nodeToRemove: List<Int>?): Long {
        var newNodes = nodes
        nodeToRemove?.forEach {
            newNodes = newNodes.remove(it)
        }
        return newNodes
    }


    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is State) return false
        return bombCount == other.bombCount &&
                notTargetedNodes == other.notTargetedNodes
    }

    private fun computeHashCode(): Int {
        var hash = 7
        hash = 31 * hash + ((bomb1 + 1) + (bomb2 + 1) * 401 + (bomb3 + 1) * 401 * 401)
        hash = 31 * hash + turn
        hash = 31 * hash + notTargetedNodes.hashCode()
        return hash
    }

    override fun hashCode(): Int {
        return hash
    }
}


fun main() {

    val (width, height) = readln().split(" ").map { it.toInt() }

    val gridBuilder = SimulatorBuilder(width, height)
    val simulator = gridBuilder.buildSimulator()
    System.err.println("Simulator is ready")

    println("WAIT")


//    var state = simulator.getFirstState().play(-1, simulator)
//    repeat(9) {
//        val actions = state.getPossibleActions(simulator)
//        System.err.println("actions : ${actions}")
//        System.err.println(state.nodes.toString(2))
//        System.err.println(state.notTargetedNodes.toString(2))
//        state = state.play(actions.first(), simulator)
//
//    }
//
//    System.err.println(state.nodes.toString(2))
//    System.err.println(state.notTargetedNodes.toString(2))

//    val actions = List(9 ){
//        val action = state.getPossibleActions(simulator).first()
//        state = state.play(action, simulator)
//        action
//    }



//    System.err.println(simulator.rounds.joinToString(", ") { it.nodes.joinToString() })
    val actions = findValidActions(simulator)
    System.err.println(actions)

    actions.drop(1).map {
        if (it == -1) "WAIT"
        else simulator.indexToPosition(it).toString()
    }.forEach { action ->

        readln().split(" ").map { it.toInt() }
        List(height) { readln() }.joinToString("")
        println(action)
    }

    while (true) {
        val (turns, _) = readln().split(" ").map { it.toInt() }
        val grid = List(height) { readln() }.joinToString("")
        val nodes = grid.indices.filter { grid[it] == NODE }.sorted()
        System.err.println("INPUT :   $turns -> $nodes")
        System.err.println("COMPUTED: $turns -> ${simulator.rounds[turns].nodes.sorted()}")
        System.err.println("Ranges :  $turns -> ${simulator.rounds[turns].nodesInRange.mapNotNull{(i, nodes)-> i.takeIf{nodes.isNotEmpty()}}.sorted()}")
        println("WAIT")
    }
}

fun findValidActions(simulator: GameSimulator): List<Int> {
    val state = simulator.getFirstState().play(-1, simulator)

    val toVisit = ArrayDeque<State>().apply { add(state) }
    val visited = mutableMapOf(state.hash to state.bombCount)

    var stateCount = 0
    while (toVisit.isNotEmpty()) {
        val current = toVisit.removeLast()
        stateCount++
        if (current.isWin()) {
            System.err.println("states : $stateCount")
            return current.actions
        }
        if (current.isFinal()) {
            continue
        }
        for (action in current.getPossibleActions(simulator).reversed()) {
            val child = current.play(action, simulator)
            val  visitedBombs = visited[child.hash] ?: -1

           if (child.bombCount > visitedBombs) {
                toVisit.addLast(child)
                visited[child.hash] = child.bombCount
            }
        }

        if (stateCount % 100 == 0) {
            System.err.println("states : $stateCount")
        }

    }
    System.err.println(stateCount)
    return emptyList()
}