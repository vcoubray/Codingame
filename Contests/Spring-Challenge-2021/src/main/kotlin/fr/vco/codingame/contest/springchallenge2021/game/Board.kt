package fr.vco.codingame.contest.springchallenge2021.game

import java.util.*

const val BOARD_SIZE = 37

object Board {
    lateinit var cells: Array<Cell>

    operator fun get(index: Int) = cells[index]

    fun init(input: Scanner) {
        val numberOfCells = input.nextInt()
        cells = Array(numberOfCells) {
            Cell(
                input.nextInt(),
                input.nextInt(),
                List(6) { input.nextInt() }
            )
        }
        repeat(numberOfCells) { initNeigh(it) }
    }

    fun initNeigh(origin: Int) {
        val cell = cells[origin]
        for (dir in 0 until 6) {
            cell.neighByDirection.add(getLine(cell, dir, 3))
            cell.neighByRange = getRangedNeighbors(cell, 3)
        }
        cell.seedableNeighByRange = mutableListOf(
            emptyList(),
            emptyList(),
            cell.neighByRange[2] - cell.neighByDirection.mapNotNull { it.getOrNull(1) },
            cell.neighByRange[3] - cell.neighByDirection.map{it.drop(1)}.flatten()
        )
    }

    fun getLine(origin: Cell, dir: Int, range: Int = 1): List<Cell> {
        val line = mutableListOf<Cell>()
        var cellIndex = origin.index
        for (i in 0 until range) {
            cellIndex = cells[cellIndex].neighIndex[dir]
            if (cellIndex == -1) break
            line.add(cells[cellIndex])
        }
        return line
    }

    fun getRentabilityBoard(trees: List<Tree>): List<Int> {
        val treeBoard = MutableList(cells.size) { false }
        trees.forEach { treeBoard[it.cellIndex] = true }
        val rentability = MutableList(cells.size) { 5 }

        cells.forEach { cell ->
            rentability[cell.index] = cell.neighByDirection.count { n -> n.none { treeBoard[it.index] } } * 100 / 6
        }
        return rentability
    }

    fun getRangedNeighbors(origin: Cell, range: Int = 1): List<List<Cell>> {

        val neigh = List(range + 1) { mutableListOf<Cell>() }

        val visited = mutableSetOf<Int>()
        val toVisit = LinkedList<Pair<Int, Int>>()
        toVisit.add(origin.index to 0)

        while (toVisit.isNotEmpty()) {
            val (cell, depth) = toVisit.pop()
            if (!visited.contains(cell)) {
                visited.add(cell)
                if(depth > 1) neigh[depth].add(cells[cell])
                cells[cell].neighIndex.forEach {
                    if (it != -1 && depth < range) {
                        toVisit.add(it to depth + 1)
                    }
                }
            }

        }
        return List(range + 1) { neigh.take(it + 1).flatten() }
    }
}
