package fr.vco.codingame.contest.springchallenge2021

import kotlin.math.max
import kotlin.math.min

const val NODES_POOL_SIZE = 50_000
val NODES_POOL = List(NODES_POOL_SIZE) { Node() }


object BFS {
    var totalCount = 0L
    var explorationCount = 0
    var maxExploredCount = 0
    var maxExecutionTime = 0L

    //var toVisit = LinkedList<Int>()


    fun explore(state: State, timeout: Int = 30): Node {
        val startBFS = System.currentTimeMillis()
        val end = startBFS + timeout
        explorationCount++

        val rootIndex = 0
        NODES_POOL[rootIndex].fromState(state)
        // toVisit.clear()
        //toVisit.add(rootIndex)

//        NODES_POOL[rootIndex].initChildren(rootIndex+1)
//        NODES_POOL[rootIndex].children.forEach{log(NODES_POOL[it])}

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
                //toVisit.add(it)
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


