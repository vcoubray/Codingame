package fr.vco.codingame.contest.springchallenge2021

import java.util.*

object BFS {
    var totalCount = 0L
    var totalCreatedNode = 0L
    var explorationCount = 0
    fun explore(root: Node, timeout: Int = 30): Node? {
        explorationCount++
        val toVisit = LinkedList<Node>()
        toVisit.add(root)
        var count = 0
        val startBFS = System.currentTimeMillis()
        val end = startBFS + timeout
        var bestNode: Node? = null
        while (toVisit.isNotEmpty() && System.currentTimeMillis() < end) {
            val current = toVisit.pop()
            current.children.forEach {
                toVisit.add(it)
            }
            ++count
            if (bestNode?.nodeScore ?: 0.0 <= current.nodeScore) {
                bestNode = current
            }
        }
        totalCount += count
        totalCreatedNode += count + toVisit.size
        log("End BFS with $count nodes in ${System.currentTimeMillis() - startBFS}ms")
        log("Total explored Nodes : $totalCount")
        log("Total created Nodes : $totalCreatedNode")
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

    //val income: Int =

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
        )


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


