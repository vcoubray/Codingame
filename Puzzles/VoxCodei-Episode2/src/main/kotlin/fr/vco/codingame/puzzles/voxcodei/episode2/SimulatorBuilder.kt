package fr.vco.codingame.puzzles.voxcodei.episode2

import kotlin.system.measureTimeMillis

const val NODE = '@'
const val WALL = '#'

val DIRECTIONS = listOf(
    Position(0, 0),
    Position(0, 1),
    Position(0, -1),
    Position(1, 0),
    Position(-1, 0)
)


data class Step(val pos: Position, val direction: Position = Position(0, 0)) {
    fun reverse() = Step(pos, -direction)
    fun next() = Step(pos + direction, direction)
}

data class Node(var trajectories: List<List<Step>>)

class SimulatorBuilder(val width: Int, val height: Int) {

    val grid = MutableList(height) { "" }
    lateinit var ranges: List<List<Int>>
    lateinit var possibleMoves: List<List<Step>>
    lateinit var possibleNodeDirections: List<Node>

    private fun init() {
        val (turns, _) = readln().split(" ").map { it.toInt() }
        repeat(height) {
            grid[it] = readln()
        }
        measureTimeMillis {
            ranges = computeRanges(grid)
            possibleMoves = computePossibleMoves(grid)
            possibleNodeDirections = initNodes(grid, turns)
        }.let { System.err.println("Init Simulator Builder in ${it}ms") }
    }

    private fun computeRanges(grid: List<String>): List<List<Int>> {
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

    private fun computePossibleMoves(grid: List<String>): List<List<Step>> {
        val possibleMoves = List(height * width) { i ->
            DIRECTIONS.map { dir -> nextStep(Step(i.toPosition(), dir), grid) }
        }
        return possibleMoves
    }

    private fun initNodes(grid: List<String>, turns: Int): List<Node> {
        val nodes = mutableListOf<Node>()
        repeat(height) { y ->
            repeat(width) { x ->
                if (grid[y][x] == NODE) {
                    val nodePos = Position(x, y)
                    val trajectories = DIRECTIONS.map { computeTrajectory(Step(nodePos, it), turns, grid) }
                    val uniqueTrajectories = mutableListOf<List<Step>>()
                    for (trajectory in trajectories) {
                        if (trajectory.first() !in uniqueTrajectories.map { it.first() }) {
                            uniqueTrajectories.add(trajectory)
                        }
                    }
                    nodes.add(Node(uniqueTrajectories))
                }
            }
        }

        System.err.println(nodes.size)
        return nodes
    }

    private fun computeTrajectory(start: Step, turns: Int, grid: List<String>): List<Step> {
        val trajectory = mutableListOf(start)
        for (turn in 1..turns) {
            val last = trajectory[turn - 1]
            trajectory.add(nextStep(last, grid))
        }
        return trajectory.reversed()
    }

    private fun updatePossibleNodeDirections(grid: List<String>, turn: Int) {
        possibleNodeDirections.forEach { node ->
            node.trajectories = node.trajectories.filter { grid[it[turn].pos] == NODE }
        }
    }

    private fun nextStep(step: Step, grid: List<String>): Step {
        var next = step.next()
        if (next.pos.x in 0 until width &&
            next.pos.y in 0 until height &&
            grid[next.pos] != WALL
        ) {
            return next
        }
        next = step.reverse().next()
        if (next.pos.x in 0 until width &&
            next.pos.y in 0 until height &&
            grid[next.pos] != WALL
        ) {
            return next
        }
        return Step(step.pos)
    }

    private fun isReady(): Boolean {
        return possibleNodeDirections.all { it.trajectories.size == 1 }
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
                    val nodes = possibleNodeDirections.map { node -> node.trajectories.first()[turn].pos.toIndex() }
                    val nodesInRange = List(ranges.size) { mutableListOf<Int>() }
                    nodes.forEachIndexed { nodeId, nodePos ->
                        ranges[nodePos].forEach { pos ->
                            nodesInRange[pos].add(nodeId)
                        }
                    }
                    Round(
                        nodes,
                        nodesInRange.indices.associateWith { nodesInRange[it]  }.filter {(_, range) -> range.isNotEmpty()}
                    )
                }
                System.err.println("Simulator initied in ${System.currentTimeMillis() - start}ms")

                return GameSimulator(width, height, bombs, turns, possibleNodeDirections.size, ranges, rounds)
            }
            println("WAIT")
        }
        throw Exception("unable to build Simulator")
    }

    private fun Position.toIndex() = y * width + x
    private fun Int.toPosition() = Position(this % width, this / width)

    private operator fun List<String>.get(pos: Position) = this.getOrNull(pos.y)?.getOrNull(pos.x) ?: WALL
}