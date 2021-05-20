package fr.vco.codingame.contest.springchallenge2021

import java.util.*

//class BitNode(
//    var parent: BitNode?,
//    var day: Int,
//    var nutrients: Int,
//    var income: Int,
//    var sun: Int,
//    var score: Int,
//    var oppSun: Int,
//    var oppScore: Int,
//    var isWaiting: Boolean,
//    var oppIsWaiting: Boolean,
//    val trees: BitSet = BitSet(37),
//    val myTrees: BitSet = BitSet(37),
//    val treeSize : List<BitSet> = List(4){BitSet(37)},
////    val seeds: BitSet = BitSet(37),
////    val littleTrees : BitSet = BitSet(37),
////    val mediumTrees : BitSet = BitSet(37),
////    val greatTrees : BitSet = BitSet(37),
//    val activeTrees : BitSet = BitSet(37),
//    var action: String = ""
//) {
//
//    //val income: Int =
//
//    val nodeScore: Double = (income * (23 - day) + sun / 3 + score).toDouble()
//
//    constructor(state: State, income: Int) : this(
//        null,
//        state.day,
//        state.nutrients,
//        income,
//        state.sun,
//        state.score,
//        state.oppSun,
//        state.oppScore,
//        false,
//        state.oppIsWaiting
//    )
//
//
//    fun initTree(trees: List<Tree>) {
//        trees.forEach {
//            this.trees[it.cellIndex] = true
//            this.myTrees[it.cellIndex] = it.isMine
//            this.activeTrees[it.cellIndex] = !it.isDormant
//            this.treeSize[it.size][it.cellIndex] = true
//        }
//    }
//
//
//    fun prepareChildren() =
//        BitNode(
//            this,
//            day,
//            nutrients,
//            income,
//            sun,
//            score,
//            oppSun,
//            oppScore,
//            isWaiting,
//            oppIsWaiting,
//            trees.clone() as BitSet,
//            myTrees.clone() as BitSet,
//            treeSize.map { it.clone() as BitSet },
//            activeTrees.clone() as BitSet
//        )
//
//
//    fun getCosts(trees: List<Tree>) = listOf(
//        trees.count { it.size == 0 } + SEED_COST,
//        trees.count { it.size == 1 } + GROW_0_COST,
//        trees.count { it.size == 2 } + GROW_1_COST,
//        trees.count { it.size == 3 } + GROW_2_COST,
//        COMPLETE_COST
//    )


//    val children: List<Node> by lazy {
//        if (day >= 24) {
//            emptyList<Node>()
//        } else {
//            val myTrees = trees.filterNotNull().filter { it.isMine }
//            val costs = getCosts(myTrees)
//            val actions = LinkedList<Node>()
//            val seedCount = myTrees.count { it.size == SEED }
//            trees.forEach { t ->
//                t?.let { tree ->
//                    when {
//                        tree.isDormant -> return@let
//                        !tree.isMine -> return@let
//                        tree.size == GREAT && day > 12 && costs[COMPLETE_ACTION] <= sun -> {
//                            actions.add(complete(tree, costs[COMPLETE_ACTION]))
//                        }
//                        tree.size < GREAT && costs[GROW_ACTION[tree.size]] <= sun -> {
//                            actions.add(grow(tree, costs[GROW_ACTION[tree.size]]))
//                        }
//                        tree.size > LITTLE && seedCount == 0 && costs[SEED_ACTION] <= sun -> {
//                            val targets = Board[tree.cellIndex].neighByRange[tree.size]
//                            targets.forEach {
//                                if (it.richness > 0 && trees[it.index] == null)
//                                    actions.add(seed(tree, it, costs[SEED_ACTION]))
//                            }
//                        }
//                    }
//                }
//            }
//            actions.add(newDay()) // WAIT
//            actions
//        }
//    }
//
//
//    private fun newDay(): BitNode {
//
//        return this.prepareChildren().apply {
//            this.day++
//            val shadow = Board.calcBitShadow(treeSize, this.day % 6)
//            treeSize[LITTLE].and(myTrees)
//            this.income = treeSize[LITTLE]
//                .filter { it?.let { it.isMine && shadow[it.cellIndex] < it.size } ?: false }
//                .sumBy { it?.size ?: 0 }
//            this.isWaiting = false
//            this.oppIsWaiting = false
//            this.activeTrees. { it?.isDormant = false }
//            this.sun += this.income
//            this.action = "WAIT"
//
//        }
//    }
//
//    private fun seed(tree: Tree, target: Cell, cost: Int): Node {
//        return prepareChildren().apply {
//            this.sun -= cost
//            this.trees[target.index] = Tree(
//                target.index,
//                0,
//                true,
//                true
//            )
//            this.trees[tree.cellIndex]?.isDormant = true
//            this.action = SeedAction(tree, target).toString()
//        }
//    }
//
//    private fun complete(tree: Tree, cost: Int): Node {
//        return prepareChildren().apply {
//            this.sun -= cost
//            this.income -= 3
//            this.trees[tree.cellIndex] = null
//            this.score += nutrients + BONUS_RICHNESS[Board[tree.cellIndex].richness]
//            this.nutrients--
//            this.action = CompleteAction(tree).toString()
//        }
//    }
//
//    private fun grow(tree: Tree, cost: Int): Node {
//        return prepareChildren().apply {
//            this.sun -= cost
//            this.income += 1
//            this.trees[tree.cellIndex]?.let { t ->
//                t.size++
//                t.isDormant = true
//            }
//            this.action = GrowAction(tree).toString()
//        }
//    }
//
//
//    fun isFinish() = day == 24
//

//}