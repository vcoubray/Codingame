package fr.vco.codingame.puzzles.voxcodei.episode2

import kotlin.collections.first
import kotlin.system.measureTimeMillis

const val BOMB_RANGE = 3

const val NODE = '@'
const val WALL = '#'

val DIRECTIONS = listOf(
    Position(0, 0),
    Position(0, 1),
    Position(0, -1),
    Position(1, 0),
    Position(-1, 0)
)

fun List<Int>.toNodeMask() = this.fold(0L) { acc, a -> acc + (1 shl a) }

data class Step(val pos: Position, val direction: Position = Position(0, 0)) {
    fun reverse() = Step(pos, -direction)
    fun next() = Step(pos + direction, direction)
}

class Node(start: Position, turns: Int, grid: List<String> ) {

    var trajectories = mutableListOf<List<Step>>()

    init {
        for (trajectory in DIRECTIONS.map { computeTrajectory(Step(start, it), turns, grid) }) {
            if (trajectory.first() !in trajectories.map { it.first() }) {
                trajectories.add(trajectory)
            }
        }
    }

    fun isReady() = trajectories.size == 1
    fun isMoving() = trajectories.first().first().direction != DIRECTIONS[0]
    fun getPositionAt(turn: Int) = trajectories.first()[turn].pos

    fun updateTrajectories(grid: List<String>, turn: Int) {
        trajectories = trajectories.filter { grid[it[turn].pos] == NODE }.toMutableList()
    }

    private fun computeTrajectory(start: Step, turns: Int, grid: List<String>): List<Step> {
        val trajectory = mutableListOf(start)
        for (turn in 1..turns) {
            val last = trajectory[turn - 1]
            trajectory.add(nextStep(last, grid))
        }
        return trajectory.reversed()
    }

    private fun nextStep(step: Step, grid: List<String>): Step {
        var next = step.next()
        if (grid[next.pos] != WALL) {
            return next
        }
        next = step.reverse().next()
        if (grid[next.pos] != WALL) {
            return next
        }
        return Step(step.pos)
    }

    private operator fun List<String>.get(pos: Position) = this.getOrNull(pos.y)?.getOrNull(pos.x) ?: WALL

}

class SimulatorBuilder(val width: Int, val height: Int) {

    val grid = MutableList(height) { "" }
    lateinit var ranges: List<List<Int>>
    lateinit var nodes: List<Node>

    private fun init() {
        val (turns, _) = readln().split(" ").map { it.toInt() }
        repeat(height) {
            grid[it] = readln()
        }
        measureTimeMillis {
            ranges = initRange(grid)
            nodes = initNodes(grid, turns)
        }.let { System.err.println("Init Simulator Builder in ${it}ms") }
    }

    private fun initRange(grid: List<String>): List<List<Int>> {
        val directions = DIRECTIONS.drop(1)
        return (0 until height).flatMap { y ->
            (0 until width).map { x ->
                if (grid[y][x] == WALL) {
                    emptyList()
                } else {
                    val start = Position(x, y)
                    listOf(start.toIndex()) +
                            directions.flatMap { dir ->
                                (1..BOMB_RANGE).map { dir * it + start }
                                    .takeWhile { it.x in 0 until width && it.y in 0 until height && grid[it.y][it.x] != WALL }
                                    .map { it.toIndex() }
                            }
                }
            }
        }
    }

    private fun initNodes(grid: List<String>, turns: Int): List<Node> {
        val nodes = mutableListOf<Node>()
        repeat(height) { y ->
            repeat(width) { x ->
                if (grid[y][x] == NODE) {
                    val nodePos = Position(x, y)
                    nodes.add(Node(nodePos, turns, grid))
                }
            }
        }
        return nodes
    }

    private fun updatePossibleNodeDirections(grid: List<String>, turn: Int) {
        nodes.forEach { node ->
            if (!node.isReady()) {
                 node.updateTrajectories(grid, turn)
            }
        }
    }

    private fun isReady(): Boolean {
        return nodes.all { it.isReady() }
    }

    fun buildSimulator(): GameSimulator {
        init()
        println("WAIT")

        while (!isReady()) {
            val (turns, bombs) = readln().split(" ").map { it.toInt() }
            measureTimeMillis {
                val grid = List(height) { readln() }
                updatePossibleNodeDirections(grid, turns)
            }.let { System.err.println("updateGrid in ${it}ms") }

            if (isReady()) {
                System.err.println("simulator ready")
                val start = System.currentTimeMillis()

                val rounds = List(turns + 1) { turn ->
                    val roundGrid = MutableList(height * width) { -1 }
                    val nodesInRange = List(ranges.size) { mutableListOf<Int>() }

                    nodes.map { node -> node.getPositionAt(turn).toIndex() }
                        .forEachIndexed { nodeId, nodePos ->
                            roundGrid[nodePos] = nodeId
                            ranges[nodePos].forEach { pos -> nodesInRange[pos].add(nodeId) }
                    }

                    val actions = nodesInRange.mapIndexed { nodeId, nodesInRange -> nodeId to nodesInRange }
                        .filter { (_, range) -> range.isNotEmpty() }
//                        .sortedWith(
//                            compareByDescending<Pair<Int, List<Int>>> { (_, range) -> range.count { nodes[it].isMoving() } }
//                                .thenByDescending { (_, range) -> range.size })
                        .sortedByDescending { (_, range) -> range.size }
                        .map{(i, range)-> i to range.toNodeMask()}

                    Round(actions, roundGrid)
                }
                System.err.println("Simulator initied in ${System.currentTimeMillis() - start}ms")

                return GameSimulator(width, height, bombs, turns, nodes.size, ranges, rounds)
            }
            println("WAIT")
        }
        throw Exception("unable to build Simulator")
    }

    private fun Position.toIndex() = y * width + x
}