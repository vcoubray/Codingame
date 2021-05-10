package fr.vco.codingame.contest.springchallenge2021

import java.util.*
import kotlin.math.max
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

interface Action {
    fun cost(trees: List<Tree>): Int
}

class SeedAction(val source: Tree, val target: Cell, private val message: String = "") : Action {
    override fun toString() = "SEED ${source.cellIndex} ${target.index} $message"
    override fun cost(trees: List<Tree>) = trees.count { it.size == SEED }
}

class GrowAction(val tree: Tree, private val message: String = "") : Action {
    override fun toString() = "GROW ${tree.cellIndex} $message"
    override fun cost(trees: List<Tree>) = trees.count { it.size == tree.size + 1 } + baseCost()
    private fun baseCost() = when (tree.size) {
        0 -> 1
        1 -> 3
        2 -> 7
        else -> 0
    }
}

class CompleteAction(val tree: Tree, private val message: String = "") : Action {
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


class Player(
    val score: Int,
    val sun: Int,
    val trees: List<Tree>
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

    val sunDir = day % 6
    val treesIndexes = trees.map { it.cellIndex }
    val me = Player(score,sun, trees.filter{it.isMine})
    val opp = Player(score,sun, trees.filterNot{it.isMine})
    val shadow = Board.calcPotentialShadow(me.trees, sunDir)

    fun bestAction(player: Player = me): String {
        val seedActions =
            if (day > 21) emptyList()
            else player.trees.filterNot { it.isDormant }
                .filterNot { it.size <= LITTLE }
                .map { t ->
                    (Board[t.cellIndex].neighByRange[t.size] - Board[t.cellIndex].neighByRange[1])
                        .asSequence()
                        .filter { it.richness > 0 }
                        .filter { shadow[it.index] == 0 }
                        .filterNot { treesIndexes.contains(it.index) }
                        .map { SeedAction(t, it) }
                        .filter { it.cost(player.trees) <= sun }
                        .toList()
                }
                .flatten()
        //seedActions.forEach(::log)

        val growActions = player.trees.filter { it.size != min(GREAT, 24 - day + 2 - it.size) }.map { GrowAction(it) }
            .filter { it.cost(player.trees) <= sun }
        //growActions.forEach(::log)

        val completeActions =
            if (player.trees.count { it.size == GREAT } < min(24 - day, 5))
                emptyList()
            else
                player.trees.filter { it.size == GREAT }.map { CompleteAction(it) }.filter { it.cost(player.trees) <= sun }
        val actions = completeActions.sortedByDescending { Board[it.tree.cellIndex].richness } +
            growActions.sortedByDescending { it.tree.size } +
            seedActions.sortedByDescending { Board[it.target.index].richness }

        return actions.firstOrNull()?.toString() ?: "WAIT"
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

    Board.init(input)

    // game loop
    while (true) {
        val state = State(input)
        possibleMoves(input)
        println(state.bestAction())
    }
}