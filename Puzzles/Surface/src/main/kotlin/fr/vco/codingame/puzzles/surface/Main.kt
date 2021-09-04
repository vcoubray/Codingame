package fr.vco.codingame.puzzles.surface

import java.util.*

fun main() {
    val input = Scanner(System.`in`)
    val L = input.nextInt()
    val H = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }
    val zone = List(H) { input.nextLine().map { if (it == 'O') -1 else 0 } }.flatten()
    val board = Board(H, L, zone.toMutableList())

    repeat(input.nextInt()) {
        println(board.getZoneSize(Position(input.nextInt(), input.nextInt())))
    }

}

class Board(
    private val height: Int,
    private val width: Int,
    private val board: MutableList<Int>
) {
    private fun getZone(pos: Position): Set<Int> {
        val toVisit = LinkedList<Position>()
        val visited = mutableSetOf<Int>()
        toVisit.add(pos)

        while (toVisit.isNotEmpty()) {
            val current = toVisit.pop()
            if (current.x in (0 until width) && current.y in (0 until height)) {
                val index = current.getIndex(width)
                if (board[index] != 0 && index !in visited) {
                    visited.add(index)
                    current.getNeighbours().forEach(toVisit::addLast)
                }
            }
        }
        return visited
    }

    fun getZoneSize(pos: Position) =
        if (board[pos.getIndex(width)] >= 0) board[pos.getIndex(width)]
        else getZone(pos).apply { forEach { this@Board.board[it] = size } }.size

}


data class Position(val x: Int, val y: Int) {

    fun getIndex(width: Int) = y * width + x
    fun getNeighbours() = listOf(
        Position(x + 1, y),
        Position(x - 1, y),
        Position(x, y + 1),
        Position(x, y - 1)
    )
}
