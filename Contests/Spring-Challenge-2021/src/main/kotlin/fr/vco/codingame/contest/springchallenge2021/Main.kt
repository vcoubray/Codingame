package fr.vco.codingame.contest.springchallenge2021

import java.util.*
import kotlin.math.min

const val SEED = 0
const val LITTLE = 1
const val MEDIUM = 2
const val GREAT = 3

fun log(message: Any?) = System.err.println(message.toString())

data class Tree(
    val cellIndex: Int,
    val size: Int,
    val isMine: Boolean,
    val isDormant: Boolean
)

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


fun List<Cell>.initNeigh(origin: Int) {
    val cell = this[origin]
    for (dir in 0 until 6) {
        cell.neighByDirection.add(getLine(cell, dir, 3))
        cell.neighByRange = getRangedNeighbors(cell, 3)
    }
}


fun List<Cell>.getLine(origin: Cell, dir: Int, range: Int = 1): List<Cell> {

    val line = mutableListOf<Cell>()
    var cellIndex = origin.index
    for (i in 0 until range) {
        cellIndex = this[cellIndex].neighIndex[dir]
        if (cellIndex == -1) break
        line.add(this[cellIndex])
    }
    return line

}


fun List<Cell>.getRangedNeighbors(origin: Cell, range: Int = 1): List<List<Cell>> {

    val neigh = List(range + 1) { mutableListOf<Cell>() }

    val visited = mutableSetOf<Int>()
    val toVisit = LinkedList<Pair<Int, Int>>()
    toVisit.add(origin.index to 0)

    while (toVisit.isNotEmpty()) {
        val (cell, depth) = toVisit.pop()
        if (!visited.contains(cell)) {
            visited.add(cell)
            neigh[depth].add(this[cell])
            this[cell].neighIndex.forEach {
                if (it != -1 && depth < range) {
                    toVisit.add(it to depth + 1)
                }
            }
        }

    }
    return List(range + 1) { neigh.take(it + 1).flatten() }
}


interface Action {
    fun cost(trees: List<Tree>): Int
}

class SeedAction(val source: Tree, val target: Cell, val message: String = "") : Action {
    override fun toString() = "SEED ${source.cellIndex} ${target.index} $message"
    override fun cost(trees: List<Tree>) = trees.count { it.size == SEED }
}

class GrowAction(val tree: Tree, val message: String = "") : Action {
    override fun toString() = "GROW ${tree.cellIndex} $message"
    override fun cost(trees: List<Tree>) = trees.count { it.size == tree.size + 1 } + baseCost()
    private fun baseCost() = when (tree.size) {
        0 -> 1
        1 -> 3
        2 -> 7
        else -> 0
    }
}

class CompleteAction(val tree: Tree, val message: String = "") : Action {
    override fun toString() = "COMPLETE ${tree.cellIndex} $message"
    override fun cost(trees: List<Tree>) = trees.count { it.size == GREAT } + 4
}


fun actionBaseCost(action: String, size: Int = 0) =
    when {
        action == "SEED" -> 0
        action == "GROW" && size == 0 -> 1
        action == "GROW" && size == 1 -> 3
        action == "GROW" && size == 2 -> 7
        action == "COMPLETE" -> 4
        else -> 0
    }

fun List<Tree>.actionCost(action: String, size: Int = 0): Int =
    when (action) {
        "SEED" -> this.count { it.size == 0 } + actionBaseCost(action, size)
        "GROW" -> this.count { it.size == size + 1 } + actionBaseCost(action, size)
        "COMPLETE" -> this.count { it.size == 3 } + actionBaseCost(action, size)
        else -> 0
    }


fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    val numberOfCells = input.nextInt()
    val cells = List(numberOfCells) {
        Cell(
            input.nextInt(),
            input.nextInt(),
            List(6) { input.nextInt() }
        )
    }
    repeat(numberOfCells) { cells.initNeigh(it) }


    // game loop
    while (true) {
        val day = input.nextInt() // the game lasts 24 days: 0-23
        val sunDir = day % 6
        val nutrients = input.nextInt() // the base score you gain from the next COMPLETE action
        val sun = input.nextInt() // your sun points
        val score = input.nextInt() // your current score
        val oppSun = input.nextInt() // opponent's sun points
        val oppScore = input.nextInt() // opponent's score
        val oppIsWaiting = input.nextInt() != 0 // whether your opponent is asleep until the next day
        val numberOfTrees = input.nextInt() // the current amount of trees
        val trees = List(numberOfTrees) {
            Tree(
                input.nextInt(),
                input.nextInt(),
                input.nextInt() != 0,
                input.nextInt() != 0
            )
        }

        val treesIndexes = trees.map { it.cellIndex }

        val numberOfPossibleMoves = input.nextInt()
        if (input.hasNextLine()) {
            input.nextLine()
        }

        val possibleMoves = List(numberOfPossibleMoves) { input.nextLine() }
        possibleMoves.forEach(::log)
        //println(possibleMoves.getOrElse(1) { "WAIT" })

        val myTrees = trees.filter { it.isMine }

        log("----")
        val seedActions =
            if (day > 21) emptyList()
            else myTrees.filterNot { it.isDormant }
                .filterNot { it.size <= LITTLE }
                .map { t ->
                    (cells[t.cellIndex].neighByRange[t.size] - cells[t.cellIndex].neighByRange[1])
                        .asSequence()
                        .filter { it.richness > 0 }
                        .filterNot { treesIndexes.contains(it.index) }
                        .map { SeedAction(t, it) }
                        .filter { it.cost(myTrees) <= sun }
                        .toList()
                }
                .flatten()
        //seedActions.forEach(::log)

        val growActions = myTrees.filter { it.size != min(GREAT, 24 - day + 2 - it.size) }.map { GrowAction(it) }
            .filter { it.cost(myTrees) <= sun }
        //growActions.forEach(::log)

        val completeActions =
            if (myTrees.count { it.size == GREAT } < min(24 - day, 5))
                emptyList()
            else
                myTrees.filter { it.size == GREAT }.map { CompleteAction(it) }.filter { it.cost(myTrees) <= sun }
        val actions = completeActions.sortedByDescending { cells[it.tree.cellIndex].richness } +
            growActions.sortedByDescending { it.tree.size } +
            seedActions.sortedByDescending { cells[it.target.index].richness }
        actions.forEach(::log)
        println(actions.firstOrNull() ?: "WAIT")

    }
}