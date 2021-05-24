package fr.vco.codingame.contest.springchallenge2021

import fr.vco.codingame.contest.springchallenge2021.mcts.Game
import kotlin.math.max
import kotlin.math.min

const val NODES_POOL_SIZE = 0 //40_000
val NODES_POOL = List(NODES_POOL_SIZE) { Node() }


object BFS {
    var totalCount = 0L
    var explorationCount = 0
    var maxExploredCount = 0
    var maxExecutionTime = 0L

    //var toVisit = LinkedList<Int>()


    fun explore(state: Game, timeout: Int = 30): Node {
        val startBFS = System.currentTimeMillis()
        val end = startBFS + timeout
        explorationCount++

        val rootIndex = 0
        NODES_POOL[rootIndex].fromState(state)

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

class BFSPlayer(var isMe: Boolean) {
    var income: Int = 0
    var sun: Int = 0
    var score: Int = 0
    var isWaiting: Boolean = false
    var costs: MutableList<Int> = MutableList(5) { 0 }

    fun calculateNodeScore(day: Int) = when {
        day <= 8 -> income * (23.0 - day) + sun
        day in 8..18 -> income * (23.0 - day) + sun + score
        else -> sun / 3.0 + score
    }

    fun calcIncome(day: Int, trees: List<BFSTree>) {
        val invertSunDir = (day + 3) % 6
        income = 0
        trees.forEach { tree ->
            if (tree.size != NONE && tree.isMine == isMe) {
                if (Board[tree.cellIndex].neighByDirection[invertSunDir].none { trees[it.index].size >= tree.size })
                    income += tree.size
            }
        }

    }

}


class Node {
    var parent: Node? = null
    var myTurn: Boolean = true
    var day: Int = 0
    var turn: Int = 0
    var nutrients: Int = 0
    val me: BFSPlayer = BFSPlayer(true)
    val opp: BFSPlayer = BFSPlayer(false)
    var currentPlayer: BFSPlayer = me
    val trees: List<BFSTree> = List(BOARD_SIZE) { BFSTree(it) }
    var action: String = "WAIT"
    var nodeScore: Double = Double.NEGATIVE_INFINITY
    val children: MutableList<Int> = mutableListOf()


    override fun toString(): String {
        return "Day: $day, action: $action, NodeScore: $nodeScore, income: ${me.income}, sun: ${me.sun}, score: ${me.score}"
    }

    private fun calculateNodeScore() {
        nodeScore = me.calculateNodeScore(day) - opp.calculateNodeScore(day)
    }

    fun fromState(state: Game) {
        parent = null
        children.clear()
        myTurn = true
        day = state.day
        turn = state.turn
        nutrients = state.nutrients
        state.trees.forEachIndexed { i, tree ->
            trees[i].apply {
                cellIndex = tree?.cellIndex ?: i
                size = tree?.size ?: -1
                isMine = tree?.isMine ?: false
                isDormant = tree?.isDormant ?: false
            }
        }
        me.sun = state.sun
        me.score = state.score
        me.isWaiting = false
        me.calcIncome(day, trees)
        calcCosts(me)

        opp.sun = state.oppSun
        opp.score = state.oppScore
        opp.isWaiting = state.oppIsWaiting
        opp.calcIncome(day, trees)
        calcCosts(opp)

    }

    private fun prepareChildren(index: Int) =
        NODES_POOL[index].also {
            it.parent = this
            it.children.clear()
            it.myTurn = !this.myTurn
            it.day = day
            it.turn = turn + 1
            it.nutrients = nutrients

            this.trees.forEachIndexed { i, tree ->
                it.trees[i].apply {
                    cellIndex = tree.cellIndex
                    size = tree.size
                    isMine = tree.isMine
                    isDormant = tree.isDormant
                }
            }

            it.me.income = me.income
            it.me.sun = me.sun
            it.me.score = me.score
            it.me.isWaiting = me.isWaiting
            this.me.costs.forEachIndexed { i, c -> it.me.costs[i] = c }

            it.opp.income = opp.income
            it.opp.sun = opp.sun
            it.opp.score = opp.score
            it.opp.isWaiting = opp.isWaiting
            this.opp.costs.forEachIndexed { i, c -> it.opp.costs[i] = c }

            currentPlayer = if (myTurn) me else opp

            this.children.add(index)
        }

    private fun calcCosts(player: BFSPlayer = me) {
        player.costs[SEED_ACTION] = trees.count { it.size == 0 && it.isMine == player.isMe } + SEED_COST
        player.costs[GROW_0_ACTION] = trees.count { it.size == 1 && it.isMine == player.isMe } + GROW_0_COST
        player.costs[GROW_1_ACTION] = trees.count { it.size == 2 && it.isMine == player.isMe } + GROW_1_COST
        player.costs[GROW_2_ACTION] = trees.count { it.size == 3 && it.isMine == player.isMe } + GROW_2_COST
        player.costs[COMPLETE_ACTION] = COMPLETE_COST
    }

    fun initChildren(nextIndex: Int): Int {
        children.clear()
        var index = nextIndex

        if (day >= 24) return index

        if (me.isWaiting && opp.isWaiting) {
            newDay(index)
            index++
            return index
        }

        if (!currentPlayer.isWaiting) {
            val seedCount = trees.count { it.size == SEED && it.isMine == currentPlayer.isMe }
            val greatTreeCount = trees.count { it.size == GREAT && it.isMine == currentPlayer.isMe }
            trees.forEach loop@{ tree ->
                if (tree.isDormant || tree.isMine != currentPlayer.isMe || tree.size == NONE) return@loop
                if (tree.size == GREAT &&
                    day > 12 &&
                    greatTreeCount >= min(4, 23 - day) &&
                    currentPlayer.costs[COMPLETE_ACTION] <= currentPlayer.sun
                ) {
                    complete(index, tree, currentPlayer.costs[COMPLETE_ACTION])
                    index++
                }
                if (tree.size < GREAT && currentPlayer.costs[GROW_ACTION[tree.size]] <= currentPlayer.sun) {
                    grow(index, tree, currentPlayer.costs[GROW_ACTION[tree.size]])
                    index++
                }
                if (tree.size > LITTLE && seedCount == 0 && currentPlayer.costs[SEED_ACTION] <= currentPlayer.sun) {
                    val targets = Board[tree.cellIndex].neighByRange[tree.size]
                    targets.forEach {
                        if (it.richness > 0 && trees[it.index].size == NONE) {
                            seed(index, tree, it, currentPlayer.costs[SEED_ACTION])
                            index++
                        }
                    }
                }
            }
        }
        wait(index)// WAIT
        index++

        return index
    }


    private fun wait(nodeIndex: Int): Node {
        return this.prepareChildren(nodeIndex).also {
            val player = if (this.myTurn) it.me else it.opp
            player.isWaiting = true
            it.action = "WAIT"
            it.calculateNodeScore()
        }
    }

    private fun newDay(nodeIndex: Int): Node {

        return this.prepareChildren(nodeIndex).also {
            it.day++
            if (it.day < 24) {
                it.myTurn = true
                it.trees.forEach { t -> t.isDormant = false }

                it.me.isWaiting = false
                it.me.calcIncome(it.day, trees)
                it.me.sun += it.me.income


                it.opp.isWaiting = false
                it.opp.calcIncome(it.day, trees)
                it.opp.sun += it.opp.income
            } else {
                it.me.income = 0
                it.opp.income = 0
            }
            it.action = "NEW DAY"
        }
    }

    private fun seed(nodeIndex: Int, source: BFSTree, target: Cell, cost: Int): Node {
        return prepareChildren(nodeIndex).also {
            val player = if (this.myTurn) it.me else it.opp

            it.trees[target.index].cellIndex = target.index
            it.trees[target.index].size = 0
            it.trees[target.index].isMine = true
            it.trees[target.index].isDormant = true
            it.trees[source.cellIndex].isDormant = true
            it.action = "SEED ${source.cellIndex} ${target.index}"
            player.sun -= cost
            player.costs[SEED_ACTION]++
            it.calculateNodeScore()
        }
    }

    private fun complete(nodeIndex: Int, tree: BFSTree, cost: Int): Node {
        return prepareChildren(nodeIndex).also {
            val player = if (this.myTurn) it.me else it.opp
            player.sun -= cost
            player.income -= 3
            player.costs[GROW_2_ACTION]--
            player.score += nutrients + BONUS_RICHNESS[Board[tree.cellIndex].richness]

            it.trees[tree.cellIndex].size = -1
            it.nutrients--
            it.action = "COMPLETE ${tree.cellIndex}"
            it.calculateNodeScore()
        }
    }

    private fun grow(nodeIndex: Int, tree: BFSTree, cost: Int): Node {
        return prepareChildren(nodeIndex).also {
            val player = if (this.myTurn) it.me else it.opp
            player.sun -= cost
            player.income += 1
            player.costs[tree.size]--
            player.costs[tree.size + 1]++
            it.trees[tree.cellIndex].size++
            it.trees[tree.cellIndex].isDormant = true
            it.action = "GROW ${tree.cellIndex}"
            it.calculateNodeScore()
        }
    }


    fun isFinish() = day == 24

    fun isVictoryMine() = (me.score + me.sun/3) > (opp.score + opp.sun/3)

    fun getFirstAction(): String {
        var current = this
        var action = ""
        while (current.parent != null) {
            if (current.action != "") action = current.action
            log("${if (current.myTurn) "OPP - " else ""}$action")
            current = current.parent!!

        }
        return action
    }

}


