package fr.vco.codingame.contest.springchallenge2021

import fr.vco.codingame.contest.springchallenge2021.mcts.Game
import fr.vco.codingame.contest.springchallenge2021.mcts.Mcts
//import fr.vco.codingame.contest.springchallenge2021.mcts.PoolState
import java.util.*
import kotlin.math.max

fun log(message: Any?) = System.err.println(message.toString())


data class Tree(
    val cellIndex: Int,
    var size: Int = NONE,
    var owner: Int = -1,
    // var isMine: Boolean = false,
    var isDormant: Boolean = false
)

fun List<Tree>.getBits() = this.fold(0L){acc, tree -> acc.addTree(tree.cellIndex)}

sealed class Action(val player: Int, var message: String)

class SeedAction(player: Int, val source: Int, val target: Int, message: String = "") : Action(player, message) {
    constructor(player: Int, source: Tree, target: Cell, message: String = "") : this(player, source.cellIndex, target.index, message)
    override fun toString() = "SEED $source $target $message"
}

class GrowAction(player: Int, val treeId: Int, val size :Int, message: String = "") : Action(player, message) {
    constructor (player: Int, tree: Tree, message: String = "") : this(player, tree.cellIndex, tree.size, message)
    override fun toString() = "GROW $treeId $message"
}

class CompleteAction(player: Int, val treeId: Int, message: String = "") : Action(player, message) {
    constructor(player:Int, tree: Tree, message: String = "") : this (player,tree.cellIndex,message)
    override fun toString() = "COMPLETE $treeId $message"
}

class WaitAction(player: Int, message: String = "") : Action(player, message) {
    override fun toString() = "WAIT $message"
}




class State2(
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
                cellIndex = input.nextInt(),
                size = input.nextInt(),
                owner = if (input.nextInt() != 0) ME else OPP,
                isDormant = input.nextInt() != 0
            )
        }
    )

    var turn = 0
    val boardTrees = MutableList<Tree?>(37) { null }


//    val sunDir = day % 6
//    val treesIndexes = trees.map { it.cellIndex }
//    val me = Player(score, sun, trees.filter { it.isMine })
//    val opp = Player(oppScore, oppSun, trees.filterNot { it.isMine })
//    val myShadow = Board.calcPotentialShadowCount(me.trees)
//    val globalShadow = Board.calcPotentialShadowCount(trees)
//    val rentability = Board.getRentabilityBoard(me.trees)
//    val globalRentability = Board.getRentabilityBoard(me.trees)

    init {
        trees.forEach { boardTrees[it.cellIndex] = it }
    }

//    fun bestAction(player: Player = me): String {
//
//        val harvestableTrees = nutrients
//        val maxTrees = harvestableTrees - trees.count { it.size == GREAT }
//        val maxGreatTrees = min(5, maxTrees)
//
//        val greatTrees = player.trees.filter { it.size == GREAT }
//        val mediumTrees = player.trees.filter { it.size == MEDIUM }
//        val availableMediumTrees = mediumTrees.filterNot { it.isDormant }
//        val availableGreatTrees = greatTrees.filterNot { it.isDormant }
//        val availableLittleTrees = player.trees.filter { it.size == LITTLE }.filterNot { it.isDormant }
//        val availableSeeds = player.trees.filter { it.size == SEED }.filterNot { it.isDormant }
//
//
//        val seedTrees = player.trees.filterNot { it.isDormant || it.size <= LITTLE }
//        val shouldSeed = when {
//            day >= 20 -> false
//            seedTrees.isEmpty() -> false
//            player.costs[SEED_ACTION] > player.sun -> false
//            //player.trees.count() < maxTrees &&
//            player.trees.count { it.size == SEED } < 1 -> true
//            else -> false
//        }
//        if (shouldSeed) {
//
//            val seedActions = seedTrees.map { t ->
//                val targets = Board[t.cellIndex].neighByRange[t.size]
//                targets
//                    .asSequence()
//                    .filter { it.richness > 0 }
//                    .filterNot { treesIndexes.contains(it.index) }
//                    .map { SeedAction(t, it) }
//                    .toList()
//            }.flatten()
//
//            return seedActions.maxWith(
//                compareBy(
//                    { rentability[it.target.index] },
//                    { it.target.richness },
//                    { -globalRentability[it.target.index] },
//                    { -it.source.size },
//                    { -Board[it.source.cellIndex].richness }
//                )
//            )?.toString() ?: "WAIT"
//        }
//
//
//        val shouldComplete = when {
//            availableGreatTrees.isEmpty() -> false
//            player.costs[COMPLETE_ACTION] > player.sun -> false
//            day == 23 -> true
//            greatTrees.size > maxGreatTrees -> true
//            greatTrees.size >= maxGreatTrees && availableMediumTrees.isNotEmpty() &&
//                player.costs[COMPLETE_ACTION] + player.costs[GROW_2_ACTION] <= sun -> true
//            else -> false
//        }
//
//        if (shouldComplete) {
//            val target = availableGreatTrees.minWith(
//                compareBy({ rentability[it.cellIndex] },
//                    { -Board[it.cellIndex].richness })
//            )!!
//            val cost = player.costs[COMPLETE_ACTION]
//            val gain = nutrients + BONUS_RICHNESS[Board[target.cellIndex].richness]
//            if (gain + (sun - cost) / 3 > sun / 3)
//                return CompleteAction(target).toString()
//        }
//
//
//        val shouldGrow2 = when {
//            availableMediumTrees.isEmpty() -> false
//            player.costs[GROW_2_ACTION] > player.sun -> false
//            day >= 22 -> false
//            availableLittleTrees.isNotEmpty() && availableSeeds.isNotEmpty() && mediumTrees.size < 2 &&
//                player.costs[GROW_1_ACTION] + player.costs[GROW_0_ACTION] - 1 <= player.costs[GROW_2_ACTION] && day < 8 -> false
//            else -> true
//        }
//
//        if (shouldGrow2)
//            return GrowAction(availableMediumTrees.firstOrNull()!!).toString()
//
//        if (availableLittleTrees.isNotEmpty() && player.costs[GROW_1_ACTION] <= player.sun && day < 21)
//            return GrowAction(availableLittleTrees.firstOrNull()!!).toString()
//
//        if (availableSeeds.isNotEmpty() && player.costs[GROW_0_ACTION] <= player.sun && day < 20)
//            return GrowAction(availableSeeds.firstOrNull()!!).toString()
//
//        return "WAIT"
//    }

}

fun possibleMoves(input: Scanner): List<String> {
    val numberOfPossibleMoves = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }
    return List(numberOfPossibleMoves) { input.nextLine() }
}


//val POOL = List(PoolState.MAX_SIZE) { State() }
////
//
//object PoolState {
//    const val MAX_SIZE = 50_000
//
//
//    var index = 0
//
//    //fun nextIndex() = index++
//    operator fun get(i: Int) = POOL[i]
//    fun getNextState() = POOL[index++]
//    fun reset() {
//        index = 0
//    }
//}




fun main() {
    val input = Scanner(System.`in`)




    val startInit = System.currentTimeMillis()
    Board.init(input)
    log("init board in ${System.currentTimeMillis() - startInit}ms")

    var maxTime = 0L

//    PoolState.reset()

    val game = Game()

    // game loop
    while (true) {
        game.readInput(input)
        game.turn++
        possibleMoves(input)

        log("Read state in ${game.currentExecutionTime()}ms")

//        val state = State().initFromGame(game)
//        state.actions.forEach(::log)
//        state.costs.forEach(::log)
//
//        val state2 = state.getNextState(state.actions[0])
//        val state3 = state2.getNextState(state.actions[1])
//        state3.actions.forEach(::log)
//        state3.costs.forEach(::log)

        val timeout = if (game.turn == 1) 800 else 70
        val result = Mcts.findNextMove(game, timeout)

        println(result)

        val executionTime = game.currentExecutionTime()
        maxTime = max(executionTime, maxTime)
        log("End turn in ${executionTime}ms ")
        log("Max Execution in ${maxTime}ms ")
    }
}
