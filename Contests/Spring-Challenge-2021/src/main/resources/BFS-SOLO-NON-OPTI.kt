import java.util.*
import kotlin.math.max
import kotlin.math.min


object BFS {
    var totalCount = 0L

    var explorationCount = 0
    fun explore(root: Node, timeout: Int = 30): Node? {
        explorationCount++
        val toVisit = LinkedList<Node>()
        toVisit.add(root)
        var count = 0
        val startBFS = System.currentTimeMillis()
        val end = startBFS + timeout
        var bestNode: Node? = null
        var maxTurn = root.turn
        while (toVisit.isNotEmpty() && System.currentTimeMillis() < end) {
            val current = toVisit.pop()
            current.children.forEach {
                ++count
                if (bestNode?.nodeScore ?: 0.0 <= it.nodeScore) {
                    bestNode = it
                }
                maxTurn = it.turn
                toVisit.add(it)
            }

        }
        totalCount += count

        log("End BFS with $count nodes in ${System.currentTimeMillis() - startBFS}ms")
        log("Max Depth : ${maxTurn - root.turn}")
        log("Total explored Nodes : $totalCount")
        log("Average explored Nodes : ${totalCount / explorationCount}")
        return bestNode
    }
}

class Node(
    var parent: Node?,
    var day: Int,
    var nutrients: Int,
    var income: Int,
    var sun: Int,
    var score: Int,
    var oppSun: Int,
    var oppScore: Int,
    var isWaiting: Boolean,
    var oppIsWaiting: Boolean,
    val trees: MutableList<Tree?> = MutableList(37) { null },
    var action: String = ""
) {
    var turn: Int =0
    val nodeScore: Double = (income * (23 - day) + sun / 3 + score).toDouble()

    constructor(state: State, income: Int) : this(
        null,
        state.day,
        state.nutrients,
        income,
        state.sun,
        state.score,
        state.oppSun,
        state.oppScore,
        false,
        state.oppIsWaiting,
        state.boardTrees.toMutableList()
    )

    fun prepareChildren() =
        Node(
            this,
            day,
            nutrients,
            income,
            sun,
            score,
            oppSun,
            oppScore,
            isWaiting,
            oppIsWaiting,
            trees.map { it?.copy() }.toMutableList()
        ).also{it.turn = this.turn+1 }


    fun getCosts(trees: List<Tree>) = listOf(
        trees.count { it.size == 0 } + SEED_COST,
        trees.count { it.size == 1 } + GROW_0_COST,
        trees.count { it.size == 2 } + GROW_1_COST,
        trees.count { it.size == 3 } + GROW_2_COST,
        COMPLETE_COST
    )


    val children: List<Node> by lazy {
        if (day >= 24) {
            emptyList<Node>()
        } else {
            val myTrees = trees.filterNotNull().filter { it.isMine }
            val costs = getCosts(myTrees)
            val actions = mutableListOf<Node>()
            val seedCount = myTrees.count { it.size == SEED }
            trees.forEach { t ->
                t?.let { tree ->
                    when {
                        tree.isDormant -> return@let
                        !tree.isMine -> return@let
                        tree.size == GREAT && day > 12 && costs[COMPLETE_ACTION] <= sun -> {
                            actions.add(complete(tree, costs[COMPLETE_ACTION]))
                        }
                        tree.size < GREAT && costs[GROW_ACTION[tree.size]] <= sun -> {
                            actions.add(grow(tree, costs[GROW_ACTION[tree.size]]))
                        }
                        tree.size > LITTLE && seedCount == 0 && costs[SEED_ACTION] <= sun -> {
                            val targets = Board[tree.cellIndex].neighByRange[tree.size]
                            targets.forEach {
                                if (it.richness > 0 && trees[it.index] == null)
                                    actions.add(seed(tree, it, costs[SEED_ACTION]))
                            }
                        }
                    }
                }
            }
            actions.add(newDay()) // WAIT
            actions
        }
    }


    private fun newDay(): Node {

        return this.prepareChildren().apply {
            this.day++
            val shadow = Board.calcShadow(trees, this.day % 6)
            this.income = trees
                .filter { it?.let { it.isMine && shadow[it.cellIndex] < it.size } ?: false }
                .sumBy { it?.size ?: 0 }
            this.isWaiting = false
            this.oppIsWaiting = false
            this.trees.forEach { it?.isDormant = false }
            this.sun += this.income
            this.action = "WAIT"

        }
    }

    private fun seed(tree: Tree, target: Cell, cost: Int): Node {
        return prepareChildren().apply {
            this.sun -= cost
            this.trees[target.index] = Tree(
                target.index,
                0,
                true,
                true
            )
            this.trees[tree.cellIndex]?.isDormant = true
            this.action = SeedAction(tree, target).toString()
        }
    }

    private fun complete(tree: Tree, cost: Int): Node {
        return prepareChildren().apply {
            this.sun -= cost
            this.income -= 3
            this.trees[tree.cellIndex] = null
            this.score += nutrients + BONUS_RICHNESS[Board[tree.cellIndex].richness]
            this.nutrients--
            this.action = CompleteAction(tree).toString()
        }
    }

    private fun grow(tree: Tree, cost: Int): Node {
        return prepareChildren().apply {
            this.sun -= cost
            this.income += 1
            this.trees[tree.cellIndex]?.let { t ->
                t.size++
                t.isDormant = true
            }
            this.action = GrowAction(tree).toString()
        }
    }

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

    fun calcShadow(trees: List<Tree?>, sunDir: Int): List<Int> {
        val shadow = MutableList(cells.size) { 0 }
        trees.forEach { tree ->
            if(tree != null) {
                for (i in 0 until tree.size) {
                    val index = cells[tree.cellIndex].neighByDirection[sunDir].getOrNull(i)?.index ?: -1
                    if (index != -1) shadow[index] = max(shadow[index], tree.size)
                }
            }
        }
        return shadow
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
                neigh[depth].add(cells[cell])
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

interface Action

class SeedAction(val source: Tree, val target: Cell, private val message: String = "") : Action {
    override fun toString() = "SEED ${source.cellIndex} ${target.index} $message"
}

class GrowAction(val tree: Tree, private val message: String = "") : Action {
    override fun toString() = "GROW ${tree.cellIndex} $message"
}

class CompleteAction(val tree: Tree, private val message: String = "") : Action {
    override fun toString() = "COMPLETE ${tree.cellIndex} $message"
}

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

    var maxTime = 0L
    // game loop
    var turn = 0
    while (true) {
        turn++
        val stateInit = System.currentTimeMillis()
        val state = State(input)
        state.turn = turn
        val shadow = Board.calcShadow(state.trees, state.day % 6)
        val income = state.trees
            .filter { it.let { it.isMine && shadow[it.cellIndex] < it.size } }
            .sumBy { it.size }
        possibleMoves(input)
        log("Read state in ${System.currentTimeMillis() - stateInit}ms")
        val start = System.currentTimeMillis()
        val root = Node(state, income)
        log("Create state in  ${System.currentTimeMillis() - start}ms")
        val result = BFS.explore(root, 30)

        if (result != null) {
            log("Find a good Node : score : ${result.nodeScore}, day : ${result.day}, ${result.income}, ${result.sun}, ${result.score}")
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

