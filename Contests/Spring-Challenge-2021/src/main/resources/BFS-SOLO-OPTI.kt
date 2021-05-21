package bfs.solo.opti

import kotlin.math.max
import kotlin.math.min
import java.util.*


const val NODES_POOL_SIZE = 50_000
val NODES_POOL = List(NODES_POOL_SIZE) { Node() }


object BFS {
    var totalCount = 0L
    var explorationCount = 0
    var maxExploredCount = 0
    var maxExecutionTime = 0L


    fun explore(state: State, timeout: Int = 30): Node {
        val startBFS = System.currentTimeMillis()
        val end = startBFS + timeout
        explorationCount++

        val rootIndex = 0
        NODES_POOL[rootIndex].fromState(state)

        var nextToVisit = rootIndex
        var nextToCreateIndex = rootIndex + 1
        var bestNode = NODES_POOL[rootIndex]
        var maxTurn = NODES_POOL[rootIndex].turn
        while (nextToVisit < nextToCreateIndex && System.currentTimeMillis() < end && nextToCreateIndex < NODES_POOL_SIZE - 100) {
            val current = NODES_POOL[nextToVisit]
            nextToVisit++
            nextToCreateIndex = current.initChildren(nextToCreateIndex)
            current.children.forEach {
                if (NODES_POOL[it].nodeScore > bestNode.nodeScore) {
                    bestNode = NODES_POOL[it]
                }
                maxTurn = NODES_POOL[it].turn
            }
        }

        maxExploredCount = max(nextToCreateIndex, maxExploredCount)
        val executionTime = System.currentTimeMillis() - startBFS
        if (timeout < 100) {
            maxExecutionTime = max(executionTime, maxExecutionTime)
        }
        totalCount += nextToCreateIndex
        log("End BFS with $nextToCreateIndex nodes in ${executionTime}ms")
        log("Depth Reached : ${maxTurn - NODES_POOL[rootIndex].turn}")
        log("------")
        log("Total explored Nodes : $totalCount")
        log("Average explored Nodes : ${totalCount / explorationCount}")
        log("Max Explored Nodes : $maxExploredCount")
        log("Max BFS Execution Time : $maxExecutionTime")
        return bestNode
    }
}

data class BFSTree(
    var cellIndex: Int = 0,
    var size: Int = -1,
    var isMine: Boolean = false,
    var isDormant: Boolean = false
)


class Node {

    var parent: Node? = null
    var day: Int = 0
    var turn: Int = 0
    var nutrients: Int = 0
    var income: Int = 0
    var sun: Int = 0
    var score: Int = 0
    var oppSun: Int = 0
    var oppScore: Int = 0
    var isWaiting: Boolean = false
    var oppIsWaiting: Boolean = false
    val trees: List<BFSTree> = List(BOARD_SIZE) { BFSTree(it) }


    var action: String = "WAIT"
    var nodeScore: Double = 0.0
    var costs: MutableList<Int> = MutableList(5) { 0 }
    val children: MutableList<Int> = mutableListOf()


    override fun toString(): String {
        return "Day: $day, action: $action, NodeScore: $nodeScore, income: $income, sun: $sun, score: $score"
    }

    private fun calculateNodeScore() {
        nodeScore = when {
            day <= 8 -> income * (23.0 - day) + sun
            day in 8..18 -> income * (23.0 - day) + sun + score
            else -> sun / 3.0 + score
        }
    }

    fun fromState(state: State) {
        parent = null
        day = state.day
        turn = state.turn
        nutrients = state.nutrients
        sun = state.sun
        score = state.score
        oppSun = state.oppSun
        oppScore = state.oppScore
        isWaiting = false
        oppIsWaiting = state.oppIsWaiting
        state.boardTrees.forEachIndexed { i, tree ->
            trees[i].apply {
                cellIndex = tree?.cellIndex ?: i
                size = tree?.size ?: -1
                isMine = tree?.isMine ?: false
                isDormant = tree?.isDormant ?: false
            }
        }
        calcIncome()
        calcCosts()
    }

    private fun prepareChildren(index: Int) =
        NODES_POOL[index].also {
            it.parent = this@Node
            it.day = day
            it.turn = turn + 1
            it.nutrients = nutrients
            it.income = income
            it.sun = sun
            it.score = score
            it.oppScore = oppScore
            it.isWaiting = isWaiting
            it.oppIsWaiting = oppIsWaiting
            this.trees.forEachIndexed { i, tree ->
                it.trees[i].apply {
                    cellIndex = tree.cellIndex
                    size = tree.size
                    isMine = tree.isMine
                    isDormant = tree.isDormant
                }
            }
            this.costs.forEachIndexed{i, c -> it.costs[i] = c}
            this.children.add(index)
        }

    private fun calcCosts(isMine: Boolean = true) {
        costs[SEED_ACTION] = trees.count { it.size == 0 && it.isMine == isMine } + SEED_COST
        costs[GROW_0_ACTION] = trees.count { it.size == 1 && it.isMine == isMine } + GROW_0_COST
        costs[GROW_1_ACTION] = trees.count { it.size == 2 && it.isMine == isMine } + GROW_1_COST
        costs[GROW_2_ACTION] = trees.count { it.size == 3 && it.isMine == isMine } + GROW_2_COST
        costs[COMPLETE_ACTION] = COMPLETE_COST
    }

    private fun calcIncome() {
        val invertSunDir = (day + 3) % 6
        income = 0
        trees.forEach { tree ->
            if (tree.size != NONE && tree.isMine) {
                if (Board[tree.cellIndex].neighByDirection[invertSunDir].none { trees[it.index].size >= tree.size })
                    income += tree.size
            }
        }
    }


    fun initChildren(nextIndex: Int): Int {
        children.clear()
        var index = nextIndex
        if (day < 24) {

            val seedCount = trees.count { it.size == SEED && it.isMine }
            val greatTreeCount = trees.count { it.size == GREAT && it.isMine }
            trees.forEach loop@{ tree ->
                if (tree.isDormant || !tree.isMine || tree.size == NONE) return@loop
                if (tree.size == GREAT &&
                    day > 12 &&
                    greatTreeCount >= min(4, 23 - day) &&
                    costs[COMPLETE_ACTION] <= sun
                ) {
                    complete(index, tree, costs[COMPLETE_ACTION])
                    index++
                }
                if (tree.size < GREAT && costs[GROW_ACTION[tree.size]] <= sun) {
                    grow(index, tree, costs[GROW_ACTION[tree.size]])
                    index++
                }
                if (tree.size > LITTLE && seedCount == 0 && costs[SEED_ACTION] <= sun) {
                    val targets = Board[tree.cellIndex].neighByRange[tree.size]
                    targets.forEach {
                        if (it.richness > 0 && trees[it.index].size == NONE) {
                            seed(index, tree, it, costs[SEED_ACTION])
                            index++
                        }
                    }
                }
            }
            newDay(index)// WAIT
            index++
        }
        return index
    }


    private fun newDay(nodeIndex: Int): Node {

        return this.prepareChildren(nodeIndex).also {
            it.day++
            if (it.day < 24) {
                it.calcIncome()
                it.isWaiting = false
                it.oppIsWaiting = false
                it.trees.forEach { t-> t.isDormant = false }
                it.sun += it.income
            } else {
                it.income = 0
            }
            it.action = "WAIT"
            it.calculateNodeScore()

        }
    }

    private fun seed(nodeIndex: Int, source: BFSTree, target: Cell, cost: Int): Node {
        return prepareChildren(nodeIndex).also {
            it.sun -= cost
            it.trees[target.index].cellIndex = target.index
            it.trees[target.index].size = 0
            it.trees[target.index].isMine = true
            it.trees[target.index].isDormant = true
            it.trees[source.cellIndex].isDormant = true
            it.action = "SEED ${source.cellIndex} ${target.index}"
            it.costs[SEED_ACTION]++
            it.calculateNodeScore()
        }
    }

    private fun complete(nodeIndex: Int, tree: BFSTree, cost: Int): Node {
        return prepareChildren(nodeIndex).also {
            it.sun -= cost
            it.income -= 3
            it.trees[tree.cellIndex].size = -1
            it.score += nutrients + BONUS_RICHNESS[Board[tree.cellIndex].richness]
            it.nutrients--
            it.action = "COMPLETE ${tree.cellIndex}"
            it.costs[GROW_2_ACTION]--
            it.calculateNodeScore()
        }
    }

    private fun grow(nodeIndex: Int, tree: BFSTree, cost: Int): Node {
        return prepareChildren(nodeIndex).also {
            it.sun -= cost
            it.income += 1
            it.costs[tree.size]--
            it.costs[tree.size+1]++
            it.trees[tree.cellIndex].size++
            it.trees[tree.cellIndex].isDormant = true
            it.action = "GROW ${tree.cellIndex}"
            it.calculateNodeScore()
        }
    }


    fun isFinish() = day == 24

    fun getFirstAction(): String {
        var current = this
        var action = ""
        while (current.parent != null) {
            if (current.action != "") action = current.action
            log(action)
            current = current.parent!!

        }
        return action
    }

}




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
    private lateinit var cells: List<Cell>

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

    }

    private fun initNeigh(origin: Int) {
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

const val INITIAL_NUTRIENTS = 20

const val SEED_ACTION = 0
const val GROW_0_ACTION = 1
const val GROW_1_ACTION = 2
const val GROW_2_ACTION = 3
const val COMPLETE_ACTION = 4

val GROW_ACTION = listOf(GROW_0_ACTION,GROW_1_ACTION,GROW_2_ACTION)

const val SEED_COST = 0
const val GROW_0_COST = 1
const val GROW_1_COST = 3
const val GROW_2_COST = 7
const val COMPLETE_COST = 4

const val NONE = -1
const val SEED = 0
const val LITTLE = 1
const val MEDIUM = 2
const val GREAT = 3

val BONUS_RICHNESS = listOf(0, 0, 2, 4)



fun log(message: Any?) = System.err.println(message.toString())

data class Tree(
    val cellIndex: Int,
    var size: Int,
    var isMine: Boolean,
    var isDormant: Boolean
)

class State(
    val day: Int,
    val nutrients: Int,
    val sun: Int,
    val score: Int,
    val oppSun: Int,
    val oppScore: Int,
    val oppIsWaiting: Boolean,
    val trees: List<Tree>
) {
    constructor(input: Scanner) : this(
        day = input.nextInt(), // the game lasts 24 days: 0-23
        nutrients = input.nextInt(), // the base score you gain from the next COMPLETE action
        sun = input.nextInt(), // your sun points
        score = input.nextInt(), // your current score
        oppSun = input.nextInt(), // opponent's sun points
        oppScore = input.nextInt(), // opponent's score
        oppIsWaiting = input.nextInt() != 0, // whether your opponent is asleep until the next day
        trees = List(input.nextInt()) {
            Tree(
                input.nextInt(),
                input.nextInt(),
                input.nextInt() != 0,
                input.nextInt() != 0
            )
        }
    )

    var turn = 0
    val boardTrees = MutableList<Tree?>(37) { null }

    init {
        trees.forEach { boardTrees[it.cellIndex] = it }
    }



}

fun possibleMoves(input: Scanner): List<String> {
    val numberOfPossibleMoves = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }
    return List(numberOfPossibleMoves) { input.nextLine() }
}

fun main(args: Array<String>) {
    val input = Scanner(System.`in`)

    val startInit = System.currentTimeMillis()
    Board.init(input)
    log("init board in ${System.currentTimeMillis() - startInit}ms")
    var turn = 0
    var maxTime = 0L
    // game loop
    while (true) {
        turn++
        val stateInit = System.currentTimeMillis()
        val state = State(input)
        possibleMoves(input)
        log("Read state in ${System.currentTimeMillis() - stateInit}ms")

        val start = System.currentTimeMillis()

        val timeout = if (turn ==1 ) 800 else 40
        val result = BFS.explore(state, timeout)

        if (result != null) {
            log("Find a good Node : $result")
            println(result.getFirstAction())
        } else {
            log("No good node found :(")
            println("WAIT")
        }


        val executionTime = System.currentTimeMillis() - start
        maxTime = max(executionTime, maxTime)
        log("End turn in ${executionTime}ms ")
        log("Max Execution in ${maxTime}ms ")
    }
}


