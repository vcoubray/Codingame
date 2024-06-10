package fr.vco.codingame.puzzles.voxcodei.episode2

const val BOMB_RANGE = 3
const val EMPTY = -1
const val WALL = -2

data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
    operator fun minus(other: Position) = Position(x - other.x, y - other.y)
    operator fun times(times: Int) = Position(x * times, y * times)
    operator fun unaryMinus() = Position(-x, -y)
}

data class Node(val pos: Position, val direction: Position = Position(0, 0)) {
    fun reverse() = Node(pos, -direction)
    fun next() = Node(pos + direction, direction)

}

val DIRECTIONS = listOf(
    Position(0, 0),
    Position(0, 1),
    Position(0, -1),
    Position(1, 0),
    Position(-1, 0)
)

class SimulatorBuilder(val width: Int, val height: Int) {
    var possibleDirection: Map<Position, Set<Node>> = emptyMap()
    fun updateGrid(grid: List<String>) {
        val walls = mutableListOf<Position>()
        val nodePositions = mutableListOf<Position>()

        repeat(height) { y ->
            repeat(width) { x ->
                when (grid[y][x]) {
                    '@' -> nodePositions.add(Position(x, y))
                    '#' -> walls.add(Position(x, y))
                }
            }
        }

        if (possibleDirection.isEmpty()) {
            possibleDirection = nodePositions.associateWith { node -> DIRECTIONS.map { Node(node, it) }.toSet() }
        } else {
            possibleDirection = possibleDirection.map { (node, dirs) ->
                node to dirs.mapNotNull { nextNode(it, grid) }.filter { grid[it.pos.y][it.pos.x] == '@' }.toSet()
            }.toMap()
        }
    }

    private fun nextNode(node: Node, grid: List<String>): Node? {
        var next = node.next()
        if (next.pos.x in 0 until width &&
            next.pos.y in 0 until height &&
            grid[next.pos.y][next.pos.x] != '#'
        ) {
            return next
        }
        next = node.reverse().next()
        if (next.pos.x in 0 until width &&
            next.pos.y in 0 until height &&
            grid[next.pos.y][next.pos.x] != '#'
        ) {
            return next
        }
        return null
    }

    fun isReady(): Boolean {
        return possibleDirection.isNotEmpty() && possibleDirection.values.all { it.size == 1 }
    }

    private fun getNodes(): List<Node> {
        return possibleDirection.values.map { it.first() }
    }

    fun buildSimulator(initialGrid: List<String>, turns: Int): GameSimulator {

        var currentNodes = getNodes()
        val grids = List(turns) {
            val currentGrid = buildGrid(currentNodes.map { it.pos }, initialGrid)
            currentNodes = currentNodes.map { nextNode(it, initialGrid)!! }
            currentGrid
        }
        return GameSimulator(grids.reversed(), buildRange(initialGrid))
    }

    private fun buildGrid(nodes: List<Position>, grid: List<String>): List<List<Int>> {
        val currentGrid = grid.map { line ->
            line.map { if (it == '#') WALL else EMPTY }.toMutableList()
        }
        nodes.forEachIndexed { i, it -> currentGrid[it.y][it.x] = i }
        return currentGrid
    }

    private fun buildRange(grid: List<String>): List<List<Position>> {
        return (0 until height).flatMap { y ->
            (0 until width).map { x ->
                if (grid[y][x] == '#') {
                    emptyList()
                } else {
                    val start = Position(x, y)
                    listOf(start) +
                        DIRECTIONS.drop(1).flatMap { dir ->
                            (1..3).map { dir * it + start }
                                .takeWhile { it.x in 0 until width && it.y in 0 until height && grid[it.y][it.x] != '#' }
                        }
                }
            }

        }
    }
}

class GameSimulator(
    val grids: List<List<List<Int>>>,
    val ranges: List<List<Position>>,
)

fun main() {
    val (width, height) = readln().split(" ").map { it.toInt() }

    val gridBuilder = SimulatorBuilder(width, height)
    // game loop
    while (true) {
        val (rounds, bombs) = readln().split(" ").map { it.toInt() }
        val grid = List(height) { readln() }

        gridBuilder.updateGrid(grid)
        if (gridBuilder.isReady()) {
            System.err.println("IS READY")
            val simulator = gridBuilder.buildSimulator(grid, rounds)
            //simulator.grids.first().forEach(System.err::println)
            simulator.ranges.forEach(System.err::println)
        } else {
            gridBuilder.possibleDirection.forEach { (node, dirs) ->
                System.err.println(node)
                dirs.forEach { System.err.println("   $it") }
            }
        }

        println("WAIT")
    }
}