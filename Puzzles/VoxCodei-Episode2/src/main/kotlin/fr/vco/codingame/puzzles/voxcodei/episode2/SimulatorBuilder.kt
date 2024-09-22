package fr.vco.codingame.puzzles.voxcodei.episode2


val DIRECTIONS = listOf(
    Position(0, 0),
    Position(0, 1),
    Position(0, -1),
    Position(1, 0),
    Position(-1, 0)
)

data class Node(val pos: Position, val direction: Position = Position(0, 0)) {
    fun reverse() = Node(pos, -direction)
    fun next() = Node(pos + direction, direction)

}

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

    fun buildSimulator(): GameSimulator {

        while(!isReady()) {
            val (rounds, bombs) = readln().split(" ").map { it.toInt() }
            val grid = List(height) { readln() }
            updateGrid(grid)
            
            if(isReady()) {
                var currentNodes = getNodes()
                val grids = List(rounds) {
                    val currentGrid = buildGrid(currentNodes.map { it.pos }, grid)
                    currentNodes = currentNodes.map { nextNode(it, grid)!! }
                    currentGrid
                }
                return GameSimulator(width, height, bombs, rounds, currentNodes.size, grids.reversed(), buildRange(grid))
            }
            println("WAIT")
        }
        
        throw Exception("unable to build Simulator")
    }

    private fun buildGrid(nodes: List<Position>, grid: List<String>): List<Int> {
        val currentGrid = grid.flatMap { line ->
            line.map { if (it == '#') WALL else EMPTY }.toMutableList()
        }.toMutableList()
        nodes.forEachIndexed { i, it -> currentGrid[it.y * width + it.x] = i }
        return currentGrid
    }

    private fun buildRange(grid: List<String>): List<List<Int>> {
        return (0 until height).flatMap { y ->
            (0 until width).map { x ->
                if (grid[y][x] == '#') {
                    emptyList()
                } else {
                    val start = Position(x, y)
                    listOf(start.toIndex()) +
                        DIRECTIONS.drop(1).flatMap { dir ->
                            (1..BOMB_RANGE).map { dir * it + start }
                                .takeWhile { it.x in 0 until width && it.y in 0 until height && grid[it.y][it.x] != '#' }
                                .map{it.toIndex()}
                        }
                }
            }
        }
    }
    
    private fun Position.toIndex() = y * width + x
}