package fr.vco.codingame.contest.springchallenge2021

import java.util.*
import kotlin.math.max

const val BOARD_SIZE = 37

class Cell(
    val index: Int,
    val richness: Int,
    val neighIndex: List<Int>,
    val neighByDirection: MutableList<List<Cell>> = mutableListOf(),
    var neighByRange: List<List<Cell>> = emptyList()
) {
    override fun toString(): String {
        val sb = StringBuffer("Cell [index = $index; richness = $richness;]\n")
        neighByDirection.forEachIndexed { i, it ->
            sb.append("dir $i : ${it.joinToString(" ") { c -> c.index.toString() }}\n")
        }
        return sb.toString()
    }

}

object Board {
    lateinit var cells: List<Cell>

    operator fun get(index: Int) = cells[index]

    fun init(input: Scanner) {
        val numberOfCells = input.nextInt()
        cells = List(numberOfCells) {
            Cell(
                input.nextInt(),
                input.nextInt(),
                List(6) { input.nextInt() }
            )
        }
        repeat(numberOfCells) { initNeigh(it) }
//        cells.forEach{
//            log("Cell(${it.index},${it.richness},listOf(${it.neighIndex.joinToString(",") })),")
//        }
    }

    fun initNeigh(origin: Int) {
        val cell = cells[origin]
        for (dir in 0 until 6) {
            cell.neighByDirection.add(getLine(cell, dir, 3))
            cell.neighByRange = getRangedNeighbors(cell, 3)
        }
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


//    fun calcShadow(trees: List<Tree?>, sunDir: Int): List<Int> {
//        val shadow = MutableList(cells.size) { 0 }
//        trees.forEach { tree ->
//            if (tree != null) {
//                for (i in 0 until tree.size) {
//                    val index = cells[tree.cellIndex].neighByDirection[sunDir].getOrNull(i)?.index ?: -1
//                    if (index != -1) shadow[index] = max(shadow[index], tree.size)
//                }
//            }
//        }
//        return shadow
//    }

    fun calcShadow(trees: List<Tree>, sunDir: Int): List<Int> {
        val shadow = MutableList(cells.size) { 0 }
        trees.forEach { tree ->
            for (i in 0 until tree.size) {
                val index = cells[tree.cellIndex].neighByDirection[sunDir].getOrNull(i)?.index ?: -1
                if (index != -1) shadow[index] = max(shadow[index], tree.size)
            }
        }
        return shadow
    }


//    fun calcBitShadow(treeSize: List<BitSet>, sunDir: Int): List<BitSet> {
//        return treeSize.drop(1).mapIndexed { size, trees ->
//            calcBitShadow(trees, size, sunDir)
//        }
//
//    }



//    fun calcPotentialShadow(trees: List<Tree>): List<Int> {
//        val shadow = MutableList(cells.size) { 0 }
//        trees.forEach { tree ->
//            cells[tree.cellIndex].neighByDirection.flatten().forEach { cell ->
//                shadow[cell.index] = 3
//            }
//        }
//        return shadow
//    }
//
//
//    fun calcPotentialShadowCount(trees: List<Tree>): List<Int> {
//        val shadow = MutableList(cells.size) { 0 }
//        trees.forEach { tree ->
//            cells[tree.cellIndex].neighByDirection.flatten().forEach { cell ->
//                shadow[cell.index] += 1
//            }
//        }
//        return shadow
//    }
//
//    fun getRentabilityBoard(trees: List<Tree>): List<Int> {
//        val treeBoard = MutableList(cells.size) { false }
//        trees.forEach { treeBoard[it.cellIndex] = true }
//        val rentability = MutableList(cells.size) { 5 }
//
//        cells.forEach { cell ->
//            rentability[cell.index] = cell.neighByDirection.count { n -> n.none { treeBoard[it.index] } } * 100 / 6
//        }
//        return rentability
//    }

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