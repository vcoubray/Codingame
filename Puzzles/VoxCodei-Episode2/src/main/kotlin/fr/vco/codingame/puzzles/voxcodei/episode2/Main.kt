package fr.vco.codingame.puzzles.voxcodei.episode2

import kotlin.math.absoluteValue


data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
    operator fun minus(other: Position) = Position(x - other.x, y - other.y)
    operator fun unaryMinus() = Position(-x, -y)
    fun distance(other: Position) = (x - other.x).absoluteValue + (y - other.y).absoluteValue
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

class GridBuilder(val width: Int, val height: Int) {
    
    var possibleDirection: Map<Position, Set<Node>> = emptyMap()

    fun update(grid: List<String>) {
        val nodePositions = buildList {
            repeat(height) { y ->
                repeat(width) { x ->
                    if (grid[y][x] == '@') {
                        add(Position(x, y))
                    }
                }
            }
        }

        if (possibleDirection.isEmpty()) {
            possibleDirection = nodePositions.associateWith { node -> DIRECTIONS.map { Node(node, it) }.toSet() }
        } else {
            possibleDirection = possibleDirection.map { (node, dirs) ->
                node to dirs.mapNotNull { nextNode(it, grid) }.filter{grid[it.pos.y][it.pos.x] == '@'}.toSet()
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

    fun isReady() : Boolean {
        return possibleDirection.isNotEmpty() && possibleDirection.values.all{it.size == 1}
    }
    
    fun getNodes(): List<Node> {
        return possibleDirection.values.map{it.first()}
    }
}


fun main() {
    val (width, height) = readln().split(" ").map { it.toInt() }

    val gridBuilder = GridBuilder(width, height)
    // game loop
    while (true) {
        val (rounds, bombs) = readln().split(" ").map { it.toInt() }
        val grid = List(height) { readln() }
        
        gridBuilder.update(grid)
        if(gridBuilder.isReady()){
            System.err.println("IS READY")
            val nodes = gridBuilder.getNodes()
            nodes.forEach(System.err::println)
        }else {
            gridBuilder.possibleDirection.forEach{(node, dirs) -> 
                System.err.println(node)
                dirs.forEach{System.err.println("   $it")}
            }
        }


        println("WAIT")
    }
}